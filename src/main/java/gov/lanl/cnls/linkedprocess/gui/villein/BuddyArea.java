package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.gui.JTreeImage;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 11:13:22 PM
 */
public class BuddyArea extends JPanel implements ActionListener {

    protected VilleinGui villeinGui;
    protected JTreeImage vmTree;
    protected JTextArea farmFeaturesText;
    protected DefaultMutableTreeNode vmTreeRoot;

    public BuddyArea(VilleinGui villeinGui) {
        this.villeinGui = villeinGui;
        this.vmTreeRoot = new DefaultMutableTreeNode("LOP APP"); // TODO: build the LoP App!
        this.vmTree = new JTreeImage(this.vmTreeRoot, ImageHolder.farmBackground);
        this.vmTree.setCellRenderer(new TreeRenderer());
        this.vmTree.setModel(new DefaultTreeModel(vmTreeRoot));

        JScrollPane vmTreeScroll = new JScrollPane(this.vmTree);
        JButton shutdownButton = new JButton("shutdown app");
        shutdownButton.addActionListener(this);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(vmTreeScroll, BorderLayout.CENTER);
        treePanel.add(shutdownButton, BorderLayout.SOUTH);
        treePanel.setOpaque(false);
        treePanel.setBorder(BorderFactory.createLineBorder(ImageHolder.GRAY_COLOR, 2));


    }

    public void actionPerformed(ActionEvent event) {
        //this.farmGui.getFarm().shutDown();
        // this.farmGui.loadLoginFrame();
    }


    private class TreeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            this.setOpaque(false);
            this.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
            //this.setBackgroundSelectionColor(new Color(255,255,255,255));
            //this.setTextNonSelectionColor(new Color(255,255,255,255));

            return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
    }

}
