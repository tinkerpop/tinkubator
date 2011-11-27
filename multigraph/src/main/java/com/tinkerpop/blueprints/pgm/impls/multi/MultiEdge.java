package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Collection;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class MultiEdge extends MultiElement implements Edge {
    private final Collection<Edge> bases;

    public MultiEdge(final MultiGraph graph,
                     final Object id,
                     final Collection<Edge> bases) {
        super(graph, id);
        this.bases = bases;
    }

    protected Collection<Element> getBases() {
        return (Collection<Element>) (Collection) bases;
    }

    public Vertex getOutVertex() {
        return graph.getVertex(bases.iterator().next().getOutVertex().getId());
    }

    public Vertex getInVertex() {
        return graph.getVertex(bases.iterator().next().getInVertex().getId());
    }

    public String getLabel() {
        return bases.iterator().next().getLabel();
    }
}