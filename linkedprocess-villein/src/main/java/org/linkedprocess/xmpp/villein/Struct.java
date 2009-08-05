package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Presence;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 10:55:56 PM
 */
public class Struct implements Comparable {

    protected Presence presence;
    protected String fullJid;

    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public Presence getPresence() {
        return this.presence;
    }

    public void setFullJid(String fullJid) {
        this.fullJid = fullJid;
    }

    public String getFullJid() {
        return this.fullJid;
    }

    public int compareTo(Object struct) {
        if (struct instanceof Struct) {
            return this.fullJid.compareTo(((Struct) struct).getFullJid());
        } else {
            throw new ClassCastException();
        }
    }
}
