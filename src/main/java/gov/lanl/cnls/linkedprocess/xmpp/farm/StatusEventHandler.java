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

    protected XmppFarm xmppFarm;

    public StatusEventHandler(XmppFarm xmppFarm) {
        this.xmppFarm = xmppFarm;
    }

    public void schedulerStatusChanged(LinkedProcess.FarmStatus status) {
        this.xmppFarm.getConnection().sendPacket(this.xmppFarm.createPresence(status));
    }

    public void virtualMachineStatusChanged(String vmJid, LinkedProcess.VmStatus status) {
        try {

            XmppVirtualMachine vm = this.xmppFarm.getVirtualMachine(vmJid);
            if(status == LinkedProcess.VmStatus.NOT_FOUND) {
                vm.terminateSelf();
            } else {
                vm.getConnection().sendPacket(vm.createPresence(status));
            }

        } catch(VMWorkerNotFoundException e) {
            this.xmppFarm.LOGGER.severe(e.getMessage());
        }
    }
}
