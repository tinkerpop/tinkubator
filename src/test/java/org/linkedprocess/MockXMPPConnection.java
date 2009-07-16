package gov.lanl.cnls.linkedprocess;

import java.util.ArrayList;
import java.util.logging.Logger;

import gov.lanl.cnls.linkedprocess.xmpp.XMPPConnectionWrapper;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;


public class MockXMPPConnection extends XMPPConnectionWrapper{

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
	public MockXMPPConnection(ConnectionConfiguration connConfig, String id) {
		this.id = id;
		logger.fine(id);
		host=connConfig.getHost();
		port=connConfig.getPort();
		sentPackets = new ArrayList<Packet>();
	}

	@Override
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		logger.info("registering " + listener);
		packetListeners .add(listener);
		
	}

	@Override
	public void connect() throws XMPPException {
		logger.fine("connecting");
		
	}

	@Override
	public void disconnect(Presence presence) {
		
	}

	@Override
	public void disconnect() {
		
	}

	@Override
	public XMPPConnection getDelegate() {
		logger.fine(id +"returning" + xmppConnection);
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
		return username+LinkedProcess.FORWARD_SLASH+resource;
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
		sentPackets.add(packet);
		logger.fine(id + ": adding " + packet.toXML());
		
	}

	public void setRoster(Roster mockRoster) {
		this.mockRoster = mockRoster;
		
	}

	public void clearPackets() {
		sentPackets.clear();
		logger.fine("clearing packets");
	}


}
