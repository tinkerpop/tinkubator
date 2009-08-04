package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 11:57:44 AM
 */
public class PresenceListener extends LopVilleinListener {


    public PresenceListener(XmppVillein xmppVillein) {
        super(xmppVillein);
    }

    public void processPacket(Packet packet) {
        Presence presence = (Presence) packet;

        XmppVillein.LOGGER.info("Presence received from " + presence.getFrom());
        XmppVillein.LOGGER.info(presence.toXML());


        if (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed) {
            this.getXmppVillein().removeStruct(packet.getFrom());
            return;
        }

        //Struct struct = xmppVillein.getStruct(packet.getFrom());
        //if(null != struct && (struct instanceof HostStruct || struct instanceof FarmStruct || struct instanceof VmStruct)) {
        //    struct.setPresence(presence);
        //} else {
            DiscoverInfo discoInfo = this.getDiscoInfo(packet.getFrom());
    
            if (LinkedProcess.isBareJid(packet.getFrom())) {
                //System.out.println("Bare Jid " + packet.getFrom());
                Struct checkStruct = this.getXmppVillein().getStruct(packet.getFrom(), XmppVillein.StructType.COUNTRYSIDE);
                if (checkStruct == null) {
                    CountrysideStruct countrysideStruct = new CountrysideStruct();
                    countrysideStruct.setFullJid(packet.getFrom());
                    countrysideStruct.setPresence(presence);
                    this.getXmppVillein().addCountrysideStruct(countrysideStruct);
                } else {
                    checkStruct.setPresence(presence);
                }
            } else if (isFarm(discoInfo)) {
                //System.out.println("Farm Jid " + packet.getFrom());
                Struct checkStruct = this.getXmppVillein().getStruct(packet.getFrom(), XmppVillein.StructType.FARM);
                if (checkStruct == null) {
                    FarmStruct farmStruct = new FarmStruct();
                    farmStruct.setFullJid(packet.getFrom());
                    farmStruct.setPresence(presence);
                    farmStruct.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                    try {
                        this.getXmppVillein().addFarmStruct(farmStruct);
                    } catch(ParentStructNotFoundException e) {
                        XmppVillein.LOGGER.severe(e.getMessage());
                    }
                } else {
                    checkStruct.setPresence(presence);
                }

            } else if (isRegistry(discoInfo)) {
                Struct checkStruct = this.getXmppVillein().getStruct(packet.getFrom(), XmppVillein.StructType.REGISTRY);
                if (checkStruct == null) {
                    RegistryStruct registryStruct = new RegistryStruct();
                    registryStruct.setFullJid(packet.getFrom());
                    registryStruct.setPresence(presence);
                    try {
                        this.getXmppVillein().addRegistryStruct(registryStruct);
                    } catch(ParentStructNotFoundException e) {
                        XmppVillein.LOGGER.severe(e.getMessage());
                    }
                } else {
                    checkStruct.setPresence(presence);
                }
            } else {
                // ONLY REPRESENT THOSE VMS THAT YOU HAVE SPAWNEDs
                //System.out.println("Vm Jid " + packet.getFrom());
                Struct checkStruct = this.getXmppVillein().getStruct(packet.getFrom());
                if (checkStruct != null) {
                    //System.out.println("DOES NOT EQUAL NULL");
                    checkStruct.setPresence(presence);
                }
            }
        //}
    }
}
