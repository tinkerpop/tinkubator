package org.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.testing.offline.MockXmppConnection;
import org.linkedprocess.farm.PresenceSubscriptionPacketListener;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.farm.SpawnVmPacketListener;
import org.xmlpull.v1.XmlPullParserException;

public class MockFarmXmppConnection extends MockXmppConnection {

    public PacketListener spawn, subscribe;

    public MockFarmXmppConnection(ConnectionConfiguration connConfig,
                                  String id, XMPPConnection connection) {
        super(connConfig, id, connection);
    }

    @Override
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        super.addPacketListener(listener, filter);
        if (listener instanceof SpawnVmPacketListener) {
            spawn = listener;
        }
        if (listener instanceof PresenceSubscriptionPacketListener) {
            subscribe = listener;
        }
    }

    public void receiveSpawn(SpawnVm spawnPacket) throws Exception,
            XmlPullParserException {
        spawn.processPacket(spawnPacket);
    }

    public void receiveSubscribe(Presence presencePacket) {
        subscribe.processPacket(presencePacket);
    }

}
