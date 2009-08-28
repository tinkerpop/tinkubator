package org.linkedprocess;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Jid implements Comparable {

    public String jid;
    private static final String FORWARD_SLASH = "/";

    public Jid(String jid) {
        this.jid = jid.trim();
    }

    public String getResource() {
        return this.jid.substring(this.jid.indexOf(FORWARD_SLASH) + 1);
    }

    public Jid getBareJid() {
        if(this.jid.contains(FORWARD_SLASH))
            return new Jid(this.jid.substring(0, this.jid.indexOf(FORWARD_SLASH)));
        else
            return new Jid(this.jid);
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public boolean isBareJid() {
        return !this.jid.contains(FORWARD_SLASH);
    }

    public String toString() {
        return this.jid;
    }

    public static String generateRandomResourceId() {
        return LinkedProcess.generateRandomPassword();
    }

    public int compareTo(Object jid) {
        if(jid instanceof Jid) {
            return jid.toString().compareTo(this.jid);
        } else {
            throw new ClassCastException();
        }
    }

    public boolean equals(Object jid) {
        if(jid instanceof Jid) {
            return jid.toString().equals(this.jid);   
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.jid.hashCode();
    }
}
