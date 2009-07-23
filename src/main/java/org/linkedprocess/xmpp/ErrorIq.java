package org.linkedprocess.xmpp;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 23, 2009
 * Time: 11:27:20 AM
 */
public class ErrorIq extends IQ {

    protected String errorMessage;
    protected XMPPError.Type xmppErrorType;
    protected LinkedProcess.LopErrorType lopErrorType;
    protected XMPPError.Condition condition;
    protected ClientType clientType;

    public enum ClientType {
        FARM("LoPFarm"), VM("LoPVM");
        private String type;

        private ClientType(String type) {
            this.type = type;
        }

        public String toString() {
            return this.type;
        }

    }

    public ClientType getClientType() {
        return this.clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }


    public XMPPError.Condition getCondition() {
        return condition;
    }

    public void setCondition(XMPPError.Condition condition) {
        this.condition = condition;
    }

    public LinkedProcess.LopErrorType getLopErrorType() {
        return this.lopErrorType;
    }

    public void setLopErrorType(LinkedProcess.LopErrorType lopErrorType) {
        this.lopErrorType = lopErrorType;
    }

    public XMPPError.Type getXmppErrorType() {
        return this.xmppErrorType;
    }

    public void setXmppErrorType(XMPPError.Type xmppErrorType) {
        this.xmppErrorType = xmppErrorType;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getChildElementXML() {
        this.setType(IQ.Type.ERROR);
        Element errorElement = new Element(LinkedProcess.ERROR_TAG, Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
        errorElement.setAttribute(LinkedProcess.TYPE_ATTRIBUTE, this.xmppErrorType.toString().toLowerCase());
        if (null != this.errorMessage) {
            Element textElement = new Element(LinkedProcess.TEXT_TAG, Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
            textElement.setText(this.errorMessage);
            errorElement.addContent(textElement);
        }
        if (null != this.lopErrorType) {
            Element lopErrorElement = new Element(this.lopErrorType.toString(), Namespace.getNamespace(LinkedProcess.LOP_NAMESPACE + "protocol/" + this.clientType.toString()));
            errorElement.addContent(lopErrorElement);
        }
        if (null != this.condition) {
            Element conditionElement = new Element(this.condition.toString(), Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
            errorElement.addContent(conditionElement);
        }
        return LinkedProcess.xmlOut.outputString(errorElement);
    }
}
