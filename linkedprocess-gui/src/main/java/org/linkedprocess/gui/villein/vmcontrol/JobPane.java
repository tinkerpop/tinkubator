package org.linkedprocess.gui.villein.vmcontrol;

import org.jivesoftware.smack.packet.PacketExtension;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.structs.JobStruct;
import org.linkedprocess.xmpp.vm.AbortJob;
import org.linkedprocess.gui.GenericErrorHandler;

import javax.swing.*;
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
        this.expressionTextArea = new JTextArea();
        this.resultTextArea = new JTextArea();
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
        splitPane.setDividerLocation(200);
        splitPane.setOpaque(false);
        this.add(splitPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.jobId = jobId;
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

    public JobStruct getSubmitJob() {
        JobStruct jobStruct = new JobStruct();
        jobStruct.setExpression(this.expressionTextArea.getText());
        jobStruct.setJobId(this.jobId);
        return jobStruct;
    }

    public JobStruct getAbortJob() {
        JobStruct jobStruct = new JobStruct();
        jobStruct.setJobId(this.jobId);
        return jobStruct;
    }

    public void handleIncomingSubmitJob(JobStruct jobStruct) {
        if (jobStruct.getError() == null) {
            this.resultTextArea.setText(jobStruct.getResult());
            this.submitJobButton.setEnabled(false);
        } else {
            StringBuffer errorMessage = new StringBuffer();
            if (jobStruct.getError().getType() != null) {
                errorMessage.append(jobStruct.getError().getType().toString().toLowerCase() + "\n");
            }
            if (jobStruct.getError().getCondition() != null) {
                errorMessage.append(jobStruct.getError().getCondition() + "\n");
            }
            if (jobStruct.getError().getExtensions() != null) {
                for (PacketExtension extension : jobStruct.getError().getExtensions()) {
                    errorMessage.append(extension.getElementName() + "\n");
                }
            }
            if (jobStruct.getError().getMessage() != null) {
                errorMessage.append(jobStruct.getError().getMessage());
            }
            this.resultTextArea.setText(errorMessage.toString().trim());
        }
    }

    public void handleIncomingAbortJob(String jobId) {

        this.resultTextArea.setText("job aborted");
        /*} else {
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
        }*/
    }


    public String toString() {
        return this.jobId;
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(SUBMIT_JOB)) {
            Handler<JobStruct> resultHandler = new Handler<JobStruct>() {
                public void handle(JobStruct jobStruct) {
                    vmControlFrame.handleIncomingSubmitJob(jobStruct);
                }
            };
            this.vmControlFrame.getVmStruct().submitJob(this.getSubmitJob(), resultHandler, new GenericErrorHandler());
            submitJobButton.setText(ABORT_JOB);
            submitJobButton.setActionCommand(ABORT_JOB);
            clearButton.setEnabled(false);
            expressionTextArea.setEditable(false);
        } else if (event.getActionCommand().equals(CLEAR)) {
            this.expressionTextArea.setText("");
        } else if (event.getActionCommand().equals(ABORT_JOB)) {
            this.vmControlFrame.getVmStruct().abortJob(this.getAbortJob(), new GenericErrorHandler());
            this.expressionTextArea.setEditable(false);
            this.submitJobButton.setEnabled(false);
            this.clearButton.setEnabled(false);
        }
    }
}
