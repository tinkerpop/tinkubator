package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.xmpp.farm.SpawnVmProvider;
import org.linkedprocess.xmpp.villein.PresenceHandler;
import org.linkedprocess.xmpp.villein.proxies.*;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.vm.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:59:48 PM
 */
public class XmppVillein extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVillein.class);
    public static final String RESOURCE_PREFIX = "LoPVillein";
    public static final String STATUS_MESSAGE = "LoPSideD Villein";
    protected LinkedProcess.VilleinStatus status;
    protected Dispatcher dispatcher;

    protected Set<PresenceHandler> presenceHandlers = new HashSet<PresenceHandler>();

    public enum ProxyType {
        COUNTRYSIDE, FARM, REGISTRY, VM
    }

    protected Map<String, CountrysideProxy> countrysideProxies;

    public XmppVillein(final String server, final int port, final String username, final String password) throws XMPPException {
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());
        pm.addIQProvider(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new SubmitJobProvider());
        pm.addIQProvider(LinkedProcess.PING_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new PingJobProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());
        pm.addIQProvider(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new ManageBindingsProvider());
        this.logon(server, port, username, password, RESOURCE_PREFIX);
        this.dispatcher = new Dispatcher(this);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

        PacketFilter lopFilter = new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR));
        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);

        this.connection.addPacketListener(new PresenceListener(this), presenceFilter);
        this.connection.addPacketListener(new LopVilleinListener(this), lopFilter);
        this.countrysideProxies = new HashMap<String, CountrysideProxy>();
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status)); 
    }

    public Collection<CountrysideProxy> getCountrysideProxies() {
        return this.countrysideProxies.values();
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
            LOGGER.info("Removing proxy for " + jid);
        } else {
            ((FarmProxy) parentProxy).removeVmProxy(jid);
            LOGGER.info("Removing proxy for " + jid);
        }
    }

    public void addCountrysideProxy(CountrysideProxy countrysideProxy) {
        this.countrysideProxies.put(countrysideProxy.getFullJid(), countrysideProxy);
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

    public synchronized void createCountrysideProxiesFromRoster() {
        for (RosterEntry entry : this.getRoster().getEntries()) {
            CountrysideProxy countrysideProxy = this.countrysideProxies.get(entry.getUser());
            if (countrysideProxy == null) {
                countrysideProxy = new CountrysideProxy(entry.getUser(), this.dispatcher);
                this.countrysideProxies.put(countrysideProxy.getFullJid(), countrysideProxy);
            }
        }
    }

    public final Presence createPresence(final LinkedProcess.VilleinStatus status) {
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, XmppVillein.STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case INACTIVE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    public void setStatus(LinkedProcess.VilleinStatus status) {
        this.status = status;
    }

    public LinkedProcess.VilleinStatus getStatus() {
        return this.status;
    }

    public void requestUnsubscription(String jid, boolean removeFromRoster) {
        super.requestUnsubscription(jid, removeFromRoster);
        CountrysideProxy countrysideProxy = this.countrysideProxies.get(jid);
        if (countrysideProxy != null) {
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    if (vmProxy.getVmPassword() != null) {
                        vmProxy.terminateVm(null, null);
                    }
                }
            }
        }
        this.countrysideProxies.remove(jid);
    }

     protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppVillein.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public void addPresenceHandler(PresenceHandler presenceHandler) {
        this.presenceHandlers.add(presenceHandler);
    }

    public Set<PresenceHandler> getPresenceHandlers() {
        return this.presenceHandlers;
    }

    public void removePresenceHandler(PresenceHandler presenceHandler) {
        this.presenceHandlers.remove(presenceHandler);
    }
}
