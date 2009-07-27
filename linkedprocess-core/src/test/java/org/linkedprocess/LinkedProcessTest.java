package org.linkedprocess;

import junit.framework.TestCase;

/**
 * User: marko
 * Date: Jul 11, 2009
 * Time: 4:23:37 PM
 */
public class LinkedProcessTest extends TestCase {

    public void testIsBareJid() {
        assertTrue(LinkedProcess.isBareJid("linked.process.1@xmpp.linkedprocess.org"));
    }

    public void testGenerateBareJid() {
        assertEquals(LinkedProcess.generateBareJid("linked.process.1@xmpp.linkedprocess.org/LoPFarm/123456"),
                "linked.process.1@xmpp.linkedprocess.org");
    }
}
