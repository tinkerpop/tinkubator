/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.proxies;

import junit.framework.TestCase;
import org.linkedprocess.Jid;
import org.linkedprocess.villein.proxies.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class CloudProxyTest extends TestCase {

    public void testCountrysideManipulations() {
        CloudProxy cloudProxy = new CloudProxy();
        FarmProxy noParentFarmProxy = new FarmProxy(new Jid("lonely@test/1234"), null);
        try {
            cloudProxy.addFarmProxy(noParentFarmProxy);
            assertTrue(false);
        } catch (ParentProxyNotFoundException e) {
            assertTrue(true);
        }

        CountrysideProxy countrysideProxy = new CountrysideProxy(new Jid("test@test"));
        cloudProxy.addCountrysideProxy(countrysideProxy);
        for (int i = 0; i < 100; i++) {
            FarmProxy farmProxy = new FarmProxy(new Jid("test@test/" + Jid.generateRandomResourceId()), null);
            cloudProxy.addFarmProxy(farmProxy);
            assertEquals(countrysideProxy, cloudProxy.getCountrysideProxy(farmProxy.getJid().getBareJid()));
        }
        assertEquals(cloudProxy.getFarmProxies().size(), 100);
    }

    public void testCountrysideHashCode() {
        CloudProxy cloudProxy = new CloudProxy();
        for(int i=0; i<1000; i++) {
            CountrysideProxy countrysideProxy = new CountrysideProxy(new Jid("test@test"));
            cloudProxy.addCountrysideProxy(countrysideProxy);
        }
        assertEquals(cloudProxy.getCountrysideProxies().size(), 1);
    }

    public void testFarmHashCode() {
        CloudProxy cloudProxy = new CloudProxy();
        cloudProxy.addCountrysideProxy(new CountrysideProxy(new Jid("test@test")));
        for(int i=0; i<1000; i++) {
            FarmProxy farmProxy = new FarmProxy(new Jid("test@test/1234"), null, null);
            cloudProxy.addFarmProxy(farmProxy);
        }
        assertEquals(cloudProxy.getFarmProxies().size(), 1);
    }

    public void testRegistryHashCode() {
        CloudProxy cloudProxy = new CloudProxy();
        cloudProxy.addCountrysideProxy(new CountrysideProxy(new Jid("test@test")));
        for(int i=0; i<1000; i++) {
            RegistryProxy registryProxy = new RegistryProxy(new Jid("test@test/1234"), null, null, null);
            cloudProxy.addRegistryProxy(registryProxy);
        }
        assertEquals(cloudProxy.getRegistryProxies().size(), 1);
    }

}
