package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.errors.JobAlreadyExistsException;
import org.linkedprocess.os.errors.VMWorkerIsFullException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.LopXmppError;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 */
public class SubmitJobListener extends LopVmListener {


    public SubmitJobListener(XmppVirtualMachine xmppVirtualMachine) {
        super(xmppVirtualMachine);
    }

    public void processPacket(Packet packet) {

        try {
            processSubmitJobPacket((SubmitJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processSubmitJobPacket(SubmitJob submitJob) {
        XmppVirtualMachine.LOGGER.info("Arrived " + SubmitJobListener.class.getName());
        XmppVirtualMachine.LOGGER.info(submitJob.toXML());

        String expression = submitJob.getExpression();
        String iqId = submitJob.getPacketID();
        String villeinJid = submitJob.getFrom();
        String vmPassword = submitJob.getVmPassword();

        SubmitJob returnSubmitJob = new SubmitJob();
        returnSubmitJob.setPacketID(submitJob.getPacketID());
        returnSubmitJob.setFrom(this.getXmppVm().getFullJid());
        returnSubmitJob.setTo(submitJob.getFrom());

        if (null == vmPassword || null == expression) {
            String errorMessage = "";
            if (null == vmPassword) {
                errorMessage = "submitJob XML packet is missing the vm_password attribute";
            }
            if (null == expression) {
                if (errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "submitJob XML stanza is missing the expression text body";
            }
            if (errorMessage.length() == 0)
                errorMessage = null;

            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage, LOP_CLIENT_TYPE));


        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            returnSubmitJob.setType(IQ.Type.ERROR);
            returnSubmitJob.setError(new LopXmppError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null, LOP_CLIENT_TYPE));
        } else {
            Job job = new Job(this.xmppClient.getFullJid(), villeinJid, iqId, expression);
            try {
                ((XmppVirtualMachine) xmppClient).scheduleJob(job);
                submitJob = null;
            } catch (VMWorkerNotFoundException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setError(new LopXmppError(XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE));
            } catch (VMWorkerIsFullException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setError(new LopXmppError(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.VM_IS_BUSY, e.getMessage(), LOP_CLIENT_TYPE));
            } catch (JobAlreadyExistsException e) {
                returnSubmitJob.setType(IQ.Type.ERROR);
                returnSubmitJob.setError(new LopXmppError(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.JOB_ALREADY_EXISTS, e.getMessage(), LOP_CLIENT_TYPE));
            }
        }

        if (submitJob != null) {
            XmppVirtualMachine.LOGGER.fine("Sent " + JobStatusListener.class.getName());
            XmppVirtualMachine.LOGGER.fine(returnSubmitJob.toXML());
            this.getXmppVm().getConnection().sendPacket(returnSubmitJob);
        }

    }
}
