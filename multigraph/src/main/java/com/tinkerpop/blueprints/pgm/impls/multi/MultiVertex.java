package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class MultiVertex extends MultiElement implements Vertex {
    private final Collection<Vertex> bases;

    public MultiVertex(final Object id,
                       final Collection<Vertex> bases) {
        super(id);
        this.bases = bases;
    }

    public Iterable<Edge> getOutEdges(String... labels) {
        Collection<Iterable<Edge>> bases = new LinkedList<Iterable<Edge>>();

        for (Vertex v : this.bases) {
            bases.add(v.getOutEdges(labels));
        }

        return new MultiIterable<Edge>(bases);
    }

    public Iterable<Edge> getInEdges(String... labels) {
        Collection<Iterable<Edge>> bases = new LinkedList<Iterable<Edge>>();

        for (Vertex v : this.bases) {
            bases.add(v.getInEdges(labels));
        }

        return new MultiIterable<Edge>(bases);
    }

    protected Collection<Element> getBases() {
        return (Collection<Element>) (Collection) bases;
    }

}
