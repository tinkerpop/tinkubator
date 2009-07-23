package org.linkedprocess;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMScheduler;
import org.linkedprocess.os.VMWorker;
import org.linkedprocess.security.VMSecurityManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    public static final String
            JAVASCRIPT = "JavaScript",
            PYTHON = "jython",
            RUBY = "jruby";

    // TODO: how about a "queued" status for jobs?
    public enum JobStatus {
        IN_PROGRESS("in_progress");

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

    public enum VmStatus {
        ACTIVE("active"), ACTIVE_FULL("full"), NOT_FOUND("not_found");

        private final String name;

        private VmStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum VilleinStatus {
        ACTIVE("active"), INACTIVE("inactive");

        private final String name;

        private VilleinStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum ErrorType {
        WRONG_FARM_PASSWORD("wrong_farm_password"),
        WRONG_VM_PASSWORD("wrong_vm_password"),
        MALFORMED_PACKET("malformed_packet"), // when a received packet is not as expected
        EVALUATION_ERROR("evaluation_error"),
        FARM_IS_BUSY("farm_is_busy"), // VMSchedulerIsFullException
        INTERNAL_ERROR("internal_error"), // VMAlreadyExistsException, VMWorkerNotFoundException
        INVALID_VALUE("invalid_value"), // InvalidValueException
        JOB_ABORTED("job_aborted"),
        JOB_ALREADY_EXISTS("job_already_exists"),
        JOB_NOT_FOUND("job_not_found"), // JobNotFoundException
        JOB_TIMED_OUT("job_timed_out"),
        SPECIES_NOT_SUPPORTED("species_not_supported"), // UnsupportedScriptEngineException
        UNKNOWN_DATATYPE("unknown_datatype"),
        VM_IS_BUSY("vm_is_busy"); // VMWorkerIsFullException

        private final String name;

        private ErrorType(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }


        public static ErrorType getErrorType(final String name) {
            for (ErrorType t : ErrorType.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }


    public static String getBareClassName(Class aClass) {
        String name = aClass.getName();
        if (name.contains(".")) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        return name;
    }

    public static String generateResource(String fullJid) {
        return fullJid.substring(fullJid.indexOf("/") + 1);
    }

    public static String generateBareJid(String fullJid) {
        return fullJid.substring(0, fullJid.indexOf("/"));
    }

    public static boolean isBareJid(String jid) {
        return !jid.contains("/");
    }

    public static String createPrettyXML(String xml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        return output.outputString(doc);

    }

    public static Document createXMLDocument(String xml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(new StringReader(xml));
    }

    public static final String LOP_NAMESPACE = "http://linkedprocess.org/";
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPFarm";
    public static final String LOP_VM_NAMESPACE = LOP_NAMESPACE + "protocol#LoPVM";
    public static final String BLANK_NAMESPACE = "";
    public static final String DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info";
    public static final String X_NAMESPACE = "jabber:x:data";
    public static final String DISCO_BOT = "bot";
    public static final String FORWARD_SLASH = "/";

    ///////////////////////////////////////////////////////

    // LoP Farm XMPP tag and attribute names
    // tag names
    public static final String SPAWN_VM_TAG = "spawn_vm";
    // attribute names
    public static final String FARM_PASSWORD_ATTRIBUTE = "farm_password";
    public static final String VM_SPECIES_ATTRIBUTE = "vm_species";
    public static final String VM_JID_ATTRIBUTE = "vm_jid";
    public static final String VM_PASSWORD_ATTRIBUTE = "vm_password";
    // Lop VM XMPP tag and attribute names
    // tag names
    public static final String SUBMIT_JOB_TAG = "submit_job";
    public static final String MANAGE_BINDINGS_TAG = "manage_bindings";
    public static final String JOB_STATUS_TAG = "job_status";
    public static final String ABORT_JOB_TAG = "abort_job";
    public static final String TERMINATE_VM_TAG = "terminate_vm";
    // attribute names
    public static final String JOB_ID_ATTRIBUTE = "job_id";
    public static final String ERROR_TYPE_ATTRIBUTE = "error_type";
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String DATATYPE_ATTRIBUTE = "datatype";
    public static final String BINDING_TAG = "binding";
    public static final String NAME_ATTRIBUTE = "name";

    ///////////////////////////////////////////////////////

    public static final int LOWEST_PRIORITY = -128;
    public static final int HIGHEST_PRIORITY = 127;

    public static final String
            MAX_CONCURRENT_WORKER_THREADS = "org.linkedprocess.maxConcurrentWorkerThreads",
            MAX_TIME_SPENT_PER_JOB = "org.linkedprocess.maxTimeSpentPerJob",
            MAX_VIRTUAL_MACHINES_PER_SCHEDULER = "org.linkedprocess.maxVirtualMachinesPerScheduler",
            MESSAGE_QUEUE_CAPACITY = "org.linkedprocess.messageQueueCapacity",
            ROUND_ROBIN_TIME_SLICE = "org.linkedprocess.roundRobinTimeSlice",
            VM_TIMEOUT = "org.linkedprocess.virtualMachineTimeout",
            SCHEDULER_CLEANUP_INTERVAL = "org.linkedprocess.schedulerCleanupInterval";

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER;
    private static final String LOP_PROPERTIES = "lop.properties";
    public static final String SECURITYDEFAULT_PROPERTIES = "security-default.properties";
    public static final XMLOutputter xmlOut = new XMLOutputter();

    static {
        LOGGER = getLogger(LinkedProcess.class);
        try {
            PROPERTIES.load(LinkedProcess.class.getResourceAsStream(LOP_PROPERTIES));
        } catch (IOException e) {
            LOGGER.severe("unable to load properties file " + LOP_PROPERTIES);
            System.exit(1);
        }

        // Necessary for sandboxing of VM threads.
        Properties p = new Properties();
        try {
            p.load(VMSecurityManager.class.getResourceAsStream(SECURITYDEFAULT_PROPERTIES));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        System.setSecurityManager(new VMSecurityManager(p));

        preLoadingHack();
    }

    private static void preLoadingHack() {
        // Hack to pre-load JobResult so that a VM worker thread doesn't have to
        // load a class (which is in general not allowed) to produce the first
        // result.
        new JobResult(null, (String) null);

        // Hack to pre-load Rhino and Jython resource bundles.  This will have to be extended.
        VMScheduler.VMResultHandler nullHandler = new VMScheduler.VMResultHandler() {
            public void handleResult(JobResult result) {
            }
        };
        ClassLoader loader = new ScriptEngineClassLoader();
        ScriptEngineManager m = new ScriptEngineManager(loader);
        for (String name : new String[]{JAVASCRIPT, PYTHON, RUBY}) {
            ScriptEngine engine = m.getEngineByName(name);
            for (String expr : new String[]{
                    "1 + 1;",
                    "1 ... 1;",
                    "0...0;",
                    "print \"\"\n",
                    "function write(var s) {result = s;}",
                    "42"}) {
                try {

                    // Avoid ClassNotFoundException for inner class
                    if (VMWorker.Status.ACTIVE_INPROGRESS.toString().equals("")) {
                        // Do nothing.  The point was to load VMWorker.Status.
                    }

                    //VMWorker w = new VMWorker(engine, nullHandler);
                    //Job j = new Job(null, null, null, "42");
                    //w.addJob(j);
                    //w.work(100);
                    //w.terminate();

                    engine.eval(expr);
                } catch (Exception e) {
                    // Do nothing.
                }
            }
        }
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }

    public static ScriptEngineManager createScriptEngineManager() {
        ClassLoader loader = new ScriptEngineClassLoader();

//        return new ScriptEngineManager();
        return new ScriptEngineManager(loader);
    }

    public static void main(final String[] args) throws Exception {
        Properties props = System.getProperties();
        for (Object key : props.keySet()) {
            System.out.println("" + key + " --> " + props.get(key));
        }
    }
}
