package org.linkedprocess.xmpp.vm;

import org.jdom.Element;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 12:12:20 PM
 */
public class SubmitJob extends VirtualMachineIq {

    String expression;


    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
       return this.expression;
    }

    public String getChildElementXML() {

        Element submitJobElement = new Element(LinkedProcess.SUBMIT_JOB_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if(this.vmPassword != null) {
            submitJobElement.setAttribute(LinkedProcess.VM_PASSWORD_ATTRIBUTE, this.vmPassword);
        }
        if(this.errorType != null) {
            submitJobElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
            if(this.errorMessage != null) {
                submitJobElement.setText(this.errorMessage);
            }
        } else if(this.expression != null) {
                submitJobElement.setText(this.expression);
        }
        
        return LinkedProcess.xmlOut.outputString(submitJobElement);
    }
}
