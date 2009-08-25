package org.linkedprocess.farm.os.errors;

import org.linkedprocess.LinkedProcessFarm;
import org.linkedprocess.farm.os.errors.SchedulerException;

import javax.script.ScriptEngineFactory;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class UnsupportedScriptEngineException extends SchedulerException {
    public UnsupportedScriptEngineException(final String name) {
        super("script engine '" + name + "' is not supported. Supported languages are: " + listAllLanguages());
    }

    private static String listAllLanguages() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ScriptEngineFactory f : LinkedProcessFarm.getSupportedScriptEngineFactories()) {
            String lang = f.getLanguageName();
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(lang);
        }

        return sb.toString();
    }
}
