package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.xmpp.vm.Evaluate;
import gov.lanl.cnls.linkedprocess.xmpp.vm.AbortJob;
import gov.lanl.cnls.linkedprocess.LinkedProcess;
import gov.lanl.cnls.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import javax.swing.border.LineBorder;

import org.jivesoftware.smack.packet.Packet;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: marko
 * Date: Jul 12, 2009
 * Time: 10:15:56 AM
 */
public class JobPane extends JTabbedPane implements ActionListener {

    protected String jobId;
    protected JTextArea evaluateTextArea;
    protected JTextArea resultTextArea;
    protected JButton evaluateButton;
    protected JButton clearButton;
    protected VmFrame vmFrame;

    protected static final String EVALUATE = "evaluate";
    protected static final String CLEAR = "clear";
    protected static final String ABORT_JOB = "abort job";
    protected static final String BLANK_STRING = "";


    public JobPane(VmFrame vmFrame, String jobId) {
        this.vmFrame = vmFrame;
        this.evaluateTextArea = new JTextArea(17,32);
        this.resultTextArea = new JTextArea(17,32);
        this.resultTextArea.setEditable(false);
        JScrollPane scrollPane1 = new JScrollPane(this.evaluateTextArea);
        JScrollPane scrollPane2 = new JScrollPane(this.resultTextArea); 
        this.evaluateButton = new JButton(EVALUATE);
        this.evaluateButton.addActionListener(this);
        this.clearButton = new JButton(CLEAR);
        this.clearButton.addActionListener(this);
        JPanel evaluatePanel = new JPanel(new BorderLayout());
        JPanel evaluateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        evaluatePanel.setOpaque(false);
        evaluateButtonPanel.setOpaque(false);
        scrollPane1.setOpaque(false);
        scrollPane2.setOpaque(false);
        evaluateButtonPanel.add(this.evaluateButton);
        evaluateButtonPanel.add(this.clearButton);
        evaluatePanel.add(scrollPane1, BorderLayout.CENTER);
        evaluatePanel.add(evaluateButtonPanel, BorderLayout.SOUTH);

        this.addTab("evaluate", evaluatePanel);
        this.addTab("result", scrollPane2);
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
        this.evaluateTextArea.setEnabled(enabled);
        this.resultTextArea.setEnabled(enabled);
        this.evaluateButton.setEnabled(enabled);
        this.clearButton.setEnabled(enabled);
    }

    public void clearAllText() {
        this.evaluateTextArea.setText(BLANK_STRING);
        this.resultTextArea.setText(BLANK_STRING);
    }

    public Evaluate getEvaluate() {
        Evaluate evaluate = new Evaluate();
        evaluate.setTo(this.vmFrame.getVmStruct().getFullJid());
        evaluate.setExpression(this.evaluateTextArea.getText());
        evaluate.setVmPassword(this.vmFrame.getVmStruct().getVmPassword());
        evaluate.setPacketID(this.jobId);
        return evaluate;
    }

    public AbortJob getAbortJob() {
        AbortJob abortJob = new AbortJob();
        abortJob.setTo(this.vmFrame.getVmStruct().getFullJid());
        abortJob.setVmPassword(this.vmFrame.getVmStruct().getVmPassword());
        abortJob.setJobId(this.jobId);
        return abortJob;
    }

    public void handleIncomingEvaluate(Evaluate evaluate) {
        if(evaluate.getErrorType() == null) {
            this.resultTextArea.setText(evaluate.getExpression());
            this.evaluateButton.setEnabled(false);
        } else {
            this.resultTextArea.setText(evaluate.getErrorType().toString() +
                    "\n" + evaluate.getErrorMessage());
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
        if(event.getActionCommand().equals(EVALUATE)) {
            //System.out.println(this.getEvaluate().toXML());
            this.vmFrame.getVilleinGui().getConnection().sendPacket(this.getEvaluate());
            evaluateButton.setText(ABORT_JOB);
            evaluateButton.setActionCommand(ABORT_JOB);
            clearButton.setEnabled(false);
            evaluateTextArea.setEnabled(false);
        } else if(event.getActionCommand().equals(CLEAR)) {
            this.evaluateTextArea.setText("");
        } else if(event.getActionCommand().equals(ABORT_JOB)) {
            //System.out.println(this.getAbortJob().toXML());
            this.vmFrame.getVilleinGui().getConnection().sendPacket(this.getAbortJob());
            this.evaluateTextArea.setEnabled(false);
            this.evaluateButton.setEnabled(false);
            this.clearButton.setEnabled(false);
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(ImageHolder.cowBackground.getImage(), 0, 0, null);
        super.paintComponent(g);
    }

}
