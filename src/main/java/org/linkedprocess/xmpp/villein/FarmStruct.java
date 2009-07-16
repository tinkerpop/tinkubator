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
public class FarmStruct extends Struct {

    protected Map<String, VmStruct> vmStructs = new HashMap<String, VmStruct>();


    public VmStruct getVmStruct(String vmJid) {
        return vmStructs.get(vmJid);
    }


    public void addVmStruct(VmStruct vmStruct) {
        this.vmStructs.put(vmStruct.getFullJid(), vmStruct);
    }

    public Collection<VmStruct> getVmStructs() {
        return this.vmStructs.values();
    }

    public void removeVmStruct(String vmJid) {
        this.vmStructs.remove(vmJid);
    }
}
