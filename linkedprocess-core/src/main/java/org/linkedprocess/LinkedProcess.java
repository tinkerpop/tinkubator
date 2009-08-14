package org.linkedprocess;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.linkedprocess.security.VMSecurityManager;
import org.jivesoftware.smack.packet.Packet;

import java.io.*;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.LogManager;

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

    public static String generateRandomResourceId() {
        // e.g. from gtalk 6D56433B
        Random random = new Random();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int x = random.nextInt(36);
            if (x < 10)
                b.append(x);
            else
                b.append(((char) (x + 55)));

        }
        return b.toString();
    }

    public static String generateRandomPassword() {
        return generateRandomResourceId();
    }

    public static String generateRandomJobId() {
        return Packet.nextID();
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    public enum ClientType {
        VM, FARM, REGISTRY, VILLEIN
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
        ACTIVE("active"), ACTIVE_FULL("full"), INACTIVE("inactive");

        private final String name;

        private FarmStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum VmStatus {
        ACTIVE("active"), ACTIVE_FULL("full"), NOT_FOUND("not_found"), INACTIVE("inactive");

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

    public enum RegistryStatus {
        ACTIVE("active"), INACTIVE("inactive");

        private final String name;

        private RegistryStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public enum LopErrorType {
        EVALUATION_ERROR("evaluation_error"),
        FARM_IS_BUSY("farm_is_busy"), // VMSchedulerIsFullException
        INTERNAL_ERROR("internal_error"), // VMAlreadyExistsException, VMWorkerNotFoundException
        INVALID_VALUE("invalid_value"), // InvalidValueException
        JOB_ABORTED("job_aborted"),
        JOB_ALREADY_EXISTS("job_already_exists"),
        JOB_NOT_FOUND("job_not_found"), // JobNotFoundException
        JOB_TIMED_OUT("job_timed_out"),
        MALFORMED_PACKET("malformed_packet"), // when a received packet is not as expected
        PERMISSION_DENIED("permission_denied"),
        SPECIES_NOT_SUPPORTED("species_not_supported"), // UnsupportedScriptEngineException
        UNKNOWN_DATATYPE("unknown_datatype"),
        VM_IS_BUSY("vm_is_busy"), // VMWorkerIsFullException
        WRONG_FARM_PASSWORD("wrong_farm_password"),
        WRONG_VM_PASSWORD("wrong_vm_password");

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
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "Farm#";
    public static final String LOP_VM_NAMESPACE = LOP_NAMESPACE + "VirtualMachine#";
    public static final String LOP_REGISTRY_NAMESPACE = LOP_NAMESPACE + "Registry#";
    public static final String BLANK_NAMESPACE = "";
    public static final String DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info";
    public static final String DISCO_ITEMS_NAMESPACE = "http://jabber.org/protocol/disco#items";
    public static final String QUERY_TAG = "query";
    public static final String X_JABBER_DATA_NAMESPACE = "jabber:x:data";
    public static final String X_TAG = "x";
    public static final String ITEM_TAG = "item";
    public static final String JID_ATTRIBUTE = "jid";
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
    public static final String PING_JOB_TAG = "ping_job";
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

    public static final String MAX_CONCURRENT_VIRTUAL_MACHINES = "max_concurrent_vms";
    public static final String JOB_QUEUE_CAPACITY = "job_queue_capacity";
    public static final String JOB_TIMEOUT = "job_timeout";
    public static final String VM_TIME_TO_LIVE = "vm_time_to_live";
    public static final String FARM_PASSWORD_REQUIRED = "farm_password_required";
    public static final String FARM_START_TIME = "farm_start_time";
    public static final String VM_START_TIME = "vm_start_time";

    ///////////////////////////////////////////////////////

    public static final int LOWEST_PRIORITY = -128;
    public static final int HIGHEST_PRIORITY = 127;

    // Configuration properties
    public static final String
            CONFIGURATION_PROPERTIES_PROPERTY = "org.linkedprocess.configurationProperties",
            FARM_SERVER_PROPERTY = "org.linkedprocess.farmServer",
            FARM_PORT_PROPERTY = "org.linkedprocess.farmPort",
            FARM_USERNAME_PROPERTY = "org.linkedprocess.farmUsername",
            FARM_USERPASSWORD_PROPERTY = "org.linkedprocess.farmUserPassword",
            FARM_PASSWORD_PROPERTY = "org.linkedprocess.farmPassword",
            REGISTRY_SERVER_PROPERTY = "org.linkedprocess.registryServer",
            REGISTRY_PORT_PROPERTY = "org.linkedprocess.registryPort",
            REGISTRY_USERNAME_PROPERTY = "org.linkedprocess.registryUsername",
            REGISTRY_PASSWORD_PROPERTY = "org.linkedprocess.registryPassword",
            CONCURRENT_WORKER_THREADS_PROPERTY = "org.linkedprocess.concurrentWorkerThreads",
            JOB_TIMEOUT_PROPERTY = "org.linkedprocess.jobTimeout",
            MAX_CONCURRENT_VIRTUAL_MACHINES_PROPERTY = "org.linkedprocess.maxConcurrentVirtualMachines",
            JOB_QUEUE_CAPACITY_PROPERTY = "org.linkedprocess.jobQueueCapacity",
            ROUND_ROBIN_TIME_SLICE_PROPERTY = "org.linkedprocess.roundRobinTimeSlice",
            VIRTUAL_MACHINE_TIME_TO_LIVE_PROPERTY = "org.linkedprocess.virtualMachineTimeToLive",
            SCHEDULER_CLEANUP_INTERVAL_PROPERTY = "org.linkedprocess.schedulerCleanupInterval";

    private static final Properties PROPERTIES;
    private static final Logger LOGGER;
    private static final String LOP_DEFAULT_PROPERTIES = "lop-default.properties";
    public static final XMLOutputter xmlOut = new XMLOutputter();

    static {
        InputStream resourceAsStream = LinkedProcess.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(resourceAsStream);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER = getLogger(LinkedProcess.class);

        PROPERTIES = new Properties();
        String file = System.getProperty(CONFIGURATION_PROPERTIES_PROPERTY);
        try {
            if (null == file) {
                file = LOP_DEFAULT_PROPERTIES;
                LOGGER.info("loading default configuration: " + file);
                PROPERTIES.load(LinkedProcess.class.getResourceAsStream(file));
            } else {
                LOGGER.info("loading configuration from external file: " + file);
                PROPERTIES.load(new FileInputStream(file));
            }
        } catch (IOException e) {
            LOGGER.severe("unable to load properties file " + file);
            System.exit(1);
        }

        // Necessary for sandboxing of VM threads.
        System.setSecurityManager(new VMSecurityManager(PROPERTIES));
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Properties getConfiguration() {
        return PROPERTIES;
    }

    public static void main(final String[] args) throws Exception {
        Properties props = System.getProperties();
        for (Object key : props.keySet()) {
            System.out.println("" + key + " --> " + props.get(key));
        }
    }
}
