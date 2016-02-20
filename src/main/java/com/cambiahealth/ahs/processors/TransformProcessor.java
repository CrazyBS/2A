package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by msnook on 2/19/2016.
 */
public class TransformProcessor {
    public static Map<String,String> processTransformationForFile(LocalDate start, LocalDate end, Map<String,String> data){
        Map<String, String> transformedResult = new HashMap<String, String>();

        String NDW_Plan_ID = (null == data.get(ClaimsConfig.PLAN.toString())) ? "" : processPlan(data.get(ClaimsConfig.PLAN.toString()));
        String Home_Plan_Product_ID = (null == data.get(AcorsEligibility.PRODUCT_ID.toString())) ? "" : data.get(AcorsEligibility.PRODUCT_ID.toString()).substring(0,14);
        String NDW_Product_Category_Code = "PPO";
        String Member_ID = data.get(CspiHistory.MEME_CK.toString());
        String Consistent_Member_ID = (null == data.get(AcorsEligibility.CTG_ID.toString())) ? "" : data.get(AcorsEligibility.CTG_ID.toString());
        String Member_Date_of_Birth = (new LocalDate(data.get(AcorsEligibility.DOB.toString()))).toString("yyyyMMdd");
        String Member_Gender = (null == data.get(AcorsEligibility.GENDER.toString())) ? "U" : data.get(AcorsEligibility.GENDER.toString());
        String Member_Confidentiality_Code ="";// (data.get(AcorsEligibility.MASK_IND.toString()).equals("Y")) ? "BLU" : (data.get("").equals("CONF")) ? "PHI" : "NON";//TODO Address type
        String Coverage_Begin_Date = start.toString("yyyyMMdd");
        String Coverage_End_Date = end.toString("yyyyMMdd");
        String Member_Relationship = processRelationship((null == data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString())) ? "" : data.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString()));
        String ITS_Subscriber_ID = data.get(CspiHistory.CSPI_ITS_PREFIX.toString()) + data.get(CspiHistory.SBSB_ID.toString());
        String Group_or_Individual_Code = "GROUP";
        String Alpha_Prefix = (null == data.get(CspiHistory.CSPI_ITS_PREFIX.toString())) ? "" : data.get(CspiHistory.CSPI_ITS_PREFIX.toString());
        String Member_Prefix = (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString());
        String Member_Last_Name = (null == data.get(MemberHistory.MEME_LAST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_LAST_NAME.toString());
        String Member_First_Name = (null == data.get(MemberHistory.MEME_FIRST_NAME.toString())) ? "" : data.get(MemberHistory.MEME_FIRST_NAME.toString());
        String Member_Middle_Initial = (null != data.get(MemberHistory.MEME_MID_INIT.toString()) && data.get(MemberHistory.MEME_MID_INIT.toString()).length() > 0) ? data.get(MemberHistory.MEME_MID_INIT.toString()) + "." : "";
        String Member_Suffix = (null == data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString())) ? "" : data.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString());
        String Member_Primary_Street_Address_1 ="";// (null == data.get("")) ? "" : data.get("");//TODO: All address
        String Member_Primary_Street_address_2 ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Primary_City ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Primary_State ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Primary_ZIP_Code ="";// (null == data.get("")) ? "" : data.get("").substring(0,4);
        String Member_Primary_ZIP_Code_4 ="";// (null != data.get("") && data.get("").length() > 5) ? data.get("").substring(5,8) : "0000";
        String Member_Primary_Phone_Number ="";// (null == data.get("")) ? "0000000000" : data.get("");
        String Member_Primary_Email_Address ="";// (null != data.get("") && !data.get("").equals("\n")) ? data.get(""): "";
        String Member_Secondary_Street_Address_1 ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_Street_Address_2 ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_City ="";// (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_State = "";// (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_ZIP_Code ="";// (null == data.get("")) ? "" : data.get("").substring(0,4);
        String Member_Secondary_ZIP_Code_4 ="";// (null == data.get("")) ? "" : data.get("").substring(5,8);
        String Host_Plan_Override = (null == data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString())) ? "" : data.get(AcorsEligibility.HOST_PLAN_OVERRIDE.toString());
        String Member_Participation_Code = (null == data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString())) ? "N" : data.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString());
        String Member_Medical_COB_Code = (null == data.get(Cob.COB_VALUE.toString())) ? "P" : (data.get(Cob.COB_VALUE.toString()).equals("P")) ? "S" : "M";
        String Void_Indicator = "N";
        String MMI_Indicator = "";
        String Host_Plan_Code = "";
        String Home_Plan_Corporate_Plan_Code = "";
        String Pharmacy_Carve_Out_Indicator = "";

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

    public static Map<String,Object> processTransformationForOracle(LocalDate start, LocalDate end, Map<String,String> data){
        Map<String, Object> transformedResult = new HashMap<String, Object>();

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
}
