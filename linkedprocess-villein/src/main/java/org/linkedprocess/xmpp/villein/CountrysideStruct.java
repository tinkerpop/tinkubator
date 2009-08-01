package org.linkedprocess.xmpp.villein;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 1:02:16 PM
 */
public class CountrysideStruct extends Struct {


    protected Map<String, FarmStruct> farmStructs = new HashMap<String, FarmStruct>();
    protected Map<String, RegistryStruct> registryStructs = new HashMap<String, RegistryStruct>();


    public FarmStruct getFarmStruct(String farmJid) {
        return this.farmStructs.get(farmJid);
    }

    public RegistryStruct getRegistryStruct(String countrysideJid) {
        return this.registryStructs.get(countrysideJid);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        this.farmStructs.put(farmStruct.getFullJid(), farmStruct);
    }

    public void addRegistryStruct(RegistryStruct registryStruct) {
        this.registryStructs.put(registryStruct.getFullJid(), registryStruct);
    }

    public void removeRegistryStruct(String countrysideJid)  {
        this.registryStructs.remove(countrysideJid);
    }

    public Collection<FarmStruct> getFarmStructs() {
        return this.farmStructs.values();
    }

    public Collection<RegistryStruct> getRegistryStructs() {
        return this.registryStructs.values();
    }

    public void removeFarmStruct(String farmJid) {
        this.farmStructs.remove(farmJid);
    }

}
