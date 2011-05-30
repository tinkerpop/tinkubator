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
 * A submit_job packet is modeled by this class.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SubmitJob extends FarmIq {

    protected String expression;

    /**
     * Set the text body expression of the packet
     *
     * @param expression the text body expression of the packet
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Get the text body expression of the packet
     *
     * @return the text body expression of the packet
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * Get the submit_job component of this IQ packet.
     *
     * @return the submit_job component of this IQ packet
     */
    public String getChildElementXML() {

        Element submitJobElement = new Element(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_FARM_NAMESPACE);

        if (this.getVmId() != null) {
            submitJobElement.setAttribute(LinkedProcess.VM_ID_ATTRIBUTE, this.getVmId());
        }

        if (this.expression != null) {
            submitJobElement.setText(this.expression);
        }

        return LinkedProcess.xmlOut.outputString(submitJobElement);
    }
}
