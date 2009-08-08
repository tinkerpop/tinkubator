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

    protected Map<String, CountrysideStruct> countrysideStructs;

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
        this.countrysideStructs = new HashMap<String, CountrysideStruct>();
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status)); 
    }

    public Collection<CountrysideStruct> getCountrysideStructs() {
        return this.countrysideStructs.values();
    }

    public Collection<FarmStruct> getFarmStructs() {
        Collection<FarmStruct> farmStructs = new HashSet<FarmStruct>();
        for (CountrysideStruct countrysideStruct : this.countrysideStructs.values()) {
            farmStructs.addAll(countrysideStruct.getFarmStructs());
        }
        return farmStructs;
    }

    public Collection<RegistryStruct> getRegistryStructs() {
        Collection<RegistryStruct> registryStructs = new HashSet<RegistryStruct>();
        for (CountrysideStruct countrysideStruct : this.countrysideStructs.values()) {
            registryStructs.addAll(countrysideStruct.getRegistryStructs());
        }
        return registryStructs;
    }

    public Collection<VmStruct> getVmStructs() {
        Collection<VmStruct> vmStructs = new HashSet<VmStruct>();
        for (FarmStruct farmStruct : this.getFarmStructs()) {
            vmStructs.addAll(farmStruct.getVmStructs());
        }
        return vmStructs;
    }

    public Struct getStruct(String jid) {
        return this.getStruct(jid, null);
    }

    public boolean structExists(String jid) {
        if (this.getStruct(jid) != null)
            return true;
        else
            return false;
    }

    public Struct getParentStruct(String jid) {
        for (CountrysideStruct countrysideStruct : this.countrysideStructs.values()) {
            if (countrysideStruct.getFullJid().equals(jid))
                return null;
            for (RegistryStruct registryStruct : countrysideStruct.getRegistryStructs()) {
                if (registryStruct.getFullJid().equals(jid))
                    return countrysideStruct;
            }
            for (FarmStruct farmStruct : countrysideStruct.getFarmStructs()) {
                if (farmStruct.getFullJid().equals(jid))
                    return countrysideStruct;
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getFullJid().equals(jid))
                        return farmStruct;
                }
            }
        }
        return null;
    }

    public Struct getStruct(String jid, StructType type) {
        for (CountrysideStruct countrysideStruct : this.countrysideStructs.values()) {
            if (countrysideStruct.getFullJid().equals(jid) && (type == null || type == StructType.COUNTRYSIDE))
                return countrysideStruct;
            for (RegistryStruct registryStruct : countrysideStruct.getRegistryStructs()) {
                if (registryStruct.getFullJid().equals(jid) && (type == null || type == StructType.REGISTRY))
                    return registryStruct;
            }
            for (FarmStruct farmStruct : countrysideStruct.getFarmStructs()) {
                if (farmStruct.getFullJid().equals(jid) && (type == null || type == StructType.FARM))
                    return farmStruct;
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getFullJid().equals(jid) && (type == null || type == StructType.VM))
                        return vmStruct;
                }
            }
        }
        return null;
    }

    public void removeStruct(String jid) {
        Struct parentStruct = this.getParentStruct(jid);
        if (parentStruct == null) {
            this.countrysideStructs.remove(jid);
        } else if (parentStruct instanceof CountrysideStruct) {
            ((CountrysideStruct) parentStruct).removeFarmStruct(jid);
            ((CountrysideStruct) parentStruct).removeRegistryStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        } else {
            ((FarmStruct) parentStruct).removeVmStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        }
    }

    public void addCountrysideStruct(CountrysideStruct countrysideStruct) {
        this.countrysideStructs.put(countrysideStruct.getFullJid(), countrysideStruct);
    }

    public void addFarmStruct(FarmStruct farmStruct) throws ParentStructNotFoundException {
        Struct farmlandStruct = this.getStruct(LinkedProcess.generateBareJid(farmStruct.getFullJid()), StructType.COUNTRYSIDE);
        if (farmlandStruct != null && farmlandStruct instanceof CountrysideStruct)
            ((CountrysideStruct) farmlandStruct).addFarmStruct(farmStruct);
        else
            throw new ParentStructNotFoundException("countryside struct null for " + farmStruct.getFullJid());
    }

    public void addRegistryStruct(RegistryStruct registryStruct) throws ParentStructNotFoundException {
        Struct farmlandStruct = this.getStruct(LinkedProcess.generateBareJid(registryStruct.getFullJid()), StructType.COUNTRYSIDE);
        if (farmlandStruct != null && farmlandStruct instanceof CountrysideStruct)
            ((CountrysideStruct) farmlandStruct).addRegistryStruct(registryStruct);
        else
            throw new ParentStructNotFoundException("countryside struct null for " + registryStruct.getFullJid());
    }

    public void addVmStruct(String farmJid, VmStruct vmStruct) throws ParentStructNotFoundException {
        Struct farmStruct = this.getStruct(farmJid, StructType.FARM);
        if (farmStruct != null && farmStruct instanceof FarmStruct)
            ((FarmStruct) farmStruct).addVmStruct(vmStruct);
        else
            throw new ParentStructNotFoundException("farm struct null for " + vmStruct.getFullJid());
    }

    public synchronized void createCountrysideStructsFromRoster() {
        for (RosterEntry entry : this.getRoster().getEntries()) {
            CountrysideStruct countrysideStruct = this.countrysideStructs.get(entry.getUser());
            if (countrysideStruct == null) {
                countrysideStruct = new CountrysideStruct(this.dispatcher);
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
        CountrysideStruct countrysideStruct = this.countrysideStructs.get(jid);
        if (countrysideStruct != null) {
            for (FarmStruct farmStruct : countrysideStruct.getFarmStructs()) {
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
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
