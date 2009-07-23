package org.linkedprocess.gui.villein.vmcontrol;

import org.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 16, 2009
 * Time: 4:33:54 PM
 */
public class JobListRenderer extends DefaultListCellRenderer {

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
