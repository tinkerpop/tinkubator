package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.handlers.PresenceHandler;

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

        Struct struct = this.getXmppVillein().getStruct(presence.getFrom());

        if (struct != null && (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed)) {
            this.getXmppVillein().removeStruct(presence.getFrom());
            for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(struct, presence.getType());
            }
            return;
        }


        if (null != struct && (struct instanceof CountrysideStruct || struct instanceof FarmStruct || struct instanceof VmStruct)) {
            struct.setPresence(presence);
        } else {
            DiscoverInfo discoInfo = this.getDiscoInfo(presence.getFrom());

            if (LinkedProcess.isBareJid(presence.getFrom())) {
                //System.out.println("Bare Jid " + packet.getFrom());
                CountrysideStruct countrysideStruct = new CountrysideStruct();
                countrysideStruct.setFullJid(presence.getFrom());
                countrysideStruct.setPresence(presence);
                this.getXmppVillein().addCountrysideStruct(countrysideStruct);
                struct = countrysideStruct;

            } else if (isFarm(discoInfo)) {
                //System.out.println("Farm Jid " + packet.getFrom());
                FarmStruct farmStruct = new FarmStruct();
                farmStruct.setFullJid(presence.getFrom());
                farmStruct.setPresence(presence);
                farmStruct.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                try {
                    this.getXmppVillein().addFarmStruct(farmStruct);
                    struct = farmStruct;
                } catch (ParentStructNotFoundException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
            } else if (isRegistry(discoInfo)) {
                RegistryStruct registryStruct = new RegistryStruct();
                registryStruct.setFullJid(presence.getFrom());
                registryStruct.setPresence(presence);
                try {
                    this.getXmppVillein().addRegistryStruct(registryStruct);
                    struct = registryStruct;
                } catch (ParentStructNotFoundException e) {
                    XmppVillein.LOGGER.severe(e.getMessage());
                }
            }
        }

        for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
            presenceHandler.handlePresenceUpdate(struct, presence.getType());
        }
    }
}
