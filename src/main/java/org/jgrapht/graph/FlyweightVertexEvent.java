package org.jgrapht.graph;

import org.jgrapht.event.GraphVertexChangeEvent;

class FlyweightVertexEvent<VV>
extends
GraphVertexChangeEvent<VV>
{
private static final long serialVersionUID = 3257848787857585716L;

/**
 * @see GraphVertexChangeEvent#GraphVertexChangeEvent(Object, int, Object)
 */
public FlyweightVertexEvent(Object eventSource, int type, VV vertex)
{
    super(eventSource, type, vertex);
}

/**
 * Set the event type of this event.
 *
 * @param type type to be set.
 */
protected void setType(int type)
{
    this.type = type;
}

/**
 * Sets the vertex of this event.
 *
 * @param vertex the vertex to be set.
 */
protected void setVertex(VV vertex)
{
    this.vertex = vertex;
}
}
