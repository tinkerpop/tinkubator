package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdIndexVertexIterable implements Iterable<Vertex> {
    private final Iterable<Vertex> base;

    public IdIndexVertexIterable(Iterable<Vertex> base) {
        this.base = base;
    }

    public Iterator<Vertex> iterator() {
        final Iterator<Vertex> baseIter = base.iterator();

        return new Iterator<Vertex>() {
            public boolean hasNext() {
                return baseIter.hasNext();
            }

            public Vertex next() {
                return new IdIndexVertex(baseIter.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
