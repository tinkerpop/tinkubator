package com.tinkerpop.blueprints.pgm.impls.multi;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class MultiIterable<T> implements Iterable<T> {
    private final Collection<Iterable<T>> bases;

    public MultiIterable(Collection<Iterable<T>> bases) {
        this.bases = bases;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<Iterable<T>> iter = bases.iterator();
            private Iterator<T> cur;

            public boolean hasNext() {
                if (null != cur && cur.hasNext()) {
                    return true;
                }

                while (true) {
                    if (iter.hasNext()) {
                        cur = iter.next().iterator();

                        if (cur.hasNext()) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            }

            public T next() {
                // Note: requires hasNext() to have been called
                return cur.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
