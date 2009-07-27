package org.linkedprocess.security;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: josh
 * Date: Jul 9, 2009
 * Time: 3:19:15 PM
 */
public class PathPermissions {
    private static final Boolean
            PERMIT = true,
            DENY = false;

    private final Node<Boolean> rootNode;

    public PathPermissions() {
        rootNode = new Node<Boolean>("", DENY);
    }

    public void addPermitRule(final String path) {
        rootNode.addChild(path, PERMIT);
    }

    public void addDenyRule(final String path) {
        rootNode.addChild(path, DENY);

        /*PathPermissions p = new PathPermissions();
        p.addPermitRule("/tmp/somedir/somefile");
        p.addPermitRule("/opt/someotherdir");
        ((VMSecurityManager) System.getSecurityManager()).setReadPermissions(p);*/
    }

    public boolean isPermitted(final String path) {
        Boolean b = rootNode.findTarget(path);

        // Note: this check for null is not really necessary...
        return null == b ? false : b;
    }

    // TODO: interleave positive and negative rules
    public List<String> getPositiveRules() {
        return rootNode.getRulesForTarget(PERMIT);
    }

    private class Node<T> implements Comparable<Node<T>> {
        private final String prefix;
        private final T target;
        private final Collection<Node<T>> children;

        public Node(final String prefix,
                    final T target) {
            this.prefix = prefix;
            this.target = target;
            children = new LinkedList<Node<T>>();
        }

        public T findTarget(final String s) {
            if (s.startsWith(prefix)) {
                String suffix = s.substring(prefix.length());

                // Children are tested first.
                for (Node<T> child : children) {
                    T t = child.findTarget(suffix);
                    if (null != t) {
                        return t;
                    }
                }

                // If no child applies, the node's own target is used.
                return target;
            } else {
                // Node does not apply.
                return null;
            }
        }

        public void addChild(final String suffix,
                             final T target) {
            removeSupersededChildren(suffix);

            for (Node<T> child : children) {
                // If the new child modifies (but does not supersede) an existing child...
                if (suffix.startsWith(child.prefix)) {
                    // New suffix will be non-empty.
                    String newSuffix = suffix.substring(child.prefix.length());

                    child.addChild(newSuffix, target);

                    // Child will not modify more than one existing child.
                    return;
                }
            }

            // If no children are modified, simply add a new child.
            children.add(new Node<T>(suffix, target));
        }

        private void removeSupersededChildren(final String prefix) {
            // Note: it is possible for more than one existing child to be superseded by a new child.
            Collection<Node<T>> superseded = new LinkedList<Node<T>>();

            for (Node<T> child : children) {
                if (child.prefix.startsWith(prefix)) {
                    superseded.add(child);
                }
            }

            for (Node<T> child : superseded) {
                children.remove(child);
            }
        }

        public int compareTo(final Node<T> other) {
            return this.prefix.compareTo(other.prefix);
        }

        public List<String> getRulesForTarget(final T t) {
            List<String> results = new LinkedList<String>();

            if (target == t) {
                results.add(prefix);
            }

            for (Node<T> n : children) {
                for (String suffix : n.getRulesForTarget(t)) {
                    results.add(prefix + suffix);
                }
            }

            return results;
        }
    }
}
