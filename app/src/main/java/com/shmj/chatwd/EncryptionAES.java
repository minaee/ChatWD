package com.shmj.chatwd;

/**
 * Created by Shahriar on 7/4/2018.
 */

import android.util.Log;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Shahriar on 6/22/2018.
 */

public class EncryptionAES {

    private static byte[] keyValue;
    String secretKeyString;
    byte[] encryptedMsg;
    String msgContentString;
    public String encryptionKey;
    public static String algorithm;



    public EncryptionAES( byte[] keyValue, boolean algo) throws NoSuchAlgorithmException {
        this.keyValue = keyValue;
        if(algo == true)
            algorithm = "AES";
        else
            algorithm = "DES";
    }



    public static String encrypt(String cleartext)
            throws Exception {

        byte[] rawKey = getRawKey();
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String decrypt(String encrypted)
            throws Exception {

        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(enc);
        return new String(result);
    }

    private static byte[] getRawKey() throws Exception {
        SecretKey key = new SecretKeySpec(keyValue, algorithm); //"AES");

        byte[] raw = key.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKey skeySpec = new SecretKeySpec(raw, algorithm); //"AES");
        Cipher cipher = Cipher.getInstance( algorithm); //"AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] encrypted)
            throws Exception {
        SecretKey skeySpec = new SecretKeySpec(keyValue, algorithm); //"AES");
        Cipher cipher = Cipher.getInstance(algorithm); //"AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static byte[] toByte(String hexString) {
        Log.i("hexString: ", hexString);
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            Log.i("i= ", i + " toByte: "+ hexString.substring(2 * i, 2 * i + 2));
            result[i] = Integer.valueOf( hexString.substring(2 * i, 2 * i + 2),16 ).byteValue();
        }
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

}

