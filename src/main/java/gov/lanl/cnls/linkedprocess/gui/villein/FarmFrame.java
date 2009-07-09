package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.xmpp.villein.FarmStruct;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 4:51:52 PM
 */
public class FarmFrame extends JFrame implements ActionListener {

    protected VilleinGui villeinGui;
    protected FarmStruct farmStruct;
    protected JComboBox speciesCombo;

    public FarmFrame(VilleinGui villeinGui, FarmStruct farmStruct) {
        super(farmStruct.getFarmJid());
        this.villeinGui = villeinGui;
        this.farmStruct = farmStruct;

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.speciesCombo = new JComboBox();
        speciesCombo.addItem("JavaScript");
        JButton spawnButton = new JButton("spawn vm");
        panel.add(speciesCombo);
        panel.add(spawnButton);
        spawnButton.addActionListener(this);

        this.getContentPane().add(panel);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);

    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("spawn vm")) {
            System.out.println("Spawn " + speciesCombo.getSelectedItem() + " from " + this.farmStruct.getFarmJid());
            this.villeinGui.getXmppVillein().spawnVirtualMachine(this.farmStruct.getFarmJid(), speciesCombo.getSelectedItem().toString());
        }
        this.setVisible(false); // todo: make sure we don't have some memory leak
    }
}
