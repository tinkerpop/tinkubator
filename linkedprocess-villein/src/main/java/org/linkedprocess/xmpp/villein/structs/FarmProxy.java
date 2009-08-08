package org.linkedprocess.xmpp.villein.structs;

import org.linkedprocess.xmpp.villein.structs.Proxy;
import org.linkedprocess.xmpp.villein.structs.VmProxy;
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
public class FarmProxy extends Proxy {

    protected Map<String, VmProxy> vmStructs = new HashMap<String, VmProxy>();
    protected Collection<String> supportedVmSpecies = new HashSet<String>();
    protected String farmPassword;

    public FarmProxy(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public VmProxy getVmStruct(String vmJid) {
        return vmStructs.get(vmJid);
    }

    public void addVmStruct(VmProxy vmStruct) {
        this.vmStructs.put(vmStruct.getFullJid(), vmStruct);
    }

    public Collection<VmProxy> getVmStructs() {
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

    public void spawnVirtualMachine(final String vmSpecies, final Handler<VmProxy> resultHandler, final Handler<XMPPError> errorHandler) {
        this.dispatcher.getSpawnVmOperation().send(this, vmSpecies, resultHandler, errorHandler);
    }
}
