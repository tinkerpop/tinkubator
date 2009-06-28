package gov.lanl.cnls.linkedprocess.xmpp.tools;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 5:09:45 PM
 */
public class XmppTestClient extends XmppClient {

    protected Roster roster;

    public XmppTestClient(String server, int port, String username, String password) {

         LOGGER.info("Starting LoP test client");

        try {
            this.logon(server, port, username, password, "test/");
            this.initiateFeatures();
            //this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.error("error: " + e);
            System.exit(1);
        }

        this.roster = connection.getRoster();
        this.roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    }
}
