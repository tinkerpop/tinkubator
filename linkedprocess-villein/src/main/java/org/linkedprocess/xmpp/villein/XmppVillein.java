/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

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
import org.linkedprocess.xmpp.villein.proxies.CountrysideProxy;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.LopCloud;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.*;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * XmppVillein is the primary class used when creating an LoP villein. An LoP villein is an XMPP client that is identified by a fully-qualified JID.
 * The bare JID of a villein is the villein's countryside. A villein is able to leverage the resources of an LoP cloud and thus,
 * can utilize the resources on other countrysides. For anyone wishing to make use of the computing resource of an LoP cloud, a villein is the
 * means by which this is done.
 *
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
    protected LopCloud lopCloud = new LopCloud();

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
        this.status = LinkedProcess.VilleinStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status));
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

    public LopCloud getLopCloud() {
        return this.lopCloud;
    }

    public void createLopCloudFromRoster() {
        for (RosterEntry entry : this.getRoster().getEntries()) {
            CountrysideProxy countrysideProxy = this.lopCloud.getCountrysideProxy(entry.getUser());
            if (countrysideProxy == null) {
                countrysideProxy = new CountrysideProxy(entry.getUser(), this.dispatcher);
                this.lopCloud.addCountrysideProxy(countrysideProxy);
            }
        }
    }

    public void requestUnsubscription(String jid) {
        super.requestUnsubscription(jid);
        CountrysideProxy countrysideProxy = this.lopCloud.getCountrysideProxy(jid);
        if (countrysideProxy != null) {
            for (FarmProxy farmProxy : countrysideProxy.getFarmProxies()) {
                for (VmProxy vmProxy : farmProxy.getVmProxies()) {
                    if (vmProxy.getVmPassword() != null) {
                        vmProxy.terminateVm(null, null);
                    }
                }
            }
        }
        this.lopCloud.removeCountrysideProxy(jid);
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
