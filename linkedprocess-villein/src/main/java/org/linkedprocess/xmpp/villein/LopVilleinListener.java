package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopListener;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.vm.*;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 12:13:39 PM
 */
public class LopVilleinListener extends LopListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VILLEIN;

    public LopVilleinListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public XmppVillein getXmppVillein() {
        return (XmppVillein) this.xmppClient;
    }

    public void processPacket(Packet packet) {

        XmppVillein.LOGGER.info("Arrived " + packet.getClass().getName());
        XmppVillein.LOGGER.info(packet.toXML());

        if (packet instanceof SpawnVm) {
            SpawnVm spawnVm = (SpawnVm) packet;
            if (spawnVm.getType() == IQ.Type.RESULT) {
                this.getXmppVillein().getDispatcher().getSpawnVmCommand().receiveNormal(spawnVm);
            } else if (spawnVm.getType() == IQ.Type.ERROR) {
                this.getXmppVillein().getDispatcher().getSpawnVmCommand().receiveError(spawnVm);
            }
        } else if (packet instanceof SubmitJob) {
            SubmitJob submitJob = (SubmitJob) packet;
            if (submitJob.getType() == IQ.Type.RESULT) {
                this.getXmppVillein().getDispatcher().getSubmitJobCommand().receiveNormal(submitJob);
            } else if (submitJob.getType() == IQ.Type.ERROR) {
                this.getXmppVillein().getDispatcher().getSubmitJobCommand().receiveError(submitJob);
            }
        } else if (packet instanceof PingJob) {
            PingJob pingJob = (PingJob) packet;
            if (pingJob.getType() == IQ.Type.RESULT) {
                this.getXmppVillein().getDispatcher().getPingJobCommand().receiveNormal(pingJob);
            } else if (pingJob.getType() == IQ.Type.ERROR) {
                this.getXmppVillein().getDispatcher().getPingJobCommand().receiveError(pingJob);
            }
        } else if (packet instanceof AbortJob) {
            AbortJob abortJob = (AbortJob) packet;
            if (abortJob.getType() == IQ.Type.RESULT) {
                this.getXmppVillein().getDispatcher().getAbortJobCommand().receiveNormal(abortJob);
            } else if (abortJob.getType() == IQ.Type.ERROR) {
                this.getXmppVillein().getDispatcher().getAbortJobCommand().receiveError(abortJob);
            }
        } else if (packet instanceof ManageBindings) {
            ManageBindings manageBindings = (ManageBindings) packet;
            // TODO: NOT GUARENTEED SET/GET DETERMINANT
            if (null == manageBindings.getBindings()) {
                if (manageBindings.getType() == IQ.Type.RESULT) {
                    this.getXmppVillein().getDispatcher().getSetBindingsCommand().receiveNormal(manageBindings);
                } else if (manageBindings.getType() == IQ.Type.ERROR) {
                    this.getXmppVillein().getDispatcher().getSetBindingsCommand().receiveError(manageBindings);
                }
            } else {
                if (manageBindings.getType() == IQ.Type.RESULT) {
                    this.getXmppVillein().getDispatcher().getGetBindingsCommand().receiveNormal(manageBindings);
                } else if (manageBindings.getType() == IQ.Type.ERROR) {
                    this.getXmppVillein().getDispatcher().getGetBindingsCommand().receiveError(manageBindings);
                }
            }
        } else if (packet instanceof TerminateVm) {
            TerminateVm terminateVm = (TerminateVm) packet;
            if (terminateVm.getType() == IQ.Type.RESULT) {
                this.getXmppVillein().getDispatcher().getTerminateVmCommand().receiveNormal(terminateVm);
            } else if (terminateVm.getType() == IQ.Type.ERROR) {
                this.getXmppVillein().getDispatcher().getTerminateVmCommand().receiveError(terminateVm);
            }
        }
    }
}

