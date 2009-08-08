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
import org.linkedprocess.xmpp.villein.structs.*;
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

    public enum StructType {
        COUNTRYSIDE, FARM, REGISTRY, VM
    }

    protected Map<String, CountrysideProxy> countrysideStructs;

    public XmppVillein(final String server, final int port, final String username, final String password) throws XMPPException {
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());
        pm.addIQProvider(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new SubmitJobProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
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
        this.countrysideStructs = new HashMap<String, CountrysideProxy>();
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status)); 
    }

    public Collection<CountrysideProxy> getCountrysideStructs() {
        return this.countrysideStructs.values();
    }

    public Collection<FarmProxy> getFarmStructs() {
        Collection<FarmProxy> farmStructs = new HashSet<FarmProxy>();
        for (CountrysideProxy countrysideStruct : this.countrysideStructs.values()) {
            farmStructs.addAll(countrysideStruct.getFarmStructs());
        }
        return farmStructs;
    }

    public Collection<RegistryProxy> getRegistryStructs() {
        Collection<RegistryProxy> registryStructs = new HashSet<RegistryProxy>();
        for (CountrysideProxy countrysideStruct : this.countrysideStructs.values()) {
            registryStructs.addAll(countrysideStruct.getRegistryStructs());
        }
        return registryStructs;
    }

    public Collection<VmProxy> getVmStructs() {
        Collection<VmProxy> vmStructs = new HashSet<VmProxy>();
        for (FarmProxy farmStruct : this.getFarmStructs()) {
            vmStructs.addAll(farmStruct.getVmStructs());
        }
        return vmStructs;
    }

    public Proxy getStruct(String jid) {
        return this.getStruct(jid, null);
    }

    public boolean structExists(String jid) {
        if (this.getStruct(jid) != null)
            return true;
        else
            return false;
    }

    public Proxy getParentStruct(String jid) {
        for (CountrysideProxy countrysideStruct : this.countrysideStructs.values()) {
            if (countrysideStruct.getFullJid().equals(jid))
                return null;
            for (RegistryProxy registryStruct : countrysideStruct.getRegistryStructs()) {
                if (registryStruct.getFullJid().equals(jid))
                    return countrysideStruct;
            }
            for (FarmProxy farmStruct : countrysideStruct.getFarmStructs()) {
                if (farmStruct.getFullJid().equals(jid))
                    return countrysideStruct;
                for (VmProxy vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getFullJid().equals(jid))
                        return farmStruct;
                }
            }
        }
        return null;
    }

    public Proxy getStruct(String jid, StructType type) {
        for (CountrysideProxy countrysideStruct : this.countrysideStructs.values()) {
            if (countrysideStruct.getFullJid().equals(jid) && (type == null || type == StructType.COUNTRYSIDE))
                return countrysideStruct;
            for (RegistryProxy registryStruct : countrysideStruct.getRegistryStructs()) {
                if (registryStruct.getFullJid().equals(jid) && (type == null || type == StructType.REGISTRY))
                    return registryStruct;
            }
            for (FarmProxy farmStruct : countrysideStruct.getFarmStructs()) {
                if (farmStruct.getFullJid().equals(jid) && (type == null || type == StructType.FARM))
                    return farmStruct;
                for (VmProxy vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getFullJid().equals(jid) && (type == null || type == StructType.VM))
                        return vmStruct;
                }
            }
        }
        return null;
    }

    public void removeStruct(String jid) {
        Proxy parentProxy = this.getParentStruct(jid);
        if (parentProxy == null) {
            this.countrysideStructs.remove(jid);
        } else if (parentProxy instanceof CountrysideProxy) {
            ((CountrysideProxy) parentProxy).removeFarmStruct(jid);
            ((CountrysideProxy) parentProxy).removeRegistryStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        } else {
            ((FarmProxy) parentProxy).removeVmStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        }
    }

    public void addCountrysideStruct(CountrysideProxy countrysideStruct) {
        this.countrysideStructs.put(countrysideStruct.getFullJid(), countrysideStruct);
    }

    public void addFarmStruct(FarmProxy farmStruct) throws ParentStructNotFoundException {
        Proxy countrysideProxy = this.getStruct(LinkedProcess.generateBareJid(farmStruct.getFullJid()), StructType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addFarmStruct(farmStruct);
        else
            throw new ParentStructNotFoundException("countryside struct null for " + farmStruct.getFullJid());
    }

    public void addRegistryStruct(RegistryProxy registryStruct) throws ParentStructNotFoundException {
        Proxy countrysideProxy = this.getStruct(LinkedProcess.generateBareJid(registryStruct.getFullJid()), StructType.COUNTRYSIDE);
        if (countrysideProxy != null && countrysideProxy instanceof CountrysideProxy)
            ((CountrysideProxy) countrysideProxy).addRegistryStruct(registryStruct);
        else
            throw new ParentStructNotFoundException("countryside struct null for " + registryStruct.getFullJid());
    }

    public void addVmStruct(String farmJid, VmProxy vmStruct) throws ParentStructNotFoundException {
        Proxy farmProxy = this.getStruct(farmJid, StructType.FARM);
        if (farmProxy != null && farmProxy instanceof FarmProxy)
            ((FarmProxy) farmProxy).addVmStruct(vmStruct);
        else
            throw new ParentStructNotFoundException("farm struct null for " + vmStruct.getFullJid());
    }

    public synchronized void createCountrysideStructsFromRoster() {
        for (RosterEntry entry : this.getRoster().getEntries()) {
            CountrysideProxy countrysideStruct = this.countrysideStructs.get(entry.getUser());
            if (countrysideStruct == null) {
                countrysideStruct = new CountrysideProxy(this.dispatcher);
                countrysideStruct.setFullJid(entry.getUser());
                this.countrysideStructs.put(countrysideStruct.getFullJid(), countrysideStruct);
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
        CountrysideProxy countrysideStruct = this.countrysideStructs.get(jid);
        if (countrysideStruct != null) {
            for (FarmProxy farmStruct : countrysideStruct.getFarmStructs()) {
                for (VmProxy vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getVmPassword() != null) {
                        //TODO: terminateVirtualMachine(vmStruct);
                    }
                }
            }
        }
        this.countrysideStructs.remove(jid);
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
