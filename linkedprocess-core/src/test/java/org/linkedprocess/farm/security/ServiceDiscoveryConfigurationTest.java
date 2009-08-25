/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm.security;

import junit.framework.TestCase;
import org.jdom.output.XMLOutputter;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.security.PathPermissions;
import org.linkedprocess.farm.security.ServiceDiscoveryConfiguration;
import org.linkedprocess.farm.security.VmSecurityManager;

import java.util.Properties;

/**
 * Author: josh
 * Date: Jul 16, 2009
 * Time: 4:55:42 PM
 */
public class ServiceDiscoveryConfigurationTest extends TestCase {

    public void testAll() throws Exception {
        Properties p = LinkedProcess.getConfiguration();
        p.setProperty("org.linkedprocess.security.read", "true");

        VmSecurityManager m = new VmSecurityManager(p);
        PathPermissions pp = new PathPermissions();
        pp.addPermitRule("/tmp/foo/bar");
        pp.addPermitRule("/opt/stuff");
        pp.addPermitRule("/opt/");
        m.setReadPermissions(pp);

        ServiceDiscoveryConfiguration c = new ServiceDiscoveryConfiguration(m);

        XMLOutputter op = new XMLOutputter();
        op.output(c.toElement(), System.out);
    }
}
