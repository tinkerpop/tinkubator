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
public class UserStruct {

    protected Presence.Mode status;
    protected String userJid;
    protected Map<String, FarmStruct> farmStructs = new HashMap<String, FarmStruct>();

    public void setStatus(Presence.Mode status) {
        this.status = status;
    }

    public Presence.Mode getStatus() {
        return this.status;
    }

    public void setUserJid(String userJid) {
        this.userJid = userJid;
    }

    public String getUserJid() {
        return this.userJid;
    }

    public FarmStruct getFarmStruct(String farmJid) {
        return this.farmStructs.get(farmJid);
    }

    public void addFarmStruct(FarmStruct farmStruct) {
        this.farmStructs.put(farmStruct.getFarmJid(), farmStruct);
    }

    public Collection<FarmStruct> getFarmStructs() {
        return this.farmStructs.values();
    }

}
