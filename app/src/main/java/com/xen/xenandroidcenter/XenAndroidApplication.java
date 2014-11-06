package com.xen.xenandroidcenter;

import android.app.Application;
import android.util.Log;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public static void ComposeHost(Connection connection) throws Exception {
        Map<Host, Host.Record> allrecords = Host.getAllRecords(connection);
        return;
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

            ComposeHost(connection);
            targetServer.setSessionUUID(sessionUUID);;
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
