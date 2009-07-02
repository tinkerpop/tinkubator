package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import gov.lanl.cnls.linkedprocess.os.errors.UnsupportedScriptEngineException;
import gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMSchedulerIsFullException;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:49 AM
 */
public class SpawnVmListener implements PacketListener {

    private XmppFarm farm;

    public SpawnVmListener(XmppFarm farm) {
        this.farm = farm;
    }

    public void processPacket(Packet spawnVm) {
        try {
            processPacketTemp(spawnVm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processPacketTemp(Packet spawnVm) {
        XmppFarm.LOGGER.info("Arrived " + SpawnVmListener.class.getName());
        XmppFarm.LOGGER.info(spawnVm.toXML());

        SpawnVm returnSpawnVm = new SpawnVm();
        returnSpawnVm.setTo(spawnVm.getFrom());
        returnSpawnVm.setPacketID(spawnVm.getPacketID());
        String vmJid;
        String vmSpecies = ((SpawnVm) spawnVm).getVmSpecies();
        if(vmSpecies == null) {
            returnSpawnVm.setErrorType(LinkedProcess.Errortype.MALFORMED_PACKET);
            returnSpawnVm.setErrorMessage("spawn_vm XML packet is missing the vm_species attribute");
            returnSpawnVm.setType(IQ.Type.ERROR);
        } else {
            try {
                vmJid = farm.spawnVirtualMachine(vmSpecies);
                returnSpawnVm.setVmJid(vmJid);
                returnSpawnVm.setType(IQ.Type.RESULT);
            } catch (VMAlreadyExistsException e) {
                returnSpawnVm.setErrorType(LinkedProcess.Errortype.INTERNAL_ERROR);
                returnSpawnVm.setErrorMessage(e.getMessage());
                returnSpawnVm.setType(IQ.Type.ERROR);
            } catch (VMSchedulerIsFullException e) {
                returnSpawnVm.setErrorType(LinkedProcess.Errortype.FARM_IS_BUSY);
                returnSpawnVm.setErrorMessage(e.getMessage());
                returnSpawnVm.setType(IQ.Type.ERROR);
            } catch (UnsupportedScriptEngineException e) {
                returnSpawnVm.setErrorType(LinkedProcess.Errortype.SPECIES_NOT_SUPPORTED);
                returnSpawnVm.setErrorMessage(e.getMessage());
                returnSpawnVm.setType(IQ.Type.ERROR);
            }
        }

        XmppFarm.LOGGER.info("Sent " + SpawnVmListener.class.getName());
        XmppFarm.LOGGER.info(returnSpawnVm.toXML());
        farm.getConnection().sendPacket(returnSpawnVm);
    }
}

