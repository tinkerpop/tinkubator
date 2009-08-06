package org.linkedprocess.xmpp.villein.structs;

import org.linkedprocess.os.VMBindings;
import org.linkedprocess.os.errors.InvalidValueException;
import org.linkedprocess.xmpp.villein.structs.CompletedJob;
import org.linkedprocess.xmpp.villein.structs.Struct;

import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:13:19 AM
 */
public class VmStruct extends Struct {

    protected String vmPassword;
    protected String vmSpecies;
    protected Map<String, CompletedJob> jobs = new HashMap<String, CompletedJob>();
    private VMBindings vmBindings = new VMBindings();

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

    public void addJob(CompletedJob completedJob) {
        this.jobs.put(completedJob.getJobId(), completedJob);
    }

    public CompletedJob getJob(String jobId) {
        return this.jobs.get(jobId);
    }

    public void removeJob(String jobId) {
        this.jobs.remove(jobId);
    }

    public void clearJobs() {
        this.jobs.clear();
    }

    public void addVmBindings(VMBindings bindings) throws InvalidValueException {
        for(String bindingName : bindings.keySet()) {
           this.vmBindings.putTyped(bindingName, bindings.getTyped(bindingName));
        }
    }

    public void removeVmBindings(VMBindings bindings) {
        for(String bindingName : bindings.keySet()) {
           this.vmBindings.remove(bindingName);
        }
    }

    public VMBindings getVmBindings() {
        return this.vmBindings;
    }
}

