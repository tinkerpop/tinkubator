package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.xmpp.villein.handlers.ManageBindingsHandler;
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


    public void processManageBindingsPacket(ManageBindings manageBindings) {

        XmppVillein.LOGGER.info("Arrived " + ManageBindingsListener.class.getName());
        XmppVillein.LOGGER.info(manageBindings.toXML());

        if (manageBindings.getType() == IQ.Type.RESULT) {
            VmStruct vmStruct = (VmStruct) this.getXmppVillein().getStruct(manageBindings.getFrom(), XmppVillein.StructType.VM);
            if (vmStruct != null) {
                vmStruct.setBindings(manageBindings.getBindings());
                for (ManageBindingsHandler manageBindingsHandler : this.getXmppVillein().getManageBindingsHandlers()) {
                    manageBindingsHandler.handleManageBindingsResult(vmStruct, manageBindings.getBindings());
                }
            } else {
                XmppVillein.LOGGER.severe("Bindings returned from unknown virtual machine: " + manageBindings.getFrom());
            }
        } else {
            XmppVillein.LOGGER.severe("Error: " + manageBindings.getError().toXML());
        }
    }


}
