package org.linkedprocess.xmpp.villein;

import org.linkedprocess.LinkedProcess;

import java.util.*;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:53:17 AM
 */
public class FarmStruct extends Struct {

    protected Map<String, VmStruct> vmStructs = new HashMap<String, VmStruct>();
    protected Set<String> supportedVmSpecies = new HashSet<String>();

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

    public Set<String> getSupportedVmSpecies() {
        return this.supportedVmSpecies;
    }

    public void addSupportedVmSpecies(String supportedVmSpecies) {
        this.supportedVmSpecies.add(supportedVmSpecies);
    }

    public void setSupportedVmSpecies(Set<String> supportedVmSpecies) {
        this.supportedVmSpecies = supportedVmSpecies;
    }
}
