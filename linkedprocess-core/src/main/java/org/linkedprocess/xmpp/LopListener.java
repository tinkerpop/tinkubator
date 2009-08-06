package org.linkedprocess.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.FormField.Option;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.linkedprocess.LinkedProcess;

import java.util.*;

/**
 * User: marko
 * Date: Jul 23, 2009
 * Time: 11:50:55 AM
 */
public abstract class LopListener implements PacketListener {
    public XmppClient xmppClient;

    public LopListener(XmppClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    protected DiscoverInfo getDiscoInfo(String jid) {
        ServiceDiscoveryManager discoManager = this.xmppClient.getDiscoManager();
        try {
            return discoManager.discoverInfo(jid);
        } catch (XMPPException e) {
            XmppClient.LOGGER.warning("XmppException with DiscoveryManager on " + jid + ": " + e.getMessage());
            return null;
        }
    }

    protected boolean isVirtualMachine(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_VM_NAMESPACE);
        else
            return false;
    }

    protected boolean isFarm(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_FARM_NAMESPACE);
        else
            return false;
    }

    protected boolean isRegistry(DiscoverInfo discoInfo) {
        if (discoInfo != null)
            return discoInfo.containsFeature(LinkedProcess.LOP_REGISTRY_NAMESPACE);
        else
            return false;
    }

    protected Collection<String> getSupportedVmSpecies(DiscoverInfo discoInfo) {
        if (discoInfo != null) {
            List<String> supportedVmSpecies = new LinkedList<String>();
            DataForm extension = (DataForm) discoInfo.getExtension(LinkedProcess.X_JABBER_DATA_NAMESPACE);
            Iterator<FormField> fields = extension.getFields();
            while (fields.hasNext()) {
                FormField field = fields.next();
                if (field.getVariable().equals(LinkedProcess.VM_SPECIES_ATTRIBUTE)) {
                    Iterator<Option> vms = field.getOptions();
                    while (vms.hasNext()) {
                        Option next = vms.next();
                        supportedVmSpecies.add(next.getValue());
                    }
                }
            }
            return supportedVmSpecies;
        } else {
            return new HashSet<String>();
        }
    }
}
