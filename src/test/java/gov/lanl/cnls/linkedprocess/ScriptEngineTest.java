package gov.lanl.cnls.linkedprocess;

import junit.framework.TestCase;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 11:08:14 AM
 */
public class ScriptEngineTest extends TestCase {

    public void testLanguageSupport() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        assertNotNull(engine);
    }
}
