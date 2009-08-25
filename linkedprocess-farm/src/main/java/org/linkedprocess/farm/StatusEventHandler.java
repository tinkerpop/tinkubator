package org.linkedprocess.farm;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.VmScheduler;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.VmNotFoundException;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 3:12:44 PM
 */
public class StatusEventHandler implements VmScheduler.LopStatusEventHandler {

    protected Farm farm;

    public StatusEventHandler(Farm farm) {
        this.farm = farm;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        this.farm.sendPresence(status);
    }

    public void virtualMachineStatusChanged(String vmId, LinkedProcess.VmStatus status) {
            try {

                Vm vm = this.farm.getVm(vmId);
                if (status == LinkedProcess.VmStatus.NOT_FOUND) {
                    vm.terminateSelf();
                } else {
                    // tODO:?
                }

            } catch (VmNotFoundException e) {
                Farm.LOGGER.severe(e.getMessage());
            }
        }



}
