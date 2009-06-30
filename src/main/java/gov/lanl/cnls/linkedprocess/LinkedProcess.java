package gov.lanl.cnls.linkedprocess;

import org.jdom.output.XMLOutputter;

import java.util.Properties;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    // TODO: how about a "queued" status for jobs?
    public enum JobStatus {
        IN_PROGRESS("in_progress"), DOES_NOT_EXIST("does_not_exist");

        private final String name;

        private JobStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum FarmStatus {
        ACTIVE("active"), ACTIVE_FULL("full"), TERMINATED("terminated");

        private final String name;

        private FarmStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    public enum VMStatus {
        ACTIVE("active"), DOES_NOT_EXIST("not_found");

        private final String name;

        private VMStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum Errortype {
        EVALUATION_ERROR("evaluation_error"),
        INTERNAL_ERROR("internal_error"), // VMAlreadyExistsException, VMWorkerNotFoundException
        JOB_ABORTED("job_aborted"),
        JOB_NOT_FOUND("job_not_found"), // JobNotFoundException
        SPECIES_NOT_SUPPORTED("species_not_supported"), // UnsupportedScriptEngineException
        FARM_IS_BUSY("farm_is_busy"), // VMSchedulerIsFullException
        VM_IS_BUSY("vm_is_busy"); // VMWorkerIsFullException

        private final String name;

        private Errortype(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static final String LOP_NAMESPACE = "http://linkedprocess.org/";
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPFarm";
    public static final String LOP_VM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPVM";
    public static final String BLANK_NAMESPACE = "";
    public static final String DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info";
    public static final String FORWARD_SLASH = "/";

    ///////////////////////////////////////////////////////

    // LoP Farm XMPP tag and attribute names
    // tag names
    public static final String SPAWN_VM_TAG = "spawn_vm";
    public static final String TERMINATE_VM_TAG = "terminate_vm";
    // attribute names
    public static final String VM_SPECIES_ATTRIBUTE = "vm_species";
    public static final String VM_JID_ATTRIBUTE = "vm_jid";
    // Lop VM XMPP tag and attribute names
    // tag names
    public static final String EVALUATE_TAG = "evaluate";
    public static final String JOB_STATUS_TAG = "job_status";
    public static final String ABORT_JOB_TAG = "abort_job";
    // attribute names
    public static final String JOB_ID_ATTRIBUTE = "job_id";
    public static final String ERROR_ATTRIBUTE = "error";
    public static final String VALUE_ATTRIBUTE = "value";
    
    ///////////////////////////////////////////////////////

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
        
        LOGGER = getLogger(LinkedProcess.class);
        try {
            PROPERTIES.load(LinkedProcess.class.getResourceAsStream(LOP_PROPERTIES));
        } catch (IOException e) {
            LOGGER.severe("unable to load properties file " + LOP_PROPERTIES);
            System.exit(1);
        }
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
