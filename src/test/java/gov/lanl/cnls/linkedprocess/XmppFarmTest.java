package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:10:47 PM
 */
public class XmppFarmTest extends TestCase {

    private static String username = "linked.process.1@gmail.com";
    private static String password = "linked12";
    private static String server = "talk1.l.google.com";
    private static int port = 5222;

    public static void main(String[] args) throws Exception {
        new XmppFarm(server, port, username, password);
    }

    public void testLogin() throws Exception {
        assertTrue(true);
    }
}
