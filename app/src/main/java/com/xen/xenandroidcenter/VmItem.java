package com.xen.xenandroidcenter;

import com.xensource.xenapi.Session;

/**
 * Created by chengz on 11/5/2014.
 */
public class VmItem {

    private String ipAddress;
    private String vmName;
    private String vmUUID;
    private String vmMemSize;
    private String vmSrInfo;
    private String vmNicNum;
    private String vmMac;
    private String vmOSInfo;
    private String vmPowerStatus;
    private String vmUptime;
    private String vmTemplateName;

    public String getName() {
        return vmName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUUID() {
        return vmUUID;
    }

    public String getMemSize() {
        return vmMemSize;
    }

    public String getNicNum() {
        return vmNicNum;
    }

    public String getSrInfo() {
        return vmSrInfo;
    }

    public String getMac() {
        return vmMac;
    }

    public String getOSInfo() {
        return vmOSInfo;
    }

    public String getPowerStatus() {
        return vmPowerStatus;
    }

    public String getUptime() {
        return vmUptime;
    }

    public String getTemplateName() {
        return vmTemplateName;
    }


    public VmItem(String ip, String name, String UUID, String MemSize, String SrInfo, String NicNum,
                  String Mac, String OSInfo, String PowerStatus, String Uptime, String TemplateName) {
        this.ipAddress = ip;
        this.vmName = name;
        this.vmUUID = UUID;
        this.vmMemSize = MemSize;
        this.vmSrInfo = SrInfo;
        this.vmNicNum = NicNum;
        this.vmMac = Mac;
        this.vmOSInfo = OSInfo;
        this.vmPowerStatus = PowerStatus;
        this.vmUptime = Uptime;
        this.vmTemplateName = TemplateName;
    }
}

