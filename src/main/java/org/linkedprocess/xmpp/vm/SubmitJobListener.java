package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.errors.JobAlreadyExistsException;
import org.linkedprocess.os.errors.VMWorkerIsFullException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.linkedprocess.xmpp.ErrorIq;
import org.linkedprocess.xmpp.LopListener;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 */
public class SubmitJobListener extends LopListener {


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

            this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), submitJob.getFrom(), submitJob.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, errorMessage);


        } else if (!((XmppVirtualMachine) this.xmppClient).checkVmPassword(vmPassword)) {
            this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), submitJob.getFrom(), submitJob.getPacketID(), XMPPError.Type.AUTH, XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_VM_PASSWORD, null);
        } else {
            Job job = new Job(this.xmppClient.getFullJid(), villeinJid, iqId, expression);
            try {
                ((XmppVirtualMachine) xmppClient).scheduleJob(job);
            } catch (VMWorkerNotFoundException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), submitJob.getFrom(), submitJob.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.interna_server_error, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage());
            } catch (VMWorkerIsFullException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), submitJob.getFrom(), submitJob.getPacketID(), XMPPError.Type.WAIT, XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.VM_IS_BUSY, e.getMessage());
            } catch (JobAlreadyExistsException e) {
                this.sendErrorPacket(ErrorIq.ClientType.VM, this.xmppClient.getFullJid(), submitJob.getFrom(), submitJob.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.conflict, LinkedProcess.LopErrorType.JOB_ALREADY_EXISTS, e.getMessage());
            }
        }

    }
}
