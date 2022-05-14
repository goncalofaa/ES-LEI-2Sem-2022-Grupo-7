/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.*;

import java.io.*;

/**
 * Default implementation of the graph type.
 * 
 * <p>
 * The graph type describes various properties of a graph such as whether it is directed, undirected
 * or mixed, whether it contain self-loops (a self-loop is an edge where the source vertex is the
 * same as the target vertex), whether it contain multiple (parallel) edges (multiple edges which
 * connect the same pair of vertices) and whether it is weighted or not.
 * 
 * <p>
 * The type of a graph can be queried on runtime using method {@link Graph#getType()}. This way, for
 * example, an algorithm can have different behavior based on whether the input graph is directed or
 * undirected, etc.
 * 
 * @author Dimitrios Michail
 */
public class DefaultGraphType
    implements
    GraphType,
    Serializable
{
    private static final long serialVersionUID = 4291049312119347474L;

    private final boolean directed;
    private final boolean undirected;
    private final boolean selfLoops;
    private final boolean multipleEdges;
    private final boolean weighted;
    private final boolean allowsCycles;
    private final boolean modifiable;

    public DefaultGraphType(
        boolean directed, boolean undirected, boolean selfLoops, boolean multipleEdges,
        boolean weighted, boolean allowsCycles, boolean modifiable)
    {
        this.directed = directed;
        this.undirected = undirected;
        this.selfLoops = selfLoops;
        this.multipleEdges = multipleEdges;
        this.weighted = weighted;
        this.allowsCycles = allowsCycles;
        this.modifiable = modifiable;
    }

    @Override
    public boolean isDirected()
    {
        return directed && !undirected;
    }

    @Override
    public boolean isUndirected()
    {
        return undirected && !directed;
    }

    @Override
    public boolean isMixed()
    {
        return undirected && directed;
    }

    @Override
    public boolean isAllowingMultipleEdges()
    {
        return multipleEdges;
    }

    @Override
    public boolean isAllowingSelfLoops()
    {
        return selfLoops;
    }

    @Override
    public boolean isWeighted()
    {
        return weighted;
    }

    @Override
    public boolean isAllowingCycles()
    {
        return allowsCycles;
    }

    @Override
    public boolean isModifiable()
    {
        return modifiable;
    }

    @Override
    public boolean isSimple()
    {
        return !isAllowingMultipleEdges() && !isAllowingSelfLoops();
    }

    @Override
    public boolean isPseudograph()
    {
        return isAllowingMultipleEdges() && isAllowingSelfLoops();
    }

    @Override
    public boolean isMultigraph()
    {
        return isAllowingMultipleEdges() && !isAllowingSelfLoops();
    }

    @Override
    public GraphType asDirected()
    {
        return new DGT_Builder(this).directed().build();
    }

    @Override
    public GraphType asUndirected()
    {
        return new DGT_Builder(this).undirected().build();
    }

    @Override
    public GraphType asMixed()
    {
        return new DGT_Builder(this).mixed().build();
    }

    @Override
    public GraphType asUnweighted()
    {
        return new DGT_Builder(this).weighted(false).build();
    }

    @Override
    public GraphType asWeighted()
    {
        return new DGT_Builder(this).weighted(true).build();
    }

    @Override
    public GraphType asModifiable()
    {
        return new DGT_Builder(this).modifiable(true).build();
    }

    @Override
    public GraphType asUnmodifiable()
    {
        return new DGT_Builder(this).modifiable(false).build();
    }

    /**
     * A simple graph type. An undirected graph for which at most one edge connects any two
     * vertices, and self-loops are not permitted.
     * 
     * @return a simple graph type
     */
    public static DefaultGraphType simple()
    {
        return new DGT_Builder()
            .undirected().allowSelfLoops(false).allowMultipleEdges(false).weighted(false).build();
    }

    /**
     * A multigraph type. A non-simple undirected graph in which no self-loops are permitted, but
     * multiple edges between any two vertices are.
     * 
     * @return a multigraph type
     */
    public static DefaultGraphType multigraph()
    {
        return new DGT_Builder()
            .undirected().allowSelfLoops(false).allowMultipleEdges(true).weighted(false).build();
    }

    /**
     * A pseudograph type. A non-simple undirected graph in which both graph self-loops and multiple
     * edges are permitted.
     * 
     * @return a pseudograph type
     */
    public static DefaultGraphType pseudograph()
    {
        return new DGT_Builder()
            .undirected().allowSelfLoops(true).allowMultipleEdges(true).weighted(false).build();
    }

    /**
     * A directed simple graph type. An undirected graph for which at most one edge connects any two
     * vertices, and self-loops are not permitted.
     * 
     * @return a directed simple graph type
     */
    public static DefaultGraphType directedSimple()
    {
        return new DGT_Builder()
            .directed().allowSelfLoops(false).allowMultipleEdges(false).weighted(false).build();
    }

    /**
     * A directed multigraph type. A non-simple undirected graph in which no self-loops are
     * permitted, but multiple edges between any two vertices are.
     * 
     * @return a directed multigraph type
     */
    public static DefaultGraphType directedMultigraph()
    {
        return new DGT_Builder()
            .directed().allowSelfLoops(false).allowMultipleEdges(true).weighted(false).build();
    }

    /**
     * A directed pseudograph type. A non-simple undirected graph in which both graph self-loops and
     * multiple edges are permitted.
     * 
     * @return a directed pseudograph type
     */
    public static DefaultGraphType directedPseudograph()
    {
        return new DGT_Builder()
            .directed().allowSelfLoops(true).allowMultipleEdges(true).weighted(false).build();
    }

    /**
     * A mixed graph type. A graph having a set of undirected and a set of directed edges, which may
     * contain self-loops and multiple edges are permitted.
     * 
     * @return a mixed graph type
     */
    public static DefaultGraphType mixed()
    {
        return new DGT_Builder()
            .mixed().allowSelfLoops(true).allowMultipleEdges(true).weighted(false).build();
    }

    /**
     * A directed acyclic graph.
     * 
     * @return a directed acyclic graph type
     */
    public static DefaultGraphType dag()
    {
        return new DGT_Builder()
            .directed().allowSelfLoops(false).allowMultipleEdges(true).allowCycles(false)
            .weighted(false).build();
    }

    @Override
    public String toString()
    {
        return "DefaultGraphType [directed=" + directed + ", undirected=" + undirected
            + ", self-loops=" + selfLoops + ", multiple-edges=" + multipleEdges + ", weighted="
            + weighted + ", allows-cycles=" + allowsCycles + ", modifiable=" + modifiable + "]";
    }





}
