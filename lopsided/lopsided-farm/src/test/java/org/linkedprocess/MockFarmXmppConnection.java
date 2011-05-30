package org.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.farm.AbortJobPacketListener;
import org.linkedprocess.farm.ManageBindingsPacketListener;
import org.linkedprocess.farm.PingJobPacketListener;
import org.linkedprocess.farm.PresenceSubscriptionPacketListener;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.farm.SpawnVmPacketListener;
import org.linkedprocess.farm.SubmitJobPacketListener;
import org.linkedprocess.farm.TerminateVm;
import org.linkedprocess.farm.TerminateVmPacketListener;
import org.linkedprocess.testing.offline.MockXmppConnection;
import org.xmlpull.v1.XmlPullParserException;

public class MockFarmXmppConnection extends MockXmppConnection {

    public PacketListener spawn, subscribe, pingJob, submitJob, terminateVm, manageBindings, abortJob;

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
        if (listener instanceof PingJobPacketListener) {
            pingJob = listener;
        }
        if (listener instanceof SubmitJobPacketListener) {
            submitJob = listener;
        }
        if (listener instanceof TerminateVmPacketListener) {
            terminateVm = listener;
        }
        if (listener instanceof ManageBindingsPacketListener) {
            manageBindings = listener;
        }
        if (listener instanceof AbortJobPacketListener) {
            abortJob = listener;
        }
    }

    public void receiveSpawn(SpawnVm spawnPacket) throws Exception,
            XmlPullParserException {
        spawn.processPacket(spawnPacket);
    }

    public void receiveSubscribe(Presence presencePacket) {
        subscribe.processPacket(presencePacket);
    }

	public void receiveTerminate(TerminateVm terminate) {
		terminateVm.processPacket(terminate);
	}

}
