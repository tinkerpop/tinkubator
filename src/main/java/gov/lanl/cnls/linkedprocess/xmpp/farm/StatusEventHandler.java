package gov.lanl.cnls.linkedprocess.xmpp.farm;

import gov.lanl.cnls.linkedprocess.os.VMScheduler;
import gov.lanl.cnls.linkedprocess.os.errors.VMWorkerNotFoundException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.vm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jul 1, 2009
 * Time: 3:12:44 PM
 */
public class StatusEventHandler implements VMScheduler.LopStatusEventHandler {

    protected XmppFarm farm;

    public StatusEventHandler(XmppFarm farm) {
        this.farm = farm;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        this.farm.getConnection().sendPacket(this.farm.createPresence(status));
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VmStatus status) {
        try {

            XmppVirtualMachine vm = this.farm.getVirtualMachine(vmJid);
            if(status == LinkedProcess.VmStatus.NOT_FOUND) {
                vm.terminateSelf();
            } else {
                vm.getConnection().sendPacket(vm.createPresence(status));
            }

        } catch(VMWorkerNotFoundException e) {
            this.farm.LOGGER.severe(e.getMessage());
        }
    }
}
