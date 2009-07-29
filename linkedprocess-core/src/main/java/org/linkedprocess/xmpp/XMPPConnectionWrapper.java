package org.linkedprocess.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.Connection;

public class XMPPConnectionWrapper implements Connection {

    private XMPPConnection delegate;

    public XMPPConnectionWrapper(ConnectionConfiguration connConfig) {
        delegate = new XMPPConnection(connConfig);
    }

    protected XMPPConnectionWrapper() {

    }

    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        delegate.addPacketListener(listener, filter);

    }

    public void addPacketWriterInterceptor(PacketInterceptor interceptor, PacketFilter filter) {
        delegate.addPacketWriterInterceptor(interceptor, filter);
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
