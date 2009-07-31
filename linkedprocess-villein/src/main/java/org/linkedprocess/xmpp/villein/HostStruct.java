package org.linkedprocess.xmpp.villein;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 1:02:16 PM
 */
public class HostStruct extends Struct {


    protected Map<String, FarmStruct> farmStructs = new HashMap<String, FarmStruct>();
    protected Map<String, CountrysideStruct> countrysideStructs = new HashMap<String, CountrysideStruct>();


    public FarmStruct getFarmStruct(String farmJid) {
        return this.farmStructs.get(farmJid);
    }

    public CountrysideStruct getCountrysideStruct(String countrysideJid) {
        return this.countrysideStructs.get(countrysideJid);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        this.farmStructs.put(farmStruct.getFullJid(), farmStruct);
    }

    public void addCountrysideStruct(CountrysideStruct countrysideStruct) {
        this.countrysideStructs.put(countrysideStruct.getFullJid(), countrysideStruct);
    }

    public void removeCountrysideStruct(String countrysideJid)  {
        this.countrysideStructs.remove(countrysideJid);
    }

    public Collection<FarmStruct> getFarmStructs() {
        return this.farmStructs.values();
    }

    public Collection<CountrysideStruct> getCountrysideStructs() {
        return this.countrysideStructs.values();
    }

    public void removeFarmStruct(String farmJid) {
        this.farmStructs.remove(farmJid);
    }

}
