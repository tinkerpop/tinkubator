package gov.lanl.cnls.linkedprocess.xmpp.lopfarm;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:28 AM
 */
public class SpawnProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        Spawn spawn = new Spawn();
        String vmJid = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, Spawn.VM_JID_ATTRIBUTE);
        if(null != vmJid) {
            spawn.setVmJid(vmJid);
        }
        String vmSpecies = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, Spawn.VM_SPECIES_ATTRIBUTE);
        if(null != vmSpecies) {
            spawn.setVmSpecies(vmSpecies);
        }
        parser.next();
        return spawn;
    }
}
