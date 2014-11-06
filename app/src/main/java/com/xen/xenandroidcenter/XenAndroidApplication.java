package com.xen.xenandroidcenter;

import android.app.Application;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Bond;
import com.xensource.xenapi.Console;
import com.xensource.xenapi.Crashdump;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.HostCpu;
import com.xensource.xenapi.HostCrashdump;
import com.xensource.xenapi.HostMetrics;
import com.xensource.xenapi.HostPatch;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.PBD;
import com.xensource.xenapi.PIF;
import com.xensource.xenapi.PIFMetrics;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.PoolPatch;
import com.xensource.xenapi.SM;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Task;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VBDMetrics;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VIFMetrics;
import com.xensource.xenapi.VLAN;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;
import com.xensource.xenapi.VMMetrics;

/**
 * Created by zhengc on 11/5/2014.
 */
public class XenAndroidApplication extends Application {

    //<session-uuid, PoolItem>
    //PoolItem contains Session and related URL information which can be used to contruct a Connection
    public static HashMap<String, PoolItem> sessionDB = new HashMap<String, PoolItem>();

    public static final String SESSIONID = "SESSIONID";

    public static List<HostItem> ComposeHost(Connection connection) throws Exception {
        Map<Host, Host.Record> HostRecords = Host.getAllRecords(connection);

        List<HostItem> hostsList = new ArrayList<HostItem>();
        for (Host host: HostRecords.keySet()) {
            Host.Record hostR = HostRecords.get(host);
            String memUsgae = host.getMemoryOverhead(connection).toString();


            HostItem tmp = new HostItem(hostR.address, hostR.hostname, hostR.uuid, memUsgae, hostR.powerOnMode, "ismaster",
                    hostR.edition, hostR.cpuInfo.toString(), "String Uptime");
            hostsList.add(tmp);
        }

        return hostsList;
    }


    public static List<VmItem> ComposeVMs(Connection connection) throws Exception {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        List<VmItem> VMs = new ArrayList<VmItem>();
        for (VM key: allrecords.keySet()) {
            VM.Record vmItem = allrecords.get(key);
            VmItem tmp = new VmItem("vmItem.ip", vmItem.nameDescription, vmItem.uuid, vmItem.memoryTarget.toString(), "String SrInfo", "String NicNum",
                    "String Mac", "String OSInfo", "String PowerStatus", "String Uptime", "String TemplateName");
            VMs.add(tmp);
        }

        return VMs;
    }
    /**
     * Return the session UUID if successful, otherwise throw exception
     * @param targetServer
     * @return
     */
    public static String connect(PoolItem targetServer) throws XenAndroidException {
        Connection connection = null;
        try {

            Log.d("Connect - Username: ", targetServer.getUserName());
            Log.d("Connect - password: ", targetServer.getPassword());
            Log.d("Connect - IP: ", targetServer.getIpAddress());

            URL url = new URL("http://" + targetServer.getIpAddress());

            connection = new Connection(url);
            Session sessionRef = Session.loginWithPassword(connection, targetServer.getUserName(), targetServer.getPassword(), "1.3");
            String sessionUUID = sessionRef.getUuid(connection);
            targetServer.setSession(sessionRef);
            targetServer.setHostName(sessionRef.getThisHost(connection).getNameLabel(connection));
            targetServer.setHosts(ComposeHost(connection));
            targetServer.setVMs(ComposeVMs(connection));
            targetServer.setSessionUUID(sessionUUID);
            sessionDB.put(sessionUUID, targetServer);

            return sessionUUID;

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }finally {
            if(connection != null) {
                connection.dispose();
            }
        }

    }

}
