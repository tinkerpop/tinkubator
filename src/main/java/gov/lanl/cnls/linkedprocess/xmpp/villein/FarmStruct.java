package gov.lanl.cnls.linkedprocess.xmpp.villein;

import gov.lanl.cnls.linkedprocess.LinkedProcess;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:53:17 AM
 */
public class FarmStruct {

    protected String farmJid;
    protected LinkedProcess.FarmStatus farmStatus;
    protected Map<String, VmStruct> vmStructs = new HashMap<String, VmStruct>();

    public void setFarmJid(final String farmJid) {
        this.farmJid = farmJid;
    }

    public String getFarmJid() {
        return this.farmJid;
    }

    public VmStruct getVmStruct(String vmJid) {
        return vmStructs.get(vmJid);
    }

    public LinkedProcess.FarmStatus getFarmStatus() {
        return this.farmStatus;
    }

    public void setStatus(LinkedProcess.FarmStatus farmStatus) {
        this.farmStatus = farmStatus;
    }

    public void addVmStruct(VmStruct vmStruct) {
        this.vmStructs.put(vmStruct.getVmJid(), vmStruct);
    }

    public Collection<VmStruct> getVmStructs() {
        return this.vmStructs.values();
    }
}
