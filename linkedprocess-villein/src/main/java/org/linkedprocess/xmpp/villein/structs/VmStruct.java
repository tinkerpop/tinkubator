package org.linkedprocess.xmpp.villein.structs;

import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.villein.Handler;

import java.util.Set;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:13:19 AM
 */
public class VmStruct extends Struct {

    protected String vmPassword;
    protected String vmSpecies;
    private VMBindings vmBindings = new VMBindings();

    public VmStruct(final Dispatcher dispatcher) {
        super(dispatcher);
    }


    public void submitJob(final JobStruct jobStruct, final Handler<JobStruct> resultHandler, final Handler<XMPPError> errorHandler) {
       dispatcher.getSubmitJobOperation().send(this, jobStruct, resultHandler, errorHandler);
    }

    public void jobStatus(final JobStruct jobStruct, final Handler<LinkedProcess.JobStatus> resultHandler, final Handler<XMPPError> errorHandler) {
        dispatcher.getJobStatusOperation().send(this, jobStruct, resultHandler, errorHandler);
    }

    public void abortJob(final JobStruct jobStruct, final Handler<XMPPError> errorHandler) {
        dispatcher.getAbortJobOperation().send(this, jobStruct, errorHandler);
    }

    public void getBindings(final Set<String> bindingNames, final Handler<VMBindings> resultHandler, final Handler<XMPPError> errorHandler) {
        dispatcher.getGetBindingsOperation().send(this, bindingNames, resultHandler, errorHandler);
    }

    public void setBindings(final VMBindings vmBindings, final Handler<XMPPError> errorHandler) {
        dispatcher.getSetBindingsOperation().send(this, vmBindings, errorHandler);
    }

    public void terminateVm(final Handler<XMPPError> errorHandler) {
        dispatcher.getTerminateVmOperation().send(this, errorHandler);
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

