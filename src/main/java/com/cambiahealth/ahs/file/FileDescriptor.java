package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.AcorsEligibility;
import com.cambiahealth.ahs.entity.ClaimsConfig;
import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.entity.ZipCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Created by r627021 on 2/18/2016.
 */
public enum FileDescriptor {
    ACORS_ELIGIBILITY_EXTRACT(asList(enumNameToStringArray(AcorsEligibility.values()))),
    COB_EXTRACT(asList(enumNameToStringArray(Cob.values()))),
    ZIP_CODE_EXTRACT(asList(enumNameToStringArray(ZipCode.values()))),
    CLAIMS_CONFIG_EXTRACT(asList(enumNameToStringArray(ClaimsConfig.values()))),
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

    static <T extends Enum<T>> String[] enumNameToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value: values) {
            result[i++] = value.name();
        }
        return result;
    }
}
