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
public class SubmitJob extends FarmIq {

    String expression;

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return this.expression;
    }

    public String getChildElementXML() {

        Element submitJobElement = new Element(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_FARM_NAMESPACE);

        if(this.getVmId() != null) {
            submitJobElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.getVmId());
        }

        if (this.expression != null) {
            submitJobElement.setText(this.expression);
        }

        return LinkedProcess.xmlOut.outputString(submitJobElement);
    }
}
