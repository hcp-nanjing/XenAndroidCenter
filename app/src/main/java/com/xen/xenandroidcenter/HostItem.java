package com.xen.xenandroidcenter;

import com.xensource.xenapi.Session;

/**
 * Created by chengz on 11/5/2014.
 */
public class HostItem {

    private String ipAddress;
    private String Name;
    private String UUID;
    private String MemSize;
    private String Role;
    private String Uptime;
    private String Version;
    private String MantaineMode;
    private String CPUUsage;

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

    public String getMantaineMode() {
        return MantaineMode;
    }

    public String getUptime() {
        return Uptime;
    }

    public String getRole() {
        return Role;
    }

    public String getVersion() {
        return Version;
    }

    public String getCPUUsage() {
        return CPUUsage;
    }

    public HostItem(String ip, String name, String UUID, String MemSize, String MantaineMode, String Role,
                  String Version, String CPUUsage, String Uptime) {
        this.ipAddress = ip;
        this.Name = name;
        this.UUID = UUID;
        this.MemSize = MemSize;
        this.MantaineMode = MantaineMode;
        this.Role = Role;
        this.Version = Version;
        this.CPUUsage = CPUUsage;
        this.Uptime = Uptime;
    }
}

