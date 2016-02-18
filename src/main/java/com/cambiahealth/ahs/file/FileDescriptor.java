package com.cambiahealth.ahs.file;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by r627021 on 2/18/2016.
 */
public enum FileDescriptor {
    ACORS_FULL_EXTRACT(newArrayList("")),
    COB_EXTRACT(newArrayList("")),
    ZIP_CODE_EXTRACT(newArrayList("")),
    CLAIMS_CONFIG_EXTRACT(newArrayList("")),
    MEMBER_HISTORY_EXTRACT(newArrayList("")),
    CONFIDENTIAL_ADDRESS_EXTRACT(newArrayList("")),
    SUBSCRIBER_ADDRESS_EXTRACT(newArrayList("")),
    CONFIDENTIAL_EMAIL_PHONE_EXTRACT(newArrayList("")),
    CSPI_EXTRACT(newArrayList(""));

    private List<String> schema;

    FileDescriptor(List<String> schema) {
        this.schema = schema;
    }

    public List<String> getSchema() {
        return schema;
    }
}
