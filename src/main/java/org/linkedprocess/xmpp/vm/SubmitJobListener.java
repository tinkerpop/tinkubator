package org.linkedprocess.xmpp.vm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.Job;
import org.linkedprocess.os.errors.JobAlreadyExistsException;
import org.linkedprocess.os.errors.VMWorkerIsFullException;
import org.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 */
public class SubmitJobListener implements PacketListener {

    private XmppVirtualMachine xmppVirtualMachine;

    public SubmitJobListener(XmppVirtualMachine xmppVirtualMachine) {
        this.xmppVirtualMachine = xmppVirtualMachine;
    }

    public void processPacket(Packet packet) {

        try {
            processSubmitJobPacket((SubmitJob)packet);
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

        if(null == vmPassword || null == expression) {
            SubmitJob returnSubmitJob = new SubmitJob();
            returnSubmitJob.setTo(villeinJid);
            returnSubmitJob.setPacketID(iqId);
            returnSubmitJob.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
            String errorMessage = "";
            if(null == vmPassword) {
                errorMessage = "submitJob XML packet is missing the vm_password attribute";
            }
            if(null == expression) {
                if(errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "submitJob XML stanza is missing the expression text body";
            }
            if(errorMessage.length() > 0)
                returnSubmitJob.setErrorMessage(errorMessage);
            returnSubmitJob.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } else if(!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            SubmitJob returnSubmitJob = new SubmitJob();
            returnSubmitJob.setTo(villeinJid);
            returnSubmitJob.setPacketID(iqId);
            returnSubmitJob.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnSubmitJob.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } else {
            Job job = new Job(xmppVirtualMachine.getFullJid(), villeinJid, iqId, expression);
            try {
                xmppVirtualMachine.scheduleJob(job);
            } catch (VMWorkerNotFoundException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(villeinJid);
                returnSubmitJob.setPacketID(iqId);
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

            } catch (VMWorkerIsFullException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(villeinJid);
                returnSubmitJob.setPacketID(iqId);
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.VM_IS_BUSY);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());
            } catch (JobAlreadyExistsException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(villeinJid);
                returnSubmitJob.setPacketID(iqId);
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.JOB_ALREADY_EXISTS);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());
            }
        }

    }
}
