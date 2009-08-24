package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VmScheduler;
import org.linkedprocess.os.errors.VmWorkerNotFoundException;
import org.linkedprocess.vm.LopVm;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 3:12:44 PM
 */
public class StatusEventHandler implements VmScheduler.LopStatusEventHandler {

    protected LopFarm lopFarm;

    public StatusEventHandler(LopFarm lopFarm) {
        this.lopFarm = lopFarm;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        this.lopFarm.sendPresence(status);
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VmStatus status) {
        try {

            LopVm vm = this.lopFarm.getVirtualMachine(vmJid);
            if (status == LinkedProcess.VmStatus.NOT_FOUND) {
                vm.terminateSelf();
            } else {
                vm.sendPresence(status);
            }

        } catch (VmWorkerNotFoundException e) {
            LopFarm.LOGGER.severe(e.getMessage());
        }
    }
}
