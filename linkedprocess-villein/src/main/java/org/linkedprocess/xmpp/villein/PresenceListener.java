package org.linkedprocess.xmpp.villein;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.villein.PresenceHandler;
import org.linkedprocess.xmpp.villein.proxies.*;

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

        Proxy proxy = this.getXmppVillein().getProxy(presence.getFrom());

        if (proxy != null && (presence.getType() == Presence.Type.unavailable ||
                presence.getType() == Presence.Type.unsubscribe ||
                presence.getType() == Presence.Type.unsubscribed)) {
            this.getXmppVillein().removeProxy(presence.getFrom());
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
                CountrysideProxy countrysideProxy = new CountrysideProxy(this.getXmppVillein().getDispatcher());
                countrysideProxy.setFullJid(presence.getFrom());
                countrysideProxy.setPresence(presence);
                this.getXmppVillein().addCountrysideProxy(countrysideProxy);
                proxy = countrysideProxy;

            } else if (isFarm(discoInfo)) {
                //System.out.println("Farm Jid " + packet.getFrom());
                FarmProxy farmProxy = new FarmProxy(this.getXmppVillein().getDispatcher());
                farmProxy.setFullJid(presence.getFrom());
                farmProxy.setPresence(presence);
                farmProxy.setSupportedVmSpecies(this.getSupportedVmSpecies(discoInfo));
                try {
                    this.getXmppVillein().addFarmProxy(farmProxy);
                    proxy = farmProxy;
                } catch (ParentProxyNotFoundException e) {
                    XmppVillein.LOGGER.warning(e.getMessage());
                }
            } else if (isRegistry(discoInfo)) {
                RegistryProxy registryProxy = new RegistryProxy(this.getXmppVillein().getDispatcher());
                registryProxy.setFullJid(presence.getFrom());
                registryProxy.setPresence(presence);
                try {
                    this.getXmppVillein().addRegistryProxy(registryProxy);
                    proxy = registryProxy;
                } catch (ParentProxyNotFoundException e) {
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
