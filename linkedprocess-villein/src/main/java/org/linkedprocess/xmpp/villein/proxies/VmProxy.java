/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.proxies;

import org.jdom.Document;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.villein.Handler;

import java.util.Set;

/**
 * A VmProxy is a proxy to a virtual machine. A virtual machine is an XMPP client that has a full-qualified JID.
 * The bare JID component of the virtual machine is the virtual machine's countryside. A virtual machine is spawned from
 * a farm. Nearly all the computation that happens in LoP is through virtual machines.
 *
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:13:19 AM
 */
public class VmProxy extends Proxy {

    protected String vmPassword;
    protected String vmSpecies;
    private VMBindings vmBindings = new VMBindings();

    public VmProxy(final String fullJid, final Dispatcher dispatcher) {
        super(fullJid, dispatcher);
    }

    public VmProxy(final String fullJid, final Dispatcher dispatcher, final Document discoInfoDocument) {
        super(fullJid, dispatcher, discoInfoDocument);
    }

    // FIXME: why Handler<JobStruct> for errors? Seems kind of weird, even if it works.
    public void submitJob(final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<JobStruct> errorHandler) {
       dispatcher.getSubmitJobCommand().send(this, jobStruct, resultHandler, errorHandler);
    }

    public void pingJob(final JobStruct jobStruct, final Handler<LinkedProcess.JobStatus> resultHandler, final Handler<LopError> errorHandler) {
        dispatcher.getPingJobCommand().send(this, jobStruct, resultHandler, errorHandler);
    }

    public void abortJob(final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<LopError> errorHandler) {
        dispatcher.getAbortJobCommand().send(this, jobStruct, resultHandler, errorHandler);
    }

    public void getBindings(final Set<String> bindingNames, final Handler<VMBindings> resultHandler, final Handler<LopError> errorHandler) {
        dispatcher.getGetBindingsCommand().send(this, bindingNames, resultHandler, errorHandler);
    }

    public void setBindings(final VMBindings vmBindings, final Handler<VMBindings> resultHandler, final Handler<LopError> errorHandler) {
        dispatcher.getSetBindingsCommand().send(this, vmBindings, resultHandler, errorHandler);
    }

    public void terminateVm(final Handler<Object> resultHandler, final Handler<LopError> errorHandler) {
        dispatcher.getTerminateVmCommand().send(this, resultHandler, errorHandler);
    }

    public void setVmPassword(final String vmPassword) {
        this.vmPassword = vmPassword;
    }

    public String getVmPassword() {
        return this.vmPassword;
    }

    public void setVmSpecies(final String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public void addVmBindings(VMBindings bindings) throws InvalidValueException {
        for (String bindingName : bindings.keySet()) {
            this.vmBindings.putTyped(bindingName, bindings.getTyped(bindingName));
        }
    }

    public void removeVmBindings(VMBindings bindings) {
        for (String bindingName : bindings.keySet()) {
            this.vmBindings.remove(bindingName);
        }
    }

    public VMBindings getVmBindings() {
        return this.vmBindings;
    }
}

