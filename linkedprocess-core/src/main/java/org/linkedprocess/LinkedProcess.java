package org.linkedprocess;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMWorker;
import org.linkedprocess.security.VMSecurityManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    public static final String
            GROOVY = "groovy",
            JAVASCRIPT = "JavaScript",
            PYTHON = "jython",
            RUBY = "jruby";

    public enum ClientType {
        VM, FARM, VILLEIN
    }

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

    public enum LopErrorType {
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

        private LopErrorType(final String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }


        public static LopErrorType getErrorType(final String name) {
            for (LopErrorType t : LopErrorType.values()) {
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

    private static final String LOP_NAMESPACE = "http://linkedprocess.org/2009/06/";
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "LoPFarm#";
    public static final String LOP_VM_NAMESPACE = LOP_NAMESPACE + "LoPVM#";
    public static final String BLANK_NAMESPACE = "";
    public static final String DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info";
    public static final String QUERY_TAG = "query";
    public static final String X_NAMESPACE = "jabber:x:data";
    public static final String X_TAG = "x";
    public static final String FIELD_TAG = "field";
    public static final String VALUE_TAG = "value";
    public static final String OPTION_TAG = "option";
    public static final String FEATURE_TAG = "feature";
    public static final String VAR_ATTRIBUTE = "var";
    public static final String LABEL_ATTRIBUTE = "label";
    public static final String JABBER_CLIENT_NAMESPACE = "jabber:client";
    public static final String XMPP_STANZAS_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-stanzas";
    public static final String XMPP_STREAMS_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-streams";
    public static final String XML_LANG_NAMESPACE = "xml:lang";
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
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String DATATYPE_ATTRIBUTE = "datatype";
    public static final String BINDING_TAG = "binding";
    public static final String NAME_ATTRIBUTE = "name";
    // IQ tags and attributes
    // tag names
    public static final String ERROR_TAG = "error";
    public static final String TEXT_TAG = "text";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String CODE_ATTRIBUTE = "code";

    ///////////////////////////////////////////////////////

    public static final int LOWEST_PRIORITY = -128;
    public static final int HIGHEST_PRIORITY = 127;

    // Configuration properties
    public static final String
            CONFIGURATION_PROPERTIES = "org.linkedprocess.configurationProperties",
            FARM_SERVER = "org.linkedprocess.farmServer",
            FARM_PORT = "org.linkedprocess.farmPort",
            FARM_USERNAME = "org.linkedprocess.farmUserName",
            FARM_PASSWORD = "org.linkedprocess.farmPassword",
            MAX_CONCURRENT_WORKER_THREADS = "org.linkedprocess.maxConcurrentWorkerThreads",
            JOB_TIMEOUT = "org.linkedprocess.jobTimeout",
            MAX_CONCURRENT_VIRTUAL_MACHINES = "org.linkedprocess.maxConcurrentVirtualMachines",
            JOB_QUEUE_CAPACITY = "org.linkedprocess.jobQueueCapacity",
            ROUND_ROBIN_TIME_SLICE = "org.linkedprocess.roundRobinTimeSlice",
            VIRTUAL_MACHINE_TIME_TO_LIVE = "org.linkedprocess.virtualMachineTimeToLive",
            SCHEDULER_CLEANUP_INTERVAL = "org.linkedprocess.schedulerCleanupInterval";

    private static final Properties PROPERTIES = new Properties();
    private static final Logger LOGGER;
    private static final String LOP_DEFAULT_PROPERTIES = "lop-default.properties";
    public static final XMLOutputter xmlOut = new XMLOutputter();

    // Singleton ScriptEngineManager, subject to the pre-loading hack.
    private static ScriptEngineManager scriptEngineManager;

    // Note: these are specific to a farm.
    private static List<ScriptEngineFactory> supportedScriptEngineFactories;

    static {
        LOGGER = getLogger(LinkedProcess.class);
        String file = System.getProperty(CONFIGURATION_PROPERTIES);
        if (null == file) {
            file = LOP_DEFAULT_PROPERTIES;
        }
        LOGGER.info("loading configuration from " + file);
        try {
            PROPERTIES.load(LinkedProcess.class.getResourceAsStream(file));
        } catch (IOException e) {
            LOGGER.severe("unable to load properties file " + file);
            System.exit(1);
        }

        // Necessary for sandboxing of VM threads.
        System.setSecurityManager(new VMSecurityManager(PROPERTIES));

        preLoadingHack();
    }

    private static void preLoadingHack() {
        LOGGER.info("pre-loading script engines");

        // Hack to pre-load JobResult so that a VM worker thread doesn't have to
        // load a class (which is in general not allowed) to produce the first
        // result.
        new JobResult(null, (String) null);

        // Hack to pre-load Rhino and Jython resource bundles.  This will have to be extended.
        for (ScriptEngineFactory f : LinkedProcess.getSupportedScriptEngineFactories()) {
            // Avoid ClassNotFoundException for inner class
            if (VMWorker.Status.ACTIVE_INPROGRESS.toString().equals("")) {
                // Do nothing.  The point was to simply to load VMWorker.Status.
            }

            ScriptEngine engine = f.getScriptEngine();
            for (String expr : new String[]{
                    "1 + 1;",
                    "1 ... 1;",
                    "0...0;",
                    "print \"\"\n",
                    "require 'net/http'",
                    "require 'net/protocol'",
                    "function write(var s) {result = s;}",
                    "42"}) {
                try {
                    //VMWorker w = new VMWorker(engine, nullHandler);
                    //Job j = new Job(null, null, null, "42");
                    //w.addJob(j);
                    //w.work(100);
                    //w.terminate();

                    engine.eval(expr);
                } catch (Throwable e) {
                    // Do nothing.
                }
            }
        }
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Properties getConfiguration() {
        return PROPERTIES;
    }

    public static ScriptEngineManager getScriptEngineManager() {
        if (null == scriptEngineManager) {
            scriptEngineManager = new ScriptEngineManager();
            //scriptEngineManager = new ScriptEngineManager();
            //scriptEngineManager.getEngineFactories().
        }

        return scriptEngineManager;
    }

    public static List<ScriptEngineFactory> getSupportedScriptEngineFactories() {
        if (null == supportedScriptEngineFactories) {
            Set<String> classNames = new HashSet<String>();
            for (Object key : PROPERTIES.keySet()) {
                if (key.toString().startsWith("org.linkedprocess.supportedScriptEngineFactory")) {
                    classNames.add(PROPERTIES.get(key).toString().trim());
                }
            }

            supportedScriptEngineFactories = new LinkedList<ScriptEngineFactory>();
            for (ScriptEngineFactory f : getScriptEngineManager().getEngineFactories()) {
                if (classNames.contains(f.getClass().getName())) {
                    supportedScriptEngineFactories.add(f);
                }
            }

            Collections.sort(supportedScriptEngineFactories, new ScriptEngineFactoryComparator());
        }

        return supportedScriptEngineFactories;
    }

    private static class ScriptEngineFactoryComparator implements Comparator<ScriptEngineFactory> {

        public int compare(ScriptEngineFactory first, ScriptEngineFactory second) {
            String lFirst = first.getLanguageName().trim().toLowerCase();
            String lSecond = second.getLanguageName().trim().toLowerCase();
            return lFirst.compareTo(lSecond);
        }
    }

    public static void main(final String[] args) throws Exception {
        Properties props = System.getProperties();
        for (Object key : props.keySet()) {
            System.out.println("" + key + " --> " + props.get(key));
        }
    }
}
