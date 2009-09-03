/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.registry;

import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.linkedprocess.Jid;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopXmppException;
import org.linkedprocess.XmppClient;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Registry extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(Registry.class);
    public static final String RESOURCE_PREFIX = "LoPRegistry";
    public static final String STATUS_MESSAGE = "LoPSideD Registry";
    protected Set<Jid> activeFarms = new HashSet<Jid>();

    public Registry(final String server, final int port, final String username, final String password) throws LopXmppException {
        LOGGER.info("Starting " + STATUS_MESSAGE);


        this.logon(server, port, username, password, RESOURCE_PREFIX);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        PacketFilter subscribeFilter = new AndFilter(new PacketTypeFilter(Presence.class), new PresenceSubscriptionFilter());
        this.connection.addPacketListener(new PresenceSubscriptionListener(this), subscribeFilter);
        this.connection.addPacketListener(new PresencePacketListener(this), presenceFilter);
        this.connection.addPacketWriterInterceptor(new DiscoItemsInterceptor(), new PacketTypeFilter(DiscoverItems.class));

        this.sendPresence(this.getStatus(), STATUS_MESSAGE);
    }

    public LinkedProcess.Status getStatus() {
        return LinkedProcess.Status.ACTIVE;
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(Registry.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        this.getDiscoManager().addFeature(LinkedProcess.DISCO_ITEMS_NAMESPACE);
        this.getDiscoManager().addFeature(LinkedProcess.LOP_REGISTRY_NAMESPACE);
    }

    public void addActiveFarm(Jid jid) {
        this.activeFarms.add(jid);
    }

    public void removeActiveFarm(Jid jid) {
        this.activeFarms.remove(jid);
    }

    private class DiscoItemsInterceptor implements PacketInterceptor {
        public void interceptPacket(Packet packet) {
            DiscoverItems items = (DiscoverItems) packet;
            Set<String> farmCountrysides = new HashSet<String>();
            for (Jid farmJid : activeFarms) {
                farmCountrysides.add(farmJid.getBareJid().toString());
            }
            for (String countrysideJid : farmCountrysides) {
                items.addItem(new DiscoverItems.Item(countrysideJid));
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        Properties props = LinkedProcess.getConfiguration();
        String server = props.getProperty(LinkedProcess.REGISTRY_SERVER_PROPERTY);
        int port = Integer.valueOf(props.getProperty(LinkedProcess.REGISTRY_PORT_PROPERTY));
        String username = props.getProperty(LinkedProcess.REGISTRY_USERNAME_PROPERTY);
        String password = props.getProperty(LinkedProcess.REGISTRY_PASSWORD_PROPERTY);

        Registry registry = new Registry(server, port, username, password);

        Object monitor = new Object();
        try {
            synchronized (monitor) {
                // Never break out until the process is killed.
                while (true) {
                    monitor.wait();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
