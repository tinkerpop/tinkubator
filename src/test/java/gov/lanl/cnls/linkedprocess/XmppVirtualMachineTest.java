package gov.lanl.cnls.linkedprocess;

import static org.junit.Assert.assertTrue;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Cancel;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

import org.jivesoftware.smack.XMPPConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:03:25 AM
 */
public class XmppVirtualMachineTest {

    private static String username = "linked.process.3";
    private static String gtalk_username = "linked.process.3@gmail.com";
    private static String password = "linked34";
    private static String ops4j_server = "srv03.codedragons.com";
    private static String gtalk_server = "talk1.l.google.com";
    private static int port = 5222;
	private XmppVirtualMachine xmppVirtualMachine;

    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testLoginOPS4J() throws Exception {
    	xmppVirtualMachine = new XmppVirtualMachine(ops4j_server, port, username, password, null);
        XMPPConnection connection = xmppVirtualMachine.getConnection();
		assertTrue(connection.isConnected());
		xmppVirtualMachine.shutDown();
		
    }
    @Test
    public void testLoginGTalk() throws Exception {
    	xmppVirtualMachine = new XmppVirtualMachine(gtalk_server, port, gtalk_username, password, null);
        XMPPConnection connection = xmppVirtualMachine.getConnection();
		assertTrue(connection.isConnected());
		xmppVirtualMachine.shutDown();
		
    }
    @After
    public void tearDown() {
    }

     public void testEvaluateTag() {
        Evaluate eval = new Evaluate();
        eval.setExpression("for(int i=0; i<10; i++) { i; };");
        String evalString = eval.getChildElementXML();
        System.out.println(evalString);
        assertTrue(evalString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        // note that XML characters must be handled correctly
        assertTrue(evalString.contains("for(int i=0; i&lt;10; i++) { i; };"));
    }

    public void testCancelTag() {
        Cancel cancel = new Cancel();
        cancel.setJobId("wxyz");
        String cancelString = cancel.getChildElementXML();
        System.out.println(cancelString);
        assertTrue(cancelString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(cancelString.contains("job_id=\"wxyz\""));
    }
}
