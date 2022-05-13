package org.jgrapht.graph;

import org.jgrapht.GraphType;

/**
 * A builder for {@link DefaultGraphType}.
 *
 * @author Dimitrios Michail
 */
public  class DGT_Builder
{
    private boolean directed;
    private boolean undirected;
    private boolean allowSelfLoops;
    private boolean allowMultipleEdges;
    private boolean weighted;
    private boolean allowCycles;
    private boolean modifiable;

    /**
     * Construct a new Builder.
     */
    public DGT_Builder()
    {
        this.directed = false;
        this.undirected = true;
        this.allowSelfLoops = true;
        this.allowMultipleEdges = true;
        this.weighted = false;
        this.allowCycles = true;
        this.modifiable = true;
    }

    /**
     * Construct a new Builder.
     *
     * @param type the type to base the builder
     */
    public DGT_Builder(GraphType type)
    {
        this.directed = type.isDirected() || type.isMixed();
        this.undirected = type.isUndirected() || type.isMixed();
        this.allowSelfLoops = type.isAllowingSelfLoops();
        this.allowMultipleEdges = type.isAllowingMultipleEdges();
        this.weighted = type.isWeighted();
        this.allowCycles = type.isAllowingCycles();
        this.modifiable = type.isModifiable();
    }

    /**
     * Construct a new Builder.
     *
     * @param directed whether the graph contains directed edges
     * @param undirected whether the graph contains undirected edges
     */
    public DGT_Builder(boolean directed, boolean undirected)
    {
        if (!directed && !undirected) {
            throw new IllegalArgumentException(
                    "At least one of directed or undirected must be true");
        }
        this.directed = directed;
        this.undirected = undirected;
        this.allowSelfLoops = true;
        this.allowMultipleEdges = true;
        this.weighted = false;
        this.allowCycles = true;
        this.modifiable = true;
    }

    /**
     * Set the type as directed.
     *
     * @return the builder
     */
    public DGT_Builder directed()
    {
        this.directed = true;
        this.undirected = false;
        return this;
    }

    /**
     * Set the type as undirected.
     *
     * @return the builder
     */
    public DGT_Builder undirected()
    {
        this.directed = false;
        this.undirected = true;
        return this;
    }

    /**
     * Set the type as mixed.
     *
     * @return the builder
     */
    public DGT_Builder mixed()
    {
        this.directed = true;
        this.undirected = true;
        return this;
    }

    /**
     * Set whether to allow self-loops.
     *
     * @param value if true self-values are allowed, otherwise not
     * @return the builder
     */
    public DGT_Builder allowSelfLoops(boolean value)
    {
        this.allowSelfLoops = value;
        return this;
    }

    /**
     * Set whether to allow multiple edges.
     *
     * @param value if true multiple edges are allowed, otherwise not
     * @return the builder
     */
    public DGT_Builder allowMultipleEdges(boolean value)
    {
        this.allowMultipleEdges = value;
        return this;
    }

    /**
     * Set whether the graph will be weighted.
     *
     * @param value if true the graph will be weighted, otherwise unweighted
     * @return the builder
     */
    public DGT_Builder weighted(boolean value)
    {
        this.weighted = value;
        return this;
    }

    /**
     * Set whether the graph will allow cycles.
     *
     * @param value if true the graph will allow cycles, otherwise not
     * @return the builder
     */
    public DGT_Builder allowCycles(boolean value)
    {
        this.allowCycles = value;
        return this;
    }

    /**
     * Set whether the graph is modifiable.
     *
     * @param value if true the graph will be modifiable, otherwise not
     * @return the builder
     */
    public DGT_Builder modifiable(boolean value)
    {
        this.modifiable = value;
        return this;
    }

    /**
     * Build the type.
     *
     * @return the type
     */
    public DefaultGraphType build()
    {
        return new DefaultGraphType(
                directed, undirected, allowSelfLoops, allowMultipleEdges, weighted, allowCycles,
                modifiable);
    }

}
