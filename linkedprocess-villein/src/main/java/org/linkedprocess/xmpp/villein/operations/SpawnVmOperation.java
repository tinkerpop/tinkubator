package org.linkedprocess.xmpp.villein.operations;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.structs.FarmStruct;
import org.linkedprocess.xmpp.villein.structs.ParentStructNotFoundException;
import org.linkedprocess.xmpp.villein.structs.VmStruct;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 3:50:24 PM
 */
public class SpawnVmOperation extends Operation {
    private final HandlerSet<VmStruct> resultHandlers;

    public SpawnVmOperation(XmppVillein xmppVillein) {
        super(xmppVillein);
        resultHandlers = new HandlerSet<VmStruct>();
    }

    public void send(final FarmStruct farmStruct, final String vmSpecies, final Handler<VmStruct> resultHandler, final Handler<XMPPError> errorHandler) {
        String id = Packet.nextID();
        SpawnVm spawnVm = new SpawnVm();
        spawnVm.setTo(farmStruct.getFullJid());
        spawnVm.setFrom(this.xmppVillein.getFullJid());
        spawnVm.setVmSpecies(vmSpecies);
        if (null != farmStruct.getFarmPassword()) {
            spawnVm.setFarmPassword(farmStruct.getFarmPassword());
        }
        spawnVm.setType(IQ.Type.GET);
        spawnVm.setPacketID(id);

        this.resultHandlers.addHandler(id, resultHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        xmppVillein.getConnection().sendPacket(spawnVm);
    }

    public void receiveNormal(final SpawnVm spawnVm) {
        VmStruct vmStruct = new VmStruct(xmppVillein.getDispatcher());
        vmStruct.setFullJid(spawnVm.getVmJid());
        vmStruct.setVmPassword(spawnVm.getVmPassword());
        vmStruct.setVmSpecies(spawnVm.getVmSpecies());
        vmStruct.setPresence(new Presence(Presence.Type.available));
        try {
            this.xmppVillein.addVmStruct(spawnVm.getFrom(), vmStruct);
            resultHandlers.handle(spawnVm.getPacketID(), vmStruct);
        } catch (ParentStructNotFoundException e) {
            XmppVillein.LOGGER.warning(e.getMessage());
        } finally {
            resultHandlers.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }

    }

    public void receiveError(final SpawnVm spawnVm) {
        try {
            errorHandlers.handle(spawnVm.getPacketID(), spawnVm.getError());
        } finally { 
            resultHandlers.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }
    }
}
