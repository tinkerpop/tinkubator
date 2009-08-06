package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.villein.handlers.SubmitJobHandler;
import org.linkedprocess.xmpp.villein.structs.CompletedJob;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
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

        VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(submitJob.getFrom(), XmppVillein.StructType.VM);
        if (vmStruct != null) {
            CompletedJob completedJob = new CompletedJob();
            if (submitJob.getType() == IQ.Type.RESULT)
                completedJob.setResult(submitJob.getExpression());
            else if (submitJob.getType() == IQ.Type.ERROR)
                completedJob.setError(submitJob.getError());
            completedJob.setJobId(submitJob.getPacketID());
            vmStruct.addJob(completedJob);
            // Handlers
            for (SubmitJobHandler submitJobHandler : this.getXmppVillein().getSubmitJobHandlers()) {
                submitJobHandler.handleSubmitJob(vmStruct, completedJob);
            }
        } else {
            XmppVillein.LOGGER.warning("Job returned from unknown virtual machine: " + submitJob.getFrom());
        }

    }
}
