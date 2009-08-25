package org.linkedprocess;

import junit.framework.TestCase;
import org.jivesoftware.smack.packet.IQ;
import org.junit.Test;
import org.linkedprocess.os.VmBindings;
import org.linkedprocess.farm.SpawnVm;
import org.linkedprocess.farm.SubmitJob;
import org.linkedprocess.farm.AbortJob;
import org.linkedprocess.farm.ManageBindings;
import org.linkedprocess.farm.TerminateVm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class LopPacketTest extends TestCase {

    public void testSpawnTag() throws Exception {
        SpawnVm spawnVm = new SpawnVm();
        //spawnVm.setVmJid("lp1@gmail.com");
        spawnVm.setVmSpecies("javascript");
        String spawnString = spawnVm.getChildElementXML();
        System.out.println(spawnString);
        assertTrue(spawnString.contains("xmlns=\"" + LinkedProcess.LOP_FARM_NAMESPACE));
        assertTrue(spawnString.contains("vm_jid=\"lp1@gmail.com\""));
        assertTrue(spawnString.contains("vm_species=\"javascript\""));
    }

    @Test
    public void testSubmitJobTag() {
        SubmitJob submitJob = new SubmitJob();
        //submitJob.setVmPassword("pass");
        submitJob.setExpression("for(int i=0; i<10; i++) { i; };");
        String evalString = submitJob.getChildElementXML();
        System.out.println(evalString);
        //assertTrue(evalString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        // note that XML characters must be handled correctly
        assertTrue(evalString.contains("for(int i=0; i&lt;10; i++) { i; };"));
        assertTrue(evalString.contains("vm_password=\"pass\""));
    }

    @Test
    public void testManageBindingsTag() throws Exception {
        ManageBindings manageBindings = new ManageBindings();
        manageBindings.setType(IQ.Type.SET);
        manageBindings.addBinding("name", "marko", VmBindings.XMLSchemaDatatype.STRING.getURI());
        manageBindings.addBinding("age", "29", VmBindings.XMLSchemaDatatype.INTEGER.getURI());
        assertTrue(manageBindings.toXML().contains("<binding name=\"age\" value=\"29\" datatype=\"http://www.w3.org/2001/XMLSchema#integer\" />"));
        manageBindings.setType(IQ.Type.GET);
        assertFalse(manageBindings.toXML().contains("<binding name=\"age\" value=\"29\" datatype=\"http://www.w3.org/2001/XMLSchema#integer\"/>"));
        assertTrue(manageBindings.toXML().contains("<binding name=\"age\" />"));
    }

    @Test
    public void testAbortJobTag() {
        AbortJob abortJob = new AbortJob();
        abortJob.setJobId("wxyz");
        //abortJob.setVmPassword("pass");
        String abortString = abortJob.getChildElementXML();
        System.out.println(abortString);
        //assertTrue(abortString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(abortString.contains("job_id=\"wxyz\""));
        assertTrue(abortString.contains("vm_password=\"pass\""));
    }

    @Test
    public void testTerminateTag() throws Exception {
        TerminateVm terminateVm = new TerminateVm();
        //terminateVm.setVmPassword("pass");
        String terminateString = terminateVm.getChildElementXML();
        System.out.println(terminateString);
        //assertTrue(terminateString.contains("xmlns=\"" + LinkedProcess.LOP_VM_NAMESPACE));
        assertTrue(terminateString.contains("vm_password=\"pass\""));
    }

}
