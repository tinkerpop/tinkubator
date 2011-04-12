package com.tinkerpop.mutant;

import javax.script.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MutantScriptEngine extends AbstractScriptEngine {

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final List<EngineHolder> engines = new ArrayList<EngineHolder>();
    private EngineHolder currentEngine;

    public MutantScriptEngine() throws RuntimeException {
        this.manager.setBindings(new SimpleBindings());
        this.context.setBindings(manager.getBindings(), ScriptContext.GLOBAL_SCOPE);
        this.context.setBindings(manager.getBindings(), ScriptContext.ENGINE_SCOPE);

        // for ruby
        System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");
        for (ScriptEngineFactory factory : this.manager.getEngineFactories()) {
            if (!factory.getEngineName().equals(Tokens.MUTANT))
                this.engines.add(new EngineHolder(factory));
        }
        if (this.engines.size() == 0) {
            throw new RuntimeException("No script engines to load");
        } else {
            this.currentEngine = engines.get(0);
        }

    }

    public ScriptEngineFactory getFactory() {
        return new MutantScriptEngineFactory();
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        return this.eval(new StringReader(script), context);
    }

    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        String line;
        BufferedReader bReader = new BufferedReader(reader);
        Object finalValue = null;
        StringBuffer buffer = new StringBuffer();
        try {
            while ((line = bReader.readLine()) != null) {
                if (line.isEmpty())
                    continue;
                else if (line.startsWith(Tokens.QUESTION)) {
                    if (line.startsWith(Tokens.SCRIPT_SPACE)) {
                        finalValue = this.eval(new FileReader(new File(line.substring(3).trim())), this.context);
                    } else {
                        if (buffer.length() > 0) {
                            finalValue = this.currentEngine.getEngine().eval(buffer.toString(), this.context);
                            this.currentEngine = this.getEngineByLanguageName(line.substring(1));
                        } else {
                            this.currentEngine = this.getEngineByLanguageName(line.substring(1));
                            finalValue = "Switched to " + this.currentEngine.getLanguageName();
                        }
                        buffer = new StringBuffer();
                    }

                } else {
                    buffer.append(line).append("\n");
                }
            }
            if (buffer.length() > 0)
                finalValue = this.currentEngine.getEngine().eval(buffer.toString(), this.context);

            //this.manager.getBindings().putAll(this.currentEngine.getEngine().getBindings(ScriptContext.ENGINE_SCOPE));

        } catch (Exception e) {
            throw new ScriptException(e.getMessage());
        }
        return finalValue;
    }


    public List<EngineHolder> getEngines() {
        return this.engines;
    }

    public ScriptEngineManager getManager() {
        return this.manager;
    }

    public EngineHolder getCurrentEngine() {
        return this.currentEngine;
    }

    public void setCurrentEngine(EngineHolder engine) {
        this.currentEngine = engine;
    }

    public void setCurrentEngine(int engineIndex) {
        this.currentEngine = this.getEngines().get(engineIndex);
    }

    private EngineHolder getEngineByLanguageName(String languageName) throws ScriptException {
        for (EngineHolder engine : this.engines) {
            if (engine.getLanguageName().equals(languageName))
                return engine;
        }
        throw new ScriptException("No engine for the language: " + languageName);
    }


}
