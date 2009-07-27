package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.packet.PacketExtension;
import org.linkedprocess.gui.ImageHolder;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.xmpp.vm.SubmitJob;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    protected VmControlFrame vmControlFrame;

    protected static final String SUBMIT_JOB = "submit job";
    protected static final String CLEAR = "clear";
    protected static final String ABORT_JOB = "abort job";
    protected static final String BLANK_STRING = "";


    public JobPane(VmControlFrame vmControlFrame, String jobId) {
        super(new BorderLayout());
        this.setOpaque(false);
        this.vmControlFrame = vmControlFrame;
        this.expressionTextArea = new JTextArea(5, 32);
        this.resultTextArea = new JTextArea(5, 32);
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
        splitPane.setDividerLocation(250);
        splitPane.setOpaque(false);
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.jobId = jobId;
        //this.setBorder(new LineBorder(ImageHolder.GRAY_COLOR, 2));
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
        submitJob.setTo(this.vmControlFrame.getVmStruct().getFullJid());
        submitJob.setFrom(this.vmControlFrame.getVilleinGui().getXmppVillein().getFullJid());
        submitJob.setExpression(this.expressionTextArea.getText());
        submitJob.setVmPassword(this.vmControlFrame.getVmStruct().getVmPassword());
        submitJob.setPacketID(this.jobId);
        return submitJob;
    }

    public AbortJob getAbortJob() {
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(this.vmControlFrame.getVmStruct().getFullJid());
        abortJob.setFrom(this.vmControlFrame.getVilleinGui().getXmppVillein().getFullJid());
        abortJob.setVmPassword(this.vmControlFrame.getVmStruct().getVmPassword());
        abortJob.setJobId(this.jobId);
        return abortJob;
    }

    public void handleIncomingSubmitJob(SubmitJob submitJob) {
        if (submitJob.getError() == null) {
            this.resultTextArea.setText(submitJob.getExpression());
            this.submitJobButton.setEnabled(false);
        } else {
            StringBuffer errorMessage = new StringBuffer();
            if (submitJob.getError().getType() != null) {
                errorMessage.append(submitJob.getError().getType().toString().toLowerCase() + "\n");
            }
            if (submitJob.getError().getCondition() != null) {
                errorMessage.append(submitJob.getError().getCondition() + "\n");
            }
            if (submitJob.getError().getExtensions() != null) {
                for (PacketExtension extension : submitJob.getError().getExtensions()) {
                    errorMessage.append(extension.getElementName() + "\n");
                }
            }
            if (submitJob.getError().getMessage() != null) {
                errorMessage.append(submitJob.getError().getMessage());
            }
            this.resultTextArea.setText(errorMessage.toString().trim());
        }
    }

    public void handleIncomingAbortJob(AbortJob abortJob) {
        if (abortJob.getError() == null) {
            this.resultTextArea.setText("job aborted");
        } else {
            StringBuffer errorMessage = new StringBuffer();
            if (abortJob.getError().getType() != null) {
                errorMessage.append(abortJob.getError().getType().toString().toLowerCase() + "\n");
            }
            if (abortJob.getError().getCondition() != null) {
                errorMessage.append(abortJob.getError().getCondition() + "\n");
            }
            if (abortJob.getError().getExtensions() != null) {
                for (PacketExtension extension : abortJob.getError().getExtensions()) {
                    errorMessage.append(extension.getElementName() + "\n");
                }
            }
            if (abortJob.getError().getMessage() != null) {
                errorMessage.append(abortJob.getError().getMessage());
            }
            this.resultTextArea.setText(errorMessage.toString().trim());
        }
    }


    public String toString() {
        return this.jobId;
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SUBMIT_JOB)) {
            this.vmControlFrame.getVilleinGui().getConnection().sendPacket(this.getSubmitJob());
            submitJobButton.setText(ABORT_JOB);
            submitJobButton.setActionCommand(ABORT_JOB);
            clearButton.setEnabled(false);
            expressionTextArea.setEnabled(false);
        } else if (event.getActionCommand().equals(CLEAR)) {
            this.expressionTextArea.setText("");
        } else if (event.getActionCommand().equals(ABORT_JOB)) {
            this.vmControlFrame.getVilleinGui().getConnection().sendPacket(this.getAbortJob());
            this.expressionTextArea.setEnabled(false);
            this.submitJobButton.setEnabled(false);
            this.clearButton.setEnabled(false);
        }
    }

    public void paintComponent(Graphics g) {
        //g.drawImage(ImageHolder.cowBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }

}
