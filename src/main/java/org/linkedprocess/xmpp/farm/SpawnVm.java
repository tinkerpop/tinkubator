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

    public void setVmSpecies(String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
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
        if(this.errorType != null) {
            spawnVmElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                spawnVmElement.setText(this.errorMessage);
            }
        }

        return LinkedProcess.xmlOut.outputString(spawnVmElement);
    }
}
