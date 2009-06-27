package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.Spawn;
import gov.lanl.cnls.linkedprocess.xmpp.tools.SmackPlay;
import gov.lanl.cnls.linkedprocess.xmpp.tools.XmppTestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 12:10:47 PM
 */
public class XmppFarmTest extends TestCase {

    private static String username1 = "linked.process.1@gmail.com";
    private static String password1 = "linked12";
    private static String username2 = "linked.process.2@gmail.com";
    private static String password2 = "linked23";


    private static String server = "talk1.l.google.com";
    private static int port = 5222;

    private XmppFarm xmppFarm;
    private XmppTestClient xmppTest;

    public static void main(String[] args) throws Exception {
        new XmppFarm(server, port, username1, password1);
    }

    public void setUp() throws Exception {
    	xmppFarm = new XmppFarm(server, port, username1, password1);
        xmppTest = new XmppTestClient(server, port, username2, password2);
    }

    public void testLogin() throws Exception {
    	assertTrue(xmppFarm.getConnection().isConnected());
        assertTrue(xmppTest.getConnection().isConnected());
        assertTrue(xmppFarm.getConnection().isAuthenticated());
        assertTrue(xmppTest.getConnection().isAuthenticated());     
    }

    public void testSubscribe() throws Exception {
        /*xmppTest.subscribe(xmppFarm.getFullJid());
        Thread.sleep(5000);
        assertTrue(xmppFarm.getRoster().contains(username2));
        assertTrue(xmppTest.getRoster().contains(username1));
        xmppTest.unsubscribe(xmppFarm.getBareJid());
        Thread.sleep(5000);
        assertFalse(xmppTest.getRoster().contains(username1));
        assertFalse(xmppFarm.getRoster().contains(username2));*/
    }

    public void testSpawn() throws Exception {
        Spawn spawn = new Spawn();
        spawn.setTo(xmppFarm.getFullJid());
        spawn.setPacketID("abcd");
        xmppTest.getConnection().sendPacket(spawn);
    }

    public void tearDown() {
    	xmppFarm.shutDown();
        xmppTest.shutDown();
    }
}
