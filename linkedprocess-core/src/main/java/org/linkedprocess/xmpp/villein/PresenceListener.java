package org.linkedprocess.xmpp.villein;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:44 AM
 */
public class PresenceListener implements PacketListener {

    XmppVillein xmppVillein;

    public PresenceListener(XmppVillein xmppVillein) {
        this.xmppVillein = xmppVillein;
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());


        if (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed) {
            this.xmppVillein.removeStruct(packet.getFrom());
            return;
        }

        //Struct struct = xmppVillein.getStruct(packet.getFrom());
        //if(null != struct && (struct instanceof HostStruct || struct instanceof FarmStruct || struct instanceof VmStruct)) {
        //    struct.setPresence(presence);
        //} else {
            DiscoverInfo discoInfo = this.getDiscoInfo(packet.getFrom());
    
            if (LinkedProcess.isBareJid(packet.getFrom())) {
                Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom(), XmppVillein.StructType.HOST);
                if (checkStruct == null) {
                    HostStruct hostStruct = new HostStruct();
                    hostStruct.setFullJid(packet.getFrom());
                    hostStruct.setPresence(presence);
                    this.xmppVillein.addHostStruct(hostStruct);
                } else {
                    checkStruct.setPresence(presence);
                }
            } else if (isFarm(discoInfo)) {
                Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom(), XmppVillein.StructType.FARM);
                if (checkStruct == null) {
                    FarmStruct farmStruct = new FarmStruct();
                    farmStruct.setFullJid(packet.getFrom());
                    farmStruct.setPresence(presence);
                    farmStruct.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                    this.xmppVillein.addFarmStruct(farmStruct);
                } else {
                    checkStruct.setPresence(presence);
                }

            } else {
                // ONLY REPRESENT THOSE VMS THAT YOU HAVE SPAWNEDs
                Struct checkStruct = this.xmppVillein.getStruct(packet.getFrom());
                if (checkStruct != null) {
                    checkStruct.setPresence(presence);
                }
            }
        //}
    }

    protected boolean isFarm(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_FARM_NAMESPACE);
        else
            return false;
    }

    protected Collection<String> getSupportedVmSpecies(DiscoverInfo discoInfo) {
        if (discoInfo != null) {
            List<String> supportedVmSpecies = new LinkedList<String>();
            try {
                Document doc = LinkedProcess.createXMLDocument(discoInfo.toXML());
                Element queryElement = doc.getRootElement().getChild("query", Namespace.getNamespace(LinkedProcess.DISCO_INFO_NAMESPACE));
                Element xElement = queryElement.getChild("x", Namespace.getNamespace(LinkedProcess.X_NAMESPACE));
                for (Element field : (List<Element>) xElement.getChildren()) {
                    if (field.getAttributeValue("var").equals("vm_species")) {
                        for (Element option : (List<Element>) field.getChildren("option", Namespace.getNamespace(LinkedProcess.X_NAMESPACE))) {
                            Element value = option.getChild("value", Namespace.getNamespace(LinkedProcess.X_NAMESPACE));
                            if (value != null) {
                                String vmSpecies = value.getText();
                                if (vmSpecies != null && vmSpecies.length() > 0)
                                    supportedVmSpecies.add(vmSpecies);
                            }
                        }
                    }
                }
                return supportedVmSpecies;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return new HashSet<String>();
        }
    }

    protected DiscoverInfo getDiscoInfo(String jid) {
        ServiceDiscoveryManager discoManager = this.xmppVillein.getDiscoManager();
        try {
            return discoManager.discoverInfo(jid);
        } catch (XMPPException e) {
            XmppVillein.LOGGER.severe("XmppException with DiscoveryManager.");
            return null;
        }
    }


    protected boolean isVirtualMachine(String jid) {
        ServiceDiscoveryManager discoManager = this.xmppVillein.getDiscoManager();
        try {
            DiscoverInfo discoInfo = discoManager.discoverInfo(jid);
            return discoInfo.containsFeature(LinkedProcess.LOP_VM_NAMESPACE);
        } catch (XMPPException e) {
            XmppVillein.LOGGER.severe("XmppException with DiscoveryManager.");
            return false;
        }
    }
}
