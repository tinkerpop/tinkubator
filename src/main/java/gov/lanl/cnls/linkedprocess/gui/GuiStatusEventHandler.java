package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.StatusEventHandler;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;

/**
 * User: marko
 * Date: Jul 4, 2009
 * Time: 5:44:41 PM
 */
public class GuiStatusEventHandler extends StatusEventHandler {

    protected FarmGui gui;


    public GuiStatusEventHandler(FarmGui gui) {
        super(gui.getFarm());
        this.gui = gui;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        super.schedulerStatusChanged(status);
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VMStatus status) {
        super.virtualMachineStatusChanged(vmJid, status);
        gui.updateVirtualMachineList(vmJid, status);

    }
}
