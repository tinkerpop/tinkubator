package org.linkedprocess.xmpp.villein.proxies;

import org.linkedprocess.xmpp.villein.Dispatcher;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jdom.Document;

import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

/**
 * User: marko
 * Date: Jul 30, 2009
 * Time: 3:41:36 PM
 */
public class RegistryProxy extends Proxy {

    protected Set<DiscoverItems.Item> discoItems;

    public RegistryProxy(final String fullJid, final Dispatcher dispatcher) {
        super(fullJid, dispatcher);
        try {
            this.refreshDiscoItems();
        } catch (Exception e) {
            XmppVillein.LOGGER.warning("Problem loading disco#items: "  + e.getMessage());
        }
        
    }

    public RegistryProxy(final String fullJid, final Dispatcher dispatcher, final Document discoInfoDocument) {
        super(fullJid, dispatcher, discoInfoDocument);
         try {
            this.refreshDiscoItems();
        } catch (Exception e) {
             XmppVillein.LOGGER.warning("Problem loading disco#items: "  + e.getMessage());
        }
    }

    public void refreshDiscoItems() throws XMPPException {
        this.discoItems = new HashSet<DiscoverItems.Item>();
        ServiceDiscoveryManager discoManager = this.dispatcher.getServiceDiscoveryManager();
        DiscoverItems discoItems = discoManager.discoverItems(this.getFullJid());
        Iterator<DiscoverItems.Item> itty = discoItems.getItems();
        while(itty.hasNext()) {
            this.discoItems.add(itty.next());
        }
    }

    public Set<CountrysideProxy> getActiveCountrysides() {
        Set<CountrysideProxy> countrysideProxies = new HashSet<CountrysideProxy>();
        for(DiscoverItems.Item item : discoItems) {
            CountrysideProxy countrysideProxy = new CountrysideProxy(item.getEntityID(), null);
            countrysideProxies.add(countrysideProxy);
        }
        return countrysideProxies;
    }

}
