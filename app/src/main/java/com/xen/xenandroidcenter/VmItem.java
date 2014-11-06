package com.xen.xenandroidcenter;

import com.xensource.xenapi.Session;

/**
 * Created by chengz on 11/5/2014.
 */
public class VmItem {

    public final static String VMSTATUS_STOP = "Halted";
    public final static String VMSTATUS_MIGRATING = "Migrating";
    public final static String VMSTATUS_PAUSED = "Paused";
    public final static String VMSTATUS_RUNNING = "Running";
    public final static String VMSTATUS_SHUTTINGDOWN = "ShuttingDown";
    public final static String VMSTATUS_SUSPENDED = "Suspended";

    private String ipAddress;
    private String Name;
    private String UUID;
    private String MemSize;
    private String SrInfo;
    private String NicNum;
    private String Mac;
    private String OSInfo;
    private String PowerStatus;
    private String Uptime;
    private String TemplateName;

    public String getName() {
        return Name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUUID() {
        return UUID;
    }

    public String getMemSize() {
        return MemSize;
    }

    public String getNicNum() {
        return NicNum;
    }

    public String getSrInfo() {
        return SrInfo;
    }

    public String getMac() {
        return Mac;
    }

    public String getOSInfo() {
        return OSInfo;
    }

    public String getPowerStatus() {
        return PowerStatus;
    }

    public String getUptime() {
        return Uptime;
    }

    public String getTemplateName() {
        return TemplateName;
    }


    public VmItem(String ip, String name, String UUID, String MemSize, String SrInfo, String NicNum,
                  String Mac, String OSInfo, String PowerStatus, String Uptime, String TemplateName) {
        this.ipAddress = ip;
        this.Name = name;
        this.UUID = UUID;
        this.MemSize = MemSize;
        this.SrInfo = SrInfo;
        this.NicNum = NicNum;
        this.Mac = Mac;
        this.OSInfo = OSInfo;
        this.PowerStatus = PowerStatus;
        this.Uptime = Uptime;
        this.TemplateName = TemplateName;
    }
}

