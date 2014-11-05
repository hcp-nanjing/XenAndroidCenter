package com.xen.xenandroidcenter;

/**
 * Created by zhengc on 11/5/2014.
 */
public class PoolItem {

    private String ipAddress;
    private String hostName;
    private String userName;
    private String password;

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

}
