package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class MultiVertex extends MultiElement implements Vertex {
    private final Collection<Vertex> bases;

    public MultiVertex(final MultiGraph graph,
                       final Object id,
                       final Collection<Vertex> bases) {
        super(graph, id);
        this.bases = bases;
    }

    public Iterable<Edge> getOutEdges(String... labels) {
        // TODO: the hashmap is time-efficient but not scalable
        Map<Object, Edge> results = new HashMap<Object, Edge>();

        for (Vertex v : bases) {
            for (Edge e : v.getOutEdges(labels)) {
                Object id = e.getId();

                if (null == results.get(id)) {
                    results.put(id, graph.getEdge(id));
                }
            }
        }

        return results.values();
    }

    public Iterable<Edge> getInEdges(String... labels) {
        // TODO: the hashmap is time-efficient but not scalable
        Map<Object, Edge> results = new HashMap<Object, Edge>();

        for (Vertex v : bases) {
            for (Edge e : v.getInEdges(labels)) {
                Object id = e.getId();

                if (null == results.get(id)) {
                    results.put(id, graph.getEdge(id));
                }
            }
        }

        return results.values();
    }

    protected Collection<Element> getBases() {
        return (Collection<Element>) (Collection) bases;
    }

}
