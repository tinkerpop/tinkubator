package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.os.errors.VMAlreadyExistsException;
import org.linkedprocess.os.errors.VMSchedulerIsFullException;
import org.linkedprocess.xmpp.LopXmppError;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:49 AM
 */
public class SpawnVmListener extends LopFarmListener {

    public SpawnVmListener(XmppFarm xmppFarm) {
        super(xmppFarm);
    }

    public void processPacket(Packet packet) {
        try {
            processSpawnVmPacket((SpawnVm) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSpawnVmPacket(SpawnVm spawnVm) {
        XmppFarm.LOGGER.info("Arrived " + SpawnVmListener.class.getName());
        XmppFarm.LOGGER.info(spawnVm.toXML());


        SpawnVm returnSpawnVm = new SpawnVm();
        returnSpawnVm.setTo(spawnVm.getFrom());
        returnSpawnVm.setFrom(xmppClient.getFullJid());
        returnSpawnVm.setPacketID(spawnVm.getPacketID());

        String vmSpecies = spawnVm.getVmSpecies();
        String farmPassword = spawnVm.getFarmPassword();

        if (vmSpecies == null) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "spawn_vm XML packet is missing the vm_species attribute"));
        } else
        if (this.getXmppFarm().getFarmPassword() != null && !farmPassword.equals(this.getXmppFarm().getFarmPassword())) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setError(new LopXmppError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_FARM_PASSWORD, null));
        } else {
            try {
                XmppVirtualMachine vm = this.getXmppFarm().spawnVirtualMachine(spawnVm.getFrom(), vmSpecies);
                returnSpawnVm.setVmJid(vm.getFullJid());
                returnSpawnVm.setVmPassword(vm.getVmPassword());
                returnSpawnVm.setVmSpecies(vmSpecies);
                returnSpawnVm.setType(IQ.Type.RESULT);

            } catch (VMAlreadyExistsException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setError(new LopXmppError(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage()));
            } catch (VMSchedulerIsFullException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setError(new LopXmppError(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.FARM_IS_BUSY, e.getMessage()));
            } catch (UnsupportedScriptEngineException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setError(new LopXmppError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.SPECIES_NOT_SUPPORTED, e.getMessage()));
            }
        }

        XmppFarm.LOGGER.info("Sent " + SpawnVmListener.class.getName());
        XmppFarm.LOGGER.info(returnSpawnVm.toXML());
        this.getXmppFarm().getConnection().sendPacket(returnSpawnVm);


    }


}

