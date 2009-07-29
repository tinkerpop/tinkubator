package org.linkedprocess;

import org.linkedprocess.os.VMWorker;
import org.linkedprocess.os.JobResult;
import org.linkedprocess.security.VMSecurityManager;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jul 29, 2009
 * Time: 4:20:26 PM
 * To change this template use File | Settings | File Templates.
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
}
