package org.linkedprocess.xmpp.villein.operations;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.ParentProxyNotFoundException;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.LopError;

/**
 * User: marko
 * Date: Aug 7, 2009
 * Time: 3:50:24 PM
 */
public class SpawnVmCommand extends Command {
    private final HandlerSet<VmProxy> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public SpawnVmCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<VmProxy>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(final FarmProxy farmStruct, final String vmSpecies, final Handler<VmProxy> resultHandler, final Handler<LopError> errorHandler) {
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
        VmProxy vmProxy = new VmProxy(xmppVillein.getDispatcher());
        vmProxy.setFullJid(spawnVm.getVmJid());
        vmProxy.setVmPassword(spawnVm.getVmPassword());
        vmProxy.setVmSpecies(spawnVm.getVmSpecies());
        vmProxy.setPresence(new Presence(Presence.Type.available));
        try {
            this.xmppVillein.addVmProxy(spawnVm.getFrom(), vmProxy);
            resultHandlers.handle(spawnVm.getPacketID(), vmProxy);
        } catch (ParentProxyNotFoundException e) {
            XmppVillein.LOGGER.warning(e.getMessage());
        } finally {
            resultHandlers.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }

    }

    public void receiveError(final SpawnVm spawnVm) {
        try {
            errorHandlers.handle(spawnVm.getPacketID(), spawnVm.getLopError());
        } finally { 
            resultHandlers.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }
    }
}
