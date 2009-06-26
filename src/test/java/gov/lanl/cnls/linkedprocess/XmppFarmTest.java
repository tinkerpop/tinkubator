package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

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

    private XmppFarm xmppFarm;

    public static void main(String[] args) throws Exception {
        new XmppFarm(server, port, username, password);
    }

    @Before
    public void setUp() throws Exception {
    	xmppFarm = new XmppFarm(server, port, username, password);
    }
    @Test
    public void testLogin() throws Exception {
        assertTrue(true);
    }
    @After
    public void tearDown() {
    	xmppFarm.shutDown();
    }
}
