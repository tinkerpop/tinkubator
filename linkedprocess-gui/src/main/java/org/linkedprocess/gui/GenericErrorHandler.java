package org.linkedprocess.gui;

import org.linkedprocess.Error;
import org.linkedprocess.villein.Handler;

import javax.swing.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class GenericErrorHandler implements Handler<Error> {

    public void handle(org.linkedprocess.Error error) {
        if (error.getMessage() != null)
            JOptionPane.showMessageDialog(null, error.getErrorType().toString() + "\n" + error.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, error.getErrorType().toString(), "error", JOptionPane.ERROR_MESSAGE);
    }
}
