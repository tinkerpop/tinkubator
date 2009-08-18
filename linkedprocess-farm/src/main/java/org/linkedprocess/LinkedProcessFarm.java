package org.linkedprocess;

import org.linkedprocess.os.JobResult;
import org.linkedprocess.os.VMWorker;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 29, 2009
 * Time: 4:20:26 PM
 */
public class LinkedProcessFarm {
    private static final Logger LOGGER = LinkedProcess.getLogger(LinkedProcessFarm.class);

    static {
        preLoadingHack();
    }

    // Singleton ScriptEngineManager, subject to the pre-loading hack.
    private static ScriptEngineManager scriptEngineManager;

    // Note: these are specific to a farm.
    private static List<ScriptEngineFactory> supportedScriptEngineFactories;

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
            Properties props = LinkedProcess.getConfiguration();
            Set<String> classNames = new HashSet<String>();
            for (Object key : props.keySet()) {
                if (key.toString().startsWith("org.linkedprocess.supportedScriptEngineFactory")) {
                    classNames.add(props.get(key).toString().trim());
                }
            }

            supportedScriptEngineFactories = new LinkedList<ScriptEngineFactory>();
            for (ScriptEngineFactory f : getScriptEngineManager().getEngineFactories()) {
                String name = f.getClass().getName();
//LOGGER.info("candidate script engine: " + name);
                if (classNames.contains(f.getClass().getName())) {
                    LOGGER.info("registering script engine: " + name);
                    supportedScriptEngineFactories.add(f);
                    classNames.remove(name);
                }
            }
            for (String name : classNames) {
                LOGGER.warning("script engine could not be registered: " + name);
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

    private static void preLoadingHack() {
        LOGGER.info("pre-loading script engines");

        // Hack to pre-load JobResult so that a VM worker thread doesn't have to
        // load a class (which is in general not allowed) to produce the first
        // result.
        new JobResult(null, (String) null);

        // Hack to pre-load Rhino and Jython resource bundles.  This will have to be extended.
        for (ScriptEngineFactory f : getSupportedScriptEngineFactories()) {
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

    public static void main(final String[] args) throws Exception {
        //System.out.println(System.getProperty("java.class.path"));
        String[] paths = System.getProperty("java.class.path").split(":");
        for (String p : paths) {
            System.out.println(p);
        }
    }
}
