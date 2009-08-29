/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A spawn_vm parser that creates a SpawnVm object.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SpawnVmProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        SpawnVm spawnVm = new SpawnVm();
        String vmId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_ID_ATTRIBUTE);
        if (null != vmId) {
            spawnVm.setVmId(vmId);
        }
        String vmSpecies = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_SPECIES_ATTRIBUTE);
        if (null != vmSpecies) {
            spawnVm.setVmSpecies(vmSpecies);
        }
        String farmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.FARM_PASSWORD_ATTRIBUTE);
        if (null != farmPassword) {
            spawnVm.setFarmPassword(farmPassword);
        }

        parser.next();
        return spawnVm;
    }
}
