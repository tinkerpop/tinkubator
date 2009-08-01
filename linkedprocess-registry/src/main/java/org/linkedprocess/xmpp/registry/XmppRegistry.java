package org.linkedprocess.xmpp.registry;

import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 11:33:10 AM
 */
public class XmppRegistry extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppRegistry.class);
    public static final String RESOURCE_PREFIX = "LoPRegistry";
    public static final String STATUS_MESSAGE = "LoP Registry v0.1";
    protected LinkedProcess.CountrysideStatus status;
    protected Set<String> countrysides = new HashSet<String>();

    public XmppRegistry(final String server, final int port, final String username, final String password) throws XMPPException {

        InputStream resourceAsStream = getClass().getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Starting " + STATUS_MESSAGE);


        this.logon(server, port, username, password, RESOURCE_PREFIX);
        this.initiateFeatures();
        //this.printClientStatistics();

        this.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
        PacketFilter discoInfoFilter = new PacketTypeFilter(DiscoverItems.class);
        this.connection.addPacketListener(new PresenceListener(this), presenceFilter);
        this.connection.addPacketListener(new DiscoItemsListener(this),  discoInfoFilter);

        this.status = LinkedProcess.CountrysideStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status));

    }

    public final Presence createPresence(final LinkedProcess.CountrysideStatus status) {
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, XmppRegistry.STATUS_MESSAGE, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case INACTIVE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppRegistry.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        this.getDiscoManager().addFeature(LinkedProcess.DISCO_ITEMS_NAMESPACE);
        this.getDiscoManager().addFeature(LinkedProcess.LOP_REGISTRY_NAMESPACE);
    }

    public void addCountryside(String jid) {
        this.countrysides.add(jid);
    }

    public void removeCountryside(String jid) {
        this.countrysides.remove(jid);
    }

    public DiscoverItems createDiscoItems(String toJid) {
        DiscoverItems items = new DiscoverItems();
        items.setType(IQ.Type.RESULT);
        items.setFrom(this.getFullJid());
        items.setTo(toJid);
        for(String item : this.countrysides) {
            items.addItem(new DiscoverItems.Item(item));
        }
        return items;
    }

    public static void main(final String[] args) throws Exception {
        Properties props = LinkedProcess.getConfiguration();
        String server = props.getProperty(LinkedProcess.REGISTRY_SERVER);
        int port = Integer.valueOf(props.getProperty(LinkedProcess.REGISTRY_PORT));
        String username = props.getProperty(LinkedProcess.REGISTRY_USERNAME);
        String password = props.getProperty(LinkedProcess.REGISTRY_PASSWORD);

        XmppRegistry xmppRegistry = new XmppRegistry(server, port, username, password);

        Object o = "";
        try {
            synchronized (o) {
                // Never break out until the process is killed.
                while (true) {
                    o.wait();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
}
