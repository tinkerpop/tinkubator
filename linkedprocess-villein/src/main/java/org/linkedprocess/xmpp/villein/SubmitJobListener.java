package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.villein.handlers.SubmitJobHandler;
import org.linkedprocess.xmpp.vm.SubmitJob;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 1:01:30 PM
 */
public class SubmitJobListener extends LopVilleinListener {

    public SubmitJobListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        try {
            processSubmitJobPacket((SubmitJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processSubmitJobPacket(SubmitJob submitJob) {

        XmppVillein.LOGGER.info("Arrived " + SubmitJobListener.class.getName());
        XmppVillein.LOGGER.info(submitJob.toXML());

        if (submitJob.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(submitJob.getFrom(), XmppVillein.StructType.VM);
            if (vmStruct != null) {
                Job job = new Job();
                job.setResult(submitJob.getExpression());
                job.setJobId(submitJob.getPacketID());
                vmStruct.addJob(job);
                for (SubmitJobHandler submitJobHandler : this.getXmppVillein().getSubmitJobHandlers()) {
                    submitJobHandler.handleSubmitJob(vmStruct, job);
                }
            } else {
                XmppVillein.LOGGER.severe("Job returned from unknown virtual machine: " + submitJob.getFrom());
            }
        } else if (submitJob.getType() == IQ.Type.ERROR) {
            VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(submitJob.getFrom(), XmppVillein.StructType.VM);
            if (vmStruct != null) {
                Job job = new Job();
                job.setError(submitJob.getError());
                job.setJobId(submitJob.getPacketID());
                vmStruct.addJob(job);
                for (SubmitJobHandler submitJobHandler : this.getXmppVillein().getSubmitJobHandlers()) {
                    submitJobHandler.handleSubmitJob(vmStruct, job);
                }

            } else {
                XmppVillein.LOGGER.severe("Job returned from unknown virtual machine: " + submitJob.getFrom());
            }
        } else {
            XmppVillein.LOGGER.severe("Error: " + submitJob.toXML());
        }
    }
}
