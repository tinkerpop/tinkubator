package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.proxies.Proxy;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
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

    protected Map<String, VmProxy> vmProxies = new HashMap<String, VmProxy>();
    protected Collection<String> supportedVmSpecies = new HashSet<String>();
    protected String farmPassword;

    public FarmProxy(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public VmProxy getVmProxy(String vmJid) {
        return vmProxies.get(vmJid);
    }

    public void addVmProxy(VmProxy vmProxy) {
        this.vmProxies.put(vmProxy.getFullJid(), vmProxy);
    }

    public Collection<VmProxy> getVmProxies() {
        return this.vmProxies.values();
    }

    public void removeVmProxy(String vmJid) {
        this.vmProxies.remove(vmJid);
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

    public void spawnVm(final String vmSpecies, final Handler<VmProxy> resultHandler, final Handler<XMPPError> errorHandler) {
        this.dispatcher.getSpawnVmOperation().send(this, vmSpecies, resultHandler, errorHandler);
    }
}
