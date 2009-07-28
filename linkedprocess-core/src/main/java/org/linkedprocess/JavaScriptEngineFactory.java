package org.linkedprocess;

import com.sun.script.javascript.RhinoScriptEngineFactory;

/**
 * Author: josh
 * Date: Jul 27, 2009
 * Time: 5:54:13 PM
 */
public class JavaScriptEngineFactory extends RhinoScriptEngineFactory {
    @Override
    public String getLanguageName() {
        return "JavaScript";
    }
}
