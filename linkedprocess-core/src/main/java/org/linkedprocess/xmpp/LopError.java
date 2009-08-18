/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smack.packet.XMPPError;
import org.linkedprocess.LinkedProcess;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LopError extends XMPPError implements Comparable {

    protected LinkedProcess.LopErrorType lopErrorType;
    protected LinkedProcess.ClientType clientType;
    protected String packetId;

    public static final Map<Condition, Integer> conditionCodeMap = new HashMap<Condition, Integer>();
    public static final Map<Condition, XMPPError.Type> conditionErrorMap = new HashMap<Condition, XMPPError.Type>();
    public static final Map<String, Condition> stringConditionMap = new HashMap<String, Condition>();

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

    static {
        stringConditionMap.put(Condition.interna_server_error.toString(), Condition.interna_server_error);
        stringConditionMap.put(Condition.forbidden.toString(), Condition.forbidden);
        stringConditionMap.put(Condition.bad_request.toString(), Condition.bad_request);
        stringConditionMap.put(Condition.item_not_found.toString(), Condition.item_not_found);
        stringConditionMap.put(Condition.conflict.toString(), Condition.conflict);
        stringConditionMap.put(Condition.feature_not_implemented.toString(), Condition.feature_not_implemented);
        stringConditionMap.put(Condition.gone.toString(), Condition.gone);
        stringConditionMap.put(Condition.jid_malformed.toString(), Condition.jid_malformed);
        stringConditionMap.put(Condition.no_acceptable.toString(), Condition.no_acceptable);
        stringConditionMap.put(Condition.not_allowed.toString(), Condition.not_allowed);
        stringConditionMap.put(Condition.not_authorized.toString(), Condition.not_authorized);
        stringConditionMap.put(Condition.payment_required.toString(), Condition.payment_required);
        stringConditionMap.put(Condition.recipient_unavailable.toString(), Condition.recipient_unavailable);
        stringConditionMap.put(Condition.redirect.toString(), Condition.redirect);
        stringConditionMap.put(Condition.registration_required.toString(), Condition.registration_required);
        stringConditionMap.put(Condition.remote_server_not_found.toString(), Condition.remote_server_not_found);
        stringConditionMap.put(Condition.remote_server_timeout.toString(), Condition.remote_server_timeout);
        stringConditionMap.put(Condition.remote_server_error.toString(), Condition.remote_server_error);
        stringConditionMap.put(Condition.resource_constraint.toString(), Condition.resource_constraint);
        stringConditionMap.put(Condition.service_unavailable.toString(), Condition.service_unavailable);
        stringConditionMap.put(Condition.subscription_required.toString(), Condition.subscription_required);
        stringConditionMap.put(Condition.undefined_condition.toString(), Condition.undefined_condition);
        stringConditionMap.put(Condition.unexpected_request.toString(), Condition.unexpected_request);
        stringConditionMap.put(Condition.request_timeout.toString(), Condition.request_timeout);
    }


    public LopError(XMPPError.Condition condition, LinkedProcess.LopErrorType lopErrorType, String errorMessage, LinkedProcess.ClientType clientType, String packetId) {
        super(LopError.conditionCodeMap.get(condition), LopError.conditionErrorMap.get(condition), condition.toString().toLowerCase(), errorMessage, null);
        this.lopErrorType = lopErrorType;
        this.clientType = clientType;
        this.packetId = packetId;
    }

    public LinkedProcess.LopErrorType getLopErrorType() {
        return this.lopErrorType;
    }

    public LinkedProcess.ClientType getClientType() {
        return this.clientType;
    }

    public String getPacketId() {
        return this.packetId;
    }

    public String toXML() {
        Element errorElement = new Element(LinkedProcess.ERROR_TAG);
        errorElement.setAttribute(LinkedProcess.CODE_ATTRIBUTE, "" + this.getCode());
        errorElement.setAttribute(LinkedProcess.TYPE_ATTRIBUTE, this.getType().toString().toLowerCase());
        Element conditionElement = new Element(this.getCondition(), Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
        Element lopElement = new Element(lopErrorType.toString(), this.clientType == LinkedProcess.ClientType.FARM ? Namespace.getNamespace(LinkedProcess.LOP_FARM_NAMESPACE) : Namespace.getNamespace(LinkedProcess.LOP_VM_NAMESPACE));
        errorElement.addContent(conditionElement);
        errorElement.addContent(lopElement);
        if (this.getMessage() != null) {
            Element textElement = new Element(LinkedProcess.TEXT_TAG, Namespace.getNamespace(LinkedProcess.XMPP_STANZAS_NAMESPACE));
            textElement.setText(this.getMessage().replaceAll("<", "").replaceAll(">", "").replaceAll("&", ""));
            errorElement.addContent(textElement);
        }
        return LinkedProcess.xmlOut.outputString(errorElement);
    }

    public String toString() {
        if (null != lopErrorType)
            return lopErrorType.toString() + ":" + this.getCondition() + "[" + this.getCode() + "]: " + this.getMessage();
        else
            return this.getCondition() + "[" + this.getCode() + "]: " + this.getMessage();
    }

    public int compareTo(Object object) {
        if (object instanceof LopError)
            return packetId.compareTo(((LopError) object).getPacketId());
        else
            throw new ClassCastException();
    }

}
