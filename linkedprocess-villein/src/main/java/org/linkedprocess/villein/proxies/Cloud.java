/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.villein.LopVillein;

import java.util.*;

/**
 * An LoP cloud is the primary interface to all LoP resources.
 * All resources in an LoP cloud (e.g. countrysides, registries, farms, virtual machines, and jobs) are mediated through proxies.
 * As a developer, you should primarily focus on the evolving data proxy data structure whereby, an LoP cloud contains
 * contrysides, countrysides contain farms and registries, farms contains virtual machines, and virtual machines contain jobs. These
 * proxies hide many of the low-level details of the LoP XMPP protocol specification. Moreover, higher-level interfaces
 * to the underlying resources of an LoP cloud can be accessed through the various supported patterns.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Cloud implements FarmHolder {

    /**
     * The types of proxies that exist.
     */
    public enum ProxyType {
        COUNTRYSIDE, FARM, REGISTRY, VM
    }

    /**
     * The countryside proxies that are maintained by this cloud.
     */
    Map<String, CountrysideProxy> countrysideProxies = new HashMap<String, CountrysideProxy>();

    /**
     * Add a countryside proxy to this cloud.
     *
     * @param countrysideProxy the countryside proxy to add
     */
    public void addCountrysideProxy(CountrysideProxy countrysideProxy) {
        this.countrysideProxies.put(countrysideProxy.getFullJid(), countrysideProxy);
    }

    /**
     * Get a countryside proxy from this cloud.
     *
     * @param bareJid the bare jid of the countryside proxy
     * @return the countryside proxy identified by the provided bare jid
     */
    public CountrysideProxy getCountrysideProxy(String bareJid) {
        return this.countrysideProxies.get(bareJid);
    }


    /**
     * Get the collectionn of all countrysides maintained by this cloud.
     *
     * @return the set of all countrysides maintained by this cloud
     */
    public Collection<CountrysideProxy> getCountrysideProxies() {
        return this.countrysideProxies.values();
    }

    /**
     * Remove a countryside from the cloud by its bare jid.
     *
     * @param bareJid the bare jid of the countryside to remove
     */
    public void removeCountrysideProxy(String bareJid) {
        this.countrysideProxies.remove(bareJid);
    }

    /**
     * This is a helper method that will create a set of all farm proxies that are maintained by all the countrysides of this cloud.
     *
     * @return the set of all farm proxies in this cloud
     */
    public Set<FarmProxy> getFarmProxies() {
        Set<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            farmProxies.addAll(countrysideProxy.getFarmProxies());
        }
        return farmProxies;
    }

    /**
     * This is a helper method that will create a set of all registry proxies that are maintained by all the countrysides of this cloud.
     *
     * @return the set of all registry proxies in this cloud
     */
    public Set<RegistryProxy> getRegistryProxies() {
        Set<RegistryProxy> registryProxies = new HashSet<RegistryProxy>();
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            registryProxies.addAll(countrysideProxy.getRegistryProxies());
        }
        return registryProxies;
    }

    /**
     * This is a helper method that will create a set of all virtual machine proxies that are maintained by all the farms of this cloud.
     *
     * @return the set of all virtual machine proxies in this cloud
     */
    public Set<VmProxy> getVmProxies() {
        Set<VmProxy> vmProxies = new HashSet<VmProxy>();
        for (FarmProxy farmProxy : this.getFarmProxies()) {
            vmProxies.addAll(farmProxy.getVmProxies());
        }
        return vmProxies;
    }

    /**
     * This is a helper method that when given a jid (fully-qualified or bare) and the type of proxy being searched for, return the associated proxy.
     *
     * @param jid  the jid of the proxy to locate
     * @param type the type of the proxy to locate
     * @return the located proxy (can be null)
     */
    public Proxy getProxy(String jid, ProxyType type) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            if (countrysideProxy.getFullJid().equals(jid) && (type == null || type == ProxyType.COUNTRYSIDE))
                return countrysideProxy;
            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getFullJid().equals(jid) && (type == null || type == ProxyType.REGISTRY))
                    return registryProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getFullJid().equals(jid) && (type == null || type == ProxyType.FARM))
                    return farmProxy;
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    if (vmProxy.getFullJid().equals(jid) && (type == null || type == ProxyType.VM))
                        return vmProxy;
                }
            }
        }
        return null;
    }

    /**
     * This is a helper method that when given a jid (fully-qualified or bare), a proxy is located in this cloud that has that jid.
     * Note that this proxy is untyped and must be checked using instanceof.
     *
     * @param jid the jid of the proxy to locate
     * @return the located proxy (can be null)
     */
    public Proxy getProxy(String jid) {
        return this.getProxy(jid, null);
    }

    /**
     * This is a helper method that determines if a proxy exists in this cloud that has the provided jid (fully-qualified or bare).
     *
     * @param jid the jid of the proxy to determine existence for
     * @return if a proxy exists for this jid
     */
    public boolean proxyExists(String jid) {
        if (this.getProxy(jid) != null)
            return true;
        else
            return false;
    }

    /**
     * This is a helper method that gets the parent proxy of the provided jid (fully-qualified or bare).
     * A countryside proxy has no parent proxy.
     * A farm and registry proxy have a countryside parent proxy.
     * A virtual machine proxy has a farm parent proxy.
     *
     * @param jid the jid of the proxy whose parent proxy is needed
     * @return the parent proxy of the provided proxy jid
     */
    public Proxy getParentProxy(String jid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            if (countrysideProxy.getFullJid().equals(jid))
                return null;
            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getFullJid().equals(jid))
                    return countrysideProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getFullJid().equals(jid))
                    return countrysideProxy;
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    if (vmProxy.getFullJid().equals(jid))
                        return farmProxy;
                }
            }
        }
        return null;
    }

    /**
     * This is a helper method that removes a proxy from its parent.
     *
     * @param jid the jid of the proxy to remove from its parent
     */
    public void removeProxy(String jid) {
        Proxy parentProxy = this.getParentProxy(jid);
        if (parentProxy == null) {
            this.countrysideProxies.remove(jid);
        } else if (parentProxy instanceof CountrysideProxy) {
            ((CountrysideProxy) parentProxy).removeFarmProxy(jid);
            ((CountrysideProxy) parentProxy).removeRegistryProxy(jid);
            LopVillein.LOGGER.info("Removing proxy for " + jid);
        } else {
            ((FarmProxy) parentProxy).removeVmProxy(jid);
            LopVillein.LOGGER.info("Removing proxy for " + jid);
        }
    }

    /**
     * Add a farm proxy to a countryside proxy. The countryside proxy of the farm proxy is determined by tranforming the
     * fully-qualified jid of the farm proxy into a bare jid and adding the farm proxy to the countryside proxy identified by the bare jid.
     *
     * @param farmProxy the farm proxy to add to a countryside proxy maintained by this cloud
     * @throws ParentProxyNotFoundException when the parent countryside proxy of the farm proxy is not found in the cloud
     */
    public void addFarmProxy(FarmProxy farmProxy) throws ParentProxyNotFoundException {
        Proxy countrysideProxy = this.getProxy(LinkedProcess.generateBareJid(farmProxy.getFullJid()), ProxyType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addFarmProxy(farmProxy);
        else
            throw new ParentProxyNotFoundException("countryside proxy null for " + farmProxy.getFullJid());
    }

    /**
     * Add a registry proxy to a countryside proxy. The countryside proxy of the registry proxy is determined by tranforming the
     * fully-qualified jid of the registry proxy into a bare jid and adding the registry proxy to the countryside proxy identified by the bare jid.
     *
     * @param registryProxy the registry proxy to add to a countryside proxy maintained by this cloud
     * @throws ParentProxyNotFoundException when the parent countryside proxy of the registry proxy is not found in the cloud
     */
    public void addRegistryProxy(RegistryProxy registryProxy) throws ParentProxyNotFoundException {
        Proxy countrysideProxy = this.getProxy(LinkedProcess.generateBareJid(registryProxy.getFullJid()), ProxyType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addRegistryProxy(registryProxy);
        else
            throw new ParentProxyNotFoundException("countryside proxy null for " + registryProxy.getFullJid());
    }

    /**
     * Add a virtual machine proxy to the farm proxy identifed by its fully-qualified jid.
     *
     * @param farmJid the jid of the farm proxy to add the virtual machine proxy to
     * @param vmProxy the virtual machine proxy to add to the located farm proxy
     * @throws ParentProxyNotFoundException when the parent farm proxy of the virtual machine proxy is not found in the cloud
     */
    public void addVmProxy(String farmJid, VmProxy vmProxy) throws ParentProxyNotFoundException {
        Proxy farmProxy = this.getProxy(farmJid, ProxyType.FARM);
        if (farmProxy != null && farmProxy instanceof FarmProxy)
            ((FarmProxy) farmProxy).addVmProxy(vmProxy);
        else
            throw new ParentProxyNotFoundException("farm proxy null for " + vmProxy.getFullJid());
    }


}
