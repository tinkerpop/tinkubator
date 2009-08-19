package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.vm.XmppVm;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 3:12:44 PM
 */
public class StatusEventHandler implements VmScheduler.LopStatusEventHandler {

    protected XmppFarm xmppFarm;

    public StatusEventHandler(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        this.xmppFarm.getConnection().sendPacket(this.xmppFarm.createPresence(status));
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VmStatus status) {
        try {

            XmppVm vm = this.xmppFarm.getVirtualMachine(vmJid);
            if (status == LinkedProcess.VmStatus.NOT_FOUND) {
                vm.terminateSelf();
            } else {
                vm.getConnection().sendPacket(vm.createPresence(status));
            }

        } catch (VmWorkerNotFoundException e) {
            XmppFarm.LOGGER.severe(e.getMessage());
        }
    }
}
