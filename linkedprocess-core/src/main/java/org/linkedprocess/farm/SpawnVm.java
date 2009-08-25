/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.farm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SpawnVm extends FarmIq {

    protected String vmSpecies;

    public void setVmSpecies(String vmSpecies) {
        this.vmSpecies = vmSpecies;
    }

    public String getVmSpecies() {
        return this.vmSpecies;
    }

    public String getChildElementXML() {

        Element spawnVmElement = new Element(LinkedProcess.SPAWN_VM_TAG, LinkedProcess.LOP_FARM_NAMESPACE);
        if (this.vmId != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.vmId);
        }
        if (this.vmSpecies != null) {
            spawnVmElement.setAttribute(LinkedProcess.VM_SPECIES_ATTRIBUTE, this.vmSpecies);
        }
        if (this.farmPassword != null) {
            spawnVmElement.setAttribute(LinkedProcess.FARM_PASSWORD_ATTRIBUTE, this.farmPassword);
        }

        return LinkedProcess.xmlOut.outputString(spawnVmElement);
    }
}
