package org.linkedprocess;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

public interface Connection {

    XMPPConnection getDelegate();

    boolean isConnected();

    void connect() throws XMPPException;

    void addPacketListener(PacketListener listener, PacketFilter filter);

    void addPacketWriterInterceptor(PacketInterceptor interceptor, PacketFilter filter);

    void sendPacket(Packet packet);

    Roster getRoster();

    boolean isAnonymous();

    boolean isAuthenticated();

    boolean isSecureConnection();

    boolean isUsingCompression();

    boolean isUsingTLS();

    String getHost();

    void disconnect(Presence presence);

    void disconnect();

    String getUser();

    void login(String username, String password, String string) throws XMPPException;

}
