package gov.lanl.cnls.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;

public class SmackPlay {
    
    private static String username = "USERNAME";
    private static String password = "PASSWORD";
    
    public static class MessageParrot implements PacketListener {
        private XMPPConnection xmppConnection;
        
        public MessageParrot(XMPPConnection conn) {
            xmppConnection = conn;
        }
        
        public void processPacket(Packet packet) {
			System.out.println("PACKET:");
			System.out.println(packet.toXML() + "\n\n");
        }
    };
    
    
    public static void main( String[] args ) throws Exception {
        XMPPConnection.DEBUG_ENABLED = true;
        System.out.println("Starting IM client");
        
        // gtalk requires this or your messages bounce back as errors
        ConnectionConfiguration connConfig = new ConnectionConfiguration("talk1.l.google.com", 5222);
        XMPPConnection connection = new XMPPConnection(connConfig);
		SASLAuthentication sasl = connection.getSASLAuthentication();
		//sasl.supportSASLMechanism("PLAIN");
        
        try {
            connection.connect();
            System.out.println("Connected to " + connection.getHost());
        } catch (XMPPException ex) {
            //ex.printStackTrace();
            System.out.println("Failed to connect to " + connection.getHost());
			System.exit(1);
        }
        try {
            connection.login(username, password);
            System.out.println("Logged in as " + connection.getUser());
            
            //Presence presence = new Presence(Presence.Type.available);
            //connection.sendPacket(presence);
            
        } catch (XMPPException ex) {
            System.out.println("Failed to log in as " + username);
            System.out.println(ex);
			System.exit(1);
        }
        
        //PacketFilter filter = new PacketTypeFilter(IQ.class);
        connection.addPacketListener(new MessageParrot(connection), null);
       
        
        //connection.disconnect();
    }
}

