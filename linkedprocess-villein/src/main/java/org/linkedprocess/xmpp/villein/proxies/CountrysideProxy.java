package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.proxies.Proxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.RegistryProxy;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.LinkedProcess;

import java.util.*;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 1:02:16 PM
 */
public class CountrysideProxy extends Proxy {


    protected Map<String, FarmProxy> farmProxies = new HashMap<String, FarmProxy>();
    protected Map<String, RegistryProxy> registryProxies = new HashMap<String, RegistryProxy>();

    public CountrysideProxy(final String fullJid, final Dispatcher dispatcher) {
        super(fullJid, dispatcher);
    }

    public FarmProxy getFarmProxy(String farmJid) {
        return this.farmProxies.get(farmJid);
    }

    public RegistryProxy getRegistryProxy(String countrysideJid) {
        return this.registryProxies.get(countrysideJid);
    }

    public void addFarmProxy(FarmProxy farmProxy) {
        this.farmProxies.put(farmProxy.getFullJid(), farmProxy);
    }

    public void addRegistryProxy(RegistryProxy registryProxy) {
        this.registryProxies.put(registryProxy.getFullJid(), registryProxy);
    }

    public void removeRegistryProxy(String countrysideJid) {
        this.registryProxies.remove(countrysideJid);
    }

    public Collection<FarmProxy> getFarmProxies() {
        return this.farmProxies.values();
    }

    public Collection<RegistryProxy> getRegistryProxies() {
        return this.registryProxies.values();
    }

    public void removeFarmProxy(String farmJid) {
        this.farmProxies.remove(farmJid);
    }

    public Collection<FarmProxy> filterFarmProxiesByVmSpeciesSupport(Collection<FarmProxy> farmProxies, String supportsVmSpecies) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for(FarmProxy farmProxy : farmProxies) {
            Field field = farmProxy.getField(LinkedProcess.VM_SPECIES_ATTRIBUTE);
            if(field.getValues().contains(supportsVmSpecies))
                returnFarmProxies.add(farmProxy);

        }
        return returnFarmProxies;
    }

    public Collection<FarmProxy> filterFarmProxiesByVmTimeToLive(Collection<FarmProxy> farmProxies, int minimumVmTimeToLive) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for(FarmProxy farmProxy : farmProxies) {
            Field field = farmProxy.getField(LinkedProcess.VM_TIME_TO_LIVE);
            int vmTimeToLive = field.getIntegerValue();
            if(vmTimeToLive >= minimumVmTimeToLive) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    public Collection<FarmProxy> filterFarmProxiesByJobTimeout(Collection<FarmProxy> farmProxies, int minimumJobTimeout) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for(FarmProxy farmProxy : farmProxies) {
            Field field = farmProxy.getField(LinkedProcess.JOB_TIMEOUT);
            int jobTimeout = field.getIntegerValue();
            if(jobTimeout >= minimumJobTimeout) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    public Collection<FarmProxy> filterFarmProxiesByPasswordRequired(Collection<FarmProxy> farmProxies, boolean passwordRequired) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for(FarmProxy farmProxy : farmProxies) {

        }
        return returnFarmProxies;
    }

}
