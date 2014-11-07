package com.xen.xenandroidcenter;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.HostMetrics;
import com.xensource.xenapi.Event;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Task;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;
import com.xensource.xenapi.VDI;

import org.apache.xmlrpc.XmlRpcException;

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
    private static boolean eventMonitorTaskExecutingFlag = true;

    @Override
    public void onTerminate() {
        eventMonitorTaskExecutingFlag = false;
        super.onTerminate();
    }

    public static Map<String, HostItem> ComposeHosts(Connection connection) throws Exception {
        Map<Host, Host.Record> HostRecords = Host.getAllRecords(connection);

        Map<String, HostItem> hostsList = new HashMap<String, HostItem>();
        for (Host host: HostRecords.keySet()) {
            Host.Record hostR = HostRecords.get(host);
            HostMetrics HostM = host.getMetrics(connection);
            HostMetrics.Record HostMR = HostM.getRecord(connection);
            Long memoryTotal = HostMR.memoryTotal/1024/1024/1024;
            String memUsage = memoryTotal + "GB";

            HostItem tmpHost = new HostItem(hostR.address, hostR.nameLabel, hostR.uuid, memUsage, hostR.powerOnMode, "ismaster",
                    hostR.softwareVersion.get("product_version"), hostR.cpuInfo.toString(), "String Uptime");
            Log.d("host ", hostR.hostname);

            hostsList.put(tmpHost.getUUID(), tmpHost);
        }

        return hostsList;
    }

    public static Map<String, VmItem> ComposeVMs(Connection connection) throws Exception {
        Map<VM, VM.Record> allrecords = VM.getAllRecords(connection);
        Map<String, VmItem> VMs = new HashMap<String, VmItem>();

        for (VM key: allrecords.keySet()) {
            String osInfo = "No XenServer Tool";
            String srInfo = "No XenServer Tool";
            String ip = "No XenServer Tool";
            String mac = "No XenServer Tool";
            Long nicnum = 0L;

            VM.Record vmItem = allrecords.get(key);

            if(vmItem.isATemplate || vmItem.isControlDomain || vmItem.isASnapshot || vmItem.isSnapshotFromVmpp) {
                continue;
            }

            try {
                VMGuestMetrics vmGuestM = VMGuestMetrics.getByUuid(connection, vmItem.guestMetrics.getUuid(connection));
                VMGuestMetrics.Record vmGuestMR = vmGuestM.getRecord(connection);
                if (vmGuestMR.osVersion != null && vmGuestMR.osVersion.containsKey("name"))
                    osInfo = vmGuestMR.osVersion.get("name");
                if(vmGuestMR.networks != null && !vmGuestMR.networks.isEmpty())
                    ip = vmGuestMR.networks.get("0/ip");

                for (VIF vif : key.getVIFs(connection)) {
                    mac = vif.getMAC(connection).toString();
                    nicnum = nicnum + 1L;
                }

                for (VBD vbd : vmItem.VBDs) {
                    VBD.Record vbdR = vbd.getRecord(connection);
                    VDI vdi = vbd.getVDI(connection);
                    if(vbdR.type.toString().equals("Disk"))
                    {
                        Long size = vdi.getVirtualSize(connection)/1024/1024/1024;
                        srInfo = vdi.getNameLabel(connection) + ":" + size.toString() + "GB";
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Long mem = vmItem.memoryTarget/1024/1024;
            VmItem tmpVM = new VmItem(ip, vmItem.nameLabel, vmItem.uuid, mem.toString() + "MB", srInfo, nicnum.toString(),
                    mac, osInfo, vmItem.powerState.toString(), "String Uptime", vmItem.otherConfig.get("base_template_name"));

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

            new EventMonitorAsyncTask(targetServer).execute((Void)null);

            return sessionUUID;

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }

    }

    private static class EventMonitorAsyncTask extends AsyncTask<Void, Void, Void> {
        private PoolItem targetServer;

        public EventMonitorAsyncTask(PoolItem targetServer) {
            this.targetServer = targetServer;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Set<String> eventSet = new HashSet<String>();
            eventSet.add("*");

            try {

                Event.register(this.targetServer.getConnection(), eventSet);

                int eventsReceived = 0;
                long started = System.currentTimeMillis();


                while (eventMonitorTaskExecutingFlag)
                {
                    Set<Event.Record> events = Event.next(this.targetServer.getConnection());

                    for (Event.Record e : events) {
                        Log.d("Event: ", e.clazz + e.id + e.objUuid + e.operation + e.ref);
                    }
                    eventsReceived += events.size();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return (Void)null;
        }
    };

    public static void disconnect(PoolItem targetServer) {
        try {
            if (targetServer.getConnection() != null) {
                targetServer.getConnection().dispose();
            }
        }catch (Exception e) {
            e.printStackTrace();
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
            Task taskRef = vmItem.cleanShutdownAsync(connection);

            while (taskRef.getStatus(connection).equals("pending")) {
                Types.VmPowerState newPowerState = vmItem.getPowerState(connection);
                Log.d("stopVM", newPowerState.toString());
                Thread.sleep(1000);
            }
            taskRef.destroy(connection);

            Types.VmPowerState newPowerState = vmItem.getPowerState(connection);
            Log.d("stopVMAfterDestory", newPowerState.toString());

        }catch (Exception e) {

            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;

        }
    }

    /**
     *
     * @param targetServer  -- Pool Master
     * @param UUID   -- VM UUID
     * @throws XenAndroidException
     */
    public static void suspendVM(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;
        try {
            connection = targetServer.getConnection();
            VM vmItem = VM.getByUuid(connection, UUID);
            vmItem.suspend(connection);


        }catch (Exception e) {

            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;

        }
    }

    /**
     *
     * @param targetServer  -- Pool Master
     * @param UUID   -- VM UUID
     * @throws XenAndroidException
     */
    public static void resumeVM(PoolItem targetServer, String UUID) throws XenAndroidException
    {
        Connection connection = null;
        try {
            connection = targetServer.getConnection();
            VM vmItem = VM.getByUuid(connection, UUID);
            vmItem.resume(connection, false, false);

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
