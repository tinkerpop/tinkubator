package gov.lanl.cnls.linkedprocess;

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

    /*public static void main(String[] args) throws Exception {
        XmppVirtualMachine vm = new XmppVirtualMachine(server, port, username, password);
    }*/
    
    @Before
    public void setUp() throws Exception {
    	xmppVirtualMachine = new XmppVirtualMachine(server, port, username, password);
    }
    @Test
    public void testLogin() throws Exception {
        assertTrue(true);
    }
    @After
    public void tearDown() {
    	xmppVirtualMachine.shutDown();
    }
}
