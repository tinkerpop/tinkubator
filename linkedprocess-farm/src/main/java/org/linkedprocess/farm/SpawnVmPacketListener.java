package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.Error;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.os.errors.VmAlreadyExistsException;
import org.linkedprocess.os.errors.VmSchedulerIsFullException;
import org.linkedprocess.vm.LopVm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class SpawnVmPacketListener extends FarmPacketListener {

    public SpawnVmPacketListener(LopFarm lopFarm) {
        super(lopFarm);
    }

    public void processPacket(Packet packet) {
        try {
            processSpawnVmPacket((SpawnVm) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSpawnVmPacket(SpawnVm spawnVm) {
        LopFarm.LOGGER.info("Arrived " + SpawnVmPacketListener.class.getName());
        LopFarm.LOGGER.info(spawnVm.toXML());


        SpawnVm returnSpawnVm = new SpawnVm();
        returnSpawnVm.setTo(spawnVm.getFrom());
        returnSpawnVm.setFrom(lopClient.getFullJid());
        returnSpawnVm.setPacketID(spawnVm.getPacketID());

        String vmSpecies = spawnVm.getVmSpecies();
        String farmPassword = spawnVm.getFarmPassword();

        if (vmSpecies == null) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "spawn_vm XML packet is missing the vm_species attribute", LOP_CLIENT_TYPE, spawnVm.getPacketID()));
        } else if (this.getLopFarm().getFarmPassword() != null && (farmPassword == null || !farmPassword.equals(this.getLopFarm().getFarmPassword()))) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setLopError(new Error(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_FARM_PASSWORD, null, LOP_CLIENT_TYPE, spawnVm.getPacketID()));
        } else {
            try {
                LopVm vm = this.getLopFarm().spawnVirtualMachine(spawnVm.getFrom(), vmSpecies);
                returnSpawnVm.setVmJid(vm.getFullJid());
                returnSpawnVm.setVmPassword(vm.getVmPassword());
                returnSpawnVm.setVmSpecies(vmSpecies);
                returnSpawnVm.setType(IQ.Type.RESULT);

            } catch (VmAlreadyExistsException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new Error(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), LOP_CLIENT_TYPE, spawnVm.getPacketID()));
            } catch (VmSchedulerIsFullException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new Error(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.FARM_IS_BUSY, e.getMessage(), LOP_CLIENT_TYPE, spawnVm.getPacketID()));
            } catch (UnsupportedScriptEngineException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new Error(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.SPECIES_NOT_SUPPORTED, e.getMessage(), LOP_CLIENT_TYPE, spawnVm.getPacketID()));
            }
        }

        LopFarm.LOGGER.info("Sent " + SpawnVmPacketListener.class.getName());
        LopFarm.LOGGER.info(returnSpawnVm.toXML());
        this.getLopFarm().getConnection().sendPacket(returnSpawnVm);


    }


}

