package com.xen.xenandroidcenter;

import android.app.Application;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;


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


            HostItem tmp = new HostItem(hostR.address, hostR.nameLabel, hostR.uuid, memUsgae, hostR.powerOnMode, "ismaster",
                    hostR.edition, hostR.cpuInfo.toString(), "String Uptime");
            Log.d("host ", hostR.hostname);
            hostsList.add(tmp);
        }

        return hostsList;
    }

    public static List<VmItem> ComposeVMs(Connection connection) throws Exception {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        Map<VMGuestMetrics, VMGuestMetrics.Record> vmGuestMs = VMGuestMetrics.getAllRecords(connection);
        List<VmItem> VMs = new ArrayList<VmItem>();
        for (VM key: allrecords.keySet()) {
            String osInfo = "NO XENSERVER TOOL";
            VM.Record vmItem = allrecords.get(key);
            if(vmItem.isATemplate)
                continue;
            for (VMGuestMetrics vmGuestM: vmGuestMs.keySet()) {
                VMGuestMetrics.Record vmGuestMR = vmGuestMs.get(vmGuestM);
                if( vmGuestMR.uuid.equals(vmItem.uuid))
                {
                    osInfo = vmGuestMR.osVersion.get("name");
                }
            }

            VmItem tmp = new VmItem("vmItem.ip", vmItem.nameDescription, vmItem.uuid, vmItem.memoryTarget.toString(), osInfo, "String NicNum",
                    "String Mac", vmItem.otherConfig.get("base_template_name"), vmItem.powerState.toString(), "String Uptime", vmItem.otherConfig.get("base_template_name"));
            VMs.add(tmp);
        }

        return VMs;
    }
    /**
     * Return the session UUID if successful, otherwise throw exception
     * @param targetServer
     * @return
     *
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
            Map<Pool, Pool.Record> poolRecord =  Pool.getAllRecords(connection);
            if(poolRecord.isEmpty())
                targetServer.setHostName(sessionRef.getThisHost(connection).getNameLabel(connection));
            else
                for (Pool pool: poolRecord.keySet()) {
                    Pool.Record poolR = poolRecord.get(pool);
                    targetServer.setHostName(pool.getNameLabel(connection));
                    break;
                }
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

    public static void startVM(Connection connection, String UUID) throws Exception
    {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        for (VM vm: allrecords.keySet()) {
            if(vm.getUuid(connection).equals(UUID)) {
                vm.start(connection, false, false);
                break;
            }
        }
    }

    public static void stopVM(Connection connection, String UUID) throws Exception
    {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        for (VM vm: allrecords.keySet()) {
            if(vm.getUuid(connection).equals(UUID)) {
                vm.cleanShutdown(connection);
                break;
            }
        }
    }

    public static void snapshotVM(Connection connection, String UUID, String snapshotName) throws Exception
    {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        for (VM vm: allrecords.keySet()) {
            if(vm.getUuid(connection).equals(UUID)) {
                vm.snapshot(connection, snapshotName);
                break;
            }
        }
    }

    public static void shutdownHost(Connection connection, String UUID) throws Exception
    {
        Map<Host, Host.Record> allrecords = Host.getAllRecords(connection);
        for (Host host: allrecords.keySet()) {
            if(host.getUuid(connection).equals(UUID)) {
                host.shutdown(connection);
                break;
            }
        }
    }

}
