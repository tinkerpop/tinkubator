package org.linkedprocess.xmpp.villein.patterns;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.proxies.LopCloud;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;

import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

/**
 * User: marko
 * Date: Aug 14, 2009
 * Time: 11:34:19 AM
 */
public class ResourceAllocationPattern {

    private static final Logger LOGGER = LinkedProcess.getLogger(ResourceAllocationPattern.class);

    private static void checkTimeout(long startTime, long timeout) throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) > timeout) {
            throw new TimeoutException("timeout occured after " + (System.currentTimeMillis() - startTime) + "ms.");
        }
    }

    public static Set<FarmProxy> allocateFarms(final LopCloud lopCloud, final int numberOfFarms, final long timeout) throws TimeoutException {
        Set<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        long startTime = System.currentTimeMillis();
        while (true) {
            checkTimeout(startTime, timeout);
            if (lopCloud.getFarmProxies().size() >= numberOfFarms) {
                int i = 0;
                for(FarmProxy farmProxy : lopCloud.getFarmProxies()){
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
}
