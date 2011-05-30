/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.patterns;

import org.linkedprocess.Jid;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.proxies.CloudProxy;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.FarmProxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The EntityAllocationPattern is useful for allocating entities (e.g. countrysides, farms, virtual machines) in an LoP cloud.
 * This pattern simply wraps an LoP cloud with useful methods.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class EntityAllocationPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(EntityAllocationPattern.class);
    private static final long SLEEP_TIME = 10l;

    private static void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if (timeout > 0 && (System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    /**
     * Wait for a cloud to be populated with a given number of farms and then returns those farms as a set.
     *
     * @param cloudProxy    the cloud proxy to allocate the farms from
     * @param numberOfFarms the number of farms to allocate from the cloud
     * @param timeout       the number of milliseconds to wait for the cloud to become populated
     * @return a set of farms in the cloud that is the size of the specified parameter
     * @throws TimeoutException thrown if the cloud does not populate with the specified number of farms in the timeout length
     */
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
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * Wait for a cloud to be populated with a specific set of farms and then returns those farms as a set.
     *
     * @param cloudProxy the cloud proxy to allocate farms from
     * @param farmJids   the jids of the farms to allocate
     * @param timeout    the number of milliseconds to wait for the cloud to become populated with farms of those jids
     * @return the set of farms that have the specified jids
     * @throws TimeoutException thrown if the cloud does not populate with farms of the specified jids in the timeout length
     */
    public static Set<FarmProxy> allocateFarms(final CloudProxy cloudProxy, final Set<Jid> farmJids, final long timeout) throws TimeoutException {
        Set<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            for (FarmProxy farmProxy : cloudProxy.getFarmProxies()) {
                if (farmJids.contains(farmProxy.getJid())) {
                    farmProxies.add(farmProxy);
                    if (farmProxies.size() == farmJids.size()) {
                        return farmProxies;
                    }
                }
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * Wait for a cloud to be populated with a specified farm and return its farm proxy.
     * This is a helper/wrapper method to the allocateFarms() method that takes a set of jids.
     *
     * @param cloudProxy the cloud proxy to allocate the farm from
     * @param farmJid    the jid of the farm to allocate
     * @param timeout    the number of milliseconds to wait for the cloud to become populated with the specified farm
     * @return a farm with the specified farm jid
     * @throws TimeoutException thrown if the cloud does not populate with farm in the timeout length
     */
    public static FarmProxy allocateFarm(final CloudProxy cloudProxy, final Jid farmJid, final long timeout) throws TimeoutException {
        Set<Jid> farmJidSet = new HashSet<Jid>();
        farmJidSet.add(farmJid);
        return EntityAllocationPattern.allocateFarms(cloudProxy, farmJidSet, timeout).iterator().next();
    }

    /**
     * Wait for a cloud to be populated with a specific set of countrysides and then returns those countryside proxies as a set.
     *
     * @param cloudProxy      the cloud proxy to allocate countrysides from
     * @param countrysideJids the jids of the countrysides to allocate (be sure these are bare jids)
     * @param timeout         the number of milliseconds to wait for the cloud to become populated with countrysides of those jids
     * @return the set of countrysides that have the specified jids
     * @throws TimeoutException thrown if the cloud does not populate with countrysides of the specified jids in the timeout length
     */
    public static Set<CountrysideProxy> allocateCountrysides(final CloudProxy cloudProxy, final Set<Jid> countrysideJids, final long timeout) throws TimeoutException {
        Set<CountrysideProxy> countrysideProxies = new HashSet<CountrysideProxy>();
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            for (CountrysideProxy countrysideProxy : cloudProxy.getCountrysideProxies()) {
                if (countrysideJids.contains(countrysideProxy.getJid())) {
                    countrysideProxies.add(countrysideProxy);
                    if (countrysideProxies.size() == countrysideJids.size()) {
                        return countrysideProxies;
                    }
                }
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    /**
     * Wait for a cloud to be populated with a specified countryside and return that countryside.
     * This is a helper/wrapper method to the allocateCountrysides() method that takes a set of jids.
     *
     * @param cloudProxy     the cloud proxy to allocate the countryside from
     * @param countrysideJid the jid of the countryside to allocate (be sure that this is a bare jid)
     * @param timeout        the number of milliseconds to wait for the cloud to become populated with the specified countryside
     * @return a countryside with the specified countryside jid
     * @throws TimeoutException thrown if the cloud does not populate with countryside in the timeout length
     */
    public static CountrysideProxy allocateCountryside(final CloudProxy cloudProxy, final Jid countrysideJid, final long timeout) throws TimeoutException {
        Set<Jid> countrysideJidSet = new HashSet<Jid>();
        countrysideJidSet.add(countrysideJid);
        return EntityAllocationPattern.allocateCountrysides(cloudProxy, countrysideJidSet, timeout).iterator().next();
    }


    /**
     * Filters a collection of farm proxies down to only those that support a particular virtual machine species.
     *
     * @param farmProxies the collection of farm proxies to filter
     * @param vmSpecies   the virtual machine species that must be supported
     * @return a filtered farm proxy set (this is a new set as the original collection was not altered)
     */
    public static Set<FarmProxy> filterFarmProxiesByVmSpeciesSupport(final Collection<FarmProxy> farmProxies, final String vmSpecies) {
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
    public static Set<FarmProxy> filterFarmProxiesByVmTimeToLive(final Collection<FarmProxy> farmProxies, final long minimumVmTimeToLive) {
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
    public static Set<FarmProxy> filterFarmProxiesByJobTimeout(final Collection<FarmProxy> farmProxies, final long minimumJobTimeout) {
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
