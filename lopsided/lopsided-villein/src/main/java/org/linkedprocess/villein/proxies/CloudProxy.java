/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.proxies;

import org.linkedprocess.Jid;

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
    Map<Jid, CountrysideProxy> countrysideProxies = new HashMap<Jid, CountrysideProxy>();

    /**
     * Add a countryside proxy to this cloud.
     *
     * @param countrysideProxy the countryside proxy to add
     */
    public void addCountrysideProxy(CountrysideProxy countrysideProxy) {
        this.countrysideProxies.put(countrysideProxy.getJid(), countrysideProxy);
    }

    /**
     * Get a countryside proxy from this cloud.
     *
     * @param jid the bare jid of the countryside proxy
     * @return the countryside proxy identified by the provided bare jid
     */
    public CountrysideProxy getCountrysideProxy(Jid jid) {
        return this.countrysideProxies.get(jid);
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
     * @param jid the bare jid of the countryside to remove
     */
    public void removeCountrysideProxy(Jid jid) {
        this.countrysideProxies.remove(jid);
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
     * @param jid the jid of the proxy to retrieve
     * @return the xmpp proxy with the provided full jid
     */
    public XmppProxy getXmppProxy(Jid jid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {

            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getJid().equals(jid))
                    return registryProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getJid().equals(jid))
                    return farmProxy;
            }
        }
        return null;
    }

    /**
     * Find a farm proxy in the cloud by its full jid.
     *
     * @param jid the jid of the farm to retrieve
     * @return the farm with the provided jid
     */
    public FarmProxy getFarmProxy(Jid jid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getJid().equals(jid))
                    return farmProxy;
            }
        }
        return null;
    }

    /**
     * Find a registry proxy in the cloud by its full jid.
     *
     * @param jid the jid of the registry to retrieve
     * @return the registry with the provided jid
     */
    public RegistryProxy getRegistryProxy(Jid jid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getJid().equals(jid))
                    return registryProxy;
            }
        }
        return null;
    }

    /**
     * Find a virtual machine proxy in the cloud by its identifier and parent farm.
     *
     * @param farmProxy the parent farm proxy of the virtual machine to retrieve
     * @param vmId      the identifier of the virtual machine to retrieve
     * @return the virtual machine with the provided identifier
     */
    public VmProxy getVmProxy(FarmProxy farmProxy, String vmId) {
        for (VmProxy vmProxy : farmProxy.getVmProxies()) {
            if (vmProxy.getVmId().equals(vmId))
                return vmProxy;
        }
        return null;
    }

    /**
     * Find a virtual machine proxy in the cloud by its identifier.
     * While identifiers are randomly generated, it is not guarenteed to be unique.
     * Thus, be aware that there might be multiple vms with the same id (though very rare).
     *
     * @param vmId the identifier of the virtual machine to retrieve
     * @return the virtual machine with the provided identifier
     */
    public VmProxy getVmProxy(String vmId) {
        for (FarmProxy farmProxy : this.getFarmProxies()) {
            for (VmProxy vmProxy : farmProxy.getVmProxies()) {
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
     * @param jid the jid of the proxy whose parent proxy is needed
     * @return the parent proxy of the provided proxy jid
     */
    public CountrysideProxy getParentCountrysideProxy(Jid jid) {
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {

            for (RegistryProxy registryProxy : countrysideProxy.getRegistryProxies()) {
                if (registryProxy.getJid().equals(jid))
                    return countrysideProxy;
            }
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                if (farmProxy.getJid().equals(jid))
                    return countrysideProxy;

            }
        }
        return null;
    }

    /**
     * This is a helper method that removes an XMPP proxy from its parent.
     *
     * @param jid the jid of the proxy to remove from its parent
     */
    public void removeXmppProxy(Jid jid) {
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(jid.getBareJid());
        if (countrysideProxy != null) {
            countrysideProxy.removeFarmProxy(jid);
            countrysideProxy.removeRegistryProxy(jid);
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
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(farmProxy.getJid().getBareJid());
        if (countrysideProxy != null)
            countrysideProxy.addFarmProxy(farmProxy);
        else
            throw new ParentProxyNotFoundException("parent countryside proxy null for " + farmProxy.getJid());
    }

    /**
     * Add a registry proxy to a countryside proxy. The countryside proxy of the registry proxy is determined by tranforming the
     * fully-qualified jid of the registry proxy into a bare jid and adding the registry proxy to the countryside proxy identified by the bare jid.
     *
     * @param registryProxy the registry proxy to add to a countryside proxy maintained by this cloud
     * @throws ParentProxyNotFoundException when the parent countryside proxy of the registry proxy is not found in the cloud
     */
    public void addRegistryProxy(RegistryProxy registryProxy) throws ParentProxyNotFoundException {
        CountrysideProxy countrysideProxy = this.getCountrysideProxy(registryProxy.getJid().getBareJid());
        if (countrysideProxy != null)
            countrysideProxy.addRegistryProxy(registryProxy);
        else
            throw new ParentProxyNotFoundException("parent countryside proxy null for " + registryProxy.getJid());
    }

    /**
     * Add a virtual machine proxy to the farm proxy identifed by its fully-qualified jid.
     *
     * @param farmJid the jid of the farm proxy to add the virtual machine proxy to
     * @param vmProxy the virtual machine proxy to add to the located farm proxy
     * @throws ParentProxyNotFoundException when the parent farm proxy of the virtual machine proxy is not found in the cloud
     */
    public void addVmProxy(Jid farmJid, VmProxy vmProxy) throws ParentProxyNotFoundException {
        FarmProxy farmProxy = this.getFarmProxy(farmJid);
        if (farmProxy != null)
            farmProxy.addVmProxy(vmProxy);
        else
            throw new ParentProxyNotFoundException("parent farm proxy null for " + vmProxy.getVmId());
    }


}
