package gov.lanl.cnls.linkedprocess.gui.farm;

import gov.lanl.cnls.linkedprocess.xmpp.farm.StatusEventHandler;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

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
        gui.updateVirtualMachineTree();

    }
}
