package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;

/**
 * User: marko
 * Date: Jun 23, 2009
 * Time: 11:01:06 AM
 */
public class XmppVirtualMachine extends XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppVirtualMachine.class);
    public static String SCRIPT_ENGINE_NAME = "JavaScript";
    public static String RESOURCE_PREFIX = "LoPVM/";

    public static enum VirtualMachinePresence { AVAILABLE, TOO_MANY_JOBS }

    protected ScriptEngine engine;
    protected VirtualMachinePresence currentPresence;
	private static boolean shutdownRequested = false;
	private boolean shutdown = false;

    public XmppVirtualMachine(final String server, final int port, final String username, final String password) throws Exception {

        LOGGER.info("Starting " + SCRIPT_ENGINE_NAME + " LoP virtual machine");

        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName(SCRIPT_ENGINE_NAME);

        // Registering the types of IQ packets/stanzas the the Lop VM can respond to.
        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(Evaluate.EVALUATION_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE, new EvaluateProvider());
        pm.addIQProvider(Status.STATUS_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE, new StatusProvider());
        pm.addIQProvider(Cancel.CANCEL_TAGNAME, LinkedProcess.LOP_VM_NAMESPACE, new CancelProvider());

        try {
            this.logon(server, port, username, password);
            this.initiateFeatures();
            this.printClientStatistics();
        } catch (XMPPException e) {
            LOGGER.error("error: " + e);
            System.exit(1);
        }

        PacketFilter evalFilter = new AndFilter(new PacketTypeFilter(Evaluate.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter statusFilter = new AndFilter(new PacketTypeFilter(Status.class), new IQTypeFilter(IQ.Type.GET));
        PacketFilter cancelFilter = new AndFilter(new PacketTypeFilter(Cancel.class), new IQTypeFilter(IQ.Type.GET));

        connection.addPacketListener(new EvaluateListener(engine, connection), evalFilter);
        connection.addPacketListener(new StatusListener(connection), statusFilter);
        connection.addPacketListener(new CancelListener(connection), cancelFilter);

        Thread shutdownHook = new Thread(new Runnable() {

			@Override
			public void run() {
				// process packets until a quit command is sent.
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				// TODO Auto-generated method stub
		        try {
					while (!shutdownRequested) {
						Thread.sleep(10);
					}//!br.readLine().equals("quit") || 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        LOGGER.info("shutting down.");
		        logout();
		        shutdown=true;
				
			}
        });
        shutdownHook.start();
        	

    }

    public void logon(String server, int port, String username, String password) throws XMPPException {

        super.logon(server, port, username, password, RESOURCE_PREFIX);
        connection.sendPacket(this.createVMPresence(VirtualMachinePresence.AVAILABLE));
    }

    public void sendPresence(Presence presence) {
        this.connection.sendPacket(presence);
    }


    public void printClientStatistics() {
        super.printClientStatistics();
        LOGGER.info("Script Engine Name: " + engine.getFactory().getEngineName());
        LOGGER.info("Script Engine Version: " + engine.getFactory().getEngineVersion());
        LOGGER.info("Script Engine Language: " + engine.getFactory().getLanguageName());
        LOGGER.info("Script Engine Language Version: " + engine.getFactory().getLanguageVersion());
    }

    public final Presence createVMPresence(final VirtualMachinePresence type) {
        String statusMessage = engine.getFactory().getLanguageName() + "(" + engine.getFactory().getLanguageVersion() + "):" + engine.getFactory().getEngineName() + "(" + engine.getFactory().getEngineVersion() + ")";
        if(type == VirtualMachinePresence.AVAILABLE) {
            return new Presence(Presence.Type.available, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.available);
        } else  {
            return new Presence(Presence.Type.unavailable, statusMessage, LinkedProcess.LOWEST_PRIORITY, Presence.Mode.dnd);
        }
    }

	public void shutDown() {
		LOGGER.info("requesting shutdown");
		shutdownRequested = true;
		while(!shutdown ) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
	}
}
