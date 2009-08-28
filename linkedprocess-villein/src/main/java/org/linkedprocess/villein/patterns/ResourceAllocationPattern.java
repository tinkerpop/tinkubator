/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.Jid;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.CloudProxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The ResourceAllocationPattern is useful for allocating resources in an LoP cloud.
 * This pattern simply wraps an LoP cloud with useful methods.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class ResourceAllocationPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(ResourceAllocationPattern.class);

    private static void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if (timeout > 0 && (System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    public static Set<FarmProxy> allocateFarms(final CloudProxy cloudProxy, final int numberOfFarms, final long timeout) throws TimeoutException {
        Set<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            if (cloudProxy.getFarmProxies().size() >= numberOfFarms) {
                int i = 0;
                for (FarmProxy farmProxy : cloudProxy.getFarmProxies()) {
                    farmProxies.add(farmProxy);
                    i++;
                    if (i == numberOfFarms)
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

    public static CountrysideProxy allocateCountryside(final CloudProxy cloudProxy, final Jid countrysideJid, final long timeout) throws TimeoutException {
        CountrysideProxy countrysideProxy = null;
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            countrysideProxy = cloudProxy.getCountrysideProxy(countrysideJid);
            if (null != countrysideProxy)
                return countrysideProxy;
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * Filters a collection of farm proxies down to only those that support a particular virtual machine species.
     *
     * @param farmProxies the collection of farm proxies to filter
     * @param vmSpecies   the virtual machine species that must be supported
     * @return a filtered farm proxy set (this is a new set as the original collection was not altered)
     */
    public static Set<FarmProxy> filterFarmProxiesByVmSpeciesSupport(Collection<FarmProxy> farmProxies, String vmSpecies) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.supportsSpecies(vmSpecies))
                returnFarmProxies.add(farmProxy);
        }
        return returnFarmProxies;
    }

    /**
     * Filters a collection of farm proxies down to only those that have at least the mimimum virtual machine time to live.
     *
     * @param farmProxies         the collection of farm proxies to filter
     * @param minimumVmTimeToLive the minimum time to live in milliseconds
     * @return a filtered farm proxy set (this is a new set as the original collection was not altered)
     */
    public static Set<FarmProxy> filterFarmProxiesByVmTimeToLive(Collection<FarmProxy> farmProxies, long minimumVmTimeToLive) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.getVmTimeToLive() >= minimumVmTimeToLive) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    /**
     * Filters a collection of farm proxies down to only those that have at least the supplied minimum job timeout value.
     *
     * @param farmProxies       the collection of farm proxies to filter
     * @param minimumJobTimeout the minimum job timeout in milliseconds
     * @return a filtered farm proxy set (this is a new set as the original collection was not altered)
     */
    public static Set<FarmProxy> filterFarmProxiesByJobTimeout(Collection<FarmProxy> farmProxies, long minimumJobTimeout) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.getJobTimeout() >= minimumJobTimeout) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    /**
     * Filters a collection of farm proxies down to only those that require (or don't require) a farm password.
     *
     * @param farmProxies              the collection of farm proxies to filter
     * @param wantFarmPasswordRequired whether a farm password is desired or not
     * @return a filtered farm proxy set (this is a new set as the original collection was not altered)
     */
    public static Set<FarmProxy> filterFarmProxiesByPasswordRequired(Collection<FarmProxy> farmProxies, boolean wantFarmPasswordRequired) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.requiresFarmPassword() == wantFarmPasswordRequired)
                returnFarmProxies.add(farmProxy);
        }
        return returnFarmProxies;
    }

}
