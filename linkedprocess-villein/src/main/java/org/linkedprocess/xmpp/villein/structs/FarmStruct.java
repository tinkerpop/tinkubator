package org.linkedprocess.xmpp.villein.structs;

import org.linkedprocess.xmpp.villein.structs.Struct;
import org.linkedprocess.xmpp.villein.structs.VmStruct;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.Dispatcher;
import org.jivesoftware.smack.packet.XMPPError;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 9:53:17 AM
 */
public class FarmStruct extends Struct {

    protected Map<String, VmStruct> vmStructs = new HashMap<String, VmStruct>();
    protected Collection<String> supportedVmSpecies = new HashSet<String>();
    protected String farmPassword;

    public FarmStruct(Dispatcher dispatcher) {
        super(dispatcher);
    }

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

    public Collection<String> getSupportedVmSpecies() {
        return this.supportedVmSpecies;
    }

    public void addSupportedVmSpecies(String supportedVmSpecies) {
        this.supportedVmSpecies.add(supportedVmSpecies);
    }

    public void setSupportedVmSpecies(Collection<String> supportedVmSpecies) {
        this.supportedVmSpecies = supportedVmSpecies;
    }

    public String getFarmPassword() {
        return this.farmPassword;
    }

    public void setFarmPassword(String farmPassword) {
        this.farmPassword = farmPassword;
    }

    public void spawnVirtualMachine(final String vmSpecies, final Handler<VmStruct> resultHandler, final Handler<XMPPError> errorHandler) {
        this.dispatcher.getSpawnVmOperation().send(this, vmSpecies, resultHandler, errorHandler);
    }
}
