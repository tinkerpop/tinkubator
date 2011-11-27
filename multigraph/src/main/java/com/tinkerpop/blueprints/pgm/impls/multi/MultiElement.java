package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
abstract class MultiElement implements Element {
    protected final Object id;
    protected final MultiGraph graph;

    public MultiElement(final MultiGraph graph,
                        final Object id) {
        this.id = id;
        this.graph = graph;
    }

    protected abstract Collection<Element> getBases();

    public Object getId() {
        return id;
    }

    public void setProperty(String key, Object value) {
        throw new UnsupportedOperationException(MultiGraph.READONLY_MSG);
    }

    public Object removeProperty(String key) {
        throw new UnsupportedOperationException(MultiGraph.READONLY_MSG);
    }

    public Object getProperty(String key) {
        for (Element e : getBases()) {
            Object o = e.getProperty(key);
            if (null != o) {
                return o;
            }
        }

        return null;
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<String>();

        for (Element e : getBases()) {
            keys.addAll(e.getPropertyKeys());
        }

        return keys;
    }
}
