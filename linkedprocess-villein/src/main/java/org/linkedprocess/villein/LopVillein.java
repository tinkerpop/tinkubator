/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopClient;
import org.linkedprocess.vm.*;
import org.linkedprocess.farm.SpawnVmProvider;
import org.linkedprocess.villein.Dispatcher;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.Cloud;
import org.linkedprocess.villein.proxies.VmProxy;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * LopVillein is the primary class used when creating an LoP villein. An LoP villein is an XMPP client that is identified by a fully-qualified JID.
 * The bare JID of a villein is the villein's countryside. A villein is able to leverage the resources of an LoP cloud and thus,
 * can utilize the resources on other countrysides. For anyone wishing to make use of the computing resource of an LoP cloud, a villein is the
 * means by which this is done.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LopVillein extends LopClient {

    public static Logger LOGGER = LinkedProcess.getLogger(LopVillein.class);
    public static final String RESOURCE_PREFIX = "LoPVillein";
    public static final String STATUS_MESSAGE = "LoPSideD Villein";
    protected LinkedProcess.VilleinStatus status;
    protected Dispatcher dispatcher;

    protected Set<PresenceHandler> presenceHandlers = new HashSet<PresenceHandler>();
    protected Cloud cloud = new Cloud();

    /**
     * Creates a new LoP villein.
     *
     * @param server   the XMPP server to log into
     * @param port     the port that the XMPP server is listening on
     * @param username the username to log into the XMPP server with
     * @param password the password to use to log into the XMPP server with
     * @throws XMPPException is thrown when some communication error occurs with the XMPP server
     */
    public LopVillein(final String server, final int port, final String username, final String password) throws XMPPException {
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

        this.connection.addPacketListener(new PresencePacketListener(this), presenceFilter);
        this.connection.addPacketListener(new VilleinPacketListener(this), lopFilter);
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status));
    }

    public final Presence createPresence(final LinkedProcess.VilleinStatus status) {
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, LopVillein.STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
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

    /**
     * An LoP cloud is the primary data structure which contains methods for accessing all other resources in a cloud.
     *
     * @return an LoP cloud data structure
     */
    public Cloud getLopCloud() {
        return this.cloud;
    }

    /**
     * An XMPP roster maintains a collection of subscriptions to bare JIDs (i.e. countrysides).
     * This collection of countrysides may contain farms and other LoP-based resources.
     */
    public void createLopCloudFromRoster() {
        for (RosterEntry entry : this.getRoster().getEntries()) {
            CountrysideProxy countrysideProxy = this.cloud.getCountrysideProxy(entry.getUser());
            if (countrysideProxy == null && (entry.getType() == RosterPacket.ItemType.to || entry.getType() == RosterPacket.ItemType.both)) {
                countrysideProxy = new CountrysideProxy(entry.getUser());
                this.cloud.addCountrysideProxy(countrysideProxy);
            }
        }
    }

    /**
     * When a unsubscription is requested, all virtual machines that this villein has access to on the countryside are terminatd.
     *
     * @param bareJid the countryside JID to unsubscribe from
     */
    public void requestUnsubscription(String bareJid) {
        super.requestUnsubscription(bareJid);
        CountrysideProxy countrysideProxy = this.cloud.getCountrysideProxy(bareJid);
        if (countrysideProxy != null) {
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    if (vmProxy.getVmPassword() != null) {
                        vmProxy.terminateVm(null, null);
                    }
                }
            }
        }
        this.cloud.removeCountrysideProxy(bareJid);
    }

    /**
     * Adds the identity name and type of the Villein to its disco#info document.
     */
    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(LopVillein.RESOURCE_PREFIX);
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
