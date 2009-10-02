/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import junit.framework.TestCase;

import java.util.HashSet;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class JidTest extends TestCase {

    public void testBareJid() {
        Jid jid1 = new Jid("linked.process.1@xmpp.linkedprocess.org/LoPFarm/123456");
        Jid jid2 = new Jid("linked.process.1@xmpp.linkedprocess.org");
        assertFalse(jid1.isBareJid());
        assertTrue(jid2.isBareJid());
        assertEquals(jid1.getBareJid(), jid2);
        assertEquals(jid2, jid2.getBareJid());
        assertFalse(jid1.equals(jid1.getBareJid()));

    }

    public void testJidInSet() {
        HashSet<Jid> set = new HashSet<Jid>();
        set.add(new Jid("linked.process.1@xmpp.linkedprocess.org"));
        set.add(new Jid("linked.process.1@xmpp.linkedprocess.org"));
        set.add(new Jid("linked.process.1@xmpp.linkedprocess.org"));
        assertEquals(set.size(), 1);
    }

    public void testGenerateResource() {
        Jid jid1 = new Jid("lop1@xmpp.linkedprocess.org/test/");
        assertEquals(jid1.getResource(), "test/");
        jid1 = new Jid("lop1@xmpp.linkedprocess.org");
        assertNull(jid1.getResource());
        jid1 = new Jid("lop1@xmpp.linkedprocess.org/1234/ABCD");
        assertEquals("1234/ABCD", jid1.getResource());
    }
}
