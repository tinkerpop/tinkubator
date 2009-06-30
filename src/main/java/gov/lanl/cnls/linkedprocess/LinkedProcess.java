package gov.lanl.cnls.linkedprocess;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.output.XMLOutputter;

import java.util.Properties;
import java.io.IOException;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {

    public static final String LOP_NAMESPACE = "http://linkedprocess.org/";
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPFarm";
    public static final String LOP_VM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPVM";
    public static final String BLANK_NAMESPACE = "";
    public static final String DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info";
    public static final String FORWARD_SLASH = "/";

    public static final int LOWEST_PRIORITY = -128;
    public static final int HIGHEST_PRIORITY = 127;

    public static final String
            MAX_CONCURRENT_WORKER_THREADS = "gov.lanl.cnls.linkedprocess.maxConcurrentWorkerThreads",
            MAX_VIRTUAL_MACHINES_PER_SCHEDULER = "gov.lanl.cnls.linkedprocess.maxVirtualMachinesPerScheduler",
            MESSAGE_QUEUE_CAPACITY = "gov.lanl.cnls.linkedprocess.messageQueueCapacity",
            ROUND_ROBIN_TIME_SLICE = "gov.lanl.cnls.linkedprocess.roundRobinTimeSlice";

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER;
    private static final String LOP_PROPERTIES = "lop.properties";
    public static final XMLOutputter xmlOut = new XMLOutputter();

    static {
        PropertyConfigurator.configure(
                LinkedProcess.class.getResource(LOP_PROPERTIES));

        LOGGER = getLogger(LinkedProcess.class);
        try {
            PROPERTIES.load(LinkedProcess.class.getResourceAsStream(LOP_PROPERTIES));
        } catch (IOException e) {
            LOGGER.error("unable to load properties file " + LOP_PROPERTIES);
            System.exit(1);
        }
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c);
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
