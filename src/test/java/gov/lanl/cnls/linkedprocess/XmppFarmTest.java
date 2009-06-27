package gov.lanl.cnls.linkedprocess;

import static org.junit.Assert.assertTrue;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.Spawn;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.tools.XmppTestClient;

import org.jivesoftware.smack.XMPPConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * User: marko Date: Jun 25, 2009 Time: 12:10:47 PM
 */
@RunWith(JUnit4.class)
public class XmppFarmTest {

	private static String username1 = "linked.process.4@gmail.com";
	private static String password1 = "linked45";
	private static String username2 = "linked.process.2@gmail.com";
	private static String password2 = "linked23";

	private static String server = "talk1.l.google.com";
	private static int port = 5222;

	private static XmppFarm xmppFarm;
	private static XmppTestClient client;

	public static void main(String[] args) throws Exception {
		new XmppFarm(server, port, username1, password1);
	}

	private XMPPConnection mockConnection;


	@Before
	public void setup() throws Exception {

		xmppFarm = new XmppFarm(server, port, username1, password1);
		Thread.sleep(1000);
		client = new XmppTestClient(server, port, username2, password2);
		Thread.sleep(1000);
	}

	@Test
	public void testLogin() throws Exception {
		assertTrue(xmppFarm.getConnection().isConnected());
		assertTrue(client.getConnection().isConnected());
		assertTrue(xmppFarm.getConnection().isAuthenticated());
		assertTrue(client.getConnection().isAuthenticated());
	}

	/*
	 * public void testSubscribe() throws Exception {
	 * xmppTest.subscribe(xmppFarm.getFullJid()); Thread.sleep(10000);
	 * assertTrue(xmppFarm.getRoster().contains(username2));
	 * assertTrue(xmppTest.getRoster().contains(username1));
	 * xmppTest.unsubscribe(xmppFarm.getBareJid()); Thread.sleep(5000);
	 * assertFalse(xmppTest.getRoster().contains(username1));
	 * assertFalse(xmppFarm.getRoster().contains(username2)); }
	 */

	@Test
	public void testSpawning() throws Exception {
		Spawn spawn = new Spawn();
		spawn.setTo(xmppFarm.getFullJid());
		spawn.setPacketID("abcd");
		client.getConnection().sendPacket(spawn);

	}

	@After
	public void teardown() {
		xmppFarm.shutDown();
		client.shutDown();
	}
}
