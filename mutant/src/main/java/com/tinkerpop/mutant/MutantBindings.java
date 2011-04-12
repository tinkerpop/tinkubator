package com.tinkerpop.mutant;

import javax.script.Bindings;
import java.util.HashMap;

/**
 * Provides support for getting variables regardless of variable prefixes.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MutantBindings extends HashMap<String, Object> implements Bindings {

    private static final String AT_SYMBOL = "@";
    private static final String DOLLAR_SYMBOL = "$";

    public Object get(final Object variable) {
        String var = (String) variable;
        Object value = super.get(var);
        if (null == value) {
            if (var.startsWith(AT_SYMBOL) || var.startsWith(DOLLAR_SYMBOL)) {
                return super.get(var.substring(1));
            } else {
                value = super.get(DOLLAR_SYMBOL + var);
                if (value == null)
                    return super.get(AT_SYMBOL + var);
                else
                    return value;
            }
        } else {
            return value;
        }
    }
}
