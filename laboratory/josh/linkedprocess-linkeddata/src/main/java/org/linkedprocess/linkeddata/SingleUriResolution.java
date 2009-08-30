package org.linkedprocess.linkeddata;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailConnection;

import java.util.Set;
import java.util.HashSet;

import info.aduna.iteration.CloseableIteration;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SingleUriResolution {

    public long resolveSingleUri(Sail sail, Resource uri) throws SailException {
        SailConnection c = sail.getConnection();
        long startTime = System.currentTimeMillis();
        c.getStatements(uri, null, null, false);
        return System.currentTimeMillis() - startTime;
    }
}
