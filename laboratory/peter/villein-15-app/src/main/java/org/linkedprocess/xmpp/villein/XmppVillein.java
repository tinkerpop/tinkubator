package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.ProbePresence;
import org.linkedprocess.xmpp.XmppClient;
//import org.linkedprocess.xmpp.farm.SpawnVm;
//import org.linkedprocess.xmpp.farm.SpawnVmProvider;
import org.linkedprocess.xmpp.vm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:59:48 PM
 */
public class XmppVillein extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVillein.class);
    public static final String RESOURCE_PREFIX = "LoPVillein";
    public static final String STATUS_MESSAGE = "LoP Villein v0.1";
    protected LinkedProcess.VilleinStatus status;

    public enum StructType {
        HOST, FARM, VM
    }

    protected Map<String, HostStruct> hostStructs;

    public XmppVillein(final String server, final int port, final String username, final String password) throws XMPPException {
//        InputStream resourceAsStream = getClass().getResourceAsStream("/logging.properties");
//        try {
//            LogManager.getLogManager().readConfiguration(resourceAsStream);
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
//        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());
        pm.addIQProvider(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new SubmitJobProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());
//        pm.addIQProvider(LinkedProcess.MANAGE_BINDINGS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new ManageBindingsProvider());

        this.logon(server, port, username, password, RESOURCE_PREFIX);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

//        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);

 //       this.connection.addPacketListener(new SpawnVmListener(this), spawnFilter);
        this.connection.addPacketListener(new PresenceListener(this), presenceFilter);

        this.hostStructs = new HashMap<String, HostStruct>();
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status));
        // this.createFarmsFromRoster();
    }


    public Collection<HostStruct> getHostStructs() {
        return this.hostStructs.values();
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
        for (HostStruct hostStruct : this.hostStructs.values()) {
            if (hostStruct.getFullJid().equals(jid))
                return null;
            for (FarmStruct farmStruct : hostStruct.getFarmStructs()) {
                if (farmStruct.getFullJid().equals(jid))
                    return hostStruct;
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getFullJid().equals(jid))
                        return farmStruct;
                }
            }
        }
        return null;
    }

    public Struct getStruct(String jid, StructType type) {
        for (HostStruct hostStruct : this.hostStructs.values()) {
            if (hostStruct.getFullJid().equals(jid) && (type == null || type == StructType.HOST))
                return hostStruct;
            for (FarmStruct farmStruct : hostStruct.getFarmStructs()) {
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
            this.hostStructs.remove(jid);
        } else if (parentStruct instanceof HostStruct) {
            ((HostStruct) parentStruct).removeFarmStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        } else {
            ((FarmStruct) parentStruct).removeVmStruct(jid);
            LOGGER.info("Removing struct for " + jid);
        }
    }


    public void addHostStruct(HostStruct hostStruct) {
        this.hostStructs.put(hostStruct.getFullJid(), hostStruct);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        Struct hostStruct = this.getStruct(LinkedProcess.generateBareJid(farmStruct.getFullJid()), StructType.HOST);
        if (hostStruct != null)
            ((HostStruct) hostStruct).addFarmStruct(farmStruct);
        else
            LOGGER.severe("host struct null for " + farmStruct.getFullJid());
    }

    public void addVmStruct(String farmJid, VmStruct vmStruct) {
        Struct farmStruct = this.getStruct(farmJid, StructType.FARM);
        if (farmStruct != null) {
            ((FarmStruct) farmStruct).addVmStruct(vmStruct);
        } else {
            LOGGER.severe("farm struct null for " + vmStruct.getFullJid());
        }
    }


//    public void spawnVirtualMachine(String farmJid, String vmSpecies) {
//        SpawnVm spawn = new SpawnVm();
//        spawn.setTo(farmJid);
//        spawn.setVmSpecies(vmSpecies);
//        spawn.setType(IQ.Type.GET);
//        this.connection.sendPacket(spawn);
//    }

    public void terminateVirtualMachine(VmStruct vmStruct) {
        TerminateVm terminate = new TerminateVm();
        terminate.setTo(vmStruct.getFullJid());
        terminate.setVmPassword(vmStruct.getVmPassword());
        terminate.setType(IQ.Type.GET);
        this.connection.sendPacket(terminate);
    }

    public void createHostStructsFromRoster() {
        this.roster.reload();
        //this.userStructs.clear();
        for (RosterEntry entry : this.getRoster().getEntries()) {
            HostStruct hostStruct = this.hostStructs.get(entry.getUser());
            if (hostStruct == null)
                hostStruct = new HostStruct();
            hostStruct.setFullJid(entry.getUser());
            hostStruct.setPresence(this.roster.getPresence(entry.getUser()));
            this.hostStructs.put(hostStruct.getFullJid(), hostStruct);
            ProbePresence probe = new ProbePresence();
            probe.setTo(entry.getUser());
            this.connection.sendPacket(probe);
        }
    }

    public final Presence createPresence(final LinkedProcess.VilleinStatus status) {
        String statusMessage = "LoP Villein v0.1";
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
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
        HostStruct hostStruct = this.hostStructs.get(jid);
        if (hostStruct != null) {
            for (FarmStruct farmStruct : hostStruct.getFarmStructs()) {
                for (VmStruct vmStruct : farmStruct.getVmStructs()) {
                    if (vmStruct.getVmPassword() != null) {
                        terminateVirtualMachine(vmStruct);
                    }
                }
            }
        }
        this.hostStructs.remove(jid);

    }
}
