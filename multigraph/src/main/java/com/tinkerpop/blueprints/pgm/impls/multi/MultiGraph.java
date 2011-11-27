package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MultiGraph implements Graph {
    static final String READONLY_MSG = "MultiGraph is read-only";

    private final Graph[] bases;

    public MultiGraph(Graph... bases) {
        this.bases = bases;
    }

    public Vertex addVertex(Object id) {
        throw new UnsupportedOperationException(READONLY_MSG);
    }

    public Vertex getVertex(Object id) {
        Collection<Vertex> baseVertices = new LinkedList<Vertex>();

        // TODO: allow bases to be refreshed
        for (Graph g : bases) {
            Vertex v = g.getVertex(id);
            if (null != v) {
                baseVertices.add(v);
            }
        }

        if (baseVertices.size() > 0) {
            return new MultiVertex(id, baseVertices);
        } else {
            return null;
        }
    }

    public void removeVertex(Vertex vertex) {
        throw new UnsupportedOperationException(READONLY_MSG);
    }

    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        throw new UnsupportedOperationException(READONLY_MSG);
    }

    public Edge getEdge(Object id) {
        Collection<Edge> baseEdges = new LinkedList<Edge>();

        // TODO: allow bases to be refreshed
        for (Graph g : bases) {
            Edge e = g.getEdge(id);
            if (null != e) {
                baseEdges.add(e);
            }
        }

        if (baseEdges.size() > 0) {
            return new MultiEdge(id, baseEdges);
        } else {
            return null;
        }
    }

    public void removeEdge(Edge edge) {
        throw new UnsupportedOperationException(READONLY_MSG);
    }

    public Iterable<Vertex> getVertices() {
        Collection<Iterable<Vertex>> base = new LinkedList<Iterable<Vertex>>();

        for (int pos = 0; pos < bases.length; pos++) {
            base.add(new MultiVertexIterable(pos));
        }

        return new MultiIterable<Vertex>(base);
    }

    public Iterable<Edge> getEdges() {
        Collection<Iterable<Edge>> base = new LinkedList<Iterable<Edge>>();

        for (int pos = 0; pos < bases.length; pos++) {
            base.add(new MultiEdgeIterable(pos));
        }

        return new MultiIterable<Edge>(base);
    }

    public void clear() {
        throw new UnsupportedOperationException(READONLY_MSG);
    }

    public void shutdown() {
        for (Graph g : bases) {
            g.shutdown();
        }
    }

    private class MultiVertexIterable implements Iterable<Vertex> {
        private final int pos;

        public MultiVertexIterable(int pos) {
            this.pos = pos;
        }

        public Iterator<Vertex> iterator() {
            return new Iterator<Vertex>() {
                private Iterator<Vertex> iter = bases[pos].getVertices().iterator();
                private Vertex next;

                public boolean hasNext() {
                    while (null == next) {
                        if (iter.hasNext()) {
                            Object id = iter.next().getId();

                            boolean repeat = false;
                            for (int i = 0; i < pos; i++) {
                                if (null != bases[i].getVertex(id)) {
                                    repeat = true;
                                    break;
                                }
                            }

                            if (!repeat) {
                                next = getVertex(id);
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }

                    return true;
                }

                public Vertex next() {
                    // Note: requires hasNext to have been called
                    Vertex v = next;
                    next = null;
                    return v;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private class MultiEdgeIterable implements Iterable<Edge> {
        private final int pos;

        public MultiEdgeIterable(int pos) {
            this.pos = pos;
        }

        public Iterator<Edge> iterator() {
            return new Iterator<Edge>() {
                private Iterator<Edge> iter = bases[pos].getEdges().iterator();
                private Edge next;

                public boolean hasNext() {
                    while (null == next) {
                        if (iter.hasNext()) {
                            Object id = iter.next().getId();

                            boolean repeat = false;
                            for (int i = 0; i < pos; i++) {
                                if (null != bases[i].getEdge(id)) {
                                    repeat = true;
                                    break;
                                }
                            }

                            if (!repeat) {
                                next = getEdge(id);
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }

                    return true;
                }

                public Edge next() {
                    // Note: requires hasNext to have been called
                    Edge v = next;
                    next = null;
                    return v;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
