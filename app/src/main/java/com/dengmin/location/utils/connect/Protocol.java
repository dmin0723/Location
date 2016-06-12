package com.dengmin.location.utils.connect;

/**
 * 这是协议接口
 */
public interface Protocol {

    public byte[] decodeString(String str);

    public String encodeString(byte[] buffer);
}
