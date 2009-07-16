package org.linkedprocess.gui.farm;

import org.linkedprocess.xmpp.farm.StatusEventHandler;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 4, 2009
 * Time: 5:44:41 PM
 */
public class FarmGuiStatusEventHandler extends StatusEventHandler {

    protected FarmGui farmGui;


    public FarmGuiStatusEventHandler(FarmGui farmGui) {
        super(farmGui.getXmppFarm());
        this.farmGui = farmGui;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        super.schedulerStatusChanged(status);
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VmStatus status) {
        super.virtualMachineStatusChanged(vmJid, status);
        farmGui.updateVirtualMachineTree(vmJid, status);

    }
}
