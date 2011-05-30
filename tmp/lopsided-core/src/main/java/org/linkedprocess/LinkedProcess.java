/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.linkedprocess.farm.security.VmSecurityManager;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jun 24, 2009
 * Time: 2:44:21 PM
 */
public class LinkedProcess {
    /**
     * The currently supported virtual machine species of LoPSideD.
     */
    public static final String
            GROOVY = "groovy",
            JAVASCRIPT = "JavaScript",
            PYTHON = "jython",
            RUBY = "jruby";

    /**
     * This is a helper method that will turn an InputStream into a String.
     * The benefit of this is when loading submit_job expressions from a file.
     * If an expression is large and saved in a file, then to render it to a string for a submit_job is necessary.
     * This method accomplishes that.
     *
     * @param inputStream the InputStream to render into a String
     * @return the String representation of the InputStream's data
     * @throws IOException thrown when there is an IO error with the InputStream
     */
    public static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    /**
     * The status of a job.
     */
    public enum JobStatus {
        // TODO: how about a "queued" status for jobs?
        IN_PROGRESS("in_progress");

        private final String name;

        private JobStatus(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * The status of a Linked Process entity. Any entity can be active or inactive. Farms can also be busy.
     */
    public enum Status {
        ACTIVE("active"), BUSY("busy"), INACTIVE("inactive");

        private final String name;

        private Status(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    /**
     * The set of all errors that are possible in Linked Process.
     */
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
        VM_NOT_FOUND("vm_not_found"), // when a virtual machine id doesn't point to an actual virtual machine
        WRONG_FARM_PASSWORD("wrong_farm_password");

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

    /**
     * A helper method that creates a pretty indented representation of an XML blurb.
     *
     * @param xml a String representation of some legal XML
     * @return a pretty formatted version of the provided XML
     * @throws JDOMException thrown if the provided XML is not legal XML
     * @throws IOException   thrown if the internal StringReader fails
     */
    public static String createPrettyXML(String xml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        return output.outputString(doc);

    }

    /**
     * A helper method that creates a JDOM document from a String representation of an XML blurb.
     *
     * @param xml a String representation of some legal XML
     * @return a JDOM document of the provided XML
     * @throws JDOMException thrown if the provided XML is not legal XML
     * @throws IOException   thrown if the internal StringReader fails
     */
    public static Document createXMLDocument(String xml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(new StringReader(xml));
    }

    private static final String LOP_NAMESPACE = "http://linkedprocess.org/2009/06/";
    public static final String LOP_FARM_NAMESPACE = LOP_NAMESPACE + "Farm#";
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
    public static final String VM_ID_ATTRIBUTE = "vm_id";
    // Lop VM XMPP tag and attribute names
    // tag names
    public static final String SUBMIT_JOB_TAG = "submit_job";
    public static final String MANAGE_BINDINGS_TAG = "manage_bindings";
    public static final String PING_JOB_TAG = "ping_job";
    public static final String ABORT_JOB_TAG = "abort_job";
    public static final String TERMINATE_VM_TAG = "terminate_vm";
    // attribute names
    public static final String JOB_ID_ATTRIBUTE = "job_id";
    public static final String STATUS_ATTRIBUTE = "status";
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
            CONCURRENT_WORKER_THREADS_PROPERTY = "org.linkedprocess.farm.concurrentWorkerThreads",
            JOB_TIMEOUT_PROPERTY = "org.linkedprocess.farm.jobTimeout",
            MAX_CONCURRENT_VIRTUAL_MACHINES_PROPERTY = "org.linkedprocess.farm.maxConcurrentVirtualMachines",
            JOB_QUEUE_CAPACITY_PROPERTY = "org.linkedprocess.farm.jobQueueCapacity",
            ROUND_ROBIN_QUANTUM_PROPERTY = "org.linkedprocess.farm.roundRobinQuantum",
            VIRTUAL_MACHINE_TIME_TO_LIVE_PROPERTY = "org.linkedprocess.farm.virtualMachineTimeToLive",
            SCHEDULER_CLEANUP_INTERVAL_PROPERTY = "org.linkedprocess.farm.schedulerCleanupInterval";

    private static final Properties CONFIGURATION;
    private static final Logger LOGGER;
    private static final String LOP_DEFAULT_CONFIGURATION = "lop-default.properties";
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

        CONFIGURATION = new Properties();
        String file = System.getProperty(CONFIGURATION_PROPERTIES_PROPERTY);
        try {
            if (null == file) {
                file = LOP_DEFAULT_CONFIGURATION;
                LOGGER.info("loading default configuration: " + file);
                Properties p = new Properties();
                p.load(LinkedProcess.class.getResourceAsStream(file));
                setConfiguration(p);
            } else {
                LOGGER.info("loading configuration from external file: " + file);
                Properties p = new Properties();
                p.load(new FileInputStream(file));
                setConfiguration(p);
            }
        } catch (IOException e) {
            LOGGER.warning("unable to load configuration file " + file
                    + ". Configuration properties will need to be set with LinkedProcess.setConfiguration");
        }
    }

    public static Logger getLogger(final Class c) {
        return Logger.getLogger(c.getName());
    }

    public static Properties getConfiguration() {
        if (0 == CONFIGURATION.values().size()) {
            throw new IllegalStateException("configuration properties have not been set");
        }
        
        return CONFIGURATION;
    }

    public static void setConfiguration(final Properties conf) {
        CONFIGURATION.clear();
        Enumeration propertyNames = conf.propertyNames();
		while (propertyNames.hasMoreElements()) {
            String key = propertyNames.nextElement().toString();
			String value = conf.getProperty(key);
            CONFIGURATION.setProperty(key, value);
        }

        // Necessary for sandboxing of VM threads.
        System.setSecurityManager(new VmSecurityManager(CONFIGURATION));
    }

    public static void main(final String[] args) throws Exception {
        Properties props = System.getProperties();
        for (Object key : props.keySet()) {
            System.out.println("" + key + " --> " + props.get(key));
        }
    }
}
