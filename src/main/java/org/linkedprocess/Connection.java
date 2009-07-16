package org.linkedprocess;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

public interface Connection {

	XMPPConnection getDelegate();

	boolean isConnected();

	void connect() throws XMPPException;

	void addPacketListener(PacketListener listener,
			PacketFilter filter);

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
