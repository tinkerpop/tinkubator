package gov.lanl.cnls.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Presence;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 1:02:16 PM
 */
public class HostStruct extends Struct {


    protected Map<String, FarmStruct> farmStructs = new HashMap<String, FarmStruct>();

    public FarmStruct getFarmStruct(String farmJid) {
        return this.farmStructs.get(farmJid);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        this.farmStructs.put(farmStruct.getFullJid(), farmStruct);
    }

    public Collection<FarmStruct> getFarmStructs() {
        return this.farmStructs.values();
    }

    public void removeFarmStruct(String farmJid) {
        this.farmStructs.remove(farmJid);
    }

}
