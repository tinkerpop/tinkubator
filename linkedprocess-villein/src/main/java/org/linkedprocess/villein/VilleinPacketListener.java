/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LopPacketListener;
import org.linkedprocess.vm.*;
import org.linkedprocess.farm.SpawnVm;

/**
 * This class is repsonsible for dynamically adjusting the LoP cloud and all of its underlying resource proxies.
 * As incomming packets are processed by the XMPP villein, respective proxy data structures are updated.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
class VilleinPacketListener extends LopPacketListener {

    public static final LinkedProcess.ClientType LOP_CLIENT_TYPE = LinkedProcess.ClientType.VILLEIN;

    public VilleinPacketListener(LopVillein lopVillein) {
        super(lopVillein);
    }

    public LopVillein getLopVillein() {
        return (LopVillein) this.lopClient;
    }

    public void processPacket(Packet packet) {

        LopVillein.LOGGER.info("Arrived " + packet.getClass().getName());
        LopVillein.LOGGER.info(packet.toXML());

        if (packet instanceof SpawnVm) {
            SpawnVm spawnVm = (SpawnVm) packet;
            if (spawnVm.getType() == IQ.Type.RESULT) {
                this.getLopVillein().getDispatcher().getSpawnVmCommand().receiveSuccess(spawnVm);
            } else if (spawnVm.getType() == IQ.Type.ERROR) {
                this.getLopVillein().getDispatcher().getSpawnVmCommand().receiveError(spawnVm);
            }
        } else if (packet instanceof SubmitJob) {
            SubmitJob submitJob = (SubmitJob) packet;
            if (submitJob.getType() == IQ.Type.RESULT) {
                this.getLopVillein().getDispatcher().getSubmitJobCommand().receiveSuccess(submitJob);
            } else if (submitJob.getType() == IQ.Type.ERROR) {
                this.getLopVillein().getDispatcher().getSubmitJobCommand().receiveError(submitJob);
            }
        } else if (packet instanceof PingJob) {
            PingJob pingJob = (PingJob) packet;
            if (pingJob.getType() == IQ.Type.RESULT) {
                this.getLopVillein().getDispatcher().getPingJobCommand().receiveSuccess(pingJob);
            } else if (pingJob.getType() == IQ.Type.ERROR) {
                this.getLopVillein().getDispatcher().getPingJobCommand().receiveError(pingJob);
            }
        } else if (packet instanceof AbortJob) {
            AbortJob abortJob = (AbortJob) packet;
            if (abortJob.getType() == IQ.Type.RESULT) {
                this.getLopVillein().getDispatcher().getAbortJobCommand().receiveSuccess(abortJob);
            } else if (abortJob.getType() == IQ.Type.ERROR) {
                this.getLopVillein().getDispatcher().getAbortJobCommand().receiveError(abortJob);
            }
        } else if (packet instanceof ManageBindings) {
            ManageBindings manageBindings = (ManageBindings) packet;
            // TODO: NOT GUARENTEED SET/GET DETERMINANT
            if (manageBindings.getBindings().isEmpty()) {
                if (manageBindings.getType() == IQ.Type.RESULT) {
                    this.getLopVillein().getDispatcher().getSetBindingsCommand().receiveSuccess(manageBindings);
                } else if (manageBindings.getType() == IQ.Type.ERROR) {
                    this.getLopVillein().getDispatcher().getSetBindingsCommand().receiveError(manageBindings);
                }
            } else {
                if (manageBindings.getType() == IQ.Type.RESULT) {
                    this.getLopVillein().getDispatcher().getGetBindingsCommand().receiveSuccess(manageBindings);
                } else if (manageBindings.getType() == IQ.Type.ERROR) {
                    this.getLopVillein().getDispatcher().getGetBindingsCommand().receiveError(manageBindings);
                }
            }
        } else if (packet instanceof TerminateVm) {
            TerminateVm terminateVm = (TerminateVm) packet;
            if (terminateVm.getType() == IQ.Type.RESULT) {
                this.getLopVillein().getDispatcher().getTerminateVmCommand().receiveSuccess(terminateVm);
            } else if (terminateVm.getType() == IQ.Type.ERROR) {
                this.getLopVillein().getDispatcher().getTerminateVmCommand().receiveError(terminateVm);
            }
        }
    }
}

