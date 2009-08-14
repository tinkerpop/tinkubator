/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.CountrysideProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmHolder;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.LopCloud;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The ResourceAllocationPattern is useful for allocating resources in an LoP cloud.
 * This pattern simply wraps an LoP cloud with useful methods.
 *
 * User: marko
 * Date: Aug 14, 2009
 * Time: 11:34:19 AM
 */
public class ResourceAllocationPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(ResourceAllocationPattern.class);

    private static void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if (timeout > 0 && (System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    public static Set<FarmProxy> allocateFarms(final FarmHolder farmHolder, final int numberOfFarms, final long timeout) throws TimeoutException {
        Set<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            if (farmHolder.getFarmProxies().size() >= numberOfFarms) {
                int i = 0;
                for(FarmProxy farmProxy : farmHolder.getFarmProxies()){
                    farmProxies.add(farmProxy);
                    i++;
                    if(i == numberOfFarms)
                        return farmProxies;
                }
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    public static CountrysideProxy allocateCountryside(final LopCloud lopCloud, final String countrysideJid, final long timeout) throws TimeoutException {
        CountrysideProxy countrysideProxy = null;
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            countrysideProxy = lopCloud.getCountrysideProxy(countrysideJid);
            if(null != countrysideProxy)
                return countrysideProxy;
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

}
