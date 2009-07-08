package gov.lanl.cnls.linkedprocess.xmpp.villein;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:13:19 AM
 */
public class VmStruct {

    protected String vmJid;
    protected String vmPassword;
    protected String vmSpecies;
    protected LinkedProcess.VmStatus vmStatus;

    public void setVmJid(final String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
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

    public void setStatus(LinkedProcess.VmStatus vmStatus) {
        this.vmStatus = vmStatus;
    }

    public LinkedProcess.VmStatus getVmStatus() {
        return this.vmStatus;
    }

}

