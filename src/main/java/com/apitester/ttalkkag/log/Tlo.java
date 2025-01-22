package com.apitester.ttalkkag.log;

import lombok.Data;

@Data
public class Tlo {
    private String seqId;
    private String logTime;
    private String logType;
    private String sid;
    private String resultCode;
    private String reqTime;
    private String rspTime;
    private String clientIp;
    private String devInfo;
    private String osInfo;
    private String nwInfo;
    private String svcName;
    private String devModel;
    private String carrierType;
    private String funcId;
    private String logKey;
    private String channelType;
    private String url;
    private String mid;

    @Override
    public String toString() {
        return "SEQ_ID=" + seqId + "|" +
                "LOG_TIME=" + logTime + "|" +
                "LOG_TYPE=" + logType + "|" +
                "SID=" + sid + "|" +
                "RESULT_CODE=" + resultCode + "|" +
                "REQ_TIME=" + reqTime + "|" +
                "RSP_TIME=" + rspTime + "|" +
                "CLIENT_IP=" + clientIp + "|" +
                "DEV_INFO=" + devInfo + "|" +
                "OS_INFO=" + osInfo + "|" +
                "NW_INFO=" + nwInfo + "|" +
                "SVC_NAME=" + svcName + "|" +
                "DEV_MODEL=" + devModel + "|" +
                "CARRIER_TYPE=" + carrierType + "|" +
                "FUNC_ID=" + funcId + "|" +
                "LOG_KEY=" + logKey + "|" +
                "CHANNEL_TYPE=" + channelType + "|" +
                "URL=" + url + "|" +
                "MID=" + mid;
    }
}
