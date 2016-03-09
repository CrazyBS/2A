package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.*;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Created by r627021 on 2/18/2016.
 */
public enum FileDescriptor {
    ACORS_ELIGIBILITY_EXTRACT(asList(enumNameToColumnArray(AcorsEligibility.values()))),
    COB_EXTRACT(asList(enumNameToColumnArray(Cob.values()))),
    ZIP_CODE_EXTRACT(asList(enumNameToColumnArray(ZipCode.values()))),
    CLAIMS_CONFIG_EXTRACT(asList(enumNameToColumnArray(ClaimsConfig.values()))),
    MEMBER_HISTORY_EXTRACT(asList(enumNameToColumnArray(MemberHistory.values()))),
    CONFIDENTIAL_ADDRESS_EXTRACT(asList(enumNameToColumnArray(ConfidentialAddress.values()))),
    SUBSCRIBER_ADDRESS_EXTRACT(asList(enumNameToColumnArray(SubscriberAddress.values()))),
    CONFIDENTIAL_EMAIL_PHONE_EXTRACT(asList(enumNameToColumnArray(ConfidentialEmailPhone.values()))),
    CSPI_EXTRACT(asList(enumNameToColumnArray(CspiHistory.values()))),
    BCBSA_MBR_PFX_SFX_XREF(asList(enumNameToColumnArray(BcbsaMbrPfxSfxXref.values()))),
    FINAL_2A_OUTPUT(asList(enumFixedWidthToColumnArray(NdwMember.values())), true),
    FINAL_2A_CONTROL(asList(enumFixedWidthToColumnArray(NdwFileSubmissionControl.values())), true);

    private List<Column> schema;
    private boolean isFixed;

    FileDescriptor(List<Column> schema) {
        this(schema, false);
    }

    FileDescriptor(List<Column> schema, boolean isFixed) {
        this.schema = schema;
        this.isFixed = isFixed;
    }

    public List<Column> getSchema() {
        return schema;
    }

    public boolean isFixed() {
        return isFixed;
    }

    static <T extends Enum<T>> Column[] enumNameToColumnArray(T[] values) {
        int i = 0;
        Column[] result = new Column[values.length];
        for (T value: values) {
            result[i++] = new Column(value.name());
        }
        return result;
    }

    static <T extends FixedWidth> Column[] enumFixedWidthToColumnArray(T[] values) {
        int i = 0;
        Column[] result = new Column[values.length];
        for (T value: values) {
            result[i++] = new Column(value.name(), value.getFixedWidth());
        }
        return result;
    }

}
