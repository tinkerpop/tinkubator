package org.linkedprocess.xmpp;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;

import java.util.HashMap;
import java.util.Map;

/**
 * User: marko
 * Date: Jul 24, 2009
 * Time: 9:52:06 AM
 */
public class LopXmppError extends XMPPError {

    LinkedProcess.LopErrorType lopErrorType;
    public static final Map<Condition, Integer> conditionCodeMap = new HashMap<Condition, Integer>();
    public static final Map<Condition, XMPPError.Type> conditionErrorMap = new HashMap<Condition, XMPPError.Type>();

    static {
        conditionCodeMap.put(Condition.interna_server_error, 500);
        conditionCodeMap.put(Condition.forbidden, 403);
        conditionCodeMap.put(Condition.bad_request, 400);
        conditionCodeMap.put(Condition.item_not_found, 404);
        conditionCodeMap.put(Condition.conflict, 409);
        conditionCodeMap.put(Condition.feature_not_implemented, 501);
        conditionCodeMap.put(Condition.gone, 302);
        conditionCodeMap.put(Condition.jid_malformed, 400);
        conditionCodeMap.put(Condition.no_acceptable, 406);
        conditionCodeMap.put(Condition.not_allowed, 405);
        conditionCodeMap.put(Condition.not_authorized, 401);
        conditionCodeMap.put(Condition.payment_required, 402);
        conditionCodeMap.put(Condition.recipient_unavailable, 404);
        conditionCodeMap.put(Condition.redirect, 302);
        conditionCodeMap.put(Condition.registration_required, 407);
        conditionCodeMap.put(Condition.remote_server_not_found, 404);
        conditionCodeMap.put(Condition.remote_server_timeout, 504);
        conditionCodeMap.put(Condition.remote_server_error, 502);
        conditionCodeMap.put(Condition.resource_constraint, 500);
        conditionCodeMap.put(Condition.service_unavailable, 503);
        conditionCodeMap.put(Condition.subscription_required, 407);
        conditionCodeMap.put(Condition.undefined_condition, 500);
        conditionCodeMap.put(Condition.unexpected_request, 400);
        conditionCodeMap.put(Condition.request_timeout, 408);
    }

    static {
        conditionErrorMap.put(Condition.interna_server_error, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.forbidden, XMPPError.Type.AUTH);
        conditionErrorMap.put(Condition.bad_request, XMPPError.Type.MODIFY);
        conditionErrorMap.put(Condition.item_not_found, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.conflict, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.feature_not_implemented, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.gone, XMPPError.Type.MODIFY);
        conditionErrorMap.put(Condition.jid_malformed, XMPPError.Type.MODIFY);
        conditionErrorMap.put(Condition.no_acceptable, XMPPError.Type.MODIFY);
        conditionErrorMap.put(Condition.not_allowed, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.not_authorized, XMPPError.Type.AUTH);
        conditionErrorMap.put(Condition.payment_required, XMPPError.Type.AUTH);
        conditionErrorMap.put(Condition.recipient_unavailable, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.redirect, XMPPError.Type.MODIFY);
        conditionErrorMap.put(Condition.registration_required, XMPPError.Type.AUTH);
        conditionErrorMap.put(Condition.remote_server_not_found, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.remote_server_timeout, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.remote_server_error, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.resource_constraint, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.service_unavailable, XMPPError.Type.CANCEL);
        conditionErrorMap.put(Condition.subscription_required, XMPPError.Type.AUTH);
        conditionErrorMap.put(Condition.undefined_condition, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.unexpected_request, XMPPError.Type.WAIT);
        conditionErrorMap.put(Condition.request_timeout, XMPPError.Type.CANCEL);
    }


    public LopXmppError(XMPPError.Condition condition, LinkedProcess.LopErrorType lopErrorType, String errorMessage) {
        super(LopXmppError.conditionCodeMap.get(condition), LopXmppError.conditionErrorMap.get(condition), condition.toString().toLowerCase(), errorMessage, null);
        this.lopErrorType = lopErrorType;
    }

    public String toXML() {
        Element errorElement = new Element(LinkedProcess.ERROR_TAG);
        errorElement.setAttribute(LinkedProcess.CODE_ATTRIBUTE, "" + this.getCode());
        errorElement.setAttribute(LinkedProcess.TYPE_ATTRIBUTE, this.getType().toString().toLowerCase());
        Element conditionElement = new Element(this.getCondition(), Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
        Element lopElement = new Element(lopErrorType.toString(), Namespace.getNamespace(LinkedProcess.LOP_NAMESPACE));
        errorElement.addContent(conditionElement);
        errorElement.addContent(lopElement);
        if (this.getMessage() != null) {
            Element textElement = new Element(LinkedProcess.TEXT_TAG, Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
            textElement.setText(this.getMessage().replaceAll(">", "").replaceAll("<", ""));
            errorElement.addContent(textElement);
        }
        return LinkedProcess.xmlOut.outputString(errorElement);
    }

}
