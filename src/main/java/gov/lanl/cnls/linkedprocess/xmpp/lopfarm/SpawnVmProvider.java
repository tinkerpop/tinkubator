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
public class SpawnVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        SpawnVm spawnVm = new SpawnVm();
        String vmJid = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_JID_ATTRIBUTE);
        if(null != vmJid) {
            spawnVm.setVmJid(vmJid);
        }
        String vmSpecies = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_SPECIES_ATTRIBUTE);
        if(null != vmSpecies) {
            spawnVm.setVmSpecies(vmSpecies);
        }
        parser.next();
        return spawnVm;
    }
}
