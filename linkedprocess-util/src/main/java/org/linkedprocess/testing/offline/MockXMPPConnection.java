package org.linkedprocess.testing.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

import org.easymock.Capture;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DataForm;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.XMPPConnectionWrapper;

public class MockXMPPConnection extends XMPPConnectionWrapper {

	Logger logger = LinkedProcess.getLogger(this.getClass());
	private String host;
	private int port;
	private String username;
	private String password;
	private String resource;
	public ArrayList<Packet> sentPackets;
	private Roster mockRoster;
	public ArrayList<PacketListener> packetListeners = new ArrayList<PacketListener>();
	private XMPPConnection xmppConnection;
	private final String id;
	public Vector<String> disc_info_features;
	public Capture<DataForm> dataformCapture = new Capture<DataForm>();

	public MockXMPPConnection(ConnectionConfiguration connConfig, String id,
			XMPPConnection connection) {
		this.id = id;
		setDelegate(connection);
		logger.fine(id);
		host = connConfig.getHost();
		port = connConfig.getPort();
		sentPackets = new ArrayList<Packet>();
		disc_info_features = new Vector<String>();

	}

	public void waitForResponse(int timeout) {
		int currentSize = sentPackets.size();
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + timeout) {
			if (sentPackets.size() > currentSize) {
				return;
			}
			try {
				Thread.sleep(50);
				// System.out.println(watching.size() + " > " + currentSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		logger.info(id + ": registering " + listener);

		packetListeners.add(listener);

	}

	@Override
	public void connect() throws XMPPException {
		logger.fine(id + ": connecting");

	}

	@Override
	public void disconnect(Presence presence) {

	}

	@Override
	public void disconnect() {

	}

	@Override
	public XMPPConnection getDelegate() {
		logger.fine(id + ": returning" + xmppConnection);
		return xmppConnection;
	}

	public void setDelegate(XMPPConnection connection) {
		xmppConnection = connection;

	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public Roster getRoster() {
		return mockRoster;
	}

	@Override
	public String getUser() {
		return username + LinkedProcess.FORWARD_SLASH + resource;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean isSecureConnection() {
		return false;
	}

	@Override
	public boolean isUsingCompression() {
		return false;
	}

	@Override
	public boolean isUsingTLS() {
		return false;
	}

	@Override
	public void login(String username, String password, String resource)
			throws XMPPException {
		this.username = username;
		this.password = password;
		this.resource = resource;

	}

	@Override
	public void sendPacket(Packet packet) {
		logger.info(id + ": adding " + packet.toXML());
		sentPackets.add(packet);

	}

	public void setRoster(Roster mockRoster) {
		this.mockRoster = mockRoster;

	}

	public void clearPackets() {
		sentPackets.clear();
		logger.fine(id + "clearing packets");
	}

	public Packet getLastPacket() {
		return sentPackets.get(sentPackets.size() - 1);
	}

	public Collection<String> getDiscInfoFeatures() {
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(disc_info_features);
		return result;
	}

}
