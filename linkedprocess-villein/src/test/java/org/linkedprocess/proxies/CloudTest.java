/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.proxies;

import junit.framework.TestCase;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.proxies.Cloud;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.ParentProxyNotFoundException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class CloudTest extends TestCase {

    public void testCountrysideManipulations() {
        Cloud cloud = new Cloud();
        FarmProxy noParentFarmProxy = new FarmProxy("lonely@test/1234", null);
        try {
            cloud.addFarmProxy(noParentFarmProxy);
            assertTrue(false);
        } catch(ParentProxyNotFoundException e) {
            assertTrue(true);
        }

        CountrysideProxy countrysideProxy = new CountrysideProxy("test@test");
        cloud.addCountrysideProxy(countrysideProxy);
        for (int i = 0; i < 10; i++) {
            FarmProxy farmProxy = new FarmProxy("test@test/" + LinkedProcess.generateRandomResourceId(), null);
            cloud.addFarmProxy(farmProxy);
            assertEquals(countrysideProxy, cloud.getParentProxy(farmProxy.getJid()));
        }
        assertEquals(cloud.getFarmProxies().size(), 10);
    }

}
