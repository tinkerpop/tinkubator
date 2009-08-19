/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

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
        JAVA_VERSION("java.version", "java_version", "version of the Java language"),
        JAVA_VM_NAME("java.vm.name", "java_vm_name", "name of the Java virtual machine"),
        JAVA_VM_VERSION("java.vm.version", "java_vm_version", "version of the Java virtual machine"),
        OS_ARCH("os.arch", "os_arch", "operating system architecture"),
        OS_NAME("os.name", "os_name", "operating system name"),
        OS_VERSION("os.version", "os_version", "operating system version");

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
