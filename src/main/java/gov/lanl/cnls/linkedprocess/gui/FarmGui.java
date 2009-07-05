package gov.lanl.cnls.linkedprocess.gui;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.os.errors.UnsupportedScriptEngineException;
import gov.lanl.cnls.linkedprocess.os.errors.VMAlreadyExistsException;
import gov.lanl.cnls.linkedprocess.os.errors.VMSchedulerIsFullException;
import gov.lanl.cnls.linkedprocess.xmpp.lopfarm.XmppFarm;
import gov.lanl.cnls.linkedprocess.xmpp.lopvm.XmppVirtualMachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * User: marko
 * Date: Jul 4, 2009
 * Time: 5:33:39 PM
 */
public class FarmGui extends JFrame {

    protected XmppFarm farm;
    protected JList vmList;
    protected Map<String, LinkedProcess.VMStatus> vmMap;

    public FarmGui(final XmppFarm farm) {

        super("LoP Farm Manager");
        this.farm = farm;
        farm.setStatusEventHandler(new GuiStatusEventHandler(this));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultListModel listModel = new DefaultListModel();
        this.vmList = new JList(listModel);
        this.vmList.setCellRenderer(new CustomCellRenderer());
        JPanel panel = new JPanel();
        final JTextField vmSpecLabel = new JTextField("javascript");
        JButton spawnButton = new JButton("spawn");
        spawnButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					farm.spawnVirtualMachine(vmSpecLabel.getText());
				} catch (VMAlreadyExistsException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (VMSchedulerIsFullException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedScriptEngineException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				refreshListData();
			}});
		panel.add(spawnButton );
		panel.add(vmSpecLabel);
		panel.add(this.vmList);
        panel.setForeground(Color.black);
        panel.setBackground(Color.white);


        this.vmMap = new HashMap<String, LinkedProcess.VMStatus>();

        this.getContentPane().add(panel);
        this.setSize(450, 200);
        //this.pack();
        this.setVisible(true);
    }

    public void updateVirtualMachineList(String vmJid, LinkedProcess.VMStatus status) {
        vmMap.put(vmJid, status);
        this.refreshListData();
    }


    public void refreshListData() {
        Vector<JPanel> vmListData = new Vector<JPanel>();
        for(String vmJid : vmMap.keySet()) {
            JPanel panel = new JPanel();
            java.net.URL imageURL;
            if (vmMap.get(vmJid) == LinkedProcess.VMStatus.ACTIVE)
                imageURL = FarmGui.class.getResource("active.png");
            else
                imageURL = FarmGui.class.getResource("inactive.png");
            panel.add(new JLabel(new ImageIcon(imageURL)));
            panel.add(new JLabel(vmJid));
            panel.setLayout(new FlowLayout());
            vmListData.addElement(panel);

        }
        this.vmList.setListData(vmListData);
    }

    public XmppFarm getFarm() {
        return this.farm;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                XmppFarm farm = new XmppFarm("xmpp.linkedprocess.org", 5222, "linked.process.1", "linked12");
                new FarmGui(farm);
                
            }
        });
    }

    class CustomCellRenderer implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = (Component) value;
            component.setBackground
                    (isSelected ? Color.white : Color.white);
            component.setForeground
                    (isSelected ? Color.gray : Color.black);
            return component;
        }
    }


}
