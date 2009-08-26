package org.linkedprocess.farm.os;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.Farm;
import org.linkedprocess.farm.os.Job;
import org.linkedprocess.farm.os.VmBindings;
import org.linkedprocess.farm.os.errors.VmIsFullException;
import org.linkedprocess.farm.os.errors.*;
import org.linkedprocess.farm.os.errors.VmNotFoundException;

import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class Vm {

    public static Logger LOGGER = LinkedProcess.getLogger(Vm.class);

    protected final Farm farm;
    protected final String vmSpecies;
    protected final String spawningVilleinJid;
    protected final String vmId;

    public Vm(Farm farm, final String vmId, final String spawningVilleinJid, final String vmSpecies) {

        this.farm = farm;
        this.vmSpecies = vmSpecies;
        this.spawningVilleinJid = spawningVilleinJid;
        this.vmId = vmId;
    }

    public String getVmId() {
        return this.vmId;
    }

    public Farm getFarm() {
        return this.farm;
    }

    public void abortJob(String jobId) throws VmNotFoundException, JobNotFoundException {
        this.farm.getVmScheduler().abortJob(this.vmId, jobId);
    }

    public LinkedProcess.JobStatus getJobStatus(String jobId) throws VmNotFoundException, JobNotFoundException {
        return this.farm.getVmScheduler().getJobStatus(this.vmId, jobId);
    }

    public void scheduleJob(Job job) throws VmNotFoundException, VmIsFullException, JobAlreadyExistsException {
        this.farm.getVmScheduler().submitJob(this.vmId, job);
    }

    public void setBindings(VmBindings bindings) throws VmNotFoundException {
        this.farm.getVmScheduler().setBindings(this.vmId, bindings);
    }

    public VmBindings getBindings(Set<String> names) throws VmNotFoundException {
        return this.farm.getVmScheduler().getBindings(this.vmId, names);
    }


    public void terminateSelf() throws VmNotFoundException {
        this.farm.terminateVm(this.vmId);
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public String getSpawningVilleinJid() {
        return this.spawningVilleinJid;
    }

    public LinkedProcess.VmStatus getVmStatus() {
        return this.farm.getVmScheduler().getVirtualMachineStatus(this.vmId);
    }

}
