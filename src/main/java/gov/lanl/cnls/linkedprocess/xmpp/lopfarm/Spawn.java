package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.packet.IQ;
import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:20 AM
 */
public class Spawn extends IQ {

    public static final String SPAWN_TAGNAME = "spawn";
    public static final String VM_JID_ATTRIBUTE = "vm_jid";
    public static final String VM_SPECIES_ATTRIBUTE = "vm_species";
    public String vmJid;
    public String vmSpecies;

    public void setVmJid(String vmJid) {
        this.vmJid = vmJid;
    }

    public String getVmJid() {
        return this.vmJid;
    }

    public void setVmSpecies(String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public String getChildElementXML() {

        Element spawnElement = new Element(SPAWN_TAGNAME, LinkedProcess.LOP_FARM_NAMESPACE);
        if(this.vmJid != null) {
            spawnElement.setAttribute(VM_JID_ATTRIBUTE, this.vmJid);
        }
        if(this.vmSpecies != null) {
            spawnElement.setAttribute(VM_SPECIES_ATTRIBUTE, this.vmSpecies);
        }
        return LinkedProcess.xmlOut.outputString(spawnElement);
    }
}