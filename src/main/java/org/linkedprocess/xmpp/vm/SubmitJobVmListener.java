package gov.lanl.cnls.linkedprocess.xmpp.vm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.Job;
import gov.lanl.cnls.linkedprocess.os.errors.JobAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerIsFullException;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 2:32:50 PM
 */
public class SubmitJobVmListener implements PacketListener {

    private XmppVirtualMachine xmppVirtualMachine;

    public SubmitJobVmListener(XmppVirtualMachine xmppVirtualMachine) {
        this.xmppVirtualMachine = xmppVirtualMachine;
    }

    public void processPacket(Packet evaluate) {

        try {
            processPacketTemp(evaluate);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processPacketTemp(Packet evaluate) {
        XmppVirtualMachine.LOGGER.info("Arrived " + SubmitJobVmListener.class.getName());
        XmppVirtualMachine.LOGGER.info(evaluate.toXML());

        String expression = ((SubmitJob) evaluate).getExpression();
        String iqId = evaluate.getPacketID();
        String villeinJid = evaluate.getFrom();
        String vmPassword = ((SubmitJob)evaluate).getVmPassword();

        if(null == vmPassword || null == expression) {
            SubmitJob returnSubmitJob = new SubmitJob();
            returnSubmitJob.setTo(evaluate.getFrom());
            returnSubmitJob.setPacketID(evaluate.getPacketID());
            returnSubmitJob.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
            String errorMessage = "";
            if(null == vmPassword) {
                errorMessage = "evaluate XML packet is missing the vm_password attribute";
            }
            if(null == expression) {
                if(errorMessage.length() > 0)
                    errorMessage = errorMessage + "\n";
                errorMessage = errorMessage + "evaluate XML stanza is missing the expression text body";
            }
            if(errorMessage.length() > 0)
                returnSubmitJob.setErrorMessage(errorMessage);
            returnSubmitJob.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobVmListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } else if(!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            SubmitJob returnSubmitJob = new SubmitJob();
            returnSubmitJob.setTo(evaluate.getFrom());
            returnSubmitJob.setPacketID(evaluate.getPacketID());
            returnSubmitJob.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnSubmitJob.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

            XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobVmListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

        } else {
            Job job = new Job(xmppVirtualMachine.getFullJid(), villeinJid, iqId, expression);
            try {
                xmppVirtualMachine.scheduleJob(job);
            } catch (VMWorkerNotFoundException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(evaluate.getFrom());
                returnSubmitJob.setPacketID(evaluate.getPacketID());
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());

            } catch (VMWorkerIsFullException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(evaluate.getFrom());
                returnSubmitJob.setPacketID(evaluate.getPacketID());
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.VM_IS_BUSY);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());
            } catch (JobAlreadyExistsException e) {
                SubmitJob returnSubmitJob = new SubmitJob();
                returnSubmitJob.setTo(evaluate.getFrom());
                returnSubmitJob.setPacketID(evaluate.getPacketID());
                returnSubmitJob.setErrorType(LinkedProcess.ErrorType.JOB_ALREADY_EXISTS);
                returnSubmitJob.setErrorMessage(e.getMessage());
                returnSubmitJob.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnSubmitJob);

                XmppVirtualMachine.LOGGER.info("Sent " + SubmitJobVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnSubmitJob.toXML());
            }
        }

    }
}
