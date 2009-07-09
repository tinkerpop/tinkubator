package gov.lanl.cnls.linkedprocess.xmpp.villein;

import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.ProbePresence;
import gov.lanl.cnls.linkedprocess.xmpp.vm.*;
import gov.lanl.cnls.linkedprocess.xmpp.farm.*;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.provider.ProviderManager;

import java.io.InputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

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

    protected Map<String, UserStruct> userStructs;

    public XmppVillein(final String server, final int port, final String username, final String password) throws XMPPException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Starting " + STATUS_MESSAGE);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE, new SpawnVmProvider());
        pm.addIQProvider(LinkedProcess.TERMINATE_VM_TAG, LinkedProcess.LOP_VM_NAMESPACE, new TerminateVmProvider());
        pm.addIQProvider(LinkedProcess.EVALUATE_TAG, LinkedProcess.LOP_VM_NAMESPACE, new EvaluateProvider());
        pm.addIQProvider(LinkedProcess.JOB_STATUS_TAG, LinkedProcess.LOP_VM_NAMESPACE, new JobStatusProvider());
        pm.addIQProvider(LinkedProcess.ABORT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE, new AbortJobProvider());


        this.logon(server, port, username, password, RESOURCE_PREFIX);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        this.printRoster();

        PacketFilter spawnFilter = new AndFilter(new PacketTypeFilter(SpawnVm.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter terminateFilter = new AndFilter(new PacketTypeFilter(TerminateVm.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter evaluateFilter = new AndFilter(new PacketTypeFilter(Evaluate.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter jobStatusFilter = new AndFilter(new PacketTypeFilter(JobStatus.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter abortJobFilter = new AndFilter(new PacketTypeFilter(AbortJob.class), new OrFilter(new IQTypeFilter(IQ.Type.RESULT), new IQTypeFilter(IQ.Type.ERROR)));
        PacketFilter presenceFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceFilter());

        this.addPacketListener(new SpawnVmVilleinListener(this), spawnFilter);
        this.addPacketListener(new TerminateVmVilleinListener(this), terminateFilter);
        this.addPacketListener(new EvaluateVilleinListener(this), evaluateFilter);
        this.addPacketListener(new JobStatusVilleinListener(this), jobStatusFilter);
        this.addPacketListener(new AbortJobVilleinListener(this), abortJobFilter);
        this.addPacketListener(new PresenceListener(this), presenceFilter);

        this.userStructs = new HashMap<String, UserStruct>();
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
       // this.createFarmsFromRoster();
    }

    public VmStruct getVmStruct(String vmJid) {
        for(UserStruct userStruct : this.getUserStructs()) {
            for(FarmStruct farmStruct : this.getFarmStructs(userStruct.getFullJid())) {
                VmStruct vmStruct = this.getVmStruct(farmStruct.getFullJid(), vmJid);
                if(vmStruct != null)
                    return vmStruct;
            }
        }
        return null;
    }

    public VmStruct getVmStruct(String farmJid, String vmJid) {
        UserStruct userStruct = this.userStructs.get(LinkedProcess.generateBareJid(farmJid));
        if(userStruct != null)
            return userStruct.getFarmStruct(farmJid).getVmStruct(vmJid);
        else
            LOGGER.severe("user struct null for" + farmJid);
        return null;
    }

    public void addVmStruct(String farmJid, VmStruct vmStruct) {
        UserStruct userStruct = this.userStructs.get(LinkedProcess.generateBareJid(farmJid));
        if(userStruct != null)
            userStruct.getFarmStruct(farmJid).addVmStruct(vmStruct);
        else
            LOGGER.severe("user struct null for" + farmJid);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        UserStruct userStruct = this.userStructs.get(LinkedProcess.generateBareJid(farmStruct.getFullJid()));
        if(userStruct != null)
            userStruct.addFarmStruct(farmStruct);
        else
            LOGGER.severe("user struct null for" + farmStruct.getFullJid());
    }

    public Collection<FarmStruct> getFarmStructs(String userJid) {
        return this.userStructs.get(userJid).getFarmStructs();
    }

    public FarmStruct getFarmStruct(String farmJid) {
        for(UserStruct userStruct : this.userStructs.values()) {
            FarmStruct farmStruct = userStruct.getFarmStruct(farmJid);
            if(farmStruct != null) {
                return farmStruct;
            }
        }
        return null;
    }

    public void spawnVirtualMachine(String farmJid, String vmSpecies) {
        SpawnVm spawn = new SpawnVm();
        spawn.setTo(farmJid);
        spawn.setVmSpecies(vmSpecies);
        spawn.setType(IQ.Type.GET);
        this.connection.sendPacket(spawn);
    }

    public void terminateVirtualMachine(String vmJid, String vmPassword) {
        TerminateVm terminate = new TerminateVm();
        terminate.setTo(vmJid);
        terminate.setVmPassword(vmPassword);
        terminate.setType(IQ.Type.GET);
        this.connection.sendPacket(terminate);
    }

    public void createUserStructsFromRoster() {
        this.roster.reload();
        //this.userStructs.clear();
        for(RosterEntry entry : this.getRoster().getEntries()) {


            UserStruct userStruct = this.userStructs.get(entry.getUser());
            if(userStruct == null)
               userStruct = new UserStruct();
            userStruct.setFullJid(entry.getUser());
            userStruct.setPresence(this.roster.getPresence(entry.getUser()));
            this.userStructs.put(userStruct.getFullJid(), userStruct);
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

    public Collection<UserStruct> getUserStructs() {
        return this.userStructs.values();
    }

    public void setStatus(LinkedProcess.VilleinStatus status) {
        this.status = status;
    }

    public LinkedProcess.VilleinStatus getStatus() {
        return this.status;
    }

    public void requestUnsubscription(String jid, boolean removeFromRoster) {
        super.requestUnsubscription(jid, removeFromRoster);
        UserStruct userStruct = this.userStructs.get(jid);
        if(userStruct != null) {
            for(FarmStruct farmStruct : userStruct.getFarmStructs()) {
                for(VmStruct vmStruct : farmStruct.getVmStructs()) {
                    if(vmStruct.getVmPassword() != null) {
                        TerminateVm terminate = new TerminateVm();
                        terminate.setTo(vmStruct.getFullJid());
                        terminate.setVmPassword(vmStruct.getVmPassword());
                        this.connection.sendPacket(terminate);
                    }
                }
            }
        }
        this.userStructs.remove(jid);

    }
}
