/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

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
public class CountrysideProxy {

    protected String bareJid;

    /**
     * The set of farm proxies contained in this countryside proxy.
     */
    protected Map<String, FarmProxy> farmProxies = new HashMap<String, FarmProxy>();
    /**
     * The set of registry proxies contained in this countryside proxy.
     */
    protected Map<String, RegistryProxy> registryProxies = new HashMap<String, RegistryProxy>();

    public CountrysideProxy(final String bareJid) {
        this.bareJid = bareJid;
    }

    public String getBareJid() {
        return this.bareJid;
    }

    /**
     * Get the farm proxy contained in this countryside by its jid.
     *
     * @param fullJid the jid of the farm proxy to get
     * @return the retrieved farm proxy
     */
    public FarmProxy getFarmProxy(String fullJid) {
        return this.farmProxies.get(fullJid);
    }

    /**
     * Get the registsry proxy contained in this countryside by its jid.
     *
     * @param fullJid the jid of the registry proxy to get
     * @return the retrieved registry proxy
     */
    public RegistryProxy getRegistryProxy(String fullJid) {
        return this.registryProxies.get(fullJid);
    }

    /**
     * Add a farm proxy to this countryside proxy.
     *
     * @param farmProxy the farm proxy to add
     */
    public void addFarmProxy(FarmProxy farmProxy) {
        this.farmProxies.put(farmProxy.getFullJid(), farmProxy);
    }

    /**
     * Add a registry proxy to this countryside proxy.
     *
     * @param registryProxy the registry proxy to add
     */
    public void addRegistryProxy(RegistryProxy registryProxy) {
        this.registryProxies.put(registryProxy.getFullJid(), registryProxy);
    }

    /**
     * Remove a farm proxy from this countryside proxy.
     *
     * @param fullJid the jid of the farm to remove
     */
    public void removeFarmProxy(String fullJid) {
        this.farmProxies.remove(fullJid);
    }

    /**
     * Remove a registry proxy from this countryside proxy.
     *
     * @param fullJid the jid of the registry to remove
     */
    public void removeRegistryProxy(String fullJid) {
        this.registryProxies.remove(fullJid);
    }

    /**
     * Get the collection of all farm proxies contained in this countryside proxy.
     *
     * @return the collection of all farm proxies contained in this countryside proxy
     */
    public Collection<FarmProxy> getFarmProxies() {
        return this.farmProxies.values();
    }

    /**
     * Get the collection of all registry proxies contained in this countryside proxy.
     *
     * @return the collection of all registry proxies contained in this countryside proxy
     */
    public Collection<RegistryProxy> getRegistryProxies() {
        return this.registryProxies.values();
    }


}
