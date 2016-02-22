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
    public static Map<String,Column> processTransformationForFile(LocalDate start, LocalDate end, Map<String,String> data){
        LinkedHashMap<String, Column> transformedResult = new LinkedHashMap<String, Column>();

        Column NDW_Plan_ID = new Column((null == data.get(ClaimsConfig.PLAN.toString())) ? "" : processPlan(data.get(ClaimsConfig.PLAN.toString())),3);
        Column Home_Plan_Product_ID = new Column((null == data.get(AcorsEligibility.PRODUCT_ID.toString())) ? "" : StringUtils.substring(data.get(AcorsEligibility.PRODUCT_ID.toString()),0,14),15);
        Column NDW_Product_Category_Code = new Column("PPO",3);
        Column Member_ID = new Column(data.get(CspiHistory.MEME_CK.toString()),22);
        Column Consistent_Member_ID = new Column((null == data.get(AcorsEligibility.CTG_ID.toString())) ? "" : data.get(AcorsEligibility.CTG_ID.toString()),22);
/* ** */Column Member_Date_of_Birth = new Column((new LocalDate(data.get(AcorsEligibility.DOB.toString()))).toString("yyyyMMdd"),8);
/* ** */Column Member_Gender = new Column((null == data.get(AcorsEligibility.GENDER.toString())) ? "U" : data.get(AcorsEligibility.GENDER.toString()),1);
/* ** */Column Member_Confidentiality_Code = new Column(StringUtils.isNotEmpty(data.get(ConsistentFields.IS_BLU.toString())) ? "BLU" : StringUtils.isNotEmpty(data.get(ConsistentFields.IS_PHI.toString())) ? "PHI" : "NON",3);
        Column Coverage_Begin_Date = new Column(start.toString("yyyyMMdd"),8);
        Column Coverage_End_Date = new Column(end.toString("yyyyMMdd"),8);
        Column Member_Relationship = new Column(processRelationship((null == data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString())) ? "" : data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString())),2);
        Column ITS_Subscriber_ID = new Column(data.get(CspiHistory.CSPI_ITS_PREFIX.toString()) + data.get(CspiHistory.SBSB_ID.toString()),17);
        Column Group_or_Individual_Code = new Column("GROUP",10);
        Column Alpha_Prefix = new Column((null == data.get(CspiHistory.CSPI_ITS_PREFIX.toString())) ? "" : data.get(CspiHistory.CSPI_ITS_PREFIX.toString()),3);
        Column Member_Prefix = new Column((null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString()),20);
        Column Member_Last_Name = new Column((null == data.get(MemberHistory.MEME_LAST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_LAST_NAME.toString()),150);
        Column Member_First_Name = new Column((null == data.get(MemberHistory.MEME_FIRST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_FIRST_NAME.toString()),70);
        Column Member_Middle_Initial = new Column((null != data.get(MemberHistory.MEME_MID_INIT.toString()) && data.get(MemberHistory.MEME_MID_INIT.toString()).length() > 0) ? data.get(MemberHistory.MEME_MID_INIT.toString()) + "." : "",2);
        Column Member_Suffix = new Column((null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString()),20);
        Column Member_Primary_Street_Address_1 = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_ADDR1.toString()) : data.get(ConfidentialAddress.ENAD_ADDR1.toString()),70);// (null == data.get("")) ? "" : data.get("");//TODO: All address
        Column Member_Primary_Street_address_2 = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_ADDR2.toString()) : data.get(ConfidentialAddress.ENAD_ADDR1.toString()),70);// (null == data.get("")) ? "" : data.get("");
        Column Member_Primary_City = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_CITY.toString()) : data.get(ConfidentialAddress.ENAD_CITY.toString()),35);// (null == data.get("")) ? "" : data.get("");
        Column Member_Primary_State = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? data.get(SubscriberAddress.SBAD_STATE.toString()) : data.get(ConfidentialAddress.ENAD_STATE.toString()),2);// (null == data.get("")) ? "" : data.get("");
        Column Member_Primary_ZIP_Code = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? StringUtils.substring(data.get(SubscriberAddress.SBAD_ZIP.toString()),0,5) : StringUtils.substring(data.get(ConfidentialAddress.ENAD_ZIP.toString()),0,5),5);// (null == data.get("")) ? "" : data.get("").subColumn(0,4);
        Column Member_Primary_ZIP_Code_4 = new Column(null == data.get(ConfidentialAddress.ENAD_ADDR1.toString()) ? zipCode4(data.get(SubscriberAddress.SBAD_ZIP.toString()),true) : zipCode4(data.get(ConfidentialAddress.ENAD_ZIP.toString()),true),4);// (null != data.get("") && data.get("").length() > 5) ? data.get("").subColumn(5,8) : "0000";
        Column Member_Primary_Phone_Number = new Column(null == data.get(ConfidentialEmailPhone.ENPH_PHONE.toString()) ? phone(data.get(SubscriberAddress.SBAD_PHONE.toString())) : phone(data.get(ConfidentialEmailPhone.ENPH_PHONE.toString())),10);// (null == data.get("")) ? "0000000000" : data.get("");
        Column Member_Primary_Email_Address = new Column(null == data.get(ConfidentialEmailPhone.ENEM_EMAIL.toString()) ? data.get(SubscriberAddress.SBAD_EMAIL.toString()) : data.get(ConfidentialEmailPhone.ENEM_EMAIL.toString()),70);// (null != data.get("") && !data.get("").equals("\n")) ? data.get(""): "";
        Column Member_Secondary_Street_Address_1 = new Column(data.get("secd_" + SubscriberAddress.SBAD_ADDR1.toString()),70);// (null == data.get("")) ? "" : data.get("");
        Column Member_Secondary_Street_Address_2 = new Column(data.get("secd_" + SubscriberAddress.SBAD_ADDR2.toString()),70);// (null == data.get("")) ? "" : data.get("");
        Column Member_Secondary_City = new Column(data.get("secd_" + SubscriberAddress.SBAD_CITY.toString()),35);// (null == data.get("")) ? "" : data.get("");
        Column Member_Secondary_State = new Column(data.get("secd_" + SubscriberAddress.SBAD_STATE.toString()),2);// (null == data.get("")) ? "" : data.get("");
        Column Member_Secondary_ZIP_Code = new Column(StringUtils.substring(data.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()),0,5),5);// (null == data.get("")) ? "" : data.get("").subColumn(0,4);
        Column Member_Secondary_ZIP_Code_4 = new Column(zipCode4(data.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()),false),4);// (null == data.get("")) ? "" : data.get("").subColumn(5,8);
        Column Host_Plan_Override = new Column((null == data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString())) ? "" : data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString()),3);
        Column Member_Participation_Code = new Column((null == data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString())) ? "N" : data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString()),1);
        Column Member_Medical_COB_Code = new Column((null == data.get(Cob.COB_VALUE.toString())) ? "P" : (data.get(Cob.COB_VALUE.toString()).equals("M")) ? "M" : "S",1);
        Column Void_Indicator = new Column("N",1);
        Column MMI_Indicator = new Column("",22);
        Column Host_Plan_Code = new Column("",3);
        Column Home_Plan_Corporate_Plan_Code = new Column("",3);
        Column Pharmacy_Carve_Out_Indicator = new Column("",1);

        transformedResult.put("NDW_Plan_ID",NDW_Plan_ID);
        transformedResult.put("Home_Plan_Product_ID",Home_Plan_Product_ID);
        transformedResult.put("NDW_Product_Category_Code",NDW_Product_Category_Code);
        transformedResult.put("Member_ID",Member_ID);
        transformedResult.put("Consistent_Member_ID",Consistent_Member_ID);
        transformedResult.put("Member_Date_of_Birth",Member_Date_of_Birth);
        transformedResult.put("Member_Gender",Member_Gender);
        transformedResult.put("Member_Confidentiality_Code",Member_Confidentiality_Code);
        transformedResult.put("Coverage_Begin_Date",Coverage_Begin_Date);
        transformedResult.put("Coverage_End_Date",Coverage_End_Date);
        transformedResult.put("Member_Relationship",Member_Relationship);
        transformedResult.put("ITS_Subscriber_ID",ITS_Subscriber_ID);
        transformedResult.put("Group_or_Individual_Code",Group_or_Individual_Code);
        transformedResult.put("Alpha_Prefix",Alpha_Prefix);
        transformedResult.put("Member_Prefix",Member_Prefix);
        transformedResult.put("Member_Last_Name",Member_Last_Name);
        transformedResult.put("Member_First_Name",Member_First_Name);
        transformedResult.put("Member_Middle_Initial",Member_Middle_Initial);
        transformedResult.put("Member_Suffix",Member_Suffix);
        transformedResult.put("Member_Primary_Street_Address_1",Member_Primary_Street_Address_1);
        transformedResult.put("Member_Primary_Street_address_2",Member_Primary_Street_address_2);
        transformedResult.put("Member_Primary_City",Member_Primary_City);
        transformedResult.put("Member_Primary_State",Member_Primary_State);
        transformedResult.put("Member_Primary_ZIP_Code",Member_Primary_ZIP_Code);
        transformedResult.put("Member_Primary_ZIP_Code_4",Member_Primary_ZIP_Code_4);
        transformedResult.put("Member_Primary_Phone_Number",Member_Primary_Phone_Number);
        transformedResult.put("Member_Primary_Email_Address",Member_Primary_Email_Address);
        transformedResult.put("Member_Secondary_Street_Address_1",Member_Secondary_Street_Address_1);
        transformedResult.put("Member_Secondary_Street_Address_2",Member_Secondary_Street_Address_2);
        transformedResult.put("Member_Secondary_City",Member_Secondary_City);
        transformedResult.put("Member_Secondary_State",Member_Secondary_State);
        transformedResult.put("Member_Secondary_ZIP_Code",Member_Secondary_ZIP_Code);
        transformedResult.put("Member_Secondary_ZIP_Code_4",Member_Secondary_ZIP_Code_4);
        transformedResult.put("Host_Plan_Override",Host_Plan_Override);
        transformedResult.put("Member_Participation_Code",Member_Participation_Code);
        transformedResult.put("Member_Medical_COB_Code",Member_Medical_COB_Code);
        transformedResult.put("Void_Indicator",Void_Indicator);
        transformedResult.put("MMI_Indicator",MMI_Indicator);
        transformedResult.put("Host_Plan_Code",Host_Plan_Code);
        transformedResult.put("Home_Plan_Corporate_Plan_Code",Home_Plan_Corporate_Plan_Code);
        transformedResult.put("Pharmacy_Carve_Out_Indicator",Pharmacy_Carve_Out_Indicator);

        return transformedResult;
    }

    public static LinkedHashMap<String,Object> processTransformationForOracle(LocalDate start, LocalDate end, Map<String,String> data){
        LinkedHashMap<String, Object> transformedResult = new LinkedHashMap<String, Object>();

        long MBR_ID = Long.parseLong(data.get(""));//TODO Delayed as unimportant
        DateTime MBR_EFF_DT = new DateTime(start);
        String HOME_PLN_MBR_ID = data.get(CspiHistory.MEME_CK.toString());
        String BCBSA_CMI = (null == data.get(AcorsEligibility.CTG_ID.toString())) ? data.get(CspiHistory.MEME_CK.toString()) : data.get(AcorsEligibility.CTG_ID.toString());
        String BCBSA_MMI = "";
        String MBR_CONFDNTL_CD ="";// (data.get(AcorsEligibility.MASK_IND.toString()).equals("Y")) ? "BLU" : (data.get("").equals("CONF")) ? "PHI" : "NON";//TODO Address type
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
