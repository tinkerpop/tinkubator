package org.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.linkedprocess.testing.offline.MockXmppConnection;
import org.linkedprocess.vm.*;

public class MockVmXmppConnection extends MockXmppConnection {

    public PacketListener spawn, subscribe;
    public PacketListener pingJob, submitJob, terminateVm, manageBindings, abortJob;

    public MockVmXmppConnection(ConnectionConfiguration connConfig, String id, XMPPConnection vmConn) {
        super(connConfig, id, vmConn);
    }

    @Override
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        super.addPacketListener(listener, filter);
        if (listener instanceof PingJobListener) {
            pingJob = listener;
        }
        if (listener instanceof SubmitJobListener) {
            submitJob = listener;
        }
        if (listener instanceof TerminateVmListener) {
            terminateVm = listener;
        }
        if (listener instanceof ManageBindingsListener) {
            manageBindings = listener;
        }
        if (listener instanceof AbortJobListener) {
            abortJob = listener;
        }
    }

}
