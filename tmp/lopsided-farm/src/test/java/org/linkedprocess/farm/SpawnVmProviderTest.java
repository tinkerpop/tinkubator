package org.linkedprocess.farm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;

public class SpawnVmProviderTest {

    private SpawnVmProvider sp;
    private XmlPullParser parser;

    @Before
    public void setup() throws Exception {
        sp = new SpawnVmProvider();
        parser = new MXParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

    }

    @Test
    public void correctXMLShouldGiveASpawnObject() throws Exception {
        SpawnVm spawn = new SpawnVm();
        SpawnVm result = parse(spawn.getChildElementXML());
        assertNotNull(result);
        // assertNotNull(result.getFarmPassword());

    }

    @Test
    public void incorrectXMLShouldGiveAnException() throws Exception {
        SpawnVm result = parse("<spawn/>");
        assertNotNull(result);
        result = parse("<spawn asdasd='asdas'/>");
        assertNotNull(result);
        //missing "'"
        try {
            result = parse("<spawn asdasd=asdas'/>");
            fail();
        } catch (XmlPullParserException xppe) {
            // expected
        }

        // assertNotNull(result.getFarmPassword());

    }

    private SpawnVm parse(String spawn) throws Exception {
        parser.setInput(new StringReader(spawn));
        int next = parser.next();

        SpawnVm res = (SpawnVm) sp.parseIQ(parser);
        return res;
    }
}
