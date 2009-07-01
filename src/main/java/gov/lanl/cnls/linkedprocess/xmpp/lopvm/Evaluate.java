package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jdom.Element;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 12:12:20 PM
 */
public class Evaluate extends VirtualMachineIq {

    String expression;


    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
       return this.expression;
    }

    public String getChildElementXML() {

        Element evaluateElement = new Element(LinkedProcess.EVALUATE_TAG, LinkedProcess.LOP_VM_NAMESPACE);

        if(this.errorType != null) {
            evaluateElement.setAttribute(LinkedProcess.ERROR_TYPE_ATTRIBUTE, this.errorType.toString());
        }

        if(this.expression != null) {
            evaluateElement.setText(this.expression);
        }


        return LinkedProcess.xmlOut.outputString(evaluateElement);
    }
}
