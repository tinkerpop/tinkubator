package org.linkedprocess.security;

import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;

import java.util.Properties;

/**
 * Author: josh
 * Date: Jul 23, 2009
 * Time: 1:44:12 PM
 */
public class SystemInfo {
    public enum SystemProperty {
        JAVA_VERSION("java.version", "java-version", "version of the Java language"),
        JAVA_VM_NAME("java.vm.name", "java-vm-name", "name of the Java virtual machine"),
        JAVA_VM_VERSION("java.vm.version", "java-vm-version", "version of the Java virtual machine"),
        OS_ARCH("os.arch", "os-arch", "operating system architecture"),
        OS_NAME("os.name", "os-name", "operating system name"),
        OS_VERSION("os.version", "os-version", "operating system version");

        private final String propertyName;
        private final String specName;
        private final String label;

        private SystemProperty(final String propertyName,
                               final String specName,
                               final String label) {
            this.propertyName = propertyName;
            this.specName = specName;
            this.label = label;
        }
    }

    public static void addFields(final DataForm serviceExtension) {
        Properties props = System.getProperties();
        for (SystemProperty p : SystemProperty.values()) {
            String key = p.propertyName;
            String value = props.getProperty(key);
            if (null != value) {
                FormField field = new FormField(p.specName);
                field.setLabel(p.label);
                field.setType(FormField.TYPE_TEXT_SINGLE);
                field.addValue(value);
                serviceExtension.addField(field);
            }
        }
    }
}
