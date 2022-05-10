package org.jgrapht.graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public abstract class MaskSubgraphRefactor<V, E> extends AbstractGraph<V, E> {
    protected final Graph<V, E> base;
    protected final GraphType baseType;
    protected final Set<E> edges;
    protected final Predicate<V> vertexMask;
    protected final Predicate<E> edgeMask;

    public MaskSubgraphRefactor(Graph<V, E> base, Predicate<V> vertexMask, Predicate<E> edgeMask) {
        super();
        this.base = Objects.requireNonNull(base, "Invalid graph provided");
        this.baseType = base.getType();
        this.edges = new MaskEdgeSet<>(base, base.edgeSet(), vertexMask, edgeMask);
        this.vertexMask = Objects.requireNonNull(vertexMask, "Invalid vertex mask provided");
        this.edgeMask = Objects.requireNonNull(edgeMask, "Invalid edge mask provided");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet() {
        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex) {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.edgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * By default this method returns the sum of in-degree and out-degree. The exact value returned
     * depends on the type of the underlying graph.
     */
    @Override
    public int degreeOf(V vertex) {
        if (baseType.isDirected()) {
            return inDegreeOf(vertex) + outDegreeOf(vertex);
        } else {
            int degree = 0;
            Iterator<E> it = edgesOf(vertex).iterator();
            while (it.hasNext()) {
                E e = it.next();
                degree++;
                if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                    degree++;
                }
            }
            return degree;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex) {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.incomingEdgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex) {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return incomingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex) {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.outgoingEdgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex) {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E edge) {
        assert (edgeSet().contains(edge));

        return base.getEdgeSource(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E edge) {
        assert (edgeSet().contains(edge));

        return base.getEdgeTarget(edge);
    }
}
