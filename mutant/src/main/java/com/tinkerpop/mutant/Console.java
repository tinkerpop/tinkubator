package com.tinkerpop.mutant;

import jline.ConsoleReader;
import jline.History;

import javax.script.Bindings;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Console {

    private final MutantScriptEngine mutant = (MutantScriptEngine) new MutantScriptEngineFactory().getScriptEngine();
    private final PrintStream output = System.out;
    private int currentEngineIndex = 0;

    private static final String MUTANT_HISTORY = ".mutant_history";

    public Console() throws Exception {
        this.output.println("\n" + "      //\n" + "     oO ~~-_\n" + "___m(___m___~.___  MuTanT " + Tokens.VERSION + "\n" + "_|__|__|__|__|__|     [ " + Tokens.HELP + " = help ]\n");
        this.printEngines();
        this.primaryLoop();
    }

    public void primaryLoop() throws Exception {

        final ConsoleReader reader = new ConsoleReader();
        reader.setBellEnabled(false);
        reader.setUseHistory(true);

        try {
            History history = new History();
            history.setHistoryFile(new File(MUTANT_HISTORY));
            reader.setHistory(history);
        } catch (IOException e) {
            System.err.println("Could not find history file");
        }

        String line = "";
        this.output.println();

        while (line != null) {

            try {
                line = "";
                boolean submit = false;
                boolean newline = false;
                while (!submit) {
                    if (newline)
                        line = line + "\n" + reader.readLine(Console.makeSpace(this.getPrompt().length()));
                    else
                        line = line + "\n" + reader.readLine(this.getPrompt());
                    if (line.endsWith(" .")) {
                        newline = true;
                        line = line.substring(0, line.length() - 2);
                    } else {
                        line = line.trim();
                        submit = true;
                    }
                }

                if (line.isEmpty())
                    continue;
                if (line.equals(Tokens.QUIT))
                    return;
                else if (line.equals(Tokens.PREVIOUS))
                    this.moveCurrentEngine(-1);
                else if (line.equals(Tokens.NEXT))
                    this.moveCurrentEngine(1);
                else if (line.equals(Tokens.BINDINGS))
                    this.printBindings(this.mutant.getManager().getBindings());
                else if (line.equals(Tokens.HELP))
                    this.printHelp();
                else if (line.equals(Tokens.ENGINES))
                    this.printEngines();
                else if (line.equals(Tokens.BINDINGS_DEBUG)) {
                    for (int scope : this.mutant.getCurrentEngine().getEngine().getContext().getScopes()) {
                        Bindings bindings = this.mutant.getCurrentEngine().getEngine().getContext().getBindings(scope);
                        if (null != bindings) {
                            this.output.println("Scope: " + scope);
                            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                                this.output.println("  " + entry);
                            }
                        }
                    }
                } else
                    this.output.println(this.mutant.eval(line));

            } catch (Exception e) {
                this.output.println("Evaluation error: " + e.getMessage());
            }
        }
    }

    public void printHelp() {
        this.output.println("-= Console Specific =-");
        this.output.println(Tokens.PREVIOUS + ": previous engine");
        this.output.println(Tokens.NEXT + ": next engine");
        this.output.println(Tokens.BINDINGS + ": show bindings");
        this.output.println(Tokens.ENGINES + ": show engines");
        this.output.println(Tokens.QUIT + ": quit");
        this.output.println("-= MuTanT Specific =-");
        this.output.println("?<lang-name>: jump to engine");
        this.output.println("?s <script-file>: load mutant script");

    }

    public void printEngines() {
        for (EngineHolder engine : this.mutant.getEngines()) {
            this.output.println("[" + engine.getLanguageName() + "] " + engine.getEngineName() + " " + engine.getEngineVersion());
        }
    }

    public void printBindings(final Bindings bindings) {
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            this.output.println(entry);
        }
    }

    public String getPrompt() {
        return "mutant[" + this.mutant.getCurrentEngine().getLanguageName() + "]> ";
    }


    public void moveCurrentEngine(int direction) {
        this.currentEngineIndex = this.currentEngineIndex + direction;
        if (this.currentEngineIndex == -1)
            this.currentEngineIndex = this.mutant.getEngines().size() - 1;
        else
            this.currentEngineIndex = this.currentEngineIndex % this.mutant.getEngines().size();

        this.mutant.setCurrentEngine(this.currentEngineIndex);
    }


    public static String makeSpace(int number) {
        String space = new String();
        for (int i = 0; i < number; i++) {
            space = space + " ";
        }
        return space;
    }

    public static void main(String[] args) throws Exception {
        new Console();
    }
}
