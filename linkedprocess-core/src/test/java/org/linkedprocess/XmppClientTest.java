package org.linkedprocess;

import junit.framework.TestCase;
import org.linkedprocess.xmpp.XmppClient;

import java.util.HashSet;
import java.util.Set;

/**
 * User: marko
 * Date: Jun 26, 2009
 * Time: 5:52:06 PM
 */
public class XmppClientTest extends TestCase {

    public void testRandomIDGenerator() {
        // 1,000,000 random IDs are generated without conflict.
        Set<String> uniques = new HashSet<String>();
        for (int i = 0; i < 100000; i++) {
            //System.out.println(XmppClient.generateRandomResourceId());
            uniques.add(XmppClient.generateRandomResourceId());
        }
        assertEquals(uniques.size(), 100000);
    }

    public void testBoolean() {
        assertTrue(Boolean.valueOf("true"));
        assertFalse(Boolean.valueOf("false"));
    }
}
