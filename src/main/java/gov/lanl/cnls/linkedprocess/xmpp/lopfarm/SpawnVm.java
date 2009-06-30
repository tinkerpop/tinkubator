package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:20 AM
 */
public class SpawnVm extends FarmIq {

    private String vmSpecies;
    private String errorMessage;

    public void setVmSpecies(String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;   
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getChildElementXML() {

        Element spawnVmElement = new Element(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_JID_ATTRIBUTE, this.vmJid);
        }
        if(this.vmSpecies != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_SPECIES_ATTRIBUTE, this.vmSpecies);
        }
        if(this.errorMessage != null) {
            spawnVmElement.setText(errorMessage);
        }
        return LinkedProcess.xmlOut.outputString(spawnVmElement);
    }
}