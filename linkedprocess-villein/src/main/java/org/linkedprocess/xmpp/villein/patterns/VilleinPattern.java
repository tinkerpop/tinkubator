package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.XmppVillein;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 4:56:28 PM
 */
public class VilleinPattern {

    protected XmppVillein xmppVillein;

    public VilleinPattern(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public XmppVillein getXmppVillein() {
        return this.xmppVillein;
    }
}
