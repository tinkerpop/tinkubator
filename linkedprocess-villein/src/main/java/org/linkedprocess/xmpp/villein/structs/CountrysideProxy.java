package org.linkedprocess.xmpp.villein.structs;

import org.linkedprocess.xmpp.villein.structs.Proxy;
import org.linkedprocess.xmpp.villein.structs.FarmProxy;
import org.linkedprocess.xmpp.villein.structs.RegistryProxy;
import org.linkedprocess.xmpp.villein.Dispatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 1:02:16 PM
 */
public class CountrysideProxy extends Proxy {


    protected Map<String, FarmProxy> farmStructs = new HashMap<String, FarmProxy>();
    protected Map<String, RegistryProxy> registryStructs = new HashMap<String, RegistryProxy>();

    public CountrysideProxy(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public FarmProxy getFarmStruct(String farmJid) {
        return this.farmStructs.get(farmJid);
    }

    public RegistryProxy getRegistryStruct(String countrysideJid) {
        return this.registryStructs.get(countrysideJid);
    }

    public void addFarmStruct(FarmProxy farmStruct) {
        this.farmStructs.put(farmStruct.getFullJid(), farmStruct);
    }

    public void addRegistryStruct(RegistryProxy registryStruct) {
        this.registryStructs.put(registryStruct.getFullJid(), registryStruct);
    }

    public void removeRegistryStruct(String countrysideJid) {
        this.registryStructs.remove(countrysideJid);
    }

    public Collection<FarmProxy> getFarmStructs() {
        return this.farmStructs.values();
    }

    public Collection<RegistryProxy> getRegistryStructs() {
        return this.registryStructs.values();
    }

    public void removeFarmStruct(String farmJid) {
        this.farmStructs.remove(farmJid);
    }

}
