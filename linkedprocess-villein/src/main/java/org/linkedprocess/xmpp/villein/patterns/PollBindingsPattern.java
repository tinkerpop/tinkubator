package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.xmpp.villein.XmppVillein;

/**
 * User: marko
 * Date: Aug 5, 2009
 * Time: 12:49:24 AM
 */
public class PollBindingsPattern extends VilleinPattern {

    public PollBindingsPattern(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    // TODO: create a thread that continues to poll a VM binding until that VM binding is at a desired state. When the state is reached, execute a method.
}
