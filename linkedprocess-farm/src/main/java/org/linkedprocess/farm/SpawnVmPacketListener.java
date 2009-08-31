package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LopError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.farm.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.farm.os.errors.VmAlreadyExistsException;
import org.linkedprocess.farm.os.errors.VmSchedulerIsFullException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class SpawnVmPacketListener extends FarmPacketListener {

    public SpawnVmPacketListener(Farm farm) {
        super(farm);
    }

    public void processPacket(Packet packet) {
        try {
            processSpawnVmPacket((SpawnVm) packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSpawnVmPacket(SpawnVm spawnVm) {
        Farm.LOGGER.info("Arrived " + SpawnVmPacketListener.class.getName());
        Farm.LOGGER.info(spawnVm.toXML());


        SpawnVm returnSpawnVm = new SpawnVm();
        returnSpawnVm.setTo(spawnVm.getFrom());
        returnSpawnVm.setFrom(this.getFarm().getJid().toString());
        returnSpawnVm.setPacketID(spawnVm.getPacketID());

        String vmSpecies = spawnVm.getVmSpecies();
        String farmPassword = spawnVm.getFarmPassword();

        if (vmSpecies == null) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "spawn_vm XML packet is missing the vm_species attribute", spawnVm.getPacketID()));
        } else if (this.getFarm().getFarmPassword() != null && (farmPassword == null || !farmPassword.equals(this.getFarm().getFarmPassword()))) {
            returnSpawnVm.setType(IQ.Type.ERROR);
            returnSpawnVm.setLopError(new LopError(XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_FARM_PASSWORD, null, spawnVm.getPacketID()));
        } else {
            try {
                Vm vm = this.getFarm().spawnVm(spawnVm.getFrom(), vmSpecies);
                returnSpawnVm.setVmId(vm.getVmId());
                returnSpawnVm.setVmSpecies(vmSpecies);
                returnSpawnVm.setType(IQ.Type.RESULT);

            } catch (VmAlreadyExistsException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new LopError(XMPPError.Condition.conflict, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage(), spawnVm.getPacketID()));
            } catch (VmSchedulerIsFullException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new LopError(XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.FARM_IS_BUSY, e.getMessage(), spawnVm.getPacketID()));
            } catch (UnsupportedScriptEngineException e) {
                returnSpawnVm.setType(IQ.Type.ERROR);
                returnSpawnVm.setLopError(new LopError(XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.SPECIES_NOT_SUPPORTED, e.getMessage(), spawnVm.getPacketID()));
            }
        }

        Farm.LOGGER.info("Sent " + SpawnVmPacketListener.class.getName());
        Farm.LOGGER.info(returnSpawnVm.toXML());
        this.getFarm().getConnection().sendPacket(returnSpawnVm);


    }


}

