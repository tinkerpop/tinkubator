package gov.lanl.cnls.linkedprocess;

import org.jivesoftware.smack.XMPPConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:03:25 AM
 */
public class XmppVirtualMachineTest extends TestCase {

    private static String username = "linked.process.3@gmail.com";
    private static String password = "linked34";
    private static String server = "talk1.l.google.com";
    private static int port = 5222;
	private XmppVirtualMachine xmppVirtualMachine;

    @Before
    public void setUp() throws Exception {
    	xmppVirtualMachine = new XmppVirtualMachine(server, port, username, password, null);
    }
    @Test
    public void testLogin() throws Exception {
        XMPPConnection connection = xmppVirtualMachine.getConnection();
		assertTrue(connection.isConnected());
		
    }
    @After
    public void tearDown() {
    	xmppVirtualMachine.shutDown();
    }
}
