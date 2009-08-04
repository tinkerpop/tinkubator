package org.linkedprocess.xmpp.villein;

import java.util.ArrayList;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.vm.ManageBindings;

/**
 * User: marko
 * Date: Jul 28, 2009
 * Time: 1:01:30 PM
 */
public class ManageBindingsListener extends LopVilleinListener {



	public ManageBindingsListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        try {
            processManageBindingsPacket((ManageBindings) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processManageBindingsPacket(ManageBindings bindingsJob) {

        XmppVillein.LOGGER.info("Arrived " + ManageBindingsListener.class.getName());
        XmppVillein.LOGGER.info(bindingsJob.toXML());

        if (bindingsJob.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(bindingsJob.getFrom(), XmppVillein.StructType.VM);
            if(vmStruct != null) {
                vmStruct.setBindings(bindingsJob.getBindings());
            } else {
                XmppVillein.LOGGER.severe("Bindings returned from unknown virtual machine: " + bindingsJob.getFrom());
            }
        } else {
            XmppVillein.LOGGER.severe("Error: " + bindingsJob.getError().toXML());
        }
    }
    
    
}
