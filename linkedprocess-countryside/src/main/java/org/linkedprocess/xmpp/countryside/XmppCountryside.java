package org.linkedprocess.xmpp.countryside;

import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.io.InputStream;
import java.io.IOException;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 11:33:10 AM
 */
public class XmppCountryside extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppCountryside.class);
    public static final String RESOURCE_PREFIX = "LoPCountryside";
    public static final String STATUS_MESSAGE = "LoP Countryside v0.1";
    protected LinkedProcess.CountrysideStatus status;

    public XmppCountryside(final String server, final int port, final String username, final String password) throws XMPPException {
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
        this.connection.addPacketListener(new PresenceListener(this), presenceFilter);

        this.status = LinkedProcess.CountrysideStatus.ACTIVE;
        this.connection.sendPacket(this.createPresence(this.status));
    }

    public final Presence createPresence(final LinkedProcess.CountrysideStatus status) {
        String statusMessage = "LoP Countryside v0.1";
        switch (status) {
            case ACTIVE:
                return new Presence(Presence.Type.available, statusMessage, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
            case INACTIVE:
                return new Presence(Presence.Type.unavailable);
            default:
                throw new IllegalStateException("unhandled state: " + status);
        }
    }

    protected void initiateFeatures() {
        super.initiateFeatures();
        ServiceDiscoveryManager.setIdentityName(XmppCountryside.RESOURCE_PREFIX);
        ServiceDiscoveryManager.setIdentityType(LinkedProcess.DISCO_BOT);
        discoManager.addFeature(LinkedProcess.DISCO_ITEMS_NAMESPACE);
        discoManager.addFeature(LinkedProcess.LOP_COUNTRYSIDE_NAMESPACE);
    }
    
}
