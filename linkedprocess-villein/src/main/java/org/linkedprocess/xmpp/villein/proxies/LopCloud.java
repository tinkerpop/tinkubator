package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.jivesoftware.smack.RosterEntry;

import java.util.*;

/**
 * User: marko
 * Date: Aug 12, 2009
 * Time: 2:15:04 PM
 */
public class LopCloud {

    public enum ProxyType {
        COUNTRYSIDE, FARM, REGISTRY, VM
    }

    Map<String, CountrysideProxy> countrysideProxies = new HashMap<String, CountrysideProxy>();

    public void addCountrysideProxy(CountrysideProxy countrysideProxy) {
        this.countrysideProxies.put(countrysideProxy.getFullJid(), countrysideProxy);
    }

    public CountrysideProxy getCountrysideProxy(String fullJid) {
        return this.countrysideProxies.get(fullJid);
    }

    public Collection<CountrysideProxy> getCountrysideProxies() {
        return this.countrysideProxies.values();
    }

    public void removeCountrysideProxy(String fullJid) {
        this.countrysideProxies.remove(fullJid);
    }

    public Collection<FarmProxy> getFarmProxies() {
        Collection<FarmProxy> farmProxies = new HashSet<FarmProxy>();
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            farmProxies.addAll(countrysideProxy.getFarmProxies());
        }
        return farmProxies;
    }

    public Collection<RegistryProxy> getRegistryProxies() {
        Collection<RegistryProxy> registryProxies = new HashSet<RegistryProxy>();
        for (CountrysideProxy countrysideProxy : this.countrysideProxies.values()) {
            registryProxies.addAll(countrysideProxy.getRegistryProxies());
        }
        return registryProxies;
    }

    public Collection<VmProxy> getVmProxies() {
        Collection<VmProxy> vmProxies = new HashSet<VmProxy>();
        for (FarmProxy farmProxy : this.getFarmProxies()) {
            vmProxies.addAll(farmProxy.getVmProxies());
        }
        return vmProxies;
    }

    public Proxy getProxy(String jid) {
        return this.getProxy(jid, null);
    }

    public boolean proxyExists(String jid) {
        if (this.getProxy(jid) != null)
            return true;
        else
            return false;
    }

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

    public void removeProxy(String jid) {
        Proxy parentProxy = this.getParentProxy(jid);
        if (parentProxy == null) {
            this.countrysideProxies.remove(jid);
        } else if (parentProxy instanceof CountrysideProxy) {
            ((CountrysideProxy) parentProxy).removeFarmProxy(jid);
            ((CountrysideProxy) parentProxy).removeRegistryProxy(jid);
            XmppVillein.LOGGER.info("Removing proxy for " + jid);
        } else {
            ((FarmProxy) parentProxy).removeVmProxy(jid);
            XmppVillein.LOGGER.info("Removing proxy for " + jid);
        }
    }

    public void addFarmProxy(FarmProxy farmProxy) throws ParentProxyNotFoundException {
        Proxy countrysideProxy = this.getProxy(LinkedProcess.generateBareJid(farmProxy.getFullJid()), ProxyType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addFarmProxy(farmProxy);
        else
            throw new ParentProxyNotFoundException("countryside proxy null for " + farmProxy.getFullJid());
    }

    public void addRegistryProxy(RegistryProxy registryProxy) throws ParentProxyNotFoundException {
        Proxy countrysideProxy = this.getProxy(LinkedProcess.generateBareJid(registryProxy.getFullJid()), ProxyType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addRegistryProxy(registryProxy);
        else
            throw new ParentProxyNotFoundException("countryside proxy null for " + registryProxy.getFullJid());
    }

    public void addVmProxy(String farmJid, VmProxy vmProxy) throws ParentProxyNotFoundException {
        Proxy farmProxy = this.getProxy(farmJid, ProxyType.FARM);
        if (farmProxy != null && farmProxy instanceof FarmProxy)
            ((FarmProxy) farmProxy).addVmProxy(vmProxy);
        else
            throw new ParentProxyNotFoundException("farm proxy null for " + vmProxy.getFullJid());
    }

    public static Collection<FarmProxy> filterFarmProxiesByVmSpeciesSupport(Collection<FarmProxy> farmProxies, String vmSpecies) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.supportsSpecies(vmSpecies))
                returnFarmProxies.add(farmProxy);
        }
        return returnFarmProxies;
    }

    public static Collection<FarmProxy> filterFarmProxiesByVmTimeToLive(Collection<FarmProxy> farmProxies, long minimumVmTimeToLive) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.getVmTimeToLive() >= minimumVmTimeToLive) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    public static Collection<FarmProxy> filterFarmProxiesByJobTimeout(Collection<FarmProxy> farmProxies, long minimumJobTimeout) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.getJobTimeout() >= minimumJobTimeout) {
                returnFarmProxies.add(farmProxy);
            }
        }
        return returnFarmProxies;
    }

    public static Collection<FarmProxy> filterFarmProxiesByPasswordRequired(Collection<FarmProxy> farmProxies, boolean wantPasswordRequired) {
        Set<FarmProxy> returnFarmProxies = new HashSet<FarmProxy>();
        for (FarmProxy farmProxy : farmProxies) {
            if (farmProxy.requiresPassword() == wantPasswordRequired)
                farmProxies.add(farmProxy);
        }
        return returnFarmProxies;
    }

}
