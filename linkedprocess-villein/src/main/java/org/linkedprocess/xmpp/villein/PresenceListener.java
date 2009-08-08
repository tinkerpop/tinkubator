package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.PresenceHandler;
import org.linkedprocess.xmpp.villein.structs.*;

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

        Proxy proxy = this.getXmppVillein().getStruct(presence.getFrom());

        if (proxy != null && (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed)) {
            this.getXmppVillein().removeStruct(presence.getFrom());
            // Handlers
            for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
                presenceHandler.handlePresenceUpdate(proxy, presence.getType());
            }
            return;
        }


        if (null != proxy && (proxy instanceof CountrysideProxy || proxy instanceof FarmProxy || proxy instanceof VmProxy)) {
            proxy.setPresence(presence);
        } else {
            DiscoverInfo discoInfo = this.getDiscoInfo(presence.getFrom());

            if (LinkedProcess.isBareJid(presence.getFrom())) {
                //System.out.println("Bare Jid " + packet.getFrom());
                CountrysideProxy countrysideStruct = new CountrysideProxy(this.getXmppVillein().getDispatcher());
                countrysideStruct.setFullJid(presence.getFrom());
                countrysideStruct.setPresence(presence);
                this.getXmppVillein().addCountrysideStruct(countrysideStruct);
                proxy = countrysideStruct;

            } else if (isFarm(discoInfo)) {
                //System.out.println("Farm Jid " + packet.getFrom());
                FarmProxy farmStruct = new FarmProxy(this.getXmppVillein().getDispatcher());
                farmStruct.setFullJid(presence.getFrom());
                farmStruct.setPresence(presence);
                farmStruct.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                try {
                    this.getXmppVillein().addFarmStruct(farmStruct);
                    proxy = farmStruct;
                } catch (ParentStructNotFoundException e) {
                    XmppVillein.LOGGER.warning(e.getMessage());
                }
            } else if (isRegistry(discoInfo)) {
                RegistryProxy registryStruct = new RegistryProxy(this.getXmppVillein().getDispatcher());
                registryStruct.setFullJid(presence.getFrom());
                registryStruct.setPresence(presence);
                try {
                    this.getXmppVillein().addRegistryStruct(registryStruct);
                    proxy = registryStruct;
                } catch (ParentStructNotFoundException e) {
                    XmppVillein.LOGGER.warning(e.getMessage());
                }
            }
        }
        // Handlers
        for (PresenceHandler presenceHandler : this.getXmppVillein().getPresenceHandlers()) {
            presenceHandler.handlePresenceUpdate(proxy, presence.getType());
        }
    }
}
