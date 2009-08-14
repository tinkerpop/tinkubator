package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.Dispatcher;

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

}
