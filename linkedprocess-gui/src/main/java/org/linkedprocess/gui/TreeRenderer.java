package org.linkedprocess.gui;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.farm.Farm;
import org.linkedprocess.farm.os.Vm;
import org.linkedprocess.villein.Villein;
import org.linkedprocess.villein.proxies.CountrysideProxy;
import org.linkedprocess.villein.proxies.FarmProxy;
import org.linkedprocess.villein.proxies.RegistryProxy;
import org.linkedprocess.villein.proxies.VmProxy;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class TreeRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        //this.setOpaque(true);
        //this.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
        //this.setBackgroundSelectionColor(new Color(255,255,255,255));
        //this.setTextNonSelectionColor(new Color(255,255,255,255));

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Object x = ((DefaultMutableTreeNode) value).getUserObject();
        if (x instanceof Farm) {
            Farm farm = (Farm) x;
            this.setText(farm.getJid().getResource() + TreeRenderer.getStatusString(farm.getStatus()));
            if (null == farm.getFarmPassword())
                this.setIcon(ImageHolder.farmIcon);
            else
                this.setIcon(ImageHolder.farmPasswordIcon);
            this.setToolTipText("farm");
        } else if (x instanceof Vm) {
            Vm vm = (Vm) x;
            this.setText(vm.getVmId() + TreeRenderer.getStatusString(vm.getVmStatus()));
            this.setIcon(ImageHolder.vmIcon);
            this.setToolTipText("virtual machine");
        } else if (x instanceof Villein) {
            Villein villein = (Villein) x;
            this.setText(villein.getJid().getResource());
            this.setIcon(ImageHolder.villeinIcon);
            this.setToolTipText("villein");
        } else if (x instanceof RegistryProxy) {
            RegistryProxy registryProxy = (RegistryProxy) x;
            this.setText(registryProxy.getJid().getResource() + TreeRenderer.getStatusString(registryProxy.getStatus()));
            this.setIcon(ImageHolder.registryIcon);
            this.setToolTipText("registry");
        } else if (x instanceof FarmProxy) {
            FarmProxy farmProxy = (FarmProxy) x;
            this.setText(farmProxy.getJid().getResource() + TreeRenderer.getStatusString(farmProxy.getStatus()));
            if (farmProxy.requiresFarmPassword())
                this.setIcon(ImageHolder.farmPasswordIcon);
            else
                this.setIcon(ImageHolder.farmIcon);
            this.setToolTipText("farm");
        } else if (x instanceof VmProxy) {
            VmProxy vmProxy = (VmProxy) x;
            this.setText(vmProxy.getVmId());
            this.setIcon(ImageHolder.vmIcon);
            this.setToolTipText("virtual machine");
        } else if (x instanceof CountrysideProxy) {
            CountrysideProxy countrysideProxy = (CountrysideProxy) x;
            this.setText(countrysideProxy.getJid().toString());
            this.setIcon(ImageHolder.countrysideIcon);
            this.setToolTipText("countryside");
        } else if (x instanceof TreeNodeProperty) {
            if (((TreeNodeProperty) x).getKey().equals("villein_jid")) {
                this.setIcon(ImageHolder.villeinIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("villein_jid");
            } else if (((TreeNodeProperty) x).getKey().equals("vm_status")) {
                this.setIcon(ImageHolder.statusIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("vm_status");
            } else if (((TreeNodeProperty) x).getKey().equals("vm_species")) {
                this.setIcon(ImageHolder.speciesIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("vm_species");
            } else if (((TreeNodeProperty) x).getKey().equals("running_time")) {
                this.setIcon(ImageHolder.timeIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("running_time");
            }
        }
        return this;
    }

    private static String getStatusString(LinkedProcess.Status status) {
        return " [" + status + "]";
    }

    public static class TreeNodeProperty {

        private final String key;
        private final String value;

        public TreeNodeProperty(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }
}
