/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.villein;

import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.villein.commands.*;
import org.linkedprocess.villein.LopVillein;

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

    public Dispatcher(LopVillein lopVillein) {
        this.pingJobCommand = new PingJobCommand(lopVillein);
        this.spawnVmCommand = new SpawnVmCommand(lopVillein);
        this.submitJobCommand = new SubmitJobCommand(lopVillein);
        this.abortJobCommand = new AbortJobCommand(lopVillein);
        this.terminateVmCommand = new TerminateVmCommand(lopVillein);
        this.getBindingsCommand = new GetBindingsCommand(lopVillein);
        this.setBindingsCommand = new SetBindingsCommand(lopVillein);
        this.discoManager = lopVillein.getDiscoManager();
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
