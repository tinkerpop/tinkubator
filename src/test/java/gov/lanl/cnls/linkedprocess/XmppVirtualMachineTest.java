package gov.lanl.cnls.linkedprocess;

import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.AbandonJob;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import junit.framework.TestCase;
import org.jivesoftware.smack.XMPPConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:03:25 AM
 */
public class XmppVirtualMachineTest extends TestCase {

    public static final String USERNAME = "linked.process.3";
    public static final String GTALK_USERNAME = "linked.process.3@gmail.com";
    public static final String PASSWORD = "linked34";
    public static final String OPS4J_SERVER = "srv03.codedragons.com";
    public static final String GTALK_SERVER = "talk1.l.google.com";
    public static final int PORT = 5222;

    private XmppVirtualMachine xmppVirtualMachine;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLoginOPS4J() throws Exception {
        xmppVirtualMachine = new XmppVirtualMachine(OPS4J_SERVER, PORT, USERNAME, PASSWORD, null);
        XMPPConnection connection = xmppVirtualMachine.getConnection();
        assertTrue(connection.isConnected());
        xmppVirtualMachine.shutDown();

    }

    @Test
    public void testLoginGTalk() throws Exception {
        xmppVirtualMachine = new XmppVirtualMachine(GTALK_SERVER, PORT, GTALK_USERNAME, PASSWORD, null);
        XMPPConnection connection = xmppVirtualMachine.getConnection();
        assertTrue(connection.isConnected());
        xmppVirtualMachine.shutDown();

    }

    @Test
    public void testEvaluateTag() {
        Evaluate eval = new Evaluate();
        eval.setExpression("for(int i=0; i<10; i++) { i; };");
        String evalString = eval.getChildElementXML();
        System.out.println(evalString);
        assertTrue(evalString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        // note that XML characters must be handled correctly
        assertTrue(evalString.contains("for(int i=0; i&lt;10; i++) { i; };"));
    }

    @Test
    public void testCancelTag() {
        AbandonJob abandonJob = new AbandonJob();
        abandonJob.setJobId("wxyz");
        String cancelString = abandonJob.getChildElementXML();
        System.out.println(cancelString);
        assertTrue(cancelString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(cancelString.contains("job_id=\"wxyz\""));
    }

    @Test
    public void testSpawnVirtualMachine() throws Exception {
        XmppFarm farm = new XmppFarm(OPS4J_SERVER, PORT, USERNAME, PASSWORD);
        String vmJID = farm.spawnVirtualMachine("JavaScript");
        farm.shutDown();
    }

    @Test
    public void testExecuteScript() throws Exception {
        XmppFarm farm = new XmppFarm(OPS4J_SERVER, PORT, USERNAME, PASSWORD);
        String vmJID = farm.spawnVirtualMachine("JavaScript");
        XmppVirtualMachine vm = farm.getVirtualMachine(vmJID);


        farm.shutDown();
    }
}
