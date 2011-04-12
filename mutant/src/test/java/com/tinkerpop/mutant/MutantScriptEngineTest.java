package com.tinkerpop.mutant;

import junit.framework.TestCase;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MutantScriptEngineTest extends TestCase {

    public void testBindingsToSubEngines() throws Exception {
        ScriptEngine engine = new MutantScriptEngine();
        // a mutant script engine has both context bindings set to the same binding set
        engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("$x", 45);
        engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE).put("$y", 55);

        assertEquals(engine.eval("?Groovy\n$x"), 45);
        assertEquals(engine.eval("?ECMAScript\n$x"), 45);

        assertEquals(engine.eval("?Groovy\n$y"), 55);
        assertEquals(engine.eval("?ECMAScript\n$y"), 55);
    }

    public void testBindingsFromSubEngines() throws Exception {
        ScriptEngine engine = new MutantScriptEngine();

        assertEquals(engine.eval("?Groovy\n$x = 45"), 45);
        assertEquals(engine.eval("?ECMAScript\n$x"), 45);

        assertEquals(engine.eval("?ECMAScript\n$x = 55"), 55.0);
        assertEquals(engine.eval("?Groovy\n$x"), 55.0);
    }

    public void testMidScriptConversion() throws Exception {
        ScriptEngine engine = new MutantScriptEngine();
        assertTrue((Boolean) ((List) engine.eval("?Groovy\n$x = 0\nfor(int i=0; i<10; i++) {\n$x = $x + i\n}\n?gremlin\n$x = 45")).get(0));
    }

}
