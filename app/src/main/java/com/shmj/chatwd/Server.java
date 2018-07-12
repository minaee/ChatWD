package com.shmj.chatwd;

/**
 * Created by Shahriar on 7/4/2018.
 */

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.shmj.chatwd.Chat.updateMessagesfromClient;
import static com.shmj.chatwd.Chat.updateMessagesfromServer;
import static com.shmj.chatwd.Chat.updateMessagesfromClient;
import static com.shmj.chatwd.Chat.updateMessagesfromServer;


/**
 * Created by Shahriar on 3/21/2018.
 */

public class Server extends Thread {
    InetAddress address;
    public static  int PORT = 1234;
    String msgToSend;
    private InputStream iStream;
    private OutputStream oStream;
    private boolean startReceive = false;
    private Chat myChatActivity;
    //String secretKeyString = "1111111111111111";   //16 digit secret key   AES
    String secretKeyString ;//= "11111111";   //16 digit secret key DES
    //String secretKeyString = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"; // RSA


    public EncryptionAES encryptionAES;
    public byte[] decrypted_msg = null;

    public EncryptionRSA encryptionRSA;
    PublicKey client_publicKey=null, server_publicKey=null;


    boolean rsaOrNot, aesordes, exchangedFlag = false;


    public Server(InetAddress groupOwnerAddress, Chat chatActivity){
        address = groupOwnerAddress;
        this.myChatActivity = chatActivity;
        this.rsaOrNot = chatActivity.rsaOrNot;
        this.aesordes = chatActivity.aesOrdes;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT,5,address);
            Socket socket = null;
            while (true){
                socket = serverSocket.accept();
                System.out.println("Add connection："+socket.getInetAddress()+":"+socket.getPort());
                new HandlerThread(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(rsaOrNot == true  ){
            try {
                encryptionRSA = new EncryptionRSA();
                server_publicKey = encryptionRSA.publicKey;

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }else if(rsaOrNot == false && aesordes == true) {
            try {
                secretKeyString = "1111111111111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), myChatActivity.aesOrdes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }else if(rsaOrNot == false && aesordes == false){
            try {
                secretKeyString = "11111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), myChatActivity.aesOrdes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method to write a byte array (that can be a message) on the output stream.
     * @param buffer byte[] array that represents data to write. For example, a String converted in byte[] with ".getBytes();"
     */
    public void write(byte[] buffer) {
        if(rsaOrNot == true){

            if(client_publicKey != null ) {
                try {
                    String encrypted_msg = new String(buffer, "UTF-8");
                    byte[] encrypted_byte_msg = encryptionRSA.RSAEncrypt(encrypted_msg, client_publicKey);
                    oStream.write(encrypted_byte_msg);
                } catch (UnsupportedEncodingException e) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Log.i("client_publicKey", "is null");
                myChatActivity.showMsg("client_publickey is null");
            }


        }else if(rsaOrNot == false) {
            try {

                oStream.write(buffer);
                Log.i("resid inja: ", "3");

                String encrypted_msg = new String(buffer, "UTF-8");
                String decrypted_msg_String = encryptionAES.decrypt(encrypted_msg);
                //String decrypted_msg_string = new String(decrypted_msg_byte, "UTF-8");
                updateMessagesfromServer(encrypted_msg, decrypted_msg_String);
            } catch (IOException e) {
                Log.e("Server", "Exception during write", e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class HandlerThread implements Runnable {
        private Socket socket;
        public HandlerThread(Socket client) {
            socket = client;
            try {
                iStream = socket.getInputStream();
                oStream = socket.getOutputStream();
                if( iStream != null && oStream != null){
                    Log.i("Server i&o stream: ", "not null");
                }else {
                    Log.i("Server i&o stream: ", " null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread(this).start();
        }



        public void run() {
            //Log.i("in ane doros shod",Chat.msgToSend );

            Log.i("resid inja: ", "1");

            try {
                // Read client data
                //DataInputStream input = new DataInputStream(socket.getInputStream());
                //This should pay attention to the write method of the client output stream,
                // otherwise it will throw EOFException
                //String clientInputStr = input.readUTF();
                // Processing client data
                //System.out.println("Client sent over the content:" + clientInputStr);

                //Chat.updateMessagesfromClient(clientInputStr);

                byte[] buffer = new byte[1024];
                int bytes;


                while(client_publicKey == null){
                    try {
                        oStream.write(server_publicKey.getEncoded());
                        if(server_publicKey != null && iStream!=null ) {
                            bytes = iStream.read(buffer);
                            Log.i("number of bytes: ", String.valueOf(bytes));
                            if (bytes == -1) {
                                break;
                            }
                            if (buffer != null) {
                                Log.i("buffer clientkey", new String(buffer, "UTF-8"));
                                byte[] buffer2 = new byte[bytes];
                                for (int i = 0; i < bytes; i++) {
                                    buffer2[i] = buffer[i];
                                }
                                client_publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(buffer2));
                                Log.i("client_publicKey", client_publicKey.toString() + " in server");
                                updateMessagesfromClient("client publickey", client_publicKey.toString());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }

                    if (client_publicKey != null){
                        exchangedFlag = true;
                    }else {
                        exchangedFlag = false;
                    }
                }

                 buffer = new byte[1024];
                 //bytes = 0 ;
                while( !startReceive ){
                    try{
                        if(iStream!=null) {

                            if (rsaOrNot == true && exchangedFlag == true) {
                                Log.i("resid inja: ", "2");
                                bytes = iStream.read(buffer);
                                Log.i("number of bytes: ", String.valueOf(bytes));
                                if (bytes == -1) {
                                    break;
                                }
                                if (buffer != null) {
                                    Log.i("buffer: ", new String(buffer, "UTF-8"));
                                    byte[] buffer2 = new byte[bytes];
                                    for (int i = 0; i < bytes; i++) {
                                        buffer2[i] = buffer[i];
                                    }

                                    String encrypted_msg = new String(buffer2, "UTF-8");
                                    Log.i("before decrypt Server: ", encrypted_msg);
                                    String dedcrypted = null;
                                    dedcrypted = encryptionAES.decrypt(encrypted_msg);
                                    //String decrypted_msg_string = new String(decrypted_msg, "UTF-8");
                                    Log.i("client returns: ", dedcrypted);
                                    updateMessagesfromClient(encrypted_msg, dedcrypted);
                                }


                            } else if (rsaOrNot == false) {
                                Log.i("resid inja: ", "2");
                                bytes = iStream.read(buffer);
                                Log.i("number of bytes: ", String.valueOf(bytes));
                                if (bytes == -1) {
                                    break;
                                }

                                //buffer = buffer2;
                                if (buffer != null) {
                                    Log.i("buffer: ", new String(buffer, "UTF-8"));
                                    byte[] buffer2 = new byte[bytes];
                                    for (int i = 0; i < bytes; i++) {
                                        buffer2[i] = buffer[i];
                                    }

                                    String encrypted_msg = new String(buffer2, "UTF-8");
                                    Log.i("before decrypt Server: ", encrypted_msg);
                                    String dedcrypted = null;
                                    dedcrypted = encryptionAES.decrypt(encrypted_msg);
                                    //String decrypted_msg_string = new String(decrypted_msg, "UTF-8");
                                    Log.i("client returns: ", dedcrypted);
                                    updateMessagesfromClient(encrypted_msg, dedcrypted);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                //chat.messages.setText(messages.getText() + "\n" + "client: " + clientInputStr);

                // Reply to the client
                //DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                //System.out.print("please enter:\t");
//                // Send a line of keyboard input
//                String s = new BufferedReader(new InputStreamReader(System.in)).readLine();
                /*msgToSend = Chat.getMsgToSend();
                if( msgToSend != null) {
                    out.writeUTF(msgToSend);
                    Chat.updateMessagesfromServer(msgToSend);
                }
                msgToSend = "";*/


                //out.writeUTF("test back");

                //out.close();
                //input.close();
            } catch (Exception e) {
                System.out.println("server run abnormal: " + e.getMessage());
            }
        }
    }

    public boolean exchangeKey() {
        if(exchangedFlag == true)
            return true;
        else
            return false;
    }

}
