package com.xen.xenandroidcenter;

import android.app.Application;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;

/**
 * Created by zhengc on 11/5/2014.
 */
public class XenAndroidApplication extends Application {

    //<session-uuid, PoolItem>
    //PoolItem contains Session and related URL information which can be used to contruct a Connection
    public static HashMap<String, PoolItem> sessionDB = new HashMap<String, PoolItem>();

    public static final String SESSIONID = "SESSIONID";
    public static final String HOSTUUID = "HOSTUUID";
    public static final String VMUUID = "VMUUID";

    public static Map<String, HostItem> ComposeHosts(Connection connection) throws Exception {
        Map<Host, Host.Record> HostRecords = Host.getAllRecords(connection);

        Map<String, HostItem> hostsList = new HashMap<String, HostItem>();
        for (Host host: HostRecords.keySet()) {
            Host.Record hostR = HostRecords.get(host);
            String memUsage = host.getMemoryOverhead(connection).toString();

            HostItem tmpHost = new HostItem(hostR.address, hostR.nameLabel, hostR.uuid, memUsage, hostR.powerOnMode, "ismaster",
                    hostR.edition, hostR.cpuInfo.toString(), "String Uptime");
            Log.d("host ", hostR.hostname);

            hostsList.put(tmpHost.getUUID(), tmpHost);
        }

        return hostsList;
    }

    public static Map<String, VmItem> ComposeVMs(Connection connection) throws Exception {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        Map<VMGuestMetrics, VMGuestMetrics.Record> vmGuestMs = VMGuestMetrics.getAllRecords(connection);
        Map<String, VmItem> VMs = new HashMap<String, VmItem>();

        for (VM key: allrecords.keySet()) {
            String osInfo = "NO XENSERVER TOOL";
            VM.Record vmItem = allrecords.get(key);

            if(vmItem.isATemplate || vmItem.isControlDomain || vmItem.isASnapshot || vmItem.isSnapshotFromVmpp) {
                continue;
            }

            for (VMGuestMetrics vmGuestM: vmGuestMs.keySet()) {
                VMGuestMetrics.Record vmGuestMR = vmGuestMs.get(vmGuestM);
                if( vmGuestMR.uuid.equals(vmItem.uuid))
                {
                    osInfo = vmGuestMR.osVersion.get("name");
                }
            }

            VmItem tmpVM = new VmItem("vmItem.ip", vmItem.nameLabel, vmItem.uuid, vmItem.memoryTarget.toString(), osInfo, "String NicNum",
                    "String Mac", vmItem.otherConfig.get("base_template_name"), vmItem.powerState.toString(), "String Uptime", vmItem.otherConfig.get("base_template_name"));
            VMs.put(tmpVM.getUUID(), tmpVM);
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
        try {
            URL url = new URL("http://" + targetServer.getIpAddress());
            Connection connection = new Connection(url);
            Session sessionRef = Session.loginWithPassword(connection, targetServer.getUserName(), targetServer.getPassword(), "1.3");
            String sessionUUID = sessionRef.getUuid(connection);
            targetServer.setSession(sessionRef);
            Map<Pool, Pool.Record> poolRecord =  Pool.getAllRecords(connection);

            if(poolRecord.isEmpty()) {

                targetServer.setHostName(sessionRef.getThisHost(connection).getNameLabel(connection));

            } else {
                for (Pool pool : poolRecord.keySet()) {
                    Pool.Record poolR = poolRecord.get(pool);
                    targetServer.setHostName(pool.getNameLabel(connection));
                    break;
                }
            }

            targetServer.setHosts(ComposeHosts(connection));
            targetServer.setVMs(ComposeVMs(connection));
            targetServer.setSessionUUID(sessionUUID);
            targetServer.setConnection(connection);
            sessionDB.put(sessionUUID, targetServer);
            return sessionUUID;

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }

    }

    public static void disconnect(PoolItem targetServer) throws XenAndroidException {
        if(targetServer.getConnection() != null) {
            targetServer.getConnection().dispose();
        }
    }

    /**
     *
     * @param targetServer  -- Pool Master
     * @param UUID   -- VM UUID
     * @throws XenAndroidException
     */
    public static void startVM(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;
        try {

            connection = targetServer.getConnection();
            Session.loginWithPassword(connection, targetServer.getUserName(), targetServer.getPassword(), "1.3");
            VM vmItem = VM.getByUuid(connection, UUID);
            vmItem.start(connection, false, false);

        }catch (Exception e) {

            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;

        }
    }

    /**
     *
     * @param targetServer  -- Pool Master
     * @param UUID -- VM UUID
     * @throws XenAndroidException
     */
    public static void stopVM(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;
        try {
            connection = targetServer.getConnection();
            VM vmItem = VM.getByUuid(connection, UUID);
            vmItem.cleanShutdown(connection);

        }catch (Exception e) {

            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;

        }
    }

    /**setSessionUUID
     *
     * @param targetServer  -- Pool Master
     * @param UUID  -- VM UUID
     * @param snapshotName
     * @throws XenAndroidException
     */
    public static void snapshotVM(PoolItem targetServer, String UUID, String snapshotName) throws XenAndroidException
    {
        Connection connection = null;
        try {

            connection = targetServer.getConnection();
            VM vmItem = VM.getByUuid(connection, UUID);
            vmItem.snapshot(connection, snapshotName);

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }
    }

    /**
     *
     * @param targetServer
     * @param UUID -- UUID of the Host
     * @throws XenAndroidException
     */
    public static void shutdownHost(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;
        try {

            connection = targetServer.getConnection();
            Host hostItem = Host.getByUuid(connection, UUID);
            hostItem.shutdown(connection);

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }
    }

    /**
     *
     * @param targetServer
     * @param UUID  -- UUID of the Host
     * @throws XenAndroidException
     */
    public static void rebootHost(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;

        try {
            connection = targetServer.getConnection();
            Host hostItem = Host.getByUuid(connection, UUID);
            hostItem.reboot(connection);
        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }
    }
}
