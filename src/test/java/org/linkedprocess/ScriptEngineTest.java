package org.linkedprocess;

import com.sun.phobos.script.javascript.RhinoScriptEngine;
import com.sun.script.jruby.JRubyScriptEngine;
import com.sun.script.jython.JythonScriptEngine;
import junit.framework.TestCase;
import org.linkedprocess.os.errors.UnsupportedScriptEngineException;

import javax.script.*;
import java.util.List;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:08:14 AM
 */
public class ScriptEngineTest extends TestCase {

    public void testLanguageSupport() {
        ScriptEngineManager manager = LinkedProcess.createScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(LinkedProcess.JAVASCRIPT);
        assertNotNull(engine);

        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            System.out.println("ScriptEngineFactory Info:");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
            List<String> engNames = factory.getNames();
            for (String name : engNames) {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
            System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
        }
    }

    public void testGetEngines() throws Exception {
        Class c = ScriptEngineFactory.class;
        for (Class c2 : c.getClasses()) {
            System.out.println("class: " + c2);

        }
        for (Class c2 : c.getDeclaredClasses()) {
            System.out.println("declared class: " + c2);
        }

        ScriptEngineManager manager = LinkedProcess.createScriptEngineManager();

        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            System.out.println("" + f.getEngineName());
        }
        ScriptEngine engine;
        engine = manager.getEngineByName(LinkedProcess.JAVASCRIPT);
        if (null == engine) {
            throw new UnsupportedScriptEngineException(LinkedProcess.JAVASCRIPT);
        }

        engine = manager.getEngineByName(LinkedProcess.PYTHON);
        if (null == engine) {
            throw new UnsupportedScriptEngineException(LinkedProcess.PYTHON);
        }

        engine.eval("print \"Hello, World!\"\n");
    }

    public void testJavaScript() throws Exception {
        // Note: we're using the "phobos" engine
        ScriptEngine engine = new RhinoScriptEngine();
        assertEquals("42", engine.eval("42").toString());

        ScriptEngineManager manager = LinkedProcess.createScriptEngineManager();
        engine = manager.getEngineByName(LinkedProcess.JAVASCRIPT);
        System.out.println("" + engine.getClass());
        assertNotNull(engine);
        assertEquals("42", engine.eval("42").toString());
    }

    public void testJython() throws Exception {
        /*
        ScriptEngineManager man = LinkedProcess.createScriptEngineManager();
        ScriptEngine engine = man.getEngineByName(LinkedProcess.PYTHON);

        assertEquals("2", engine.eval("my_list = ['john', 'pat', 'gary', 'michael']").toString());
        */

        JythonScriptEngine engine = new JythonScriptEngine();
        //ctx.setBindings();
        engine.eval("print 42");
        engine.eval("foo = 1331");
        ScriptContext ctx = engine.getContext();
        Bindings b = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        System.out.println(b.get("foo"));
    }

    public void testJRuby() throws Exception {
        ScriptEngine engine = new JRubyScriptEngine();
        assertEquals("42", engine.eval("42").toString());

        ScriptEngineManager manager = LinkedProcess.createScriptEngineManager();
        engine = manager.getEngineByName(LinkedProcess.RUBY);
        assertNotNull(engine);
        assertEquals("42", engine.eval("42").toString());
    }
}
