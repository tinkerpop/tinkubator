package org.linkedprocess.gui.villein;

import org.linkedprocess.xmpp.vm.SubmitJob;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 12, 2009
 * Time: 10:15:56 AM
 */
public class JobPane extends JPanel implements ActionListener {

    protected String jobId;
    protected JTextArea expressionTextArea;
    protected JTextArea resultTextArea;
    protected JButton submitJobButton;
    protected JButton clearButton;
    protected VmFrame vmFrame;

    protected static final String SUBMIT_JOB = "submit job";
    protected static final String CLEAR = "clear";
    protected static final String ABORT_JOB = "abort job";
    protected static final String BLANK_STRING = "";


    public JobPane(VmFrame vmFrame, String jobId) {
        super(new BorderLayout());
        this.setOpaque(false);
        this.vmFrame = vmFrame;
        this.expressionTextArea = new JTextArea(5,32);
        this.resultTextArea = new JTextArea(5,32);
        this.resultTextArea.setEditable(false);
        JScrollPane scrollPane1 = new JScrollPane(this.expressionTextArea);
        JScrollPane scrollPane2 = new JScrollPane(this.resultTextArea); 

        this.submitJobButton = new JButton(SUBMIT_JOB);
        this.submitJobButton.addActionListener(this);
        this.clearButton = new JButton(CLEAR);
        this.clearButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        scrollPane1.setOpaque(false);
        scrollPane2.setOpaque(false);
        buttonPanel.add(this.submitJobButton);
        buttonPanel.add(this.clearButton);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(scrollPane1);
        splitPane.add(scrollPane2);
        splitPane.setDividerLocation(183);
        splitPane.setOpaque(false);
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.jobId = jobId;
        this.setBorder(new LineBorder(ImageHolder.GRAY_COLOR, 2));
    }

    public String getJobId() {
        return this.jobId;    
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.expressionTextArea.setEnabled(enabled);
        this.resultTextArea.setEnabled(enabled);
        this.submitJobButton.setEnabled(enabled);
        this.clearButton.setEnabled(enabled);
    }

    public void clearAllText() {
        this.expressionTextArea.setText(BLANK_STRING);
        this.resultTextArea.setText(BLANK_STRING);
    }

    public SubmitJob getSubmitJob() {
        SubmitJob submitJob = new SubmitJob();
        submitJob.setTo(this.vmFrame.getVmStruct().getFullJid());
        submitJob.setExpression(this.expressionTextArea.getText());
        submitJob.setVmPassword(this.vmFrame.getVmStruct().getVmPassword());
        submitJob.setPacketID(this.jobId);
        return submitJob;
    }

    public AbortJob getAbortJob() {
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(this.vmFrame.getVmStruct().getFullJid());
        abortJob.setVmPassword(this.vmFrame.getVmStruct().getVmPassword());
        abortJob.setJobId(this.jobId);
        return abortJob;
    }

    public void handleIncomingSubmitJob(SubmitJob submitJob) {
        if(submitJob.getErrorType() == null) {
            this.resultTextArea.setText(submitJob.getExpression());
            this.submitJobButton.setEnabled(false);
        } else {
            this.resultTextArea.setText(submitJob.getErrorType().toString() +
                    "\n" + submitJob.getErrorMessage());
        }
    }

    public void handleIncomingAbortJob(AbortJob abortJob) {
        if(abortJob.getErrorType() == null) {
            this.resultTextArea.setText("job aborted.");
        } else {
            this.resultTextArea.setText(abortJob.getErrorType().toString() +
                    "\n" + abortJob.getErrorMessage());
        }
    }


    public String toString() {
        return this.jobId;
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand().equals(SUBMIT_JOB)) {
            this.vmFrame.getVilleinGui().getConnection().sendPacket(this.getSubmitJob());
            submitJobButton.setText(ABORT_JOB);
            submitJobButton.setActionCommand(ABORT_JOB);
            clearButton.setEnabled(false);
            expressionTextArea.setEnabled(false);
        } else if(event.getActionCommand().equals(CLEAR)) {
            this.expressionTextArea.setText("");
        } else if(event.getActionCommand().equals(ABORT_JOB)) {
            this.vmFrame.getVilleinGui().getConnection().sendPacket(this.getAbortJob());
            this.expressionTextArea.setEnabled(false);
            this.submitJobButton.setEnabled(false);
            this.clearButton.setEnabled(false);
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(ImageHolder.cowBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }

}
