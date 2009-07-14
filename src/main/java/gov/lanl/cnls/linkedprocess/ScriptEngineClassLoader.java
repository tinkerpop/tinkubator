package gov.lanl.cnls.linkedprocess;

import java.util.logging.Logger;

/**
 * Author: josh
 * Date: Jul 14, 2009
 * Time: 12:03:41 PM
 */
public class ScriptEngineClassLoader extends ClassLoader {
    private static final Logger LOGGER = LinkedProcess.getLogger(ScriptEngineClassLoader.class);

    public ScriptEngineClassLoader() {
        super(ScriptEngineClassLoader.class.getClassLoader());
        try {
            loadClass("com.sun.phobos.script.javascript.RhinoScriptEngineFactory");
            loadClass("com.sun.script.jython.JythonScriptEngineFactory");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("failed to load script engine factory classes: " + e);
            System.exit(1);
        }
    }
}
