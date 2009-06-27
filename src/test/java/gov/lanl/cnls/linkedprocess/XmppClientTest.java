package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;
import gov.lanl.cnls.linkedprocess.xmpp.tools.XmppTestClient;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 5:52:06 PM
 */
public class XmppClientTest extends TestCase {

    private static String username = "linked.process.1@gmail.com";
    private static String password = "linked12";
    private static String server = "talk1.l.google.com";
    private static int port = 5222;

    public void testJidConversion() {
        assertEquals(XmppClient.generateBareJid("linked.process.1@gmail.com/test/12345"), username);
    }
}
