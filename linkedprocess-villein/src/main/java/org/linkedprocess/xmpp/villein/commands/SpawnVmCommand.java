/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.farm.SpawnVm;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.FarmProxy;
import org.linkedprocess.xmpp.villein.proxies.ParentProxyNotFoundException;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

/**
 * The proxy by which a spawn_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
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
        VmProxy vmProxy = new VmProxy(spawnVm.getVmJid(), xmppVillein.getDispatcher());
        vmProxy.setVmPassword(spawnVm.getVmPassword());
        vmProxy.setVmSpecies(spawnVm.getVmSpecies());
        vmProxy.setPresence(new Presence(Presence.Type.available));
        try {
            this.xmppVillein.getLopCloud().addVmProxy(spawnVm.getFrom(), vmProxy);
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
