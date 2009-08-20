/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import junit.framework.TestCase;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LinkedProcessTest extends TestCase {

    public void testIsBareJid() {
        assertTrue(LinkedProcess.isBareJid("linked.process.1@xmpp.linkedprocess.org"));
    }

    public void testGenerateBareJid() {
        assertEquals(LinkedProcess.generateBareJid("linked.process.1@xmpp.linkedprocess.org/LoPFarm/123456"),
                "linked.process.1@xmpp.linkedprocess.org");
    }

    public void testGenerateResource() {
        assertEquals(LinkedProcess.generateResource("lop1@xmpp.linkedprocess.org/test/"), "test/");
    }
}
