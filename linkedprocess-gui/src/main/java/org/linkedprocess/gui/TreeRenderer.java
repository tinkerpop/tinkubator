package org.linkedprocess.gui;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.vm.LopVm;
import org.linkedprocess.farm.LopFarm;
import org.linkedprocess.villein.LopVillein;
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
        if (x instanceof LopFarm) {
            this.setText(LinkedProcess.generateResource(((LopFarm) x).getFullJid()));
            if (null == ((LopFarm) x).getFarmPassword())
                this.setIcon(ImageHolder.farmIcon);
            else
                this.setIcon(ImageHolder.farmPasswordIcon);
            this.setToolTipText("farm");
        } else if (x instanceof LopVm) {
            LopVm vm = (LopVm) x;
            this.setText(LinkedProcess.generateResource(vm.getFullJid()));
            this.setIcon(ImageHolder.vmIcon);
            this.setToolTipText("virtual machine");
        } else if (x instanceof LopVillein) {
            this.setText(LinkedProcess.generateResource(((LopVillein) x).getFullJid()));
            this.setIcon(ImageHolder.villeinIcon);
            this.setToolTipText("villein");
        } else if (x instanceof RegistryProxy) {
            RegistryProxy registryProxy = (RegistryProxy) x;
            this.setText(LinkedProcess.generateResource(registryProxy.getFullJid()));
            this.setIcon(ImageHolder.registryIcon);
            this.setToolTipText("registry_jid");
        } else if (x instanceof FarmProxy) {
            FarmProxy farmProxy = (FarmProxy) x;
            this.setText(LinkedProcess.generateResource(farmProxy.getFullJid()));
            if (farmProxy.requiresFarmPassword())
                this.setIcon(ImageHolder.farmPasswordIcon);
            else
                this.setIcon(ImageHolder.farmIcon);
            this.setToolTipText("farm_jid");
        } else if (x instanceof VmProxy) {
            VmProxy vmProxy = (VmProxy) x;
            this.setText(LinkedProcess.generateResource(vmProxy.getFullJid()));
            this.setIcon(ImageHolder.vmIcon);
            this.setToolTipText("vm_jid");
        } else if (x instanceof CountrysideProxy) {
            CountrysideProxy countrysideProxy = (CountrysideProxy) x;
            this.setText(countrysideProxy.getFullJid());
            this.setIcon(ImageHolder.countrysideIcon);
            this.setToolTipText("countryside_jid");
        } else if (x instanceof TreeNodeProperty) {
            if (((TreeNodeProperty) x).getKey().equals("villein_jid")) {
                this.setIcon(ImageHolder.villeinIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("spawning_app");
            } else if (((TreeNodeProperty) x).getKey().equals("vm_status")) {
                this.setIcon(ImageHolder.statusIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("vm_status");
            } else if (((TreeNodeProperty) x).getKey().equals("vm_species")) {
                this.setIcon(ImageHolder.speciesIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("vm_species");
            } else if (((TreeNodeProperty) x).getKey().equals("vm_password")) {
                this.setIcon(ImageHolder.passwordIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("vm_password");
            } else if (((TreeNodeProperty) x).getKey().equals("running_time")) {
                this.setIcon(ImageHolder.timeIcon);
                this.setText(((TreeNodeProperty) x).getValue());
                this.setToolTipText("running_time");
            }
        }
        return this;
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
