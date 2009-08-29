package org.linkedprocess.linkeddata;

import org.openrdf.model.Resource;

/**
 * Author: josh
* Date: Aug 29, 2009
* Time: 4:05:10 PM
*/
public class WeightedValue implements Comparable {
    public Resource value;
    public long weight;

    public int compareTo(Object other) {
        return - ((Long) weight).compareTo(((WeightedValue) other).weight);
    }
}
