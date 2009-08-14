package org.linkedprocess.registry;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.linkedprocess.testing.offline.MockXMPPConnection;

public class MockRegistryXmppConnection extends MockXMPPConnection {

	public MockRegistryXmppConnection(ConnectionConfiguration connConfig,
			String id, XMPPConnection connection) {
		super(connConfig, id, connection);
	}

}
