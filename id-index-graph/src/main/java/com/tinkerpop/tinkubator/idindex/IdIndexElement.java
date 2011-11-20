package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
abstract class IdIndexElement implements Element {
    protected final Element base;

    public IdIndexElement(Element base) {
        this.base = base;
    }

    public Object getProperty(String s) {
        if (s.equals(IdIndexGraph.ID)) {
            return null;
        }

        return base.getProperty(s);
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = base.getPropertyKeys();

        // TODO: this will fail if the returned collection is immutable
        keys.remove(IdIndexGraph.ID);

        return keys;
    }

    public void setProperty(String s, Object o) {
        if (s.equals(IdIndexGraph.ID)) {
            throw new IllegalArgumentException("can't set value for reserved property '" + IdIndexGraph.ID + "'");
        }

        base.setProperty(s, o);
    }

    public Object removeProperty(String s) {
        if (s.equals(IdIndexGraph.ID)) {
            throw new IllegalArgumentException("can't remove value for reserved property '" + IdIndexGraph.ID + "'");
        }

        return base.removeProperty(s);
    }

    public Object getId() {
        return base.getProperty(IdIndexGraph.ID);
    }
}
