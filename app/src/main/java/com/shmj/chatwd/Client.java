package com.shmj.chatwd;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.shmj.chatwd.Chat.updateMessagesfromClient;
import static com.shmj.chatwd.Server.PORT;

/**
 * Created by Shahriar on 3/21/2018.
 */

public class Client extends Thread {
    InetAddress address;
    String msgToSend;
    private InputStream iStream;
    private OutputStream oStream;
    Socket socket;
    private boolean startReceive = false;
    private Chat chatActivity;
    String secretKeyString ;//= "1111111111111111";   //16 digit secret key   AES
    //String secretKeyString = "11111111";   //16 digit secret key DES

    public EncryptionAES encryptionAES;
    String decrypted_msg = null;

    public EncryptionRSA encryptionRSA;
    public PrivateKey client_PrivateKey= null;
    public PublicKey server_publicKey = null;

    boolean rsaOrNot, aesordes;
    public boolean exchangedFlag;


    public Client(InetAddress address, Chat chatActivity){
        this.address = address;
        this.chatActivity = chatActivity;
        this.rsaOrNot = chatActivity.rsaOrNot;
        this.aesordes = chatActivity.aesOrdes;
        Log.i("rsa - aes", rsaOrNot + " - " + aesordes + " in CLIENT");

    }


    @Override
    public void run() {

        Log.i("resid inja", "before if rsaornot +rsa "+rsaOrNot );

        if(rsaOrNot == true ){
            try {
                encryptionRSA = new EncryptionRSA();
                client_PrivateKey = chatActivity.client_PrivateKey;
                server_publicKey = chatActivity.server_PublicKey;

                Log.i("server public server", server_publicKey.toString() );
                Log.i("client private server", client_PrivateKey.toString() );


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


        }else if(rsaOrNot == false && aesordes == true) {
            try {
                secretKeyString = "1111111111111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), chatActivity.aesOrdes);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else if(rsaOrNot == false && aesordes == false){
            try {
                secretKeyString = "11111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), chatActivity.aesOrdes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        communication();
        /*try {
            socket = new Socket(address, Server.PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error: ", e.getMessage().toString());
        }*/
    }

    private void communication() {
        Socket socket = null;

        try {
            socket = new Socket(address, PORT);
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            if (iStream != null && oStream != null) {
                Log.i("Client i&o stream: ", "not null");
                chatActivity.showMsg("i&o stream is created");
            } else {
                Log.i("Client i&o stream: ", "null");
            }

            byte[] buffer = new byte[1024];
            int bytes = 0;
            Log.i("resid inja: ", "1");

            while (!startReceive) {
                try {
                    if (iStream != null) {

                        Log.i("rsaOrNot client", String.valueOf(rsaOrNot));
                        if (rsaOrNot) {
                            //oStream.write(client_publicKey.getEncoded());
                            bytes = iStream.read(buffer);
                            Log.i("number of bytes: ", String.valueOf(bytes));
                            if (bytes == -1) {
                                break;
                            }
                            byte[] buffer2 = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                //mBuffer.set(i, buffer[i]);
                                buffer2[i] = buffer[i];
                            }
                            if (buffer != null) {
                                Log.i("buffer: ", new String(buffer, "UTF-8"));
                                String encrypted_msg = new String(buffer2, "UTF-8");
                                Log.i("before decrypt Client: ", encrypted_msg);
                                //decrypted_msg = encryptionAES.decryptMSG(secretKeyString, buffer);
                                decrypted_msg = encryptionRSA.RSADecrypt(buffer2, client_PrivateKey);
                                //String decrypted_msg_string = new String(decrypted_msg, "UTF-8");
                                Log.i("server returns: ", decrypted_msg);
                                chatActivity.updateMessagesfromServer(encrypted_msg, decrypted_msg);
                                decrypted_msg = null;
                            }
                        } else if (rsaOrNot == false) {
                            Log.i("resid inja: ", "2");
                            bytes = iStream.read(buffer);
                            Log.i("number of bytes: ", String.valueOf(bytes));
                            if (bytes == -1) {
                                break;
                            }
                            byte[] buffer2 = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                //mBuffer.set(i, buffer[i]);
                                buffer2[i] = buffer[i];
                            }

                            if (buffer != null) {
                                Log.i("buffer: ", new String(buffer, "UTF-8"));
                                String encrypted_msg = new String(buffer2, "UTF-8");

                                Log.i("before decrypt Client: ", encrypted_msg);
                                //decrypted_msg = encryptionAES.decryptMSG(secretKeyString, buffer);
                                decrypted_msg = encryptionAES.decrypt(encrypted_msg);
                                //String decrypted_msg_string = new String(decrypted_msg, "UTF-8");
                                Log.i("server returns: ", decrypted_msg);
                                chatActivity.updateMessagesfromServer(encrypted_msg, decrypted_msg);
                                decrypted_msg = null;
                            }
                        }
                    }    //istream != null
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (BadPaddingException e1) {
                    e1.printStackTrace();
                } catch (InvalidKeyException e1) {
                    e1.printStackTrace();
                } catch (NoSuchPaddingException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (IllegalBlockSizeException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            //Log.e("error: ", iStream.toString());
        } catch (Exception e) {
            System.out.println("Client exception:" + e.getMessage());
        }
    }

    /**
     * Method to write a byte array (that can be a message) on the output stream.
     * @param buffer byte[] array that represents data to write. For example, a String converted in byte[] with ".getBytes();"
     */
    public void write(byte[] buffer) {
        if(rsaOrNot){
            try {
                String encrypted_msg = new String(buffer, "UTF-8");
                byte[] encrypted_byte_msg = encryptionRSA.RSAEncrypt(encrypted_msg, server_publicKey);
                oStream.write(encrypted_byte_msg);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

        }else {
            try {
                Log.i("resid inja: ", "3");
                oStream.write(buffer);
                String encrypted_msg = new String(buffer, "UTF-8");
                //byte[] decrypted_msg_byte = encryptionAES.decrypt(encrypted_msg);
                String decrypted_msg_string = encryptionAES.decrypt(encrypted_msg);
                //String encrypted_msg_string = new String(decrypted_msg_byte, "UTF-8");
                updateMessagesfromClient(encrypted_msg, decrypted_msg_string);
            } catch (IOException e) {
                Log.e("Client", "Exception during write", e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean exchangeKey() {
        if(exchangedFlag)
            return true;
        else
            return false;
    }


}

