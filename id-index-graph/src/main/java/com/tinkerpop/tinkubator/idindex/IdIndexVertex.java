package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdIndexVertex extends IdIndexElement implements Vertex {
    public IdIndexVertex(Vertex base) {
        super(base);
    }

    public Vertex getBase() {
        return (Vertex) base;
    }

    public Iterable<Edge> getOutEdges(String... strings) {
        return new IdIndexEdgeIterable(((Vertex) base).getOutEdges(strings));
    }

    public Iterable<Edge> getInEdges(String... strings) {
        return new IdIndexEdgeIterable(((Vertex) base).getInEdges(strings));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdIndexVertex
                && ((Vertex) other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 274703 + getId().hashCode();
    }
}
