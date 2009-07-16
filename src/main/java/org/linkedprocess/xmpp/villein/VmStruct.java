package gov.lanl.cnls.linkedprocess.xmpp.villein;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:13:19 AM
 */
public class VmStruct extends Struct {

    protected String vmPassword;
    protected String vmSpecies;

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
}

