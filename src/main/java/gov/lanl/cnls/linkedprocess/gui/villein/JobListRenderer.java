package gov.lanl.cnls.linkedprocess.gui.villein;

import gov.lanl.cnls.linkedprocess.gui.ImageHolder;

import javax.swing.*;
import java.awt.*;

/**
 * User: marko
 * Date: Jul 16, 2009
 * Time: 4:33:54 PM
 */
public class JobListRenderer extends DefaultListCellRenderer {

    protected VmFrame vmFrame;

    public JobListRenderer(VmFrame vmFrame) {
        this.vmFrame = vmFrame;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String jobId = label.getText();
        VmFrame.JobStatus jobStatus = this.vmFrame.getJobStatus(jobId);
        if(jobStatus == VmFrame.JobStatus.ABORTED || jobStatus == VmFrame.JobStatus.ERROR) {
            label.setIcon(ImageHolder.inactiveIcon);
        } else if(jobStatus == VmFrame.JobStatus.COMPLETED) {
            label.setIcon(ImageHolder.activeIcon);         
        }

		return label;
	}
}