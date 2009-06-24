package gov.lanl.cnls.linkedprocess.xmpp.lopvm;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by IntelliJ IDEA.
 * User: marko
 * Date: Jun 24, 2009
 * Time: 12:12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluate extends IQ {

    public static final String EVALUATION_TAGNAME = "evaluate";
    String code;


    public void setCode(String code) {
        this.code = code;    
    }

    public String getCode() {
       return this.code;  
    }

    public String getChildElementXML() {
        StringBuilder builder = new StringBuilder("\n  <" + EVALUATION_TAGNAME + " xmlns=\"" + LopVirtualMachine.LOP_NAMESPACE +"\">");
        if(code != null) {
            builder.append(code);
        }
        builder.append("</"+ EVALUATION_TAGNAME +">\n");
        return builder.toString();
    }
}
