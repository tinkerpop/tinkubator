/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.LinkedProcess;

import java.util.*;

/**
 * A CloudProxy is the primary interface to all LoP resources.
 * All resources in a CloudProxy (e.g. countrysides, registries, farms, virtual machines, and jobs) are mediated through proxies.
 * As a developer, you should primarily focus on the evolving data proxy data structure whereby, a CloudProxy contains
 * contrysides, countrysides contain farms and registries, farms contains virtual machines, and virtual machines contain jobs. These
 * proxies hide many of the low-level details of the LoP XMPP protocol specification. Moreover, higher-level interfaces
 * to the underlying resources of a CloudProxy can be accessed through the various supported patterns.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class CloudProxy {

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
        this.countrysideProxies.put(countrysideProxy.getBareJid(), countrysideProxy);
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
     * Find an XMPP proxy (e.g. Farm or Registry) the a cloud by its full jid.
     *
     * @param fullJid the jid of the proxy to retrieve
     * @return the xmpp proxy with the provided full jid
     */
    public XmppProxy getXmppProxy(String fullJid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {

            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getFullJid().equals(fullJid))
                    return registryProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getFullJid().equals(fullJid))
                    return farmProxy;
            }
        }
        return null;
    }

    /**
     * Find a farm proxy in the cloud by its full jid.
     *
     * @param fullJid the jid of the farm to retrieve
     * @return the farm with the provided jid
     */
    public FarmProxy getFarmProxy(String fullJid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getFullJid().equals(fullJid))
                    return farmProxy;
            }
        }
        return null;
    }

    /**
     * Find a registry proxy in the cloud by its full jid.
     *
     * @param fullJid the jid of the registry to retrieve
     * @return the registry with the provided jid
     */
    public RegistryProxy getRegistryProxy(String fullJid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getFullJid().equals(fullJid))
                    return registryProxy;
            }
        }
        return null;
    }

    /**
     * Find a virtual machine proxy in the cloud by its identifier. Note that in theory
     * all virtual machines should have unique IDs, but this is not guarenteed.
     *
     * @param vmId the identifier of the virtual machine to retrieve
     * @return the virtual machine with the provided identifier
     */
    public VmProxy getVmProxy(String vmId) {
        for (FarmProxy farmProxies : this.getFarmProxies()) {
            for (VmProxy vmProxy : farmProxies.getVmProxies()) {
                if (vmProxy.getVmId().equals(vmId))
                    return vmProxy;
            }
        }
        return null;
    }

    /**
     * This is a helper method that gets the parent proxy of the provided jid (fully-qualified or bare).
     * A countryside proxy has no parent proxy.
     * A farm and registry proxy have a countryside parent proxy.
     * A virtual machine proxy has a farm parent proxy.
     *
     * @param fullJid the jid of the proxy whose parent proxy is needed
     * @return the parent proxy of the provided proxy jid
     */
    public CountrysideProxy getParentCountrysideProxy(String fullJid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {

            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getFullJid().equals(fullJid))
                    return countrysideProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getFullJid().equals(fullJid))
                    return countrysideProxy;

            }
        }
        return null;
    }

    /**
     * This is a helper method that removes an XMPP proxy from its parent.
     *
     * @param fullJid the jid of the proxy to remove from its parent
     */
    public void removeXmppProxy(String fullJid) {
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(LinkedProcess.generateBareJid(fullJid));
        if (countrysideProxy != null) {
            countrysideProxy.removeFarmProxy(fullJid);
            countrysideProxy.removeRegistryProxy(fullJid);
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
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(LinkedProcess.generateBareJid(farmProxy.getFullJid()));
        if (countrysideProxy != null)
            countrysideProxy.addFarmProxy(farmProxy);
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
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(LinkedProcess.generateBareJid(registryProxy.getFullJid()));
        if (countrysideProxy != null)
            countrysideProxy.addRegistryProxy(registryProxy);
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
        FarmProxy farmProxy = this.getFarmProxy(farmJid);
        if (farmProxy != null)
            farmProxy.addVmProxy(vmProxy);
        else
            throw new ParentProxyNotFoundException("farm proxy null for " + vmProxy.getVmId());
    }


}
