package org.linkedprocess.gui.farm.vmviewer;

import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.farm.os.Vm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class MetadataPanel extends JPanel implements ActionListener {

    protected Vm vm;
    protected JLabel timeLabel;
    protected JLabel statusLabel;
    protected final static String REFRESH = "refresh";

    public MetadataPanel(Vm vm) {
        super(new BorderLayout());
        this.vm = vm;
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBackground(Color.WHITE);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        GridBagConstraints c = new GridBagConstraints();

        JButton refreshButton = new JButton(REFRESH);
        refreshButton.addActionListener(this);
        buttonPanel.add(refreshButton);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 10;
        metaPanel.add(new JLabel(this.vm.getVmId(), ImageHolder.vmIcon, JLabel.LEFT), c);
        c.gridy = 1;
        metaPanel.add(new JLabel(this.vm.getSpawningVilleinJid(), ImageHolder.villeinIcon, JLabel.LEFT), c);
        c.gridy = 2;
        this.statusLabel = new JLabel(this.vm.getVmStatus().toString(), ImageHolder.statusIcon, JLabel.LEFT);
        metaPanel.add(this.statusLabel, c);
        c.gridy = 3;
        metaPanel.add(new JLabel(this.vm.getVmSpecies(), ImageHolder.speciesIcon, JLabel.LEFT), c);
        //c.gridy = 4;
        //this.timeLabel = new JLabel(this.vm.getRunningTimeInSeconds() + " seconds", ImageHolder.timeIcon, JLabel.LEFT);
        //metaPanel.add(this.timeLabel, c);
        this.add(metaPanel, BorderLayout.NORTH);
        this.setBackground(Color.WHITE);
        this.add(buttonPanel, BorderLayout.SOUTH);


    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REFRESH)) {
            //this.timeLabel.setText(this.vm.getRunningTimeInSeconds() + " seconds");
            this.statusLabel.setText(this.vm.getVmStatus().toString());
        }
    }

}
