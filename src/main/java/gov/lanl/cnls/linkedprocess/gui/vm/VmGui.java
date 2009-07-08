package gov.lanl.cnls.linkedprocess.gui.vm;

import gov.lanl.cnls.linkedprocess.gui.farm.LoginArea;

import javax.swing.*;

/**
 * User: marko
 * Date: Jul 7, 2009
 * Time: 10:33:02 PM
 */
public class VmGui extends JFrame {

    /*public void loadLoginFrame() {
        this.getContentPane().removeAll();
        this.getContentPane().add(new LoginArea(this));
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }*/

    public static void main(String[] args) {
        new VmGui();
    }

}
