package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdIndexEdge extends IdIndexElement implements Edge {
    private IdIndexVertex inVertex;
    private IdIndexVertex outVertex;

    public IdIndexEdge(Edge base) {
        super(base);
    }

    public Edge getBase() {
        return (Edge) base;
    }

    public Vertex getOutVertex() {
        if (null == outVertex) {
            outVertex = new IdIndexVertex(((Edge) base).getOutVertex());
        }

        return outVertex;
    }

    public Vertex getInVertex() {
        if (null == inVertex) {
            inVertex = new IdIndexVertex(((Edge) base).getInVertex());
        }

        return inVertex;
    }

    public String getLabel() {
        return ((Edge) base).getLabel();
    }
}
