/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.generate.netgen;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.mincost.MinimumCostFlowProblem;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.CollectionUtil;
import org.jgrapht.util.ElementsSequenceGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NETGEN-style network generator. This generator is capable of generating bipartite matching
 * problems (both weighted and unweighted), maximum flow problems and minimum cost flow problems.
 * Note, that this generator works only with directed graphs. The algorithm is originally described
 * in: <it>D. Klingman, A. Napier, and J. Shutz, "NETGEN - A program for generating large scale
 * (un)capacitated assignment, transportation, and minimum cost flow network problems", Management
 * Science 20, 5, 814-821 (1974)</it>
 * <p>
 * This generator is not completely equivalent to the original implementation. A number of changes
 * has been made to remove bugs and ensure parameter constraints. For a complete parameter
 * description and constraints on them, see {@link NetworkGeneratorConfig}. Under an assumption that
 * this generator receives a valid config, the following properties of the resulting minimum cost
 * flow network are guaranteed:
 * <ol>
 * <li>Network has exactly the same number of nodes, network sources, transshipment sources, network
 * sinks, transshipment sinks, and transshipment nodes;</li>
 * <li>Network has exactly the same number of arcs;</li>
 * <li>Pure network sources don't have incoming arcs; pure network sinks don't have outgoing
 * arcs;</li>
 * <li>Capacity lower and upper bounds are satisfied for all arcs except for a subset of skeleton
 * arcs, for which the capacity lower bound is equal to the supply of the source arc's chain is
 * originating from. The description of the skeleton network and source chains follows. This is done
 * to ensure that the generated network is feasible with respect to the node supplies. You can find
 * out which arcs belong to the skeleton network by using {@link NetworkInfo}. For example, if there
 * is only one network source, network supply is equal to 10, minCap = 1, maxCap = 5, then some of
 * the arcs will have the capacity equal to 10;</li>
 * <li>If percentCapacitated is 100, then all arcs have finite capacity (which is bounded by minCap
 * and maxCap). If percent capacitated is 0, every arc is uncapacitated;</li>
 * <li>Cost lower and upper bound are satisfied;</li>
 * <li>If percentWithInfCost is 100, then all arcs have infinite cost. If percentWithInfCost is 0,
 * then every arc has finite cost (which is bounded by minCost and maxCost).</li>
 * <li>Every source's supply is at least 1;</li>
 * <li>Every sink's supply is at most -1 (equivalently, demand is at least 1);</li>
 * <li>The resulting network is feasible meaning that there exist a network flow satisfying the
 * source supply, sink demand and arc capacity constraints.</li>
 * </ol>
 * Note, that transshipment sources and transshipment sinks can have incoming and outgoing arcs
 * respectively, but this property is optional.
 * <p>
 * The maximum flow networks, that are generated by this algorithm, are guaranteed to have following
 * properties:
 * <ol>
 * <li>Properties 1-5 are equivalent to the properties of the generated minimum cost flow
 * networks;</li>
 * <li>The maximum flow is <i>greater that or equal to</i> the value of the total supply specified
 * in the network config.</li>
 * </ol>
 * <p>
 * The bipartite matching problems, that are generated by this algorithm, are guaranteed to
 * following properties:
 * <ol>
 * <li>Properties 1, 2, 6, 7 are equivalent to the properties of the generated minimum cost flow
 * networks;</li>
 * <li>For the generated problem, there exist a perfect matching meaning that every vertex can be
 * matched.</li>
 * </ol>
 * <p>
 * Now a brief description of the algorithm will be provided. The generator begins by distributing
 * supply among network sources. Every source gets at least 1 unit of supply. After that,
 * approximately 60% of transshipment nodes are evenly separated between sources. For every source,
 * an initial chain is built using these transshipment nodes. A chain is effectively a path.
 * Remaining 40% of transshipment nodes are randomly distributed among source chains.
 * <p>
 * Now every chain has to be connected to at least one sink and every sink has to be connected to at
 * least on chain. For every chain a random number of arcs is generated. This number is at least 1.
 * The total number of generated arcs is max(sourceNum, sinkNum). Every chain is connected to random
 * sinks such that above constraints are satisfied. The network source supply is distributed among
 * network sinks such that every sink received at least 1 unit of supply (with negative sign).
 * <p>
 * After the skeleton network is generated, the network is guaranteed to be feasible. The remaining
 * arcs are randomly distributed between remaining pairs of vertices. The algorithm tries to
 * distribute them evenly to avoid large arc clusters.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Timofey Chudakov
 * @see NetworkGeneratorConfig
 * @see NetworkInfo
 */
public class NetworkGenerator<V, E>
{

    /**
     * Upper bound on the number of nodes in the network this generator can work with.
     */
    public static final int MAX_NODE_NUM = 100 * 1000 * 1000;
    /**
     * Upper bound on the number of supply units in the network this generator can work with.
     */
    public static final int MAX_SUPPLY = 200 * 1000 * 1000;
    /**
     * Upper bound on the number of arcs in the network this generator can work with.
     */
    public static final int MAX_ARC_NUM = 2 * 1000 * 1000 * 1000;
    /**
     * Upper bound on the arc capacities and costs values in the network this generator can work
     * with.
     */
    public static final int CAPACITY_COST_BOUND = 2 * 1000 * 1000 * 1000;

    /**
     * User-provided network configuration.
     */
    private final NetworkGeneratorConfig config;
    /**
     * Random number generator used to create a network.
     */
    private final Random rng;

    /**
     * A network that is being generated.
     */
    private Graph<V, E> graph;
    /**
     * Network structure information obtained during generation process.
     */
    private NetworkInfo<V, E> networkInfo;

    /**
     * Network nodes stored in a list. Nodes of the same type are located in the continuous
     * segments. There are 5 segments in this list:
     * <p>
     * [ pureSources | tSources | tNodes | tSinks | pureSinks ]
     * <p>
     * - [ 0, pureSourceNum ) - pure source nodes - [ pureSourceNum, sourceNum ) - transshipment
     * source nodes - [ sourceNum, sourceNum + transshipNodeNum ) - transshipment nodes - [
     * sourceNum + transshipNodeNum, nodeNum - pureSinkNum ) - transshipment sink nodes - [ nodeNum
     * - pureSinkNum, nodeNum ) - pure sink nodes
     */
    private List<Node> nodes;
    /**
     * Mapping for converting graph vertices to their internal representation as nodes.
     */
    private Map<V, Node> graphVertexMapping;

    /**
     * Supply vertex mapping which is used to define node supplies.
     */
    private Map<V, Integer> supplyMap;
    /**
     * Arc capacity mapping which is used to define arc capacity function.
     */
    private Map<E, Integer> capacityMap;
    /**
     * Arc cost mapping which is used to define arc cost function.
     */
    private Map<E, Integer> costMap;

    /**
     * Maximum number of arcs a network can contain between source nodes and t-source nodes.
     */
    private long source2TSourceUB;
    /**
     * Maximum number of arcs a network can contain between source nodes and t-nodes. This value is
     * decreased during skeleton network generation whenever an arc between corresponding pair of
     * nodes is generated.
     */
    private long source2TNodeUB;
    /**
     * Maximum number of arcs a network can contain between source nodes and sink nodes. This value
     * is decreased during skeleton network generation whenever an arc between corresponding pair of
     * nodes is generated.
     */
    private long source2SinkUB;

    /**
     * Maximum number of arcs a network can contain between t-nodes and t-sources.
     */
    private long tNode2TSourceUB;
    /**
     * Maximum number of arcs a network can contain between t-nodes. This value is decreased during
     * skeleton network generation whenever an arc between corresponding pair of nodes is generated.
     */
    private long tNode2TNodeUB;
    /**
     * Maximum number of arcs a network can contain between t-nodes and sink nodes. This value is
     * decreased during skeleton network generation whenever an arc between corresponding pair of
     * nodes is generated.
     */
    private long tNode2SinkUB;

    /**
     * Maximum number of arcs a network can contain between t-sinks and t-sources.
     */
    private long tSink2TSourceUB;
    /**
     * Maximum number of arcs a network can contain between t-sinks and t-nodes.
     */
    private long tSink2TNodeUB;
    /**
     * Maximum number of arcs a network can contain between t-sinks and sink nodes.
     */
    private long tSink2SinkUB;

    /**
     * Creates a new network generator using specified {@code config}. The created generator uses
     * random seed for the random number generator. Thus the code using this generator won't produce
     * the same networks between different invocations.
     *
     * @param config the network configuration for this generator.
     */
    public NetworkGenerator(NetworkGeneratorConfig config)
    {
        this(config, System.nanoTime());
    }

    /**
     * Creates a new network generator using specified {@code config} and {@code seed}. As the seed
     * for the random number generator is fixed, the code using this generator will produce the same
     * networks between different invocations.
     *
     * @param config the network configuration for this generator.
     * @param seed the seed for the random number generator.
     */
    public NetworkGenerator(NetworkGeneratorConfig config, long seed)
    {
        this(config, new Random(seed));
    }

    /**
     * Creates a new network generator using specified {@code config} and random number generator
     * {@code rng}. The network generated by this algorithm depends entirely on the random number
     * sequences produced by {@code rng} given a fixed network config.
     *
     * @param config the network configuration for this generator.
     * @param rng the random number generator for this algorithm.
     */
    public NetworkGenerator(NetworkGeneratorConfig config, Random rng)
    {
        this.config = config;
        this.rng = rng;
    }

    /**
     * Generates a bipartite matching problem satisfying the parameters specified in the config
     * provided to this generator. The provided network config must specify a bipartite matching
     * problem, otherwise an exception will be throws by this method. For a description of the
     * bipartite matching problem, see {@link BipartiteMatchingProblem}.
     *
     * @param graph the target graph which will represent the generated problem.
     * @return generated bipartite matching problem.
     */
    public BipartiteMatchingProblem<V, E> generateBipartiteMatchingProblem(Graph<V, E> graph)
    {
        if (!config.isAssignmentProblem()) {
            throw new IllegalArgumentException(
                "Input config doesn't specify a bipartite matching problem");
        }
        GraphTests.requireDirected(graph);

        generate(graph);

        return new BipartiteMatchingProblem.BipartiteMatchingProblemImpl<>(
            graph, new HashSet<>(networkInfo.getSources()), new HashSet<>(networkInfo.getSinks()),
            e -> (double) costMap.get(e), config.isCostWeighted());
    }

    /**
     * Generates a maximum flow problem satisfying the parameters specified in the config provided
     * to this generator. The provided network config must specify a maximum flow problem, otherwise
     * an exception will be throws by this method. For a description of the maximum flow problem,
     * see {@link MaximumFlowProblem}.
     *
     * @param graph the target graph which will represent the generated problem.
     * @return generated maximum flow problem.
     */
    public MaximumFlowProblem<V, E> generateMaxFlowProblem(Graph<V, E> graph)
    {
        if (!config.isMaxFlowProblem()) {
            throw new IllegalArgumentException(
                "Input config doesn't specify a maximum flow problem");
        }
        GraphTests.requireDirected(graph);

        generate(graph);

        // calling network info to get unmodifiable source and sink lists
        return new MaximumFlowProblem.MaximumFlowProblemImpl<>(
            graph, new HashSet<>(networkInfo.getSources()), new HashSet<>(networkInfo.getSinks()),
            e -> (double) capacityMap.get(e));
    }

    /**
     * Generates a minimum cost flow problem satisfying the parameters specified in the config
     * provided to this generator. For a description of the minimum cost flow problem, see
     * {@link MinimumCostFlowProblem}.
     *
     * @param graph the target graph which will represent the generated problem.
     * @return generated minimum cost flow problem.
     */
    public MinimumCostFlowProblem<V, E> generateMinimumCostFlowProblem(Graph<V, E> graph)
    {
        GraphTests.requireDirected(graph);

        generate(graph);

        return new MinimumCostFlowProblem.MinimumCostFlowProblemImpl<>(
            graph, v -> supplyMap.getOrDefault(v, 0), e -> capacityMap.get(e), e -> costMap.get(e));
    }

    /**
     * Runs all the steps of the generator algorithm. For the brief description of the algorithm,
     * see the class documentation. The complete NETGEN algorithm description is given in the
     * original paper.
     *
     * @param graph the target graph which will represent the generated problem.
     */
    private void generate(Graph<V, E> graph)
    {
        init(graph);

        createSupply();

        // generating skeleton network
        initChains();
        generateChains();
        connectChainsToSinks();

        addAllRemainingArcs();

        networkInfo.vertices = nodes.stream().map(n -> n.graphVertex).collect(Collectors.toList());
    }

    /**
     * Initializes internal datastructures. This method gets called during every invocation of the
     * {@link NetworkGenerator#generate(Graph)} to clear information from previous invocation.
     *
     * @param graph the target graph which will represent the generated problem.
     */
    private void init(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph);

        this.nodes = new ArrayList<>();
        this.graphVertexMapping = CollectionUtil.newHashMapWithExpectedSize(config.getNodeNum());

        this.supplyMap = new HashMap<>();
        this.capacityMap = CollectionUtil.newHashMapWithExpectedSize(config.getArcNum());
        this.costMap = CollectionUtil.newHashMapWithExpectedSize(config.getArcNum());

        this.networkInfo = new NetworkInfo<>(config);

        this.source2TSourceUB = config.getMaxSource2TSourceArcNum();
        this.source2TNodeUB = config.getMaxSource2TNodeArcNum();
        this.source2SinkUB = config.getMaxSource2SinkArcNum();

        this.tNode2TSourceUB = config.getMaxTNode2TSourceArcNum();
        this.tNode2TNodeUB = config.getMaxTNode2TNodeArcNum();
        this.tNode2SinkUB = config.getMaxTNode2SinkArcNum();

        this.tSink2TSourceUB = config.getMaxTSink2TSourceArcNum();
        this.tSink2TNodeUB = config.getMaxTSink2TNodeArcNum();
        this.tSink2SinkUB = config.getMaxTSink2SinkArcNum();

        createNodes(config.getPureSourceNum(), NodeType.PURE_SOURCE);
        createNodes(config.getTransshipSourceNum(), NodeType.TRANSSHIP_SOURCE);
        createNodes(config.getTransshipNodeNum(), NodeType.TRANSSHIP_NODE);
        createNodes(config.getTransshipSinkNum(), NodeType.TRANSSHIP_SINK);
        createNodes(config.getPureSinkNum(), NodeType.PURE_SINK);
    }

    /**
     * Creates {@code num} nodes of the specified {@code type}.
     *
     * @param num the number of nodes to generate.
     * @param type the type of nodes to generate.
     */
    private void createNodes(int num, NodeType type)
    {
        for (int i = 0; i < num; i++) {
            V vertex = graph.addVertex();
            Node node = new Node(vertex, type);
            nodes.add(node);
            graphVertexMapping.put(vertex, node);
        }
    }

    /**
     * Distributes supply units among source nodes.
     * <p>
     * The precondition for this method is that totalSupply >= max(sourceNum, sinkNum). This method
     * guarantees that every sourceNode received at least one unit of supply.
     */
    private void createSupply()
    {
        // supply per source is guaranteed to be at least 1
        int supplyPerSource = config.getTotalSupply() / config.getSourceNum();
        for (int sourceId = 0; sourceId < config.getSourceNum(); sourceId++) {
            // every source's supply is guaranteed to be at least one
            int partialSupply = generatePositiveRandom(supplyPerSource);
            nodes.get(sourceId).supply += partialSupply;

            // remaining supply is given to a random source node
            int randomSourceId = generateRandom(config.getSourceNum());
            nodes.get(randomSourceId).supply += supplyPerSource - partialSupply;
        }

        // assign the rest of the supply to a random source
        int randomSourceId = generateRandom(config.getSourceNum());
        nodes.get(randomSourceId).supply += config.getTotalSupply() % config.getSourceNum();

        // save the result in the supply map
        nodes.forEach(node -> {
            if (node.supply != 0) {
                supplyMap.put(node.graphVertex, node.supply);
            }
        });
    }

    /**
     * Initializes source chains by adding source nodes as 1-st nodes of their chains.
     */
    private void initChains()
    {
        for (Node node : getSources()) {
            node.chainNodes.add(node);
        }
    }

    /**
     * Generates source chains using all t-nodes. The generated chains are disjoint and not yet
     * connected to sinks.
     */
    private void generateChains()
    {
        int transshipmentNodeNum = config.getTransshipNodeNum();
        int sixtyPercent = (6 * transshipmentNodeNum) / 10;

        ElementsSequenceGenerator<Node> tNodesGenerator =
            new ElementsSequenceGenerator<>(getTransshipNodes(), rng);

        // generating chains from source nodes using ~60% of pure transshipment nodes
        for (int i = 0, chainSourceId = 0; i < sixtyPercent; i++, chainSourceId++) {
            if (chainSourceId == config.getSourceNum()) {
                chainSourceId = 0;
            }
            Node arcHead = tNodesGenerator.next();
            Node chainSource = nodes.get(chainSourceId);

            addSkeletonArc(chainSource, chainSource.getLastInChain(), arcHead);
        }

        // randomly extending generated chains using remaining ~40% of pure transhipment nodes
        for (Node arcHead : tNodesGenerator) {
            int sourceId = rng.nextInt(config.getSourceNum());
            Node chainSource = nodes.get(sourceId);

            addSkeletonArc(chainSource, chainSource.getLastInChain(), arcHead);
        }

    }

    /**
     * Connects generated chains to sinks and distributes network supply among sinks. This method
     * guarantees that:
     * <p>
     * 1. Every source chain is connected to at least one sink. 2. Every sink is connected to at
     * least one source chain. 3. Every sink's supply is at most -1 (or its demand is at least 1).
     */
    private void connectChainsToSinks()
    {
        int remainingArcs = config.getArcNum() - graph.edgeSet().size();
        assert remainingArcs >= config.getSinkNum();

        /*
         * First, we have to compute the number of arcs to use to connect source chains to sinks.
         * Our "guess" is 2 * max(sourceNum, sinkNum). At the same time we have to take the
         * following upper bounds into account: 1. this value is bounded by #remaining_arcs from
         * above. 2. this value is bounded by source2SinkUB + tNode2SinkUB from above.
         *
         * We have to take one more bound into account to ensure that every sink's demand is at
         * least 1. A source's supply is distributed among sinks it's connected to such that every
         * sink get's at least 1 unit of demand. Thus, for every source we don't generate more arcs
         * that the number of supply units it has.
         */
        int chainToSinkArcs =
            Math.min(remainingArcs, 2 * Math.max(config.getSourceNum(), config.getSinkNum()));
        int chainToSinkArcUB = (int) Math.min(source2SinkUB + tNode2SinkUB, MAX_ARC_NUM);
        chainToSinkArcs = Math.min(chainToSinkArcUB, chainToSinkArcs);

        List<Node> sources = getSources();

        // this sum is at least max(sourceNum, sinkNum)
        // because config.getTotalSupply() >= max(sourceNum, sinkNum)
        int supplyAndSinkNumUB = 0;
        for (Node source : sources) {
            supplyAndSinkNumUB += Math.min(config.getSinkNum(), source.supply);
        }
        chainToSinkArcs = Math.min(chainToSinkArcs, supplyAndSinkNumUB);

        // distributing sinks among sources
        Distributor<Node> sinkDistributor = new Distributor<>(rng);
        sinkDistributor.addLowerBound(source -> 1);
        sinkDistributor.addUpperBound(source -> source.supply);
        sinkDistributor.addUpperBound(source -> config.getSinkNum());
        List<Integer> sinksPerSourceDist =
            sinkDistributor.getDistribution(sources, chainToSinkArcs);

        List<Node> sinks = getSinks();
        /*
         * Generate the assigned number of source chain to sink arcs from every source. This process
         * cycles through the sink list ensuring that every sink gets at least or arc from some
         * source chain.
         */
        for (int i = 0, sinkId = 0; i < sources.size(); i++) {
            Node chainSource = sources.get(i);
            int sinksPerSource = sinksPerSourceDist.get(i);

            // taking a needed portion of sinks from the sink list.
            List<Node> chainSinks = new ArrayList<>();
            for (int j = 0; j < sinksPerSource; j++, sinkId++) {
                if (sinkId == sinks.size()) {
                    sinkId = 0;
                }
                chainSinks.add(sinks.get(sinkId));
            }

            /*
             * Randomly distribute supply units among target sinks such that every sink gets at
             * least 1 unit of demand.
             */
            Distributor<Node> sinkSupplyDistributor = new Distributor<>(rng);
            sinkSupplyDistributor.addLowerBound(sink -> 1);

            List<Integer> supplyDist =
                sinkSupplyDistributor.getDistribution(chainSinks, chainSource.supply);

            for (int j = 0; j < sinksPerSource; j++) {
                Node sink = chainSinks.get(j);
                int sinkSupply = supplyDist.get(j);

                int arcTailIndex = generateRandom(chainSource.getChainLength());
                Node arcTail = chainSource.chainNodes.get(arcTailIndex);

                addSkeletonArc(chainSource, arcTail, sink);
                supplyMap
                    .put(
                        sink.graphVertex, supplyMap.getOrDefault(sink.graphVertex, 0) - sinkSupply);
            }
        }

    }

    /**
     * Generates remaining arcs to satisfy the arcNum constraint.
     */
    private void addAllRemainingArcs()
    {

        final int remainingArcs = config.getArcNum() - graph.edgeSet().size();
        assert remainingArcs >= 0;

        /*
         * Upper bounds for every class of arcs.
         */
        List<Long> upperBounds = new ArrayList<>(
            List
                .of(
                    source2TSourceUB, source2TNodeUB, source2SinkUB, tNode2TSourceUB, tNode2TNodeUB,
                    tNode2SinkUB, tSink2TSourceUB, tSink2TNodeUB, tSink2SinkUB));

        long classBoundsSum = upperBounds.stream().mapToLong(l -> l).sum();
        if (classBoundsSum == 0) {
            return;
        }

        /*
         * Distribute remaining arcs among every arc class. Upper bounds of the number of arcs for
         * every class are taken into account. Additionally, as for large networks these upperbounds
         * are large, we introduce weight bounds to distribute arcs evenly among arc classes.
         */
        Distributor<Integer> arcNumDistributor = new Distributor<>(rng);
        arcNumDistributor
            .addUpperBound(classId -> (int) Math.min(upperBounds.get(classId), MAX_ARC_NUM));
        arcNumDistributor.addUpperBound(classId -> {
            double classWeight = (double) upperBounds.get(classId) / classBoundsSum;
            int weightBound = (int) (2.0 * classWeight * remainingArcs);
            return weightBound + 1; // make this bound positive
        });

        List<Integer> arcNumDistribution = arcNumDistributor
            .getDistribution(
                IntStream.range(0, upperBounds.size()).boxed().collect(Collectors.toList()),
                remainingArcs);

        generateArcs(getSources(), getTransshipSources(), arcNumDistribution.get(0));
        generateArcs(getSources(), getTransshipNodes(), arcNumDistribution.get(1));
        generateArcs(getSources(), getSinks(), arcNumDistribution.get(2));

        generateArcs(getTransshipNodes(), getTransshipSources(), arcNumDistribution.get(3));
        generateArcs(getTransshipNodes(), getTransshipNodes(), arcNumDistribution.get(4));
        generateArcs(getTransshipNodes(), getSinks(), arcNumDistribution.get(5));

        generateArcs(getTransshipSinks(), getTransshipSources(), arcNumDistribution.get(6));
        generateArcs(getTransshipSinks(), getTransshipNodes(), arcNumDistribution.get(7));
        generateArcs(getTransshipSinks(), getSinks(), arcNumDistribution.get(8));

        assert config.getArcNum() - graph.edgeSet().size() == 0;
    }

    /**
     * Generates {@code arcsToGenerate} number of arcs between nodes from {@code tails} and
     * {@code heads}. A node can belong to both lists at the same time.
     *
     * @param tails list of possible arc tails.
     * @param heads list of possible arc heads.
     * @param arcsToGenerate number of arcs to generate
     */
    private void generateArcs(List<Node> tails, List<Node> heads, int arcsToGenerate)
    {

        // For every tail, compute an upper bound on the number arcs it's
        // possible to generate from it.
        Set<Node> headsSet = new HashSet<>(heads);
        List<Integer> outDegrees = tails
            .stream().map(node -> getPossibleArcNum(node, headsSet)).collect(Collectors.toList());
        long degreeSum = outDegrees.stream().mapToLong(i -> i).sum();

        // Add weight bounds as well to make the distribution more uniform.
        Distributor<Integer> arcNumDistributor = new Distributor<>(rng);
        arcNumDistributor.addUpperBound(outDegrees::get);
        arcNumDistributor.addUpperBound(tailId -> {
            double tailWeight = (double) outDegrees.get(tailId) / degreeSum;
            int tailArcWeightBound = (int) (2 * tailWeight * arcsToGenerate);
            return tailArcWeightBound + 1;
        });

        List<Integer> arcNumDistribution = arcNumDistributor
            .getDistribution(
                IntStream.range(0, tails.size()).boxed().collect(Collectors.toList()),
                arcsToGenerate);

        // For every tail, generate the assigned number of arcs.
        for (int i = 0; i < tails.size(); i++) {

            int tailArcNum = tailArcNum(tails, heads, arcNumDistribution, i);
			assert tailArcNum == 0;
        }
    }

	private int tailArcNum(List<NetworkGenerator<V, E>.Node> tails, List<NetworkGenerator<V, E>.Node> heads,
			List<Integer> arcNumDistribution, int i) {
		Node tail = tails.get(i);
		int tailArcNum = arcNumDistribution.get(i);
		ElementsSequenceGenerator<Node> headGenerator = new ElementsSequenceGenerator<>(heads, rng);
		while (tailArcNum > 0 && headGenerator.hasNext()) {
			Node currentHead = headGenerator.next();
			if (isValidArc(tail, currentHead)) {
				--tailArcNum;
				addArc(tail, currentHead);
			}
		}
		return tailArcNum;
	}

    /**
     * Returns the number of arcs it is possible to generate from {@code node} to the {@code nodes}
     * set.
     *
     * @param node an arc tail.
     * @param nodes set of possible arc heads.
     * @return the computed number of arcs it's possible to generate.
     */
    private int getPossibleArcNum(Node node, Set<Node> nodes)
    {
        int possibleArcNum = nodes.size();
        if (nodes.contains(node)) {
            possibleArcNum--;
        }
        for (E arc : graph.outgoingEdgesOf(node.graphVertex)) {
            Node arcHead =
                graphVertexMapping.get(Graphs.getOppositeVertex(graph, arc, node.graphVertex));
            if (nodes.contains(arcHead)) {
                possibleArcNum--;
            }
        }
        return possibleArcNum;
    }

    /**
     * Returns the network information computed for the last generated problem. Call this method
     * only after the first invocation of any generating method.
     *
     * @return network information.
     */
    public NetworkInfo<V, E> getNetworkInfo()
    {
        return networkInfo;
    }

    /**
     * Checks if it is possible to add an arc between {@code tail} and {@code head} to the network.
     *
     * @param tail arc tail.
     * @param head arc head.
     * @return {@code true} if it's possible to add an arc, {@code false} otherwise.
     */
    private boolean isValidArc(Node tail, Node head)
    {
        return tail != head && !graph.containsEdge(tail.graphVertex, head.graphVertex);
    }

    /**
     * Adds an arc between the {@code tail} and {@code head}. The added arc is registered to update
     * upper bounds on the number of possible arcs to generate.
     *
     * @param chainSource the source of the chain.
     * @param tail arc tail.
     * @param head arc head.
     */
    private void addSkeletonArc(Node chainSource, Node tail, Node head)
    {
        assert isValidArc(tail, head);
        E arc = graph.addEdge(tail.graphVertex, head.graphVertex);
        capacityMap.put(arc, Math.max(getCapacity(), chainSource.supply));
        costMap.put(arc, getCost());

        registerSkeletonArc(tail, head);
        networkInfo.registerChainArc(arc);
        if (head.type == NodeType.TRANSSHIP_NODE) {
            chainSource.chainNodes.add(head);
        }
    }

    /**
     * Adds a simple arc to the network. The arc isn't registered.
     *
     * @param tail arc tail.
     * @param head arc head.
     */
    private void addArc(Node tail, Node head)
    {
        assert isValidArc(tail, head);
        E edge = graph.addEdge(tail.graphVertex, head.graphVertex);

        capacityMap.put(edge, getCapacity());
        costMap.put(edge, getCost());
    }

    /**
     * Registers an arc between {@code tail} and {@code head} by decreasing one of the upper bounds
     * by 1.
     *
     * @param tail arc tail.
     * @param head arc head.
     */
    private void registerSkeletonArc(Node tail, Node head)
    {
        switch (tail.type) {
        case PURE_SOURCE:
        case TRANSSHIP_SOURCE:
            switch (head.type) {
            case TRANSSHIP_NODE:
                source2TNodeUB--;
                break;
            case TRANSSHIP_SINK:
            case PURE_SINK:
                source2SinkUB--;
                break;
            default:
                // should never happen
                throw new RuntimeException();
            }
            break;
        case TRANSSHIP_NODE:
            switch (head.type) {
            case TRANSSHIP_NODE:
                tNode2TNodeUB--;
                break;
            case TRANSSHIP_SINK:
            case PURE_SINK:
                tNode2SinkUB--;
                break;
            default:
                // should never happen
                throw new RuntimeException();
            }
            break;
        default:
            // should never happen
            throw new RuntimeException();
        }
    }

    /**
     * Generates an arc capacity. This capacity can be infinite.
     *
     * @return the generated arc capacity.
     */
    private int getCapacity()
    {
        int percent = generateBetween(1, 100);
        if (percent <= config.getPercentCapacitated()) {
            return generateBetween(config.getMinCap(), config.getMaxCap());
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Generates an arc cost. This cost can be infinite.
     *
     * @return the generated arc cost.
     */
    private int getCost()
    {
        int percent = generateBetween(1, 100);
        if (percent <= config.getPercentWithInfCost()) {
            return Integer.MAX_VALUE;
        } else {
            return generateBetween(config.getMinCost(), config.getMaxCost());
        }
    }

    private int generatePositiveRandom(int boundInclusive)
    {
        return rng.nextInt(boundInclusive) + 1;
    }

    /**
     * Generates a random number using random number generator between {@code startInclusive} and
     * {@code endInclusive}.
     *
     * @param startInclusive lower bound
     * @param endInclusive upper bound
     * @return the generated number
     */
    private int generateBetween(int startInclusive, int endInclusive)
    {
        return rng.nextInt(endInclusive - startInclusive + 1) + startInclusive;
    }

    /**
     * Generates a random number using random number generator between 0 and {@code endExclusive}.
     *
     * @param endExclusive upper bound.
     * @return the generated number.
     */
    private int generateRandom(int endExclusive)
    {
        return rng.nextInt(endExclusive);
    }

    /**
     * Returns a list containing generated transshipment sources.
     *
     * @return a list containing generated transshipment sources.
     */
    private List<Node> getTransshipSources()
    {
        return nodes.subList(config.getPureSourceNum(), config.getSourceNum());
    }

    /**
     * Returns a list containing generated source (pure sources + t-sources).
     *
     * @return a list containing generated sources.
     */
    private List<Node> getSources()
    {
        return nodes.subList(0, config.getSourceNum());
    }

    /**
     * Returns a list containing generated t-nodes.
     *
     * @return a list containing generated t-nodes.
     */
    private List<Node> getTransshipNodes()
    {
        return nodes
            .subList(config.getSourceNum(), config.getSourceNum() + config.getTransshipNodeNum());
    }

    /**
     * Returns a list containing generated transshipment sinks.
     *
     * @return a list containing generated transshipment sinks.
     */
    private List<Node> getTransshipSinks()
    {
        return nodes
            .subList(
                config.getSourceNum() + config.getTransshipNodeNum(),
                nodes.size() - config.getPureSinkNum());
    }

    /**
     * Returns a list containing generated sinks (pure sinks + t-sinks).
     *
     * @return a list containing generated sinks.
     */
    private List<Node> getSinks()
    {
        return nodes.subList(config.getSourceNum() + config.getTransshipNodeNum(), nodes.size());
    }

    /**
     * Enum specifying the nodes type.
     */
    private enum NodeType
    {
        PURE_SOURCE
        {
            @Override
            public String toString()
            {
                return "Pure source";
            }
        },
        TRANSSHIP_SOURCE
        {
            @Override
            public String toString()
            {
                return "Transship source";
            }
        },
        TRANSSHIP_NODE
        {
            @Override
            public String toString()
            {
                return "Transship node";
            }
        },
        TRANSSHIP_SINK
        {
            @Override
            public String toString()
            {
                return "Transship sink";
            }
        },
        PURE_SINK
        {
            @Override
            public String toString()
            {
                return "Pure sink";
            }
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public abstract String toString();
    }

    /**
     * Internal representation of network nodes. This class is used to store auxiliary information
     * during generation process.
     */
    private class Node
    {
        /**
         * Graph vertex counterpart of this node.
         */
        V graphVertex;
        /**
         * Supply units of this node. This value is 0 for t-nodes.
         */
        int supply;
        /**
         * Type of this node.
         */
        NodeType type;
        /**
         * List of chain nodes. This list is empty for t-nodes and sinks.
         */
        List<Node> chainNodes;

        /**
         * Creates a new node using {@code graphVertex} and {@code type}.
         *
         * @param graphVertex network vertex.
         * @param type type of this node.
         */
        Node(V graphVertex, NodeType type)
        {
            this.graphVertex = graphVertex;
            this.type = type;
            chainNodes = new ArrayList<>();
        }

        /**
         * Returns the last node of this node's chain.
         *
         * @return the last node of this node's chain.
         */
        Node getLastInChain()
        {
            return chainNodes.get(chainNodes.size() - 1);
        }

        /**
         * Returns the length of this node's chain.
         *
         * @return the length of this node's chain.
         */
        int getChainLength()
        {
            return chainNodes.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String.format("{%s}: type = %s, supply = %d", graphVertex, type, supply);
        }

    }
}
