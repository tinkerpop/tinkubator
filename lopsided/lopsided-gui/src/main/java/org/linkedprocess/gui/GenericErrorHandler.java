package org.linkedprocess.gui;

import org.linkedprocess.LopError;
import org.linkedprocess.villein.Handler;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class GenericErrorHandler implements Handler<LopError> {

    public void handle(LopError error) {
        if (error.getMessage() != null)
            JOptionPane.showMessageDialog(null, error.getErrorType().toString() + "\n" + error.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, error.getErrorType().toString(), "error", JOptionPane.ERROR_MESSAGE);
    }
}
