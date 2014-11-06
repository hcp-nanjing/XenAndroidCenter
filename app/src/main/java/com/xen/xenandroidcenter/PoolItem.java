package com.xen.xenandroidcenter;

import com.xensource.xenapi.Session;
import java.util.List;

/**
 * Created by zhengc on 11/5/2014.
 */
public class PoolItem {

    private String sessionUUID;
    private String ipAddress;
    private String hostName;
    private String userName;
    private String password;
    private List<VmItem> VMs;
    private List<HostItem> Hosts;

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    private Session session = null;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public List<VmItem> getVMs() {
        return VMs;
    }

    public void setVMs(List<VmItem> VMs) {
        this.VMs = VMs;
    }

    public List<HostItem> getHosts() {
        return Hosts;
    }

    public void setHosts(List<HostItem> Hosts) {
        this.Hosts = Hosts;
    }

    public PoolItem (String ip, String host, String user, String pass) {
        this.ipAddress = ip;
        this.hostName = host;
        this.userName = user;
        this.password = pass;
    }

}
