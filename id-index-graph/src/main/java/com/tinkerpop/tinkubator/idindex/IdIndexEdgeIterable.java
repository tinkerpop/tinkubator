package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdIndexEdgeIterable implements Iterable<Edge> {
    private final Iterable<Edge> base;

    public IdIndexEdgeIterable(Iterable<Edge> base) {
        this.base = base;
    }

    public Iterator<Edge> iterator() {
        final Iterator<Edge> baseIter = base.iterator();

        return new Iterator<Edge>() {
            public boolean hasNext() {
                return baseIter.hasNext();
            }

            public Edge next() {
                return new IdIndexEdge(baseIter.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
