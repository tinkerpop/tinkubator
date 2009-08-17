/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.Dispatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A CountrysideProxy is a proxy to a countryside.
 * A countryside can contain any number of farms and registries and is identified by a bare JID.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class CountrysideProxy extends Proxy implements FarmHolder {


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
