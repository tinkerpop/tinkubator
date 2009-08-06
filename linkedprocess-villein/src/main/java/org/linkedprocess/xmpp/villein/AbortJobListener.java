package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.villein.handlers.AbortJobHandler;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.vm.AbortJob;

/**
 * User: marko
 * Date: Aug 4, 2009
 * Time: 11:49:45 PM
 */
public class AbortJobListener extends LopVilleinListener {

    public AbortJobListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        try {
            processAbortJobPacket((AbortJob) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processAbortJobPacket(AbortJob abortJob) {

        XmppVillein.LOGGER.info("Arrived " + AbortJobListener.class.getName());
        XmppVillein.LOGGER.info(abortJob.toXML());

        VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(abortJob.getFrom(), XmppVillein.StructType.VM);
        if (vmStruct != null) {
            if (abortJob.getType() == IQ.Type.RESULT) {
                // Handlers
                for (AbortJobHandler abortJobHandler : this.getXmppVillein().getAbortJobHandlers()) {
                    abortJobHandler.handleAbortJobResult(vmStruct, abortJob.getJobId());
                }
            }
        } else {
            XmppVillein.LOGGER.warning("Job returned from unknown virtual machine: " + abortJob.getFrom());
        }


    }

}
