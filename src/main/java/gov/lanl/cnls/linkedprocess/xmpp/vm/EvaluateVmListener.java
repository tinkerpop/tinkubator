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
public class EvaluateVmListener implements PacketListener {

    private XmppVirtualMachine xmppVirtualMachine;

    public EvaluateVmListener(XmppVirtualMachine xmppVirtualMachine) {
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
        XmppVirtualMachine.LOGGER.info("Arrived " + EvaluateVmListener.class.getName());
        XmppVirtualMachine.LOGGER.info(evaluate.toXML());

        String expression = ((Evaluate) evaluate).getExpression();
        String iqId = evaluate.getPacketID();
        String villeinJid = evaluate.getFrom();
        String vmPassword = ((Evaluate)evaluate).getVmPassword();

        if(null == vmPassword || null == expression) {
            Evaluate returnEvaluate = new Evaluate();
            returnEvaluate.setTo(evaluate.getFrom());
            returnEvaluate.setPacketID(evaluate.getPacketID());
            returnEvaluate.setErrorType(LinkedProcess.ErrorType.MALFORMED_PACKET);
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
                returnEvaluate.setErrorMessage(errorMessage);
            returnEvaluate.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnEvaluate);

            XmppVirtualMachine.LOGGER.info("Sent " + EvaluateVmListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());

        } else if(!this.xmppVirtualMachine.checkVmPassword(vmPassword)) {
            Evaluate returnEvaluate = new Evaluate();
            returnEvaluate.setTo(evaluate.getFrom());
            returnEvaluate.setPacketID(evaluate.getPacketID());
            returnEvaluate.setErrorType(LinkedProcess.ErrorType.WRONG_VM_PASSWORD);
            returnEvaluate.setType(IQ.Type.ERROR);
            xmppVirtualMachine.getConnection().sendPacket(returnEvaluate);

            XmppVirtualMachine.LOGGER.info("Sent " + EvaluateVmListener.class.getName());
            XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());

        } else {
            Job job = new Job(xmppVirtualMachine.getFullJid(), villeinJid, iqId, expression);
            try {
                xmppVirtualMachine.scheduleJob(job);
            } catch (VMWorkerNotFoundException e) {
                Evaluate returnEvaluate = new Evaluate();
                returnEvaluate.setTo(evaluate.getFrom());
                returnEvaluate.setPacketID(evaluate.getPacketID());
                returnEvaluate.setErrorType(LinkedProcess.ErrorType.INTERNAL_ERROR);
                returnEvaluate.setErrorMessage(e.getMessage());
                returnEvaluate.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnEvaluate);

                XmppVirtualMachine.LOGGER.info("Sent " + EvaluateVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());

            } catch (VMWorkerIsFullException e) {
                Evaluate returnEvaluate = new Evaluate();
                returnEvaluate.setTo(evaluate.getFrom());
                returnEvaluate.setPacketID(evaluate.getPacketID());
                returnEvaluate.setErrorType(LinkedProcess.ErrorType.VM_IS_BUSY);
                returnEvaluate.setErrorMessage(e.getMessage());
                returnEvaluate.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnEvaluate);

                XmppVirtualMachine.LOGGER.info("Sent " + EvaluateVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());
            } catch (JobAlreadyExistsException e) {
                Evaluate returnEvaluate = new Evaluate();
                returnEvaluate.setTo(evaluate.getFrom());
                returnEvaluate.setPacketID(evaluate.getPacketID());
                returnEvaluate.setErrorType(LinkedProcess.ErrorType.JOB_ALREADY_EXISTS);
                returnEvaluate.setErrorMessage(e.getMessage());
                returnEvaluate.setType(IQ.Type.ERROR);
                xmppVirtualMachine.getConnection().sendPacket(returnEvaluate);

                XmppVirtualMachine.LOGGER.info("Sent " + EvaluateVmListener.class.getName());
                XmppVirtualMachine.LOGGER.info(returnEvaluate.toXML());
            }
        }

    }
}
