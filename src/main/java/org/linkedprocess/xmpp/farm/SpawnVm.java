package org.linkedprocess.xmpp.farm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:20 AM
 */
public class SpawnVm extends FarmIq {

    protected String vmSpecies;
    protected String farmPassword;

    public void setVmSpecies(String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public String getChildElementXML() {

        Element spawnVmElement = new Element(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_JID_ATTRIBUTE, this.vmJid);
        }
        if(this.vmPassword != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if(this.vmSpecies != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_SPECIES_ATTRIBUTE, this.vmSpecies);
        }
        if(this.farmPassword != null) {
            spawnVmElement.setAttribute(LinkedProcess.FARM_PASSWORD_ATTRIBUTE, this.farmPassword);
        }
        if(this.errorType != null) {
            spawnVmElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                spawnVmElement.setText(this.errorMessage);
            }
        }

        return LinkedProcess.xmlOut.outputString(spawnVmElement);
    }
}
