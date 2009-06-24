package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.apache.log4j.Logger;

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

    public static Logger logger = Logger.getLogger(LopVirtualMachine.class);

    private static String username = "linked.process.1@gmail.com";
    private static String password = "linked12";
    private static String server = "talk1.l.google.com";
    private static int port = 5222;

    public static String SCRIPT_ENGINE_NAME = "JavaScript";
    public static String RESOURCE_PREFIX = "LoPVM/";
    public static String LOP_NAMESPACE = "http://linkedprocess.org/";
    public static String DISCO_INFO = "http://jabber.org/protocol/disco#info";

    protected ScriptEngine engine;
    protected XMPPConnection connection;

    public static void main(String[] args) throws Exception {
        new LopVirtualMachine();
    }

    public LopVirtualMachine() throws Exception {

        logger.info("Starting " + SCRIPT_ENGINE_NAME + " LoP virtual machine");

        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName(SCRIPT_ENGINE_NAME);

        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(Evaluate.EVALUATION_TAGNAME, LOP_NAMESPACE, new EvaluateProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            this.printClientStatistics();
        } catch (XMPPException e) {
            logger.error("error: " + e);
            System.exit(1);
        }

        PacketFilter evalFilter = new AndFilter(new PacketTypeFilter(Evaluate.class), new IQTypeFilter(IQ.Type.GET));
        connection.addPacketListener(new EvaluatePacketListener(engine, connection), evalFilter);

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

        // if connection is still active, disconnect it.
        if (null != connection && connection.isConnected()) {
            this.logout();
        }

        // logging into an XMPP server requires a username and password
        ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port);
        //connConfig.setSASLAuthenticationEnabled(true);
        this.connection = new XMPPConnection(connConfig);
        this.connection.connect();

        logger.info("Connected to " + connection.getHost());
        connection.login(username, password, RESOURCE_PREFIX);
        logger.info("Logged in as " + connection.getUser());
        String statusMessage = engine.getFactory().getLanguageName() + "(" + engine.getFactory().getLanguageVersion() + "):" + engine.getFactory().getEngineName() + "(" + engine.getFactory().getEngineVersion() + ")";
        Presence presence = new Presence(Presence.Type.available, statusMessage, -127, Presence.Mode.available);
        connection.sendPacket(presence);
    }

    public void logout() {
        logger.info("Disconnecting from " + connection.getHost());
        connection.disconnect(new Presence(Presence.Type.unavailable));
    }

    public void printClientStatistics() {
        // print a collection of statistics about the connection and virtual machine
        logger.info("Anonymous: " + connection.isAnonymous());
        logger.info("Authenticated: " + connection.isAuthenticated());
        logger.info("Connected: " + connection.isConnected());
        logger.info("Secure: " + connection.isSecureConnection());
        logger.info("Compression: " + connection.isUsingCompression());
        logger.info("Transport Layer Security: " + connection.isUsingTLS());
        logger.info("Script Engine Name: " + engine.getFactory().getEngineName());
        logger.info("Script Engine Version: " + engine.getFactory().getEngineVersion());
        logger.info("Script Engine Language: " + engine.getFactory().getLanguageName());
        logger.info("Script Engine Language Version: " + engine.getFactory().getLanguageVersion());
    }
}
