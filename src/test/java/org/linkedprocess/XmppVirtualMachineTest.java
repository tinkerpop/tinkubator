package org.linkedprocess;

import org.linkedprocess.xmpp.farm.XmppFarm;
import org.linkedprocess.xmpp.vm.*;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jivesoftware.smack.packet.IQ;

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
    public static final String XMPP42_SERVER = "xmpp42.linkedprocess.org";
    public static final int PORT = 5222;

    private XmppVirtualMachine xmppVirtualMachine;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLoginXmpp42() throws Exception {
        xmppVirtualMachine = new XmppVirtualMachine(XMPP42_SERVER, PORT, USERNAME, PASSWORD, null, null, LinkedProcess.JAVASCRIPT, "pass");
        Connection connection = xmppVirtualMachine.getConnection();
        assertTrue(connection.isConnected());
        xmppVirtualMachine.shutDown();

    }

    /*@Test
    public void testLoginOPS4J() throws Exception {
        xmppVirtualMachine = new XmppVirtualMachine(OPS4J_SERVER, PORT, USERNAME, PASSWORD, null, null, LinkedProcess.JAVASCRIPT, "pass");
        Connection connection = xmppVirtualMachine.getConnection();
        assertTrue(connection.isConnected());
        xmppVirtualMachine.shutDown();

    }*/

    @Test
    public void testLoginGTalk() throws Exception {
        xmppVirtualMachine = new XmppVirtualMachine(GTALK_SERVER, PORT, GTALK_USERNAME, PASSWORD, null, null, LinkedProcess.JAVASCRIPT, "pass");
        Connection connection = xmppVirtualMachine.getConnection();
        assertTrue(connection.isConnected());
        xmppVirtualMachine.shutDown();

    }

    @Test
    public void testSubmitJobTag() {
        SubmitJob submitJob = new SubmitJob();
        submitJob.setVmPassword("pass");
        submitJob.setExpression("for(int i=0; i<10; i++) { i; };");
        String evalString = submitJob.getChildElementXML();
        System.out.println(evalString);
        assertTrue(evalString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        // note that XML characters must be handled correctly
        assertTrue(evalString.contains("for(int i=0; i&lt;10; i++) { i; };"));
        assertTrue(evalString.contains("vm_password=\"pass\""));
    }

    @Test
    public void testManageBindingsTag() {
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setType(IQ.Type.SET);
        manageBindings.addBinding("name", "marko");
        manageBindings.addBinding("age", "29");
        assertTrue(manageBindings.toXML().contains("<binding name=\"age\" value=\"29\" />"));
        manageBindings.setType(IQ.Type.GET);
        assertFalse(manageBindings.toXML().contains("<binding name=\"age\" value=\"29\" />"));
        assertTrue(manageBindings.toXML().contains("<binding name=\"age\" />"));
    }

    @Test
    public void testAbortJobTag() {
        AbortJob abortJob = new AbortJob();
        abortJob.setJobId("wxyz");
        abortJob.setVmPassword("pass");
        String abortString = abortJob.getChildElementXML();
        System.out.println(abortString);
        assertTrue(abortString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(abortString.contains("job_id=\"wxyz\""));
        assertTrue(abortString.contains("vm_password=\"pass\""));
    }

    @Test
    public void testTerminateTag() throws Exception {
        TerminateVm terminateVm = new TerminateVm();
        terminateVm.setVmPassword("pass");
        String terminateString = terminateVm.getChildElementXML();
        System.out.println(terminateString);
        assertTrue(terminateString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(terminateString.contains("vm_password=\"pass\""));
    }

    @Test
    public void testSpawnVirtualMachine() throws Exception {
        XmppFarm farm = new XmppFarm(XMPP42_SERVER, PORT, USERNAME, PASSWORD);
        XmppVirtualMachine vm = farm.spawnVirtualMachine("appJid", LinkedProcess.JAVASCRIPT);
        farm.shutDown();
    }

    @Test
    public void testExecuteScript() throws Exception {
        XmppFarm farm = new XmppFarm(XMPP42_SERVER, PORT, USERNAME, PASSWORD);
        XmppVirtualMachine vm = farm.spawnVirtualMachine("appJid", LinkedProcess.JAVASCRIPT);
        farm.shutDown();
    }
}
