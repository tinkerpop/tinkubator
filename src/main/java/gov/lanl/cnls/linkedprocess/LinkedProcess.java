package gov.lanl.cnls.linkedprocess;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.io.IOException;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    public static final String
            MAX_CONCURRENT_WORKER_THREADS = "gov.lanl.cnls.linkedprocess.maxConcurrentWorkerThreads",
            MAX_VIRTUAL_MACHINES_PER_SCHEDULER = "gov.lanl.cnls.linkedprocess.maxVirtualMachinesPerScheduler",
            MESSAGE_QUEUE_CAPACITY = "gov.lanl.cnls.linkedprocess.messageQueueCapacity",
            ROUND_ROBIN_TIME_SLICE = "gov.lanl.cnls.linkedprocess.roundRobinTimeSlice";

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER;
    private static final String LOP_PROPERTIES = "lop.properties";
    
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
