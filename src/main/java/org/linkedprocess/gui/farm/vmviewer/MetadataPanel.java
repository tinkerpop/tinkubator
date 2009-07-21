package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.xmpp.vm.XmppVirtualMachine;
import org.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 21, 2009
 * Time: 9:44:31 AM
 */
public class MetadataPanel extends JPanel implements ActionListener {

    protected XmppVirtualMachine xmppVm;
    protected JLabel timeLabel;
    protected JLabel statusLabel;
    protected final static String REFRESH = "refresh";

    public MetadataPanel(XmppVirtualMachine xmppVm) {
        super(new BorderLayout());
        this.xmppVm = xmppVm;
        JPanel metaPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        GridBagConstraints c = new GridBagConstraints();

        JButton refreshButton = new JButton(REFRESH);
        refreshButton.addActionListener(this);
        buttonPanel.add(refreshButton);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 10;
        metaPanel.add(new JLabel(this.xmppVm.getFullJid(), ImageHolder.vmIcon, JLabel.LEFT), c);
        c.gridy = 1;
        metaPanel.add(new JLabel(this.xmppVm.getVilleinJid(), ImageHolder.villeinIcon, JLabel.LEFT), c);
        c.gridy = 2;
        this.statusLabel = new JLabel(this.xmppVm.getVmStatus().toString(), ImageHolder.statusIcon, JLabel.LEFT);
        metaPanel.add(this.statusLabel, c);
        c.gridy = 3;
        metaPanel.add(new JLabel(this.xmppVm.getVmSpecies(), ImageHolder.speciesIcon, JLabel.LEFT), c);
        c.gridy = 4;
        metaPanel.add(new JLabel(this.xmppVm.getVmPassword(), ImageHolder.passwordIcon, JLabel.LEFT), c);
        c.gridy = 5;
        this.timeLabel = new JLabel(this.xmppVm.getRunningTimeInSecods() + " seconds", ImageHolder.timeIcon, JLabel.LEFT);
        metaPanel.add(this.timeLabel, c);
        this.add(metaPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setOpaque(true);
        metaPanel.setOpaque(false);
        this.setOpaque(false);
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(REFRESH)) {
            this.timeLabel.setText(this.xmppVm.getRunningTimeInSecods() + " seconds");
            this.statusLabel.setText(this.xmppVm.getVmStatus().toString());
        }
    }

    

    public void paintComponent(Graphics g) {
        g.drawImage(ImageHolder.farmBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }
}
