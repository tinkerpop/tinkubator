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

    @Override
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        delegate.addPacketListener(listener, filter);

    }

    @Override
    public void addPacketWriterInterceptor(PacketInterceptor interceptor, PacketFilter filter) {
        delegate.addPacketWriterInterceptor(interceptor, filter);
    }

    @Override
    public void connect() throws XMPPException {
        delegate.connect();
    }

    @Override
    public void disconnect(Presence presence) {
        delegate.disconnect(presence);
    }

    @Override
    public void disconnect() {
        delegate.disconnect();

    }

    @Override
    public XMPPConnection getDelegate() {
        return delegate;
    }

    @Override
    public String getHost() {
        return delegate.getHost();
    }

    @Override
    public Roster getRoster() {
        return delegate.getRoster();
    }

    @Override
    public String getUser() {
        return delegate.getUser();
    }

    @Override
    public boolean isAnonymous() {
        return delegate.isAnonymous();
    }

    @Override
    public boolean isAuthenticated() {
        return delegate.isAuthenticated();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isSecureConnection() {
        return delegate.isSecureConnection();
    }

    @Override
    public boolean isUsingCompression() {
        return delegate.isUsingCompression();
    }

    @Override
    public boolean isUsingTLS() {
        return delegate.isUsingTLS();
    }

    @Override
    public void login(String username, String password, String resource) throws XMPPException {
        delegate.login(username, password, resource);
    }

    @Override
    public void sendPacket(Packet packet) {
        delegate.sendPacket(packet);
    }

}
