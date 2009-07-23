package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;
import org.linkedprocess.os.errors.VMAlreadyExistsException;
import org.linkedprocess.os.errors.VMSchedulerIsFullException;
import org.linkedprocess.xmpp.ErrorIq;
import org.linkedprocess.xmpp.LopListener;
import org.linkedprocess.xmpp.vm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:49 AM
 */
public class SpawnVmListener extends LopListener {

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

        String vmSpecies = spawnVm.getVmSpecies();
        String farmPassword = spawnVm.getFarmPassword();
        if (vmSpecies == null) {
            this.sendErrorPacket(ErrorIq.ClientType.FARM, xmppClient.getFullJid(), spawnVm.getFrom(), spawnVm.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.MALFORMED_PACKET, "spawn_vm XML packet is missing the vm_species attribute");
        } else
        if (((XmppFarm) xmppClient).getFarmPassword() != null && !farmPassword.equals(((XmppFarm) xmppClient).getFarmPassword())) {
            this.sendErrorPacket(ErrorIq.ClientType.FARM, xmppClient.getFullJid(), spawnVm.getFrom(), spawnVm.getPacketID(), XMPPError.Type.AUTH, XMPPError.Condition.not_authorized, LinkedProcess.LopErrorType.WRONG_FARM_PASSWORD, null);
        } else {
            try {
                XmppVirtualMachine vm = ((XmppFarm) xmppClient).spawnVirtualMachine(spawnVm.getFrom(), vmSpecies);
                SpawnVm returnSpawnVm = new SpawnVm();
                returnSpawnVm.setTo(spawnVm.getFrom());
                returnSpawnVm.setFrom(xmppClient.getFullJid());
                returnSpawnVm.setPacketID(spawnVm.getPacketID());
                returnSpawnVm.setVmJid(vm.getFullJid());
                returnSpawnVm.setVmPassword(vm.getVmPassword());
                returnSpawnVm.setVmSpecies(vmSpecies);
                returnSpawnVm.setType(IQ.Type.RESULT);

                XmppFarm.LOGGER.info("Sent " + SpawnVmListener.class.getName());
                XmppFarm.LOGGER.info(returnSpawnVm.toXML());
                this.xmppClient.getConnection().sendPacket(returnSpawnVm);

            } catch (VMAlreadyExistsException e) {
                this.sendErrorPacket(ErrorIq.ClientType.FARM, xmppClient.getFullJid(), spawnVm.getFrom(), spawnVm.getPacketID(), XMPPError.Type.CANCEL, XMPPError.Condition.conflict, LinkedProcess.LopErrorType.INTERNAL_ERROR, e.getMessage());
            } catch (VMSchedulerIsFullException e) {
                this.sendErrorPacket(ErrorIq.ClientType.FARM, xmppClient.getFullJid(), spawnVm.getFrom(), spawnVm.getPacketID(), XMPPError.Type.WAIT, XMPPError.Condition.service_unavailable, LinkedProcess.LopErrorType.FARM_IS_BUSY, e.getMessage());
            } catch (UnsupportedScriptEngineException e) {
                this.sendErrorPacket(ErrorIq.ClientType.FARM, xmppClient.getFullJid(), spawnVm.getFrom(), spawnVm.getPacketID(), XMPPError.Type.MODIFY, XMPPError.Condition.bad_request, LinkedProcess.LopErrorType.SPECIES_NOT_SUPPORTED, e.getMessage());
            }
        }


    }


}

