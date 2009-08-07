package org.linkedprocess.xmpp.villein;

import org.linkedprocess.xmpp.villein.operations.*;
import org.linkedprocess.xmpp.villein.XmppVillein;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 6, 2009
 * Time: 5:37:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Dispatcher {
    private final JobStatusOperation jobStatusOperation;
    private final SpawnVmOperation spawnVmOperation;
    private final SubmitJobOperation submitJobOperation;
    private final AbortJobOperation abortJobOperation;
    private final TerminateVmOperation terminateVmOperation;
    private final GetBindingsOperation getBindingsOperation;
    private final SetBindingsOperation setBindingsOperation;

    public Dispatcher(XmppVillein xmppVillein) {
        this.jobStatusOperation = new JobStatusOperation(xmppVillein);
        this.spawnVmOperation = new SpawnVmOperation(xmppVillein);
        this.submitJobOperation = new SubmitJobOperation(xmppVillein);
        this.abortJobOperation = new AbortJobOperation(xmppVillein);
        this.terminateVmOperation = new TerminateVmOperation(xmppVillein);
        this.getBindingsOperation = new GetBindingsOperation(xmppVillein);
        this.setBindingsOperation = new SetBindingsOperation(xmppVillein);
    }

    public JobStatusOperation getJobStatusOperation() {
        return this.jobStatusOperation;
    }
    
    public SpawnVmOperation getSpawnVmOperation() {
        return this.spawnVmOperation;
    }

    public SubmitJobOperation getSubmitJobOperation() {
        return this.submitJobOperation;    
    }

    public AbortJobOperation getAbortJobOperation() {
        return this.abortJobOperation;
    }

    public TerminateVmOperation getTerminateVmOperation() {
        return this.terminateVmOperation;
    }

    public GetBindingsOperation getGetBindingsOperation() {
        return this.getBindingsOperation;
    }

    public SetBindingsOperation getSetBindingsOperation() {
        return this.setBindingsOperation;
    }
}
