package org.linkedprocess.xmpp.countryside;

import org.linkedprocess.xmpp.XmppClient;
import org.linkedprocess.LinkedProcess;

import java.util.logging.Logger;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 11:33:10 AM
 */
public class XmppCountryside extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppCountryside.class);
    public static final String RESOURCE_PREFIX = "LoPCountryside";
    public static final String STATUS_MESSAGE = "LoP Countryside v0.1";
    protected LinkedProcess.CountrysideStatus status;
    
}
