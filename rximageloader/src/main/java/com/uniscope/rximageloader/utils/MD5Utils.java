package com.uniscope.rximageloader.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 作者：majun
 * 时间：2019/1/16 14:26
 * 说明：MD5加密工具类
 */
public class MD5Utils {

    /**
     * 将URL参数通过MD5转换为String类型的key
     * @param key
     * @return
     */
    public static String getMD5String(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
