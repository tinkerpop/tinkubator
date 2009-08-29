package org.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.linkedprocess.farm.*;
import org.linkedprocess.testing.offline.MockXmppConnection;

public class MockVmXmppConnection extends MockXmppConnection {

    public PacketListener spawn, subscribe;
    public PacketListener pingJob, submitJob, terminateVm, manageBindings, abortJob;

    public MockVmXmppConnection(ConnectionConfiguration connConfig, String id, XMPPConnection vmConn) {
        super(connConfig, id, vmConn);
    }

    @Override
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        super.addPacketListener(listener, filter);
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

}
