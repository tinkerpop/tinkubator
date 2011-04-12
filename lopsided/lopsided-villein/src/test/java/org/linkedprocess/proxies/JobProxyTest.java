package org.linkedprocess.proxies;

import junit.framework.TestCase;
import org.linkedprocess.villein.proxies.JobProxy;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class JobProxyTest extends TestCase {

    public void testEquals() {
        JobProxy job1 = new JobProxy();
        job1.setJobId("ABCD");
        JobProxy job2 = new JobProxy();
        job2.setJobId("ABCD");
        assertEquals(job1, job2);
        JobProxy job3 = new JobProxy();
        job3.setJobId("EFGH");
        assertFalse(job1 == job3);
    }

    public void testHashCode() {
        Set<JobProxy> jobProxies = new HashSet<JobProxy>();
        for (int i = 0; i < 1000; i++) {
            JobProxy jobProxy = new JobProxy();
            jobProxy.setJobId("ABCD");
            jobProxies.add(jobProxy);
        }
        assertEquals(jobProxies.size(), 1);
    }
}
