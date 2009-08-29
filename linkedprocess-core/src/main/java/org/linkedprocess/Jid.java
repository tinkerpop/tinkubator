package org.linkedprocess;

import java.util.Random;

/**
 * The class to represent a Jabber identifier (JID). In essence, this class wraps a String with JID-based functionality.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Jid implements Comparable {

    private String jid;
    private static final String FORWARD_SLASH = "/";

    /**
     * Construct a jid given a String verion of a jid.
     *
     * @param jid a bare or fully-qualified jid.
     */
    public Jid(final String jid) {
        this.jid = jid.trim();
    }

    /**
     * Get the resource component of this jid.
     * For example, given marko@linkedprocess.org/1234, 1234 is the resource.
     *
     * @return the resource component of the jid (null if no resource exists)
     */
    public String getResource() {
        if (this.jid.contains(FORWARD_SLASH))
            return this.jid.substring(this.jid.indexOf(FORWARD_SLASH) + 1);
        else
            return null;
    }

    /**
     * Get the bare jid component of this jid.
     * For example, given marko@linkedprocess.org/1234, marko@linkedprocess is the bare jid.
     *
     * @return the bare jid component of this jid
     */
    public Jid getBareJid() {
        if (this.jid.contains(FORWARD_SLASH))
            return new Jid(this.jid.substring(0, this.jid.indexOf(FORWARD_SLASH)));
        else
            return new Jid(this.jid);
    }

    /**
     * Determines if the jid is a bare jid.
     * For example, marko@linkedprocess.org is a bare jid, but marko@linkedprocess.org/1234 is not a bare jid.
     *
     * @return true if the jid is a bare jid
     */
    public boolean isBareJid() {
        return !this.jid.contains(FORWARD_SLASH);
    }

    /**
     * Get the String representation of this jid.
     *
     * @return the String representation of this jid
     */
    public String toString() {
        return this.jid;
    }

    /**
     * Generate a random 8-character hexidecimal identifier. This is useful for creating unique resource components.
     *
     * @return a random 8-character hexidecimal identifier
     */
    public static String generateRandomResourceId() {
        // e.g. 6D56433B
        Random random = new Random();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int x = random.nextInt(36);
            if (x < 10)
                b.append(x);
            else
                b.append(((char) (x + 55)));

        }
        return b.toString();
    }

    /**
     * Returns  the compare method of the String representation of the two jids being compared.
     *
     * @param jid a jid to compare to this jid
     * @return the comparison of the two jids
     */
    public int compareTo(Object jid) {
        if (jid instanceof Jid) {
            return jid.toString().compareTo(this.jid);
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Returns the equals method of the String representation of the two jids being compared.
     *
     * @param jid a jid to test for equality against this jid
     * @return whehter the two jids are equal
     */
    public boolean equals(Object jid) {
        return jid instanceof Jid && jid.toString().equals(this.jid);
    }

    /**
     * Returns the hash code of the String representation of this jid.
     *
     * @return the hash code of the String representation of this jid
     */
    public int hashCode() {
        return this.jid.hashCode();
    }
}
