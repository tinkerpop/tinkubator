package gov.lanl.cnls.linkedprocess;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    static {
        PropertyConfigurator.configure(
                LinkedProcess.class.getResource("log4j.properties"));
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c);
    }
}
