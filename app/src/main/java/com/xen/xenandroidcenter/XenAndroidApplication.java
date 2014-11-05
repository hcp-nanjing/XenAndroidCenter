package com.xen.xenandroidcenter;

import android.app.Application;

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

    //<session-uuid, Connection>
    public static HashMap<String, Connection> sessionDB = new HashMap<String,Session>();

    /**
     * Return the session UUID if successful, otherwise throw exception
     * @param targetServer
     * @return
     */
    public static String connect(PoolItem targetServer) throws XenAndroidException {

        try {

            URL url = new URL("http://" + targetServer.getIpAddress());

            final Connection connection = new Connection(url);
            Session sessionRef = Session.loginWithPassword(connection, targetServer.getUserName(), targetServer.getPassword(), "1.3");
            String sessionUUID = sessionRef.getUuid(connection);
            sessionDB.put(sessionUUID, connection);

            return sessionUUID;

        }catch (Exception e) {
            XenAndroidException err = new XenAndroidException(XenAndroidException.ConnectXSError, e.toString());
            throw err;
        }

    }
    
}
