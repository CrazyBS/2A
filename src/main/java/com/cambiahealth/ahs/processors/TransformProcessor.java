package com.cambiahealth.ahs.processors;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Pack200;

/**
 * Created by msnook on 2/19/2016.
 */
public class TransformProcessor {
    public static Map<String,String> processTransformationForFile(LocalDate start, LocalDate end, Map<String,String> data){
        Map<String, String> transformedResult = new HashMap<String, String>();

        String NDW_Plan_ID;

        if(data.get("") != null) {
            if(data.get("").equals("611")){
                NDW_Plan_ID = "611";
            } else if(data.get("").equals("350") || data.get("").equals("351") || data.get("").equals("850") || data.get("").equals("851") || data.get("").equals("852")){
                NDW_Plan_ID = "850";
            } else if(data.get("").equals("410") || data.get("").equals("910")){
                NDW_Plan_ID = "410";
            } else if(data.get("").equals("932") || data.get("").equals("933") || data.get("").equals("937") || data.get("").equals("938")){
                NDW_Plan_ID = "932";
            } else {
                NDW_Plan_ID = data.get("");
            }
        } else {
            NDW_Plan_ID = data.get("");
        }

        String Home_Plan_Product_ID = (null == data.get("")) ? "" : data.get("").substring(0,14);
        String NDW_Product_Category_Code = "PPO";
        String Member_ID = data.get("");
        String Consistent_Member_ID = (null == data.get("")) ? "" : data.get("");
        String Member_Date_of_Birth = (new LocalDate(data.get(""))).toString("yyyyMMdd");
        String Member_Gender = (null == data.get("")) ? "U" : data.get("");
        String Member_Confidentiality_Code = (data.get("").equals("Y")) ? "BLU" : (data.get("").equals("CONF")) ? "PHI" : "NON";
        String Coverage_Begin_Date = start.toString("yyyyMMdd");
        String Coverage_End_Date = end.toString("yyyyMMdd");
        String Member_Relationship;

        if(data.get("").equals("W") || data.get("").equals("H")){
            Member_Relationship = "01";
        } else if(data.get("").equals("M")){
            Member_Relationship = "18";
        } else if(data.get("").equals("S") || data.get("").equals("D")){
            Member_Relationship = "19";
        } else if(data.get("").equals("O")){
            Member_Relationship = "G8";
        } else {
            Member_Relationship = "21";
        }

        String ITS_Subscriber_ID = data.get("") + data.get("");
        String Group_or_Individual_Code = "GROUP";
        String Alpha_Prefix = (null == data.get("")) ? "" : data.get("");
        String Member_Prefix = (null == data.get("")) ? "" : data.get("");
        String Member_Last_Name = (null == data.get("")) ? "" : data.get("");
        String Member_First_Name = (null == data.get("")) ? "" : data.get("");
        String Member_Middle_Initial = (null != data.get("") && data.get("").length() > 0) ? data.get("") + "." : "";
        String Member_Suffix = (null == data.get("")) ? "" : data.get("");
        String Member_Primary_Street_Address_1 = (null == data.get("")) ? "" : data.get("");
        String Member_Primary_Street_address_2 = (null == data.get("")) ? "" : data.get("");
        String Member_Primary_City = (null == data.get("")) ? "" : data.get("");
        String Member_Primary_State = (null == data.get("")) ? "" : data.get("");
        String Member_Primary_ZIP_Code = (null == data.get("")) ? "" : data.get("").substring(0,4);
        String Member_Primary_ZIP_Code_4 = (null != data.get("") && data.get("").length() > 5) ? data.get("").substring(5,8) : "0000";
        String Member_Primary_Phone_Number = (null == data.get("")) ? "0000000000" : data.get("");
        String Member_Primary_Email_Address = (null != data.get("") && !data.get("").equals("\n")) ? data.get(""): "";
        String Member_Secondary_Street_Address_1 = (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_Street_Address_2 = (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_City = (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_State = (null == data.get("")) ? "" : data.get("");
        String Member_Secondary_ZIP_Code = (null == data.get("")) ? "" : data.get("").substring(0,4);
        String Member_Secondary_ZIP_Code_4 = (null == data.get("")) ? "" : data.get("").substring(5,8);
        String Host_Plan_Override = (null == data.get("")) ? "" : data.get("");
        String Member_Participation_Code = (null == data.get("")) ? "N" : data.get("");
        String Member_Medical_COB_Code = data.get("");
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

    public static Map<String,String> processTransformationForOracle(LocalDate start, LocalDate end, Map<String,String> data){
        Map<String, String> transformedResult = new HashMap<String, String>();

        String MBR_ID = data.get("");
        String MBR_EFF_DT = data.get("");
        String HOME_PLN_MBR_ID = data.get("");
        String BCBSA_CMI = (null == data.get("")) ? data.get("") : data.get("");
        String BCBSA_MMI = "";
        String MBR_CONFDNTL_CD = (data.get("").equals("Y")) ? "BLU" : (data.get("").equals("CONF")) ? "PHI" : "NON";;
        String ALPH_PFX = data.get("");
        String MBR_NAME_PFX = (null == data.get("")) ? "" : data.get("");
        String MBR_NAME_SFX = (null == data.get("")) ? "" : data.get("");
        String VOID_IND = "N";
        String NDW_HOME_PLN_CD;

        if(data.get("") != null) {
            if(data.get("").equals("611")){
                NDW_HOME_PLN_CD = "611";
            } else if(data.get("").equals("350") || data.get("").equals("351") || data.get("").equals("850") || data.get("").equals("851") || data.get("").equals("852")){
                NDW_HOME_PLN_CD = "850";
            } else if(data.get("").equals("410") || data.get("").equals("910")){
                NDW_HOME_PLN_CD = "410";
            } else if(data.get("").equals("932") || data.get("").equals("933") || data.get("").equals("937") || data.get("").equals("938")){
                NDW_HOME_PLN_CD = "932";
            } else {
                NDW_HOME_PLN_CD = data.get("");
            }
        } else {
            NDW_HOME_PLN_CD = data.get("");
        }

        String NDW_HOST_PLN_CD = "";
        String NDW_HOST_PLN_OVRRD_CD = (null == data.get("")) ? "" : data.get("");;
        String NDW_HOME_PLN_CORP_PLN_CD = "";
        String NDW_HOST_PLN_CORP_PLN_CD = "";
        String NDW_PROD_CAT_CD = "PPO";
        String GRP_OR_INDIVL_CNTRCT_CD = "GROUP";
        String MBR_MED_COB_CD = data.get("");

        return transformedResult;
    }
}
