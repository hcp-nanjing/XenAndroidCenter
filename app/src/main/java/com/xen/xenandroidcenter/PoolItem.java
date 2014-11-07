package com.xen.xenandroidcenter;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Session;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengc on 11/5/2014.
 */
public class PoolItem {

    private String sessionUUID;
    private Connection connection;
    private String ipAddress;
    private String hostName;
    private String userName;
    private String password;
    private Map<String, VmItem> VMs;
    private Map<String, HostItem> Hosts;

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

    public Map<String, VmItem> getVMs() {
        return VMs;
    }

    public void setVMs(Map<String, VmItem> VMs) {
        this.VMs = VMs;
    }

    public Map<String, HostItem> getHosts() {
        return Hosts;
    }

    public void setHosts(Map<String, HostItem> Hosts) {
        this.Hosts = Hosts;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public PoolItem (String ip, String host, String user, String pass) {
        this.ipAddress = ip;
        this.hostName = host;
        this.userName = user;
        this.password = pass;
    }

}
