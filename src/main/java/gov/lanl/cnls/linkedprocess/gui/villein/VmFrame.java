package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;
import gov.lanl.cnls.linkedprocess.xmpp.villein.VmStruct;
import gov.lanl.cnls.linkedprocess.xmpp.vm.AbortJob;
import gov.lanl.cnls.linkedprocess.xmpp.vm.SubmitJob;
import org.jivesoftware.smack.packet.Packet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: marko
 * Date: Jul 8, 2009
 * Time: 7:12:15 PM
 */
public class VmFrame extends JFrame implements ListSelectionListener, ActionListener {

    protected JList jobList;
    protected JSplitPane splitPane;
    protected VmStruct vmStruct;
    protected VilleinGui villeinGui;

    protected static final String ADD_JOB = "add job";
    protected static final String REMOVE_JOB = "remove job";


    public VmFrame(VmStruct vmStruct, VilleinGui villeinGui) {
        super(vmStruct.getFullJid());
        this.vmStruct = vmStruct;
        this.villeinGui = villeinGui;

        DefaultListModel listModel = new DefaultListModel();
        this.jobList = new JList(listModel);
        jobList.addListSelectionListener(this);
        JScrollPane listScrollPane = new JScrollPane(this.jobList);
        JButton addJobButton = new JButton(ImageHolder.addIcon);
        addJobButton.setActionCommand(ADD_JOB);
        JButton removeJobButton = new JButton(ImageHolder.removeIcon);
        removeJobButton.setActionCommand(REMOVE_JOB);
        addJobButton.addActionListener(this);
        removeJobButton.addActionListener(this);

        JPanel jobListPanel = new JPanel(new BorderLayout());
        jobListPanel.setBorder(new TitledBorder("Job List"));
        JPanel jobListButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jobListButtonPanel.add(addJobButton);
        jobListButtonPanel.add(new JLabel("  "));
        jobListButtonPanel.add(removeJobButton);
        jobListPanel.add(listScrollPane, BorderLayout.CENTER);
        jobListPanel.add(jobListButtonPanel, BorderLayout.SOUTH);


        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel mainPanel = new JPanel();

        JobPane jobPane = new JobPane(this, generatedJobId());
        listModel.addElement(jobPane);       
        this.splitPane.add(jobListPanel);
        this.splitPane.add(jobPane);
        mainPanel.add(splitPane);
        this.jobList.setSelectedValue(jobPane, true);


        this.getContentPane().add(mainPanel);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);


    }

    public static String generatedJobId() {
        return Packet.nextID();
    }

    public VmStruct getVmStruct() {
        return this.vmStruct;
    }

    public void valueChanged(ListSelectionEvent event) {
        if(!event.getValueIsAdjusting() && this.jobList.getSelectedValue() != null) {
            JobPane jobPane = (JobPane) this.jobList.getSelectedValue();
            this.splitPane.remove(2);
            this.splitPane.add(jobPane);
        }
        
    }

    public void handleIncomingEvaluate(SubmitJob submitJob) {
        String jobId = submitJob.getPacketID();
        for(int i=0; i < this.jobList.getModel().getSize(); i++) {
            JobPane jobPane = (JobPane) this.jobList.getModel().getElementAt(i);
            if(jobPane.getJobId().equals(jobId)) {
                jobPane.handleIncomingEvaluate(submitJob);
            }
        }   
    }

    public void handleIncomingAbortJob(AbortJob abortJob) {
        String jobId = abortJob.getPacketID();
        for(int i=0; i < this.jobList.getModel().getSize(); i++) {
            JobPane jobPane = (JobPane) this.jobList.getModel().getElementAt(i);
            if(jobPane.getJobId().equals(jobId)) {
                jobPane.handleIncomingAbortJob(abortJob);
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(ADD_JOB)) {
            JobPane jobPane = new JobPane(this, generatedJobId());
            ((DefaultListModel)this.jobList.getModel()).addElement(jobPane);
            this.jobList.setSelectedValue(jobPane, true);
        } else if(event.getActionCommand().equals(REMOVE_JOB)) {
            if(this.jobList.getSelectedValue() != null) {
                JobPane jobPane = (JobPane) this.jobList.getSelectedValue();
                jobPane.setEnabled(false);
                jobPane.clearAllText();
                int selectedIndex = this.jobList.getSelectedIndex();
                ((DefaultListModel)this.jobList.getModel()).removeElement(jobPane);
                selectedIndex = selectedIndex - 1;
                if(selectedIndex < 0)
                    selectedIndex = 0;
                this.jobList.setSelectedIndex(selectedIndex);
            }

        }

    }

    public VilleinGui getVilleinGui() {
        return this.villeinGui;
    }
   



    public static void main(String[] args) {
        VmStruct vmStruct = new VmStruct();
        vmStruct.setFullJid("linked.process.1@xmpp.linkedprocess.org/LoPVM/12345");
        vmStruct.setVmPassword("PASSWORD");
        vmStruct.setVmSpecies(LinkedProcess.JAVASCRIPT);
        new VmFrame(vmStruct, null);
    }

}


