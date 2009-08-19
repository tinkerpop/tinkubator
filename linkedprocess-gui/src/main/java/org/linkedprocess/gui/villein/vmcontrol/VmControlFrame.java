package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.gui.GenericErrorHandler;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.gui.PacketSnifferPanel;
import org.linkedprocess.gui.villein.VilleinGui;
import org.linkedprocess.os.VMBindings;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class VmControlFrame extends JFrame implements ListSelectionListener, ActionListener {

    protected JList jobList;
    protected JSplitPane splitPane;
    protected VmProxy vmProxy;
    protected VilleinGui villeinGui;
    protected ManageBindingsPanel manageBindingsPanel;
    protected Map<String, JobStatus> jobStatus = new HashMap<String, JobStatus>();

    protected static final String ADD_JOB = "add job";
    protected static final String REMOVE_JOB = "remove job";
    protected static final String TERMINATE_VM = "terminate vm";
    protected static final String CLOSE = "close";

    public enum JobStatus {
        ABORTED, COMPLETED, ERROR
    }


    public VmControlFrame(VmProxy vmProxy, VilleinGui villeinGui) {
        super(vmProxy.getFullJid());
        this.vmProxy = vmProxy;
        this.villeinGui = villeinGui;

        DefaultListModel listModel = new DefaultListModel();
        this.jobList = new JList(listModel);
        this.jobList.addListSelectionListener(this);
        jobList.setCellRenderer(new JobListRenderer(this));
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

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(new LineBorder(ImageHolder.GRAY_COLOR, 2));
        JMenu menu = new JMenu("Control");
        JMenuItem terminateItem = new JMenuItem(TERMINATE_VM);
        menu.add(terminateItem);
        JMenuItem closeItem = new JMenuItem(CLOSE);
        menu.add(new JSeparator());
        menu.add(closeItem);
        menu.setBorder(new LineBorder(ImageHolder.GRAY_COLOR, 2));
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
        terminateItem.addActionListener(this);
        closeItem.addActionListener(this);

        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JobPane jobPane = new JobPane(this, generatedJobId());
        this.manageBindingsPanel = new ManageBindingsPanel(this);

        listModel.addElement(jobPane);

        this.splitPane.add(jobListPanel);
        this.splitPane.add(jobPane);

        // select the new job and fire selection event
        this.jobList.setSelectedValue(jobPane, true);

        PacketSnifferPanel packetSnifferPanel = new PacketSnifferPanel(this.villeinGui.getXmppVillein().getFullJid());
        PacketFilter fromToFilter = new OrFilter(new FromContainsFilter(vmProxy.getFullJid()), new ToContainsFilter(vmProxy.getFullJid()));
        this.villeinGui.getXmppVillein().getConnection().addPacketListener(packetSnifferPanel, fromToFilter);
        this.villeinGui.getXmppVillein().getConnection().addPacketWriterInterceptor(packetSnifferPanel, fromToFilter);

        JTabbedPane jobBindingsTabbedPane = new JTabbedPane();
        jobBindingsTabbedPane.addTab("jobs", this.splitPane);
        jobBindingsTabbedPane.addTab("bindings", this.manageBindingsPanel);
        jobBindingsTabbedPane.addTab("packets", packetSnifferPanel);

        this.getContentPane().add(jobBindingsTabbedPane);
        this.pack();
        this.setResizable(true);
        this.setSize(600, 550);
        this.setVisible(true);


    }

    public JobStatus getJobStatus(String jobId) {
        return this.jobStatus.get(jobId);
    }

    public static String generatedJobId() {
        return Packet.nextID();
    }

    public VmProxy getVmProxy() {
        return this.vmProxy;
    }

    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting() && this.jobList.getSelectedValue() != null) {
            JobPane jobPane = (JobPane) this.jobList.getSelectedValue();
            this.splitPane.remove(2);
            this.splitPane.add(jobPane);
        }

    }

    public void handleIncomingManageBindings(VMBindings vmBindings) {
        this.manageBindingsPanel.handleIncomingManageBindings(vmBindings);
    }

    public void handleIncomingSubmitJob(JobStruct jobStruct) {
        String jobId = jobStruct.getJobId();
        if (null == this.jobStatus.get(jobId)) {
            if (null != jobStruct.getLopError())
                this.jobStatus.put(jobId, JobStatus.ERROR);
            else
                this.jobStatus.put(jobId, JobStatus.COMPLETED);
        }

        for (int i = 0; i < this.jobList.getModel().getSize(); i++) {
            JobPane jobPane = (JobPane) this.jobList.getModel().getElementAt(i);
            if (jobPane.getJobId().equals(jobId)) {
                jobPane.handleIncomingSubmitJob(jobStruct);
            }
        }
        jobList.repaint();
    }

    public void handleIncomingAbortJob(String jobId) {
        this.jobStatus.put(jobId, JobStatus.ABORTED);
        jobList.repaint();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(ADD_JOB)) {
            JobPane jobPane = new JobPane(this, generatedJobId());
            ((DefaultListModel) this.jobList.getModel()).addElement(jobPane);
            this.jobList.setSelectedValue(jobPane, true);
        } else if (event.getActionCommand().equals(REMOVE_JOB)) {
            if (this.jobList.getSelectedValue() != null) {
                JobPane jobPane = (JobPane) this.jobList.getSelectedValue();
                jobPane.setEnabled(false);
                jobPane.clearAllText();
                int selectedIndex = this.jobList.getSelectedIndex();
                ((DefaultListModel) this.jobList.getModel()).removeElement(jobPane);
                selectedIndex = selectedIndex - 1;
                if (selectedIndex < 0)
                    selectedIndex = 0;
                this.jobList.setSelectedIndex(selectedIndex);
            }

        } else if (event.getActionCommand().equals(TERMINATE_VM)) {
            this.vmProxy.terminateVm(null, new GenericErrorHandler());
            this.villeinGui.removeVmFrame(this.vmProxy);
        } else if (event.getActionCommand().equals(CLOSE)) {
            this.setVisible(false);
        }
    }

    public VilleinGui getVilleinGui() {
        return this.villeinGui;
    }

    private class JobListRenderer extends DefaultListCellRenderer {

        protected VmControlFrame vmControlFrame;

        public JobListRenderer(VmControlFrame vmControlFrame) {
            this.vmControlFrame = vmControlFrame;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String jobId = label.getText();
            VmControlFrame.JobStatus jobStatus = this.vmControlFrame.getJobStatus(jobId);
            if (jobStatus == VmControlFrame.JobStatus.ABORTED || jobStatus == VmControlFrame.JobStatus.ERROR) {
                label.setIcon(ImageHolder.inactiveIcon);
            } else if (jobStatus == VmControlFrame.JobStatus.COMPLETED) {
                label.setIcon(ImageHolder.activeIcon);
            }

            return label;
        }
    }
}


