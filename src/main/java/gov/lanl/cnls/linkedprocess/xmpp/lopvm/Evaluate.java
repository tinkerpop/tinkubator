package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 12:12:20 PM
 */
public class Evaluate extends IQ {

    public static final String EVALUATION_TAGNAME = "evaluate";
    String expression;


    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
       return this.expression;
    }

    public String getChildElementXML() {
        StringBuilder builder = new StringBuilder("\n  <" + EVALUATION_TAGNAME + " xmlns=\"" + LopVirtualMachine.LOP_NAMESPACE +"\">");
        if(expression != null) {
            builder.append(expression);
        }
        builder.append("</"+ EVALUATION_TAGNAME +">\n");
        return builder.toString();
    }
}
