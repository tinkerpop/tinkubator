/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.Error;
import org.linkedprocess.Jid;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.villein.Handler;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.ParentProxyNotFoundException;
import org.linkedprocess.villein.proxies.VmProxy;

/**
 * The proxy by which a spawn_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SpawnVmCommand extends Command {
    private final HandlerSet<VmProxy> successHandler;
    private final HandlerSet<org.linkedprocess.Error> errorHandlers;

    public SpawnVmCommand(Villein xmppVillein) {
        super(xmppVillein);
        this.successHandler = new HandlerSet<VmProxy>();
        this.errorHandlers = new HandlerSet<Error>();
    }

    public void send(final FarmProxy farmProxy, final String vmSpecies, final Handler<VmProxy> successHandler, final Handler<Error> errorHandler) {
        String id = Packet.nextID();
        SpawnVm spawnVm = new SpawnVm();
        spawnVm.setTo(farmProxy.getJid().toString());
        spawnVm.setFrom(this.villein.getJid().toString());
        spawnVm.setVmSpecies(vmSpecies);
        if (null != farmProxy.getFarmPassword()) {
            spawnVm.setFarmPassword(farmProxy.getFarmPassword());
        }
        spawnVm.setType(IQ.Type.GET);
        spawnVm.setPacketID(id);

        this.successHandler.addHandler(id, successHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        villein.getConnection().sendPacket(spawnVm);
    }

    public void receiveSuccess(final SpawnVm spawnVm) {
        VmProxy vmProxy = new VmProxy(this.villein.getCloudProxy().getFarmProxy(new Jid(spawnVm.getFrom())), spawnVm.getVmId(), villein.getDispatcher());
        vmProxy.setVmId(spawnVm.getVmId());
        vmProxy.setVmSpecies(spawnVm.getVmSpecies());
        //vmStruct.setAvailable(true);
        try {
            this.villein.getCloudProxy().addVmProxy(new Jid(spawnVm.getFrom()), vmProxy);
            successHandler.handle(spawnVm.getPacketID(), vmProxy);
        } catch (ParentProxyNotFoundException e) {
            Villein.LOGGER.warning(e.getMessage());
        } finally {
            successHandler.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }

    }

    public void receiveError(final SpawnVm spawnVm) {
        try {
            errorHandlers.handle(spawnVm.getPacketID(), spawnVm.getLopError());
        } finally {
            successHandler.removeHandler(spawnVm.getPacketID());
            errorHandlers.removeHandler(spawnVm.getPacketID());
        }
    }
}
