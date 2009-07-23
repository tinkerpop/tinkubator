package org.linkedprocess.xmpp.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:23:28 AM
 */
public class SpawnVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        SpawnVm spawnVm = new SpawnVm();
        String vmJid = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_JID_ATTRIBUTE);
        if (null != vmJid) {
            spawnVm.setVmJid(vmJid);
        }
        String vmSpecies = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_SPECIES_ATTRIBUTE);
        if (null != vmSpecies) {
            spawnVm.setVmSpecies(vmSpecies);
        }
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if (null != vmPassword) {
            spawnVm.setVmPassword(vmPassword);
        }
        /*String errorType = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.ERROR_TYPE_ATTRIBUTE);
        if(null != errorType) {
            spawnVm.setErrorType(LinkedProcess.LopErrorType.getErrorType(errorType));
        }*/
        parser.next();
        return spawnVm;
    }
}
