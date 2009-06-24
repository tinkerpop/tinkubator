package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import gov.lanl.cnls.linkedprocess.xmpp.lopvm.EvaluationPacketListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 23, 2009
 * Time: 11:01:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class LopVirtualMachine {


    private static String username = "linked.process.1@gmail.com";
    private static String password = "linked12";
    private static String server = "talk1.l.google.com";

    private static int port = 5222;

    public static String SCRIPT_ENGINE_NAME = "JavaScript";
    public static String RESOURCE_PREFIX = "js/";
    public static String LOP_NAMESPACE = "http://linkedprocess.org/";
    public static String DISCO_INFO = "http://jabber.org/protocol/disco#info";

    protected ScriptEngine engine;
    protected XMPPConnection connection;

    public static void main(String[] args) throws Exception {
        new LopVirtualMachine();
    }

    public LopVirtualMachine() throws Exception {

        System.out.println("Starting " + SCRIPT_ENGINE_NAME + " LoP virtual machine");

        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName(SCRIPT_ENGINE_NAME);

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            System.out.println();
            this.printClientStatistics();
            System.out.println();
        } catch (XMPPException e) {
            System.out.println(e);
            System.exit(1);
        }

        PacketFilter messageFilter = new PacketTypeFilter(Message.class);
        connection.addPacketListener(new EvaluationPacketListener(engine, connection), messageFilter);
        //connection.addPacketListener(new GenericPacketListener(), null);

        // process packets until a quit command is sent.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!br.readLine().equals("quit")) {

        }

        this.logout();


    }

    private void initiateFeatures() {
        ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
        Iterator<String> features = discoManager.getFeatures();
        while (features.hasNext()) {
            String feature = features.next();
            discoManager.removeFeature(feature);
        }
        discoManager.addFeature(LOP_NAMESPACE);
        discoManager.addFeature(DISCO_INFO);
    }

    public void logon(String server, int port, String username, String password) throws XMPPException {
        // logging into an XMPP server requires a username and password

        ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port);
        this.connection = new XMPPConnection(connConfig);
        this.connection.connect();
        System.out.println("Connected to " + connection.getHost());
        connection.login(username, password, RESOURCE_PREFIX);
        System.out.println("Logged in as " + connection.getUser());
        String statusMessage = engine.getFactory().getLanguageName() + "(" + engine.getFactory().getLanguageVersion() + "):" + engine.getFactory().getEngineName() + "(" + engine.getFactory().getEngineVersion() + ")";
        Presence presence = new Presence(Presence.Type.available, statusMessage, -127, Presence.Mode.available);
        connection.sendPacket(presence);
    }

    public void logout() {
        connection.disconnect(new Presence(Presence.Type.unavailable));
    }

    public void printClientStatistics() {
        // print a collection of statistics about the connection and virtual machine
        System.out.println("Anonymous: " + connection.isAnonymous());
        System.out.println("Authenticated: " + connection.isAuthenticated());
        System.out.println("Connected: " + connection.isConnected());
        System.out.println("Secure: " + connection.isSecureConnection());
        System.out.println("Compression: " + connection.isUsingCompression());
        System.out.println("Transport Layer Security: " + connection.isUsingTLS());
        System.out.println("Script Engine Name: " + engine.getFactory().getEngineName());
        System.out.println("Script Engine Version: " + engine.getFactory().getEngineVersion());
        System.out.println("Script Engine Language: " + engine.getFactory().getLanguageName());
        System.out.println("Script Engine Language Version: " + engine.getFactory().getLanguageVersion());
        System.out.println();
    }
}
