/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.Connection;

public class XmppConnectionWrapper implements Connection {

    private XMPPConnection delegate;

    public XmppConnectionWrapper(ConnectionConfiguration connConfig) {
        delegate = new XMPPConnection(connConfig);
    }

    protected XmppConnectionWrapper() {

    }

    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        delegate.addPacketListener(listener, filter);

    }

    public void addPacketWriterInterceptor(PacketInterceptor interceptor, PacketFilter filter) {
        delegate.addPacketWriterInterceptor(interceptor, filter);
    }

    public PacketCollector createPacketCollector(PacketFilter filter) {
        return delegate.createPacketCollector(filter);
    }

    public void connect() throws XMPPException {
        delegate.connect();
    }

    public void disconnect(Presence presence) {
        delegate.disconnect(presence);
    }

    public void disconnect() {
        delegate.disconnect();

    }

    public XMPPConnection getDelegate() {
        return delegate;
    }

    public String getHost() {
        return delegate.getHost();
    }

    public Roster getRoster() {
        return delegate.getRoster();
    }

    public String getUser() {
        return delegate.getUser();
    }

    public boolean isAnonymous() {
        return delegate.isAnonymous();
    }

    public boolean isAuthenticated() {
        return delegate.isAuthenticated();
    }

    public boolean isConnected() {
        return delegate.isConnected();
    }

    public boolean isSecureConnection() {
        return delegate.isSecureConnection();
    }

    public boolean isUsingCompression() {
        return delegate.isUsingCompression();
    }

    public boolean isUsingTLS() {
        return delegate.isUsingTLS();
    }

    public void login(String username, String password, String resource) throws XMPPException {
        delegate.login(username, password, resource);
    }

    public void sendPacket(Packet packet) {
        delegate.sendPacket(packet);
    }

}
