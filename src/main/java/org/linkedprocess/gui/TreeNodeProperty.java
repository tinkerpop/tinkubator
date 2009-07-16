package gov.lanl.cnls.linkedprocess.gui;

/**
 * User: marko
 * Date: Jul 6, 2009
 * Time: 3:13:23 PM
 */
public class TreeNodeProperty {

        private final String key;
        private final String value;

        public TreeNodeProperty(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

 }