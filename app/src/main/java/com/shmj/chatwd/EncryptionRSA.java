package com.shmj.chatwd;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Hex;


/**
 * Created by Shahriar on 7/9/2018.
 */

public class EncryptionRSA {

    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;
    byte [] encryptedBytes,decryptedBytes;
    Cipher cipher,cipher1;
    String encrypted,decrypted;

    public EncryptionRSA() throws NoSuchAlgorithmException {
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            kp = kpg.genKeyPair();
            this.publicKey = kp.getPublic();
            this.privateKey = kp.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }


    public byte[] RSAEncrypt(final String plain, PublicKey myPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {


        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, myPublicKey);
        encryptedBytes = cipher.doFinal(plain.getBytes());
        System.out.println("EEncrypted?????" + new String(org.apache.commons.codec.binary.Hex.encodeHex(encryptedBytes)));
        return encryptedBytes;
    }

    public String RSADecrypt(final byte[] encryptedBytes, PrivateKey myPrivateKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, myPrivateKey); //privateKey);
        decryptedBytes = cipher1.doFinal(encryptedBytes);
        decrypted = new String(decryptedBytes);
        System.out.println("DDecrypted?????" + decrypted);
        return decrypted;
    }
}
