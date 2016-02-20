package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.*;

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
    MEMBER_HISTORY_EXTRACT(asList(enumNameToStringArray(MemberHistory.values()))),
    CONFIDENTIAL_ADDRESS_EXTRACT(asList(enumNameToStringArray(ConfidentialAddress.values()))),
    SUBSCRIBER_ADDRESS_EXTRACT(asList(enumNameToStringArray(SubscriberAddress.values()))),
    CONFIDENTIAL_EMAIL_PHONE_EXTRACT(asList(enumNameToStringArray(ConfidentialEmailPhone.values()))),
    CSPI_EXTRACT(asList(enumNameToStringArray(CspiHistory.values()))),
    FINAL_2A_OUTPUT(Collections.singletonList(""));

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
