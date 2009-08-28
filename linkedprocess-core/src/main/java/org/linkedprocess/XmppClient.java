/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public abstract class XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppClient.class);
    protected Connection connection;
    protected Roster roster;
    protected boolean shutdownRequested = false;

    protected long startTime;
    private String username;
    private String password;
    private String server;
    private int port;

    protected void logon(final String server, final int port, final String username, final String password, String resource) throws LopXmppException {

        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.startTime = System.currentTimeMillis();

        // if connection is still active, disconnect it.
        if (null != connection && connection.isConnected()) {
            this.logout(null);
        }

        ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port);
        connConfig.setRosterLoadedAtLogin(false);
        connConfig.setSendPresence(false);
        //connConfig.setSASLAuthenticationEnabled(false);

        try {
            this.connection = new XmppConnectionWrapper(connConfig);
            this.connection.connect();
            LOGGER.info("Connected to " + connection.getHost());
            connection.login(username, password, resource + LinkedProcess.FORWARD_SLASH + Jid.generateRandomResourceId());
            LOGGER.info("Logged in as " + connection.getUser());
        } catch (XMPPException e) {
            throw new LopXmppException(e.getMessage());
        }

        Thread shutdownHook = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!shutdownRequested) {
                        Thread.sleep(250);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.info("Shutting down");
                //we should log out here but then the calling method would not block
                //logout();
            }
        }, "Shutdown hook");
        shutdownHook.start();

        this.roster = this.connection.getRoster();
    }

    protected void logout(Presence logoutPresence) {
        LOGGER.info("Disconnecting from " + connection.getHost());
        if (logoutPresence == null)
            connection.disconnect();
        else
            connection.disconnect(logoutPresence);
    }

    public void printClientStatistics() {
        // print a collection of statistics about the connection
        LOGGER.info("Anonymous: " + connection.isAnonymous());
        LOGGER.info("Authenticated: " + connection.isAuthenticated());
        LOGGER.info("Connected: " + connection.isConnected());
        LOGGER.info("Secure: " + connection.isSecureConnection());
        LOGGER.info("Compression: " + connection.isUsingCompression());
        LOGGER.info("Transport Layer Security: " + connection.isUsingTLS());
    }

    public LinkedProcess.Status getStatus() {
        return null;
    }

    public void sendPresence(final LinkedProcess.Status status, String statusMessage) {
        Presence presence;
        if (status == LinkedProcess.Status.ACTIVE) {
            presence = new Presence(Presence.Type.available, statusMessage, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.available);
        } else if (status == LinkedProcess.Status.BUSY) {
            presence = new Presence(Presence.Type.available, statusMessage, LinkedProcess.HIGHEST_PRIORITY, Presence.Mode.dnd);
        } else if (status == LinkedProcess.Status.INACTIVE) {
            presence = new Presence(Presence.Type.unavailable);
        } else {
            throw new IllegalStateException("unhandled state: " + status);
        }
        presence.setFrom(this.getJid().toString());
        this.connection.sendPacket(presence);
    }

    public Jid getJid() {
        return new Jid(this.connection.getUser());
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void shutdown() {
        LOGGER.info("Requesting shutdown");
        shutdownRequested = true;
        Presence presence = new Presence(Presence.Type.unavailable);
        logout(presence);
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getPort() {
        return this.port;
    }

    public String getServer() {
        return this.server;
    }

    public Roster getRoster() {
        return this.roster;
    }

    public long getRunningTime() {
        return System.currentTimeMillis() - this.startTime;
    }

    public float getRunningTimeInSeconds() {
        return this.getRunningTime() / 1000.0f;
    }

    public float getRunningTimeInMinutes() {
        return this.getRunningTime() / 6000.0f;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public String getStartTimeAsXsdDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return formatter.format(this.getStartTimeAsDate());
    }

    public java.util.Date getStartTimeAsDate() {
        return new java.util.Date(this.startTime);
    }

    public ServiceDiscoveryManager getDiscoManager() {
        return ServiceDiscoveryManager.getInstanceFor(this.connection.getDelegate());
    }

    protected void initiateFeatures() {
        Iterator<String> features = this.getDiscoManager().getFeatures();
        while (features.hasNext()) {
            String feature = features.next();
            this.getDiscoManager().removeFeature(feature);
        }
        this.getDiscoManager().addFeature(LinkedProcess.DISCO_INFO_NAMESPACE);
    }

    public void requestSubscription(Jid jid) {
        try {
            this.roster.createEntry(jid.toString(), null, null);
        } catch (XMPPException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public void requestUnsubscription(Jid jid) {
        try {
            this.roster.removeEntry(this.roster.getEntry(jid.getBareJid().toString()));
        } catch (XMPPException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public void probeJid(Jid jid) {
        ProbePresence probe = new ProbePresence();
        probe.setFrom(this.getJid().toString());
        probe.setTo(jid.toString());
        this.connection.sendPacket(probe);
    }

}
