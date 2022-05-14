package org.jgrapht.graph;

import org.jgrapht.event.GraphEdgeChangeEvent;

class FlyweightEdgeEvent<VV, EE>
extends
GraphEdgeChangeEvent<VV, EE>
{
private static final long serialVersionUID = 3907207152526636089L;

/**
 * @see GraphEdgeChangeEvent
 */
public FlyweightEdgeEvent(Object eventSource, int type, EE e)
{
    super(eventSource, type, e, null, null);
}

/**
 * Sets the edge of this event.
 *
 * @param e the edge to be set.
 */
protected void setEdge(EE e)
{
    this.edge = e;
}

protected void setEdgeSource(VV v)
{
    this.edgeSource = v;
}

protected void setEdgeTarget(VV v)
{
    this.edgeTarget = v;
}

protected void setEdgeWeight(double weight)
{
    this.edgeWeight = weight;
}

/**
 * Set the event type of this event.
 *
 * @param type the type to be set.
 */
protected void setType(int type)
{
    this.type = type;
}
}

