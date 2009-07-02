package gov.lanl.cnls.linkedprocess;

import static org.junit.Assert.assertTrue;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.SpawnVm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.TerminateVm;
import gov.lanl.cnls.linkedprocess.xmpp.tools.XmppTestClient;

import org.jivesoftware.smack.XMPPConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import junit.framework.TestCase;

/**
 * User: marko Date: Jun 25, 2009 Time: 12:10:47 PM
 */
@RunWith(JUnit4.class)
public class XmppFarmTest extends TestCase {

	private static String username1 = "linked.process.1@xmpp.linkedprocess.org";
	private static String password1 = "linked12";
	private static String username2 = "linked.process.2@xmpp.linkedprocess.org";
	private static String password2 = "linked23";

	private static String server1 = "xmpp.linkedprocess.org";
    private static String server2 = "talk1.l.google.com";

    private static int port = 5222;

	private static XmppFarm xmppFarm;
	private static XmppTestClient xmppApp;

	public static void main(String[] args) throws Exception {
		new XmppFarm(server1, port, username1, password1);
    }

    private XMPPConnection mockConnection;


	@Before
	public void setup() throws Exception {

		xmppFarm = new XmppFarm(server1, port, username1, password1);
		Thread.sleep(1000);
		xmppApp = new XmppTestClient(server1, port, username2, password2);
		Thread.sleep(1000);
	}

	@Test
	public void testLogin() throws Exception {
		assertTrue(xmppFarm.getConnection().isConnected());
		assertTrue(xmppApp.getConnection().isConnected());
		assertTrue(xmppFarm.getConnection().isAuthenticated());
		assertTrue(xmppApp.getConnection().isAuthenticated());
	}

    //@Test
	/*public void testSubscribe() throws Exception
      {
          xmppFarm = new XmppFarm(server1, port, username1, password1);
		Thread.sleep(1000);
		xmppApp = new XmppTestClient(server1, port, username2, password2);
		Thread.sleep(1000);

          xmppApp.subscribe(xmppFarm.getFullJid());
          Thread.sleep(10000);
          assertTrue(xmppFarm.getRoster().contains(username2));
          assertTrue(xmppApp.getRoster().contains(username1));
          xmppApp.unsubscribe(xmppFarm.getBareJid());
          Thread.sleep(5000);
          assertFalse(xmppApp.getRoster().contains(username1));
          assertFalse(xmppFarm.getRoster().contains(username2));
    }*/


	@Test
	public void testSpawning() throws Exception {
		SpawnVm spawnVm = new SpawnVm();
		spawnVm.setTo(xmppFarm.getFullJid());
		spawnVm.setPacketID("abcd");
		xmppApp.getConnection().sendPacket(spawnVm);

	}


    public void testSpawnTag() throws Exception {
        SpawnVm spawnVm = new SpawnVm();
        spawnVm.setVmJid("lp1@gmail.com");
        spawnVm.setVmSpecies("lop:vm:javascript");
        String spawnString = spawnVm.getChildElementXML();
        System.out.println(spawnString);
        assertTrue(spawnString.contains("xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE));
        assertTrue(spawnString.contains("vm_jid=\"lp1@gmail.com\""));
        assertTrue(spawnString.contains("vm_species=\"lop:vm:javascript\""));
    }

    public void testTerminateTag() throws Exception {
        TerminateVm terminateVm = new TerminateVm();
        terminateVm.setVmJid("lp1@gmail.com");
        String destroyString = terminateVm.getChildElementXML();
        System.out.println(destroyString);
        assertTrue(destroyString.contains("xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE));
        assertTrue(destroyString.contains("vm_jid=\"lp1@gmail.com\""));
    }

    @After
	public void teardown() {
		xmppFarm.shutDown();
		xmppApp.shutDown();
	}
}
