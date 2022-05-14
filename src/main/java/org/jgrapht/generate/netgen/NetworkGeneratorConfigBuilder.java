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

/**
 * Builder class for the {@link NetworkGeneratorConfig}. This class perform all the necessary
 * parameter validation and provides meaningful error messages. For the network parameter
 * description and a complete list of parameter constrants, see {@link NetworkGeneratorConfig}. Use
 * this class to construct instances of the {@link NetworkGeneratorConfig}.
 *
 * @author Timofey Chudakov
 * @see NetworkGenerator
 * @see NetworkGeneratorConfig
 */
public class NetworkGeneratorConfigBuilder
{
    private NetworkGeneratorConfigBuilderProduct networkGeneratorConfigBuilderProduct = new NetworkGeneratorConfigBuilderProduct();
	int maxCost = 0;
    /**
     * Builds the {@link NetworkGeneratorConfig}. This method performs remaining parameter
     * validation.
     *
     * @return the constructed {@link NetworkGeneratorConfig}.
     */
    public NetworkGeneratorConfig build()
    {
        if (networkGeneratorConfigBuilderProduct.getNodeNum() <= 0) {
            networkGeneratorConfigBuilderProduct.invalidParam("Number of nodes must be positive");
        } else if (networkGeneratorConfigBuilderProduct.getArcNum() <= 0) {
            networkGeneratorConfigBuilderProduct.invalidParam("Number of arcs must be positive");
        } else if (networkGeneratorConfigBuilderProduct.getSourceNum() <= 0) {
            networkGeneratorConfigBuilderProduct.invalidParam("Number of sources must be positive");
        } else if (networkGeneratorConfigBuilderProduct.getSinkNum() <= 0) {
            networkGeneratorConfigBuilderProduct.invalidParam("Number of sinks must be positive");
        } else if (networkGeneratorConfigBuilderProduct.getSourceNum() + networkGeneratorConfigBuilderProduct.getSinkNum() > networkGeneratorConfigBuilderProduct.getNodeNum()) {
            networkGeneratorConfigBuilderProduct.invalidParam("Number of sources and sinks must not exceed the number of nodes");
        } else if (networkGeneratorConfigBuilderProduct.getTSourceNum() > networkGeneratorConfigBuilderProduct.getSourceNum()) {
            networkGeneratorConfigBuilderProduct.invalidParam(
                "Number of transhipment sources must not exceed the overall number of sources");
        } else if (networkGeneratorConfigBuilderProduct.getTSinkNum() > networkGeneratorConfigBuilderProduct.getSinkNum()) {
            networkGeneratorConfigBuilderProduct.invalidParam(
                "Number of transhipment sinks must not exceed the overall number of sinks");
        } else if (networkGeneratorConfigBuilderProduct.getTotalSupply() < Math.max(networkGeneratorConfigBuilderProduct.getSourceNum(), networkGeneratorConfigBuilderProduct.getSinkNum())) {
            networkGeneratorConfigBuilderProduct.invalidParam(
                "Total supply must not be less than the number of sources and the number of sinks");
        } else if (networkGeneratorConfigBuilderProduct.getMinCap() > networkGeneratorConfigBuilderProduct.getMaxCap()) {
            networkGeneratorConfigBuilderProduct.invalidParam("Minimum capacity must not exceed the maximum capacity");
        } else if (networkGeneratorConfigBuilderProduct.getMinCap() <= 0) {
            networkGeneratorConfigBuilderProduct.invalidParam("Minimum capacity must be positive");
        } else if (networkGeneratorConfigBuilderProduct.getMinCost() > maxCost) {
            networkGeneratorConfigBuilderProduct.invalidParam("Minimum cost must not exceed the maximum cost");
        }
        int tNodeNum = networkGeneratorConfigBuilderProduct.getNodeNum() - networkGeneratorConfigBuilderProduct.getSourceNum() - networkGeneratorConfigBuilderProduct.getSinkNum();
        long minArcNum = NetworkGeneratorConfig.getMinimumArcNum(networkGeneratorConfigBuilderProduct.getSourceNum(), tNodeNum, networkGeneratorConfigBuilderProduct.getSinkNum());
        long maxArcNum = NetworkGeneratorConfig
            .getMaximumArcNum(networkGeneratorConfigBuilderProduct.getSourceNum(), networkGeneratorConfigBuilderProduct.getTSourceNum(), tNodeNum, networkGeneratorConfigBuilderProduct.getTSinkNum(), networkGeneratorConfigBuilderProduct.getSinkNum());

        if (networkGeneratorConfigBuilderProduct.getArcNum() < minArcNum) {
            networkGeneratorConfigBuilderProduct.invalidParam("Too few arcs to generate a valid problem");
        } else if (networkGeneratorConfigBuilderProduct.getArcNum() > maxArcNum) {
            networkGeneratorConfigBuilderProduct.invalidParam("Too many arcs to generate a valid problem");
        }
        return new NetworkGeneratorConfig(
            networkGeneratorConfigBuilderProduct.getNodeNum(), networkGeneratorConfigBuilderProduct.getArcNum(), networkGeneratorConfigBuilderProduct.getSourceNum(), networkGeneratorConfigBuilderProduct.getSinkNum(), networkGeneratorConfigBuilderProduct.getTSourceNum(), networkGeneratorConfigBuilderProduct.getTSinkNum(), networkGeneratorConfigBuilderProduct.getTotalSupply(), networkGeneratorConfigBuilderProduct.getMinCap(), networkGeneratorConfigBuilderProduct.getMaxCap(),
            networkGeneratorConfigBuilderProduct.getMinCost(), maxCost, networkGeneratorConfigBuilderProduct.getPercentCapacitated(), networkGeneratorConfigBuilderProduct.getPercentWithInfCost());
    }

    /**
     * Sets all the network parameters.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param sourceNum number of sources in the network
     * @param sinkNum number of sinks in the network
     * @param transshipSourceNum number of transshipment sources in the network
     * @param transshipSinkNum number of transshipment sinks in the network
     * @param totalSupply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @param percentCapacitated percent of arcs to have finite capacity
     * @param percentWithInfCost percent of arcs to have infinite cost
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setParams(
        int nodeNum, int arcNum, int sourceNum, int sinkNum, int transshipSourceNum,
        int transshipSinkNum, int totalSupply, int minCap, int maxCap, int minCost, int maxCost,
        int percentCapacitated, int percentWithInfCost)
    {
        networkGeneratorConfigBuilderProduct.setNodeNum(nodeNum, this);
        networkGeneratorConfigBuilderProduct.setArcNum(arcNum, this);
        networkGeneratorConfigBuilderProduct.setSourceNum(sourceNum, this);
        networkGeneratorConfigBuilderProduct.setSinkNum(sinkNum, this);
        networkGeneratorConfigBuilderProduct.setTSourceNum(transshipSourceNum, this);
        networkGeneratorConfigBuilderProduct.setTSinkNum(transshipSinkNum, this);
        networkGeneratorConfigBuilderProduct.setTotalSupply(totalSupply, this);
        networkGeneratorConfigBuilderProduct.setMinCap(minCap, this);
        networkGeneratorConfigBuilderProduct.setMaxCap(maxCap, this);
        networkGeneratorConfigBuilderProduct.setMinCost(minCost, this);
        setMaxCost(maxCost);
        networkGeneratorConfigBuilderProduct.setPercentCapacitated(percentCapacitated, this);
        networkGeneratorConfigBuilderProduct.setPercentWithInfCost(percentWithInfCost, this);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The values of minCap and maxCap are set to 1, the
     * values of {@code sourceNum} and {@code sinkNum} are set to 1 and the value of the
     * {@code percentCapacitated} is set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply)
    {
        setMaximumFlowProblemParams(nodeNum, arcNum, supply, 1, 1);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The values of {@code sourceNum} and
     * {@code sinkNum} are set to 1 and the value of the {@code percentCapacitated} is set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap)
    {
        setMaximumFlowProblemParams(nodeNum, arcNum, supply, minCap, maxCap, 1, 1);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset. The value of the {@code percentCapacitated} is
     * set to 100.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param sourceNum number of source in the network
     * @param sinkNum number of sinks in the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap, int sourceNum, int sinkNum)
    {
        setMaximumFlowProblemParams(
            nodeNum, arcNum, supply, minCap, maxCap, sourceNum, sinkNum, 100);
        return this;
    }

    /**
     * Sets maximum flow network parameter subset.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param supply total supply of the network
     * @param minCap arc capacity lower bound
     * @param maxCap arc capacity upper bound
     * @param sourceNum number of source in the network
     * @param sinkNum number of sinks in the network
     * @param percentCapacitated percent of arcs to have finite capacity
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setMaximumFlowProblemParams(
        int nodeNum, int arcNum, int supply, int minCap, int maxCap, int sourceNum, int sinkNum,
        int percentCapacitated)
    {
        setParams(
            nodeNum, arcNum, sourceNum, sinkNum, 0, 0, supply, minCap, maxCap, 1, 1,
            percentCapacitated, 0);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset. The values of the {@code minCost} and
     * {@code maxCost} are set to 1, the value of the {@code percentWithInfCost} is set to 0.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(int nodeNum, int arcNum)
    {
        setBipartiteMatchingProblemParams(nodeNum, arcNum, 1, 1);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset. The value of the {@code percentWithInfCost} is set
     * to 0.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(
        int nodeNum, int arcNum, int minCost, int maxCost)
    {
        networkGeneratorConfigBuilderProduct.setBipartiteMatchingProblemParams(nodeNum, arcNum, minCost, maxCost, 0, this);
        return this;
    }

    /**
     * Sets bipartite matching parameter subset.
     *
     * @param nodeNum number of nodes in the network
     * @param arcNum number of arcs in the network
     * @param minCost arc cost lower bound
     * @param maxCost arc cost upper bound
     * @param percentWithInfCost percent of arcs to have infinite cost
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setBipartiteMatchingProblemParams(
        int nodeNum, int arcNum, int minCost, int maxCost, int percentWithInfCost)
    {
        return networkGeneratorConfigBuilderProduct.setBipartiteMatchingProblemParams(nodeNum, arcNum, minCost, maxCost,
				percentWithInfCost, this);
    }

    /**
     * Sets the number of nodes in the network.
     *
     * @param nodeNum the number of nodes in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setNodeNum(int nodeNum)
    {
        return networkGeneratorConfigBuilderProduct.setNodeNum(nodeNum, this);
    }

    /**
     * Sets the number of arcs in the network.
     *
     * @param arcNum the number of arcs in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setArcNum(int arcNum)
    {
        return networkGeneratorConfigBuilderProduct.setArcNum(arcNum, this);
    }

    /**
     * Sets the number of sources in the network.
     *
     * @param sourceNum the number of sources in the network.
     * @return this object
     */
    public NetworkGeneratorConfigBuilder setSourceNum(int sourceNum)
    {
        return networkGeneratorConfigBuilderProduct.setSourceNum(sourceNum, this);
    }

    /**
     * Sets the number of sinks in the network.
     *
     * @param sinkNum the number of sinks in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setSinkNum(int sinkNum)
    {
        return networkGeneratorConfigBuilderProduct.setSinkNum(sinkNum, this);
    }

    /**
     * Sets the number of transshipment sources in the network.
     *
     * @param tSourceNum the number of transshipment sources in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTSourceNum(int tSourceNum)
    {
        return networkGeneratorConfigBuilderProduct.setTSourceNum(tSourceNum, this);
    }

    /**
     * Sets the number of transshipment sinks in the network.
     *
     * @param tSinkNum the number of transshipment sinks in the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTSinkNum(int tSinkNum)
    {
        return networkGeneratorConfigBuilderProduct.setTSinkNum(tSinkNum, this);
    }

    /**
     * Sets the total supply of the network.
     *
     * @param totalSupply the total supply of the network.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setTotalSupply(int totalSupply)
    {
        return networkGeneratorConfigBuilderProduct.setTotalSupply(totalSupply, this);
    }

    /**
     * Sets the arc capacity lower bound.
     *
     * @param minCap the arc capacity lower bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMinCap(int minCap)
    {
        return networkGeneratorConfigBuilderProduct.setMinCap(minCap, this);
    }

    /**
     * Sets the arc capacity upper bound.
     *
     * @param maxCap the arc capacity upper bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMaxCap(int maxCap)
    {
        return networkGeneratorConfigBuilderProduct.setMaxCap(maxCap, this);
    }

    /**
     * Sets the arc cost lower bound.
     *
     * @param minCost the arc cost lower bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMinCost(int minCost)
    {
        return networkGeneratorConfigBuilderProduct.setMinCost(minCost, this);
    }

    /**
     * Sets the arc cost upper bound.
     *
     * @param maxCost the arc cost upper bound.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setMaxCost(int maxCost)
    {
        this.maxCost = networkGeneratorConfigBuilderProduct.checkCapacityCostConstraint(maxCost);
        return this;
    }

    /**
     * Sets the percent of arcs to have finite capacity.
     *
     * @param percentCapacitated the percent of arcs to have finite capacity.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setPercentCapacitated(int percentCapacitated)
    {
        return networkGeneratorConfigBuilderProduct.setPercentCapacitated(percentCapacitated, this);
    }

    /**
     * Sets the percent of arcs to have infinite cost.
     *
     * @param percentWithInfCost the percent of arcs to have infinite cost.
     * @return this object.
     */
    public NetworkGeneratorConfigBuilder setPercentWithInfCost(int percentWithInfCost)
    {
        return networkGeneratorConfigBuilderProduct.setPercentWithInfCost(percentWithInfCost, this);
    }
}