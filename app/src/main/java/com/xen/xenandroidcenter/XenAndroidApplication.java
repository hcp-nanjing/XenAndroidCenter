package com.xen.xenandroidcenter;

import android.app.Application;
import android.util.Log;

import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Session;

/**
 * Created by zhengc on 11/5/2014.
 */
public class XenAndroidApplication extends Application {

    //<session-uuid, PoolItem>
    //PoolItem contains Session and related URL information which can be used to contruct a Connection
    public static HashMap<String, PoolItem> sessionDB = new HashMap<String, PoolItem>();

    public static final String SESSIONID = "SESSIONID";

    /**
     * Return the session UUID if successful, otherwise throw exception
     * @param targetServer
     * @return
     */
    public static String connect(PoolItem targetServer) throws XenAndroidException {

        try {

            Log.d("Connect - Username: ", targetServer.getUserName());
            Log.d("Connect - password: ", targetServer.getPassword());
            Log.d("Connect - IP: ", targetServer.getIpAddress());

            URL url = new URL("http://" + targetServer.getIpAddress());

            final Connection connection = new Connection(url);
            Session sessionRef = Session.loginWithPassword(connection, targetServer.getUserName(), targetServer.getPassword(), "1.3");
            String sessionUUID = sessionRef.getUuid(connection);
            targetServer.setSession(sessionRef);
            targetServer.setHostName(sessionRef.getThisHost(connection).getNameLabel(connection));

            sessionDB.put(sessionUUID, targetServer);

            return sessionUUID;

        }catch (Exception e) {
            e.printStackTrace();
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }

    }

}
