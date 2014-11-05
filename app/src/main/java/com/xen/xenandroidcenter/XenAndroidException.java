package com.xen.xenandroidcenter;

/**
 * Created by zhengc on 11/5/2014.
 */
public class XenAndroidException extends Exception {

    public final static int ConnectXSError = 0x00000001;


    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    private int error_code;
    private String error_string;

    public XenAndroidException(int error_code, String error_string) {
        this.error_code = error_code;
        this.error_string = error_string;
    }


}
