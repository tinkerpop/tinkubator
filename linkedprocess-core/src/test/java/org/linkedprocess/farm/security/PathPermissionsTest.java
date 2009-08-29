/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.security;

import junit.framework.TestCase;

/**
 * Author: josh
 * Date: Jul 9, 2009
 * Time: 3:31:20 PM
 */
public class PathPermissionsTest extends TestCase {

    public void testDefaultToDeny() {
        PathPermissions p = new PathPermissions();

        p.addPermitRule("a");
        assertFalse(p.isPermitted("b"));
    }

    public void testInheritance() {
        PathPermissions p = new PathPermissions();

        p.addPermitRule("a");
        assertTrue(p.isPermitted("aa"));
    }

    public void testEmptyRoot() {
        PathPermissions p = new PathPermissions();

        p.addPermitRule("");
        assertTrue(p.isPermitted("a"));

        p.addDenyRule("");
        assertFalse(p.isPermitted("a"));
    }

    public void testNewRulesOverride() {
        PathPermissions p = new PathPermissions();

        p.addPermitRule("aa");
        assertTrue(p.isPermitted("aa"));
        assertTrue(p.isPermitted("aabc"));
        assertFalse(p.isPermitted("a"));

        p.addDenyRule("aab");
        assertTrue(p.isPermitted("aa"));
        assertFalse(p.isPermitted("aabc"));
    }

    public void testNewRulesOverrideChildren() {
        PathPermissions p = new PathPermissions();

        p.addPermitRule("aa");
        assertTrue(p.isPermitted("aacd"));
        p.addDenyRule("aac");
        assertFalse(p.isPermitted("aacd"));

        p.addPermitRule("aa");
        assertTrue(p.isPermitted("aacd"));
    }
}
