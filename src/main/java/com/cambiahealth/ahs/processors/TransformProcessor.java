package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/19/2016.
 */
public class TransformProcessor {
    public static Map<NdwMember,String> processTransformationForFile(LocalDate start, LocalDate end, Map<String,String> data){
        LinkedHashMap<NdwMember,String> transformedResult = new LinkedHashMap<NdwMember,String>();

        transformedResult.put(NdwMember.NDW_PLAN_ID, (null == data.get(ClaimsConfig.PLAN.toString())) ? "" : processPlan(data.get(ClaimsConfig.PLAN.toString())));
        transformedResult.put(NdwMember.HOME_PLAN_PRODUCT_ID, (null == data.get(AcorsEligibility.PRODUCT_ID.toString())) ? "" : StringUtils.substring(data.get(AcorsEligibility.PRODUCT_ID.toString()),0,14));
        transformedResult.put(NdwMember.NDW_PRODUCT_CATEGORY_CODE, "PPO");
        transformedResult.put(NdwMember.MEMBER_ID, data.get(CspiHistory.MEME_CK.toString()));
        transformedResult.put(NdwMember.CONSISTENT_MEMBER_ID, (null == data.get(AcorsEligibility.CTG_ID.toString())) ? "" : data.get(AcorsEligibility.CTG_ID.toString()));
/* ** */transformedResult.put(NdwMember.MEMBER_DATE_OF_BIRTH, (new LocalDate(data.get(AcorsEligibility.DOB.toString()))).toString("yyyyMMdd"));
/* ** */transformedResult.put(NdwMember.MEMBER_GENDER, (null == data.get(AcorsEligibility.GENDER.toString())) ? "U" : data.get(AcorsEligibility.GENDER.toString()));
/* ** */transformedResult.put(NdwMember.MEMBER_CONFIDENTIALITY_CODE, StringUtils.isNotEmpty(data.get(ConsistentFields.IS_BLU.toString())) ? "BLU" : StringUtils.isNotEmpty(data.get(ConsistentFields.IS_PHI.toString())) ? "PHI" : "NON");
        transformedResult.put(NdwMember.COVERAGE_BEGIN_DATE, start.toString("yyyyMMdd"));
        transformedResult.put(NdwMember.COVERAGE_END_DATE, end.toString("yyyyMMdd"));
        transformedResult.put(NdwMember.MEMBER_RELATIONSHIP, processRelationship((null == data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString())) ? "" : data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString())));
        transformedResult.put(NdwMember.ITS_SUBSCRIBER_ID, data.get(CspiHistory.CSPI_ITS_PREFIX.toString()) + data.get(CspiHistory.SBSB_ID.toString()));
        transformedResult.put(NdwMember.GROUP_OR_INDIVIDUAL_CODE, "GROUP");
        transformedResult.put(NdwMember.ALPHA_PREFIX, (null == data.get(CspiHistory.CSPI_ITS_PREFIX.toString())) ? "" : data.get(CspiHistory.CSPI_ITS_PREFIX.toString()));
        transformedResult.put(NdwMember.MEMBER_PREFIX, (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString()));
        transformedResult.put(NdwMember.MEMBER_LAST_NAME, (null == data.get(MemberHistory.MEME_LAST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_LAST_NAME.toString()));
        transformedResult.put(NdwMember.MEMBER_FIRST_NAME, (null == data.get(MemberHistory.MEME_FIRST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_FIRST_NAME.toString()));
        transformedResult.put(NdwMember.MEMBER_MIDDLE_INITIAL, (null != data.get(MemberHistory.MEME_MID_INIT.toString()) && data.get(MemberHistory.MEME_MID_INIT.toString()).length() > 0) ? data.get(MemberHistory.MEME_MID_INIT.toString()) + "." : "");
        transformedResult.put(NdwMember.MEMBER_SUFFIX, (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString()));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_STREET_ADDRESS_1, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_ADDR1.toString()) : data.get(ConfidentialAddress.ENAD_ADDR1.toString()));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_STREET_ADDRESS_2, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_ADDR2.toString()) : data.get(ConfidentialAddress.ENAD_ADDR2.toString()));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_CITY, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_CITY.toString()) : data.get(ConfidentialAddress.ENAD_CITY.toString()));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_STATE, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_STATE.toString()) : data.get(ConfidentialAddress.ENAD_STATE.toString()));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_ZIP_CODE, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? StringUtils.substring(data.get(SubscriberAddress.SBAD_ZIP.toString()),0,5) : StringUtils.substring(data.get(ConfidentialAddress.ENAD_ZIP.toString()),0,5));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_ZIP_CODE_4, null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? zipCode4(data.get(SubscriberAddress.SBAD_ZIP.toString()),true) : zipCode4(data.get(ConfidentialAddress.ENAD_ZIP.toString()),true));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_PHONE_NUMBER, null == data.get(ConfidentialEmailPhone.ENPH_PHONE.toString()) ? phone(data.get(SubscriberAddress.SBAD_PHONE.toString())) : phone(data.get(ConfidentialEmailPhone.ENPH_PHONE.toString())));
        transformedResult.put(NdwMember.MEMBER_PRIMARY_EMAIL_ADDRESS, null == data.get(ConfidentialEmailPhone.ENEM_EMAIL.toString()) ? data.get(SubscriberAddress.SBAD_EMAIL.toString()) : data.get(ConfidentialEmailPhone.ENEM_EMAIL.toString()));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_STREET_ADDRESS_1, data.get("secd_" + SubscriberAddress.SBAD_ADDR1.toString()));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_STREET_ADDRESS_2, data.get("secd_" + SubscriberAddress.SBAD_ADDR2.toString()));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_CITY, data.get("secd_" + SubscriberAddress.SBAD_CITY.toString()));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_STATE, data.get("secd_" + SubscriberAddress.SBAD_STATE.toString()));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_ZIP_CODE, StringUtils.substring(data.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()),0,5));
        transformedResult.put(NdwMember.MEMBER_SECONDARY_ZIP_CODE_4, zipCode4(data.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()),false));
        transformedResult.put(NdwMember.HOST_PLAN_OVERRIDE, (null == data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString())) ? "" : data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString()));
        transformedResult.put(NdwMember.MEMBER_PARTICIPATION_CODE, (null == data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString())) ? "N" : data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString()));
        transformedResult.put(NdwMember.MEMBER_MEDICAL_COB_CODE, (null == data.get(Cob.COB_VALUE.toString())) ? "P" : (data.get(Cob.COB_VALUE.toString()).equals("M")) ? "M" : "S");
        transformedResult.put(NdwMember.VOID_INDICATOR, "N");
        transformedResult.put(NdwMember.MMI_INDICATOR, "");
        transformedResult.put(NdwMember.HOST_PLAN_CODE, "");
        transformedResult.put(NdwMember.HOME_PLAN_CORPORATE_PLAN_CODE, "");
        transformedResult.put(NdwMember.PHARMACY_CARVE_OUT_INDICATOR, "");

        return transformedResult;
    }

    /**
     * TODO: May still need this in the future, but not for now.
     *
     */
    public static LinkedHashMap<String,Object> processTransformationForOracle(LocalDate start, LocalDate end, Map<String,String> data){
        LinkedHashMap<String, Object> transformedResult = new LinkedHashMap<String, Object>();

        long MBR_ID = Long.parseLong(data.get(""));
        DateTime MBR_EFF_DT = new DateTime(start);
        String HOME_PLN_MBR_ID = data.get(CspiHistory.MEME_CK.toString());
        String BCBSA_CMI = (null == data.get(AcorsEligibility.CTG_ID.toString())) ? data.get(CspiHistory.MEME_CK.toString()) : data.get(AcorsEligibility.CTG_ID.toString());
        String BCBSA_MMI = "";
        String MBR_CONFDNTL_CD ="";// (data.get(AcorsEligibility.MASK_IND.toString()).equals("Y")) ? "BLU" : (data.get("").equals("CONF")) ? "PHI" : "NON";
        String ALPH_PFX = data.get("");
        String MBR_NAME_PFX = (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString());
        String MBR_NAME_SFX = (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString())) ? "" : data.get("");
        String VOID_IND = "N";
        String NDW_HOME_PLN_CD = (null == data.get(ClaimsConfig.PLAN.toString())) ? "" : processPlan(data.get(ClaimsConfig.PLAN.toString()));
        String NDW_HOST_PLN_CD = "";
        String NDW_HOST_PLN_OVRRD_CD = (null == data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString())) ? "" : data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString());
        String NDW_HOME_PLN_CORP_PLN_CD = "";
        String NDW_HOST_PLN_CORP_PLN_CD = "";
        String NDW_PROD_CAT_CD = "PPO";
        String GRP_OR_INDIVL_CNTRCT_CD = "GROUP";
        String MBR_MED_COB_CD = data.get(Cob.COB_VALUE.toString());


        transformedResult.put("MBR_ID",MBR_ID);
        transformedResult.put("MBR_EFF_DT",MBR_EFF_DT);
        transformedResult.put("HOME_PLN_MBR_ID",HOME_PLN_MBR_ID);
        transformedResult.put("BCBSA_CMI",BCBSA_CMI);
        transformedResult.put("BCBSA_MMI",BCBSA_MMI);
        transformedResult.put("MBR_CONFDNTL_CD",MBR_CONFDNTL_CD);
        transformedResult.put("ALPH_PFX",ALPH_PFX);
        transformedResult.put("MBR_NAME_PFX",MBR_NAME_PFX);
        transformedResult.put("MBR_NAME_SFX",MBR_NAME_SFX);
        transformedResult.put("VOID_IND",VOID_IND);
        transformedResult.put("NDW_HOME_PLN_CD",NDW_HOME_PLN_CD);
        transformedResult.put("NDW_HOST_PLN_CD",NDW_HOST_PLN_CD);
        transformedResult.put("NDW_HOST_PLN_OVRRD_CD",NDW_HOST_PLN_OVRRD_CD);
        transformedResult.put("NDW_HOME_PLN_CORP_PLN_CD",NDW_HOME_PLN_CORP_PLN_CD);
        transformedResult.put("NDW_HOST_PLN_CORP_PLN_CD",NDW_HOST_PLN_CORP_PLN_CD);
        transformedResult.put("NDW_PROD_CAT_CD",NDW_PROD_CAT_CD);
        transformedResult.put("GRP_OR_INDIVL_CNTRCT_CD",GRP_OR_INDIVL_CNTRCT_CD);
        transformedResult.put("MBR_MED_COB_CD",MBR_MED_COB_CD);

        return transformedResult;
    }

    private static String processPlan(String planToProcess) {
        String plan;

        if(planToProcess.equals("611")){
            plan = "611";
        } else if(planToProcess.equals("350") || planToProcess.equals("351") || planToProcess.equals("850") || planToProcess.equals("851") || planToProcess.equals("852")){
            plan = "850";
        } else if(planToProcess.equals("410") || planToProcess.equals("910")){
            plan = "410";
        } else if(planToProcess.equals("932") || planToProcess.equals("933") || planToProcess.equals("937") || planToProcess.equals("938")){
            plan = "932";
        } else {
            plan = planToProcess;
        }
        
        return plan;
    }

    private static String processRelationship(String relationshipToProcess){
        String Member_Relationship;

        if(relationshipToProcess.equals("W") || relationshipToProcess.equals("H")){
            Member_Relationship = "01";
        } else if(relationshipToProcess.equals("M")){
            Member_Relationship = "18";
        } else if(relationshipToProcess.equals("S") || relationshipToProcess.equals("D")){
            Member_Relationship = "19";
        } else if(relationshipToProcess.equals("O")){
            Member_Relationship = "G8";
        } else {
            Member_Relationship = "21";
        }

        return Member_Relationship;
    }

    private static String zipCode4(String zip, boolean isPrimary) {
        String zip4 = StringUtils.trimToEmpty(StringUtils.substring(zip, 5, 9));
        return (!StringUtils.equals(zip4, "0000") && !StringUtils.isEmpty(zip4)) ? zip4 : isPrimary ? "0000" : "";
    }

    private static String phone(String phone) {
        String number = StringUtils.trimToEmpty(StringUtils.substring(phone, 0, 11));
        return StringUtils.length(number) != 10 ? "0000000000" : phone;
    }
}
