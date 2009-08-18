/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.xmpp.villein.commands.*;

/**
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:37:56 PM
 */
public class Dispatcher {
    private final PingJobCommand pingJobCommand;
    private final SpawnVmCommand spawnVmCommand;
    private final SubmitJobCommand submitJobCommand;
    private final AbortJobCommand abortJobCommand;
    private final TerminateVmCommand terminateVmCommand;
    private final GetBindingsCommand getBindingsCommand;
    private final SetBindingsCommand setBindingsCommand;
    protected final ServiceDiscoveryManager discoManager;

    public Dispatcher(XmppVillein xmppVillein) {
        this.pingJobCommand = new PingJobCommand(xmppVillein);
        this.spawnVmCommand = new SpawnVmCommand(xmppVillein);
        this.submitJobCommand = new SubmitJobCommand(xmppVillein);
        this.abortJobCommand = new AbortJobCommand(xmppVillein);
        this.terminateVmCommand = new TerminateVmCommand(xmppVillein);
        this.getBindingsCommand = new GetBindingsCommand(xmppVillein);
        this.setBindingsCommand = new SetBindingsCommand(xmppVillein);
        this.discoManager = xmppVillein.getDiscoManager();
    }

    public PingJobCommand getPingJobCommand() {
        return this.pingJobCommand;
    }

    public SpawnVmCommand getSpawnVmCommand() {
        return this.spawnVmCommand;
    }

    public SubmitJobCommand getSubmitJobCommand() {
        return this.submitJobCommand;
    }

    public AbortJobCommand getAbortJobCommand() {
        return this.abortJobCommand;
    }

    public TerminateVmCommand getTerminateVmCommand() {
        return this.terminateVmCommand;
    }

    public GetBindingsCommand getGetBindingsCommand() {
        return this.getBindingsCommand;
    }

    public SetBindingsCommand getSetBindingsCommand() {
        return this.setBindingsCommand;
    }

    public ServiceDiscoveryManager getServiceDiscoveryManager() {
        return this.discoManager;
    }
}
