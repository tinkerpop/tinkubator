package org.linkedprocess.linkeddata;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.LinkedProcessFarm;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 3, 2009
 * Time: 4:32:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedData {
    public static void main(final String[] args) throws Exception {
        new LinkedData().doit();
    }

    private void doit() throws IOException, ScriptException {
        ScriptEngine engine = LinkedProcessFarm.getScriptEngineManager()
                .getEngineByName(LinkedProcess.GROOVY);

        String program = toString(LinkedData.class.getResourceAsStream("lddemo.groovy"));
        Object result = engine.eval(program);

        System.out.println("result = " + result);
    }

    private String toString(final InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (0 < is.available()) {
            bos.write(is.read());
        }
        is.close();

        return bos.toString();
    }
}
