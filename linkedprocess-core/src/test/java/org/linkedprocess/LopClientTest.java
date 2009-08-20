/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LopClientTest extends TestCase {

    public void testRandomIDGenerator() {
        // 1,000,000 random IDs are generated without conflict.
        Set<String> uniques = new HashSet<String>();
        for (int i = 0; i < 100000; i++) {
            //System.out.println(XmppClient.generateRandomResourceId());
            uniques.add(LinkedProcess.generateRandomResourceId());
        }
        assertEquals(uniques.size(), 100000);
    }

    public void testBoolean() {
        assertTrue(Boolean.valueOf("true"));
        assertFalse(Boolean.valueOf("false"));
    }
}
