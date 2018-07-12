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
    PublicKey client_publicKey=null, server_publicKey=null;

    boolean rsaOrNot, aesordes, exchangedFlag=false ;


    public Client(InetAddress address, Chat chatActivity){
        this.address = address;
        this.chatActivity = chatActivity;
        this.rsaOrNot = chatActivity.rsaOrNot;
        this.aesordes = chatActivity.aesOrdes;
    }


    @Override
    public void run() {

        if(rsaOrNot == true ){
            try {
                encryptionRSA = new EncryptionRSA();
                client_publicKey = encryptionRSA.publicKey;
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
        try {
            socket = new Socket(address, Server.PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error: ", e.getMessage().toString());
        }
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
            int bytes;
            while(server_publicKey == null){
                try {
                    oStream.write(client_publicKey.getEncoded());
                    if(client_publicKey != null && iStream != null) {
                        bytes = iStream.read(buffer);
                        Log.i("number of bytes: ", String.valueOf(bytes));
                        if (bytes == -1) {
                            break;
                        }
                        if (buffer != null) {
                            byte[] buffer2 = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                //mBuffer.set(i, buffer[i]);
                                buffer2[i] = buffer[i];
                            }
                            server_publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(buffer2));
                            Log.i("server_publicKey", server_publicKey.toString() + " in keyexchanged of client");
                            chatActivity.updateMessagesfromServer("server public key", server_publicKey.toString());
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                if(server_publicKey != null) {
                    exchangedFlag = true;
                }else {
                    exchangedFlag = false;
                }
            }

            buffer = new byte[1024];
            // int bytes = 0;
            Log.i("resid inja: ", "1");

            while (!startReceive) {
                try {
                    if (iStream != null) {

                        Log.i("rsaOrNot client", String.valueOf(rsaOrNot));
                        if (rsaOrNot == true && exchangedFlag == true) {
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
                                decrypted_msg = encryptionRSA.RSADecrypt(buffer2);
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
        if(rsaOrNot ==true){
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

    public boolean sendpubKey() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            while(server_publicKey == null) {
                outputStream.writeObject(client_publicKey);
                server_publicKey = (PublicKey) inputStream.readObject();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        if (server_publicKey != null) {
            return true;
        } else {
            return false;
        }
    }

}

