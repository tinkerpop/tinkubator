package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:20 AM
 */
public class SpawnVm extends FarmIq {

    public static final String SPAWN_VM_TAGNAME = "spawn_vm";
    public static final String VM_SPECIES_ATTRIBUTE = "vm_species";

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

        Element spawnElement = new Element(SPAWN_VM_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            spawnElement.setAttribute(VM_JID_ATTRIBUTE, this.vmJid);
        }
        if(this.vmSpecies != null) {
            spawnElement.setAttribute(VM_SPECIES_ATTRIBUTE, this.vmSpecies);
        }
        if(this.errorMessage != null) {
            spawnElement.setText(errorMessage);
        }
        return LinkedProcess.xmlOut.outputString(spawnElement);
    }
}