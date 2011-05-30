/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.Jid;
import org.linkedprocess.LinkedProcess;

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

    protected Jid jid;
    protected LinkedProcess.Status status;

    /**
     * The set of farm proxies contained in this countryside proxy.
     */
    protected Map<Jid, FarmProxy> farmProxies = new HashMap<Jid, FarmProxy>();
    /**
     * The set of registry proxies contained in this countryside proxy.
     */
    protected Map<Jid, RegistryProxy> registryProxies = new HashMap<Jid, RegistryProxy>();

    public CountrysideProxy(final Jid jid) {
        this.jid = jid;
    }

    public Jid getJid() {
        return this.jid;
    }

    /**
     * Set the status of the countryside. Countrysides are active or inactive entities.
     *
     * @param status the status of the countryside
     */
    public void setStatus(LinkedProcess.Status status) {
        this.status = status;
    }

    /**
     * Get the status of the countryside. Countrysides are active or inactive entities.
     *
     * @return the status of the countryside
     */
    public LinkedProcess.Status getStatus() {
        return this.status;
    }

    /**
     * Get the farm proxy contained in this countryside by its jid.
     *
     * @param jid the jid of the farm proxy to get
     * @return the retrieved farm proxy
     */
    public FarmProxy getFarmProxy(Jid jid) {
        return this.farmProxies.get(jid);
    }

    /**
     * Get the registsry proxy contained in this countryside by its jid.
     *
     * @param jid the jid of the registry proxy to get
     * @return the retrieved registry proxy
     */
    public RegistryProxy getRegistryProxy(Jid jid) {
        return this.registryProxies.get(jid);
    }

    /**
     * Add a farm proxy to this countryside proxy.
     *
     * @param farmProxy the farm proxy to add
     */
    public void addFarmProxy(FarmProxy farmProxy) {
        this.farmProxies.put(farmProxy.getJid(), farmProxy);
    }

    /**
     * Add a registry proxy to this countryside proxy.
     *
     * @param registryProxy the registry proxy to add
     */
    public void addRegistryProxy(RegistryProxy registryProxy) {
        this.registryProxies.put(registryProxy.getJid(), registryProxy);
    }

    /**
     * Remove a farm proxy from this countryside proxy.
     *
     * @param jid the jid of the farm to remove
     */
    public void removeFarmProxy(Jid jid) {
        this.farmProxies.remove(jid);
    }

    /**
     * Remove a registry proxy from this countryside proxy.
     *
     * @param jid the jid of the registry to remove
     */
    public void removeRegistryProxy(Jid jid) {
        this.registryProxies.remove(jid);
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

    public boolean equals(Object countrysideProxy) {
        return countrysideProxy instanceof CountrysideProxy && ((CountrysideProxy) countrysideProxy).getJid().equals(this.jid);
    }

    public int hashCode() {
        return this.jid.hashCode();
    }


}
