package com.shmj.chatwd;

/**
 * Created by Shahriar on 7/4/2018.
 */

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * Created by Shahriar on 3/5/2018.
 */

public class Chat extends AppCompatActivity {

    TextView otherDevicename, encyprionAlgo, myKey, opKey;
    Button sendButton, sendKey;
    EditText textTosend;

    static String  msgToSend;
    private Server server;
    private Client client;
    InetAddress mygroupOwnerAddress;
    boolean serverOrClient;
    WifiP2pInfo wifiP2pInfo;
    public EncryptionAES encryptionAES;
    //String secretKeyString = "1111111111111111";   //16 digit secret key   AES
    String secretKeyString; // = "11111111";   //16 digit secret key DES

    public EncryptionRSA encryptionRSA;

    public static ArrayList<String> encrypted_msg_content = new ArrayList<>();
    public static ArrayAdapter encrypted_msg_adapter;
    public static ArrayList<String> decrypted_msg_content = new ArrayList<>();
    public static ListView decrypted_messages;
    public static ListView encrypted_messages;
    public static ArrayAdapter decrypted_msg_adapter;
    String otherDeviceName;
    public String  encrypted_msg = null, algo="";

    boolean rsaOrNot, aesOrdes  ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        //WifiP2pInfo wifiP2pInfo = (WifiP2pInfo)getIntent().getSerializableExtra("WifiP2pInfo");

        // receiving content from MainActivity
        serverOrClient = getIntent().getBooleanExtra("serverOrClient",true);
        wifiP2pInfo = getIntent().getExtras().getParcelable("WifiP2pInfo");
        otherDeviceName  = getIntent().getExtras().getParcelable("otherDeviceName");
        rsaOrNot = getIntent().getBooleanExtra("rsaOrNot", true);
        aesOrdes = getIntent().getBooleanExtra("aesOrdes", true);
        algo = getIntent().getStringExtra("algo");
        Log.i("rsa - aes", String.valueOf(rsaOrNot) + " - " + String.valueOf(aesOrdes) + " in chat");
        Log.i("serverOrClient", serverOrClient + " in chat");
        Log.i("algo", algo + " in chat");

        // creating server o client instances
        if(wifiP2pInfo != null) {
            setSender(wifiP2pInfo, serverOrClient);
        }

        // initializing Views
        sendButton = (Button) findViewById(R.id.sendButton);
        sendKey = (Button) findViewById(R.id.sendKey);
        textTosend = (EditText) findViewById(R.id.textToSend);
        decrypted_messages = (ListView) findViewById(R.id.decrypt_messages);
        encrypted_messages = (ListView) findViewById(R.id.encrypt_messages);
        encyprionAlgo = (TextView) findViewById(R.id.otherDeviceName);
        myKey = (TextView) findViewById(R.id.myKey);
        opKey = (TextView) findViewById(R.id.opponentKey);

        //setting adapter for listview(chat messages)
        decrypted_msg_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, decrypted_msg_content );
        decrypted_msg_content.add("decrypted messages:");
        decrypted_messages.setAdapter(decrypted_msg_adapter);
        encrypted_msg_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, encrypted_msg_content);
        encrypted_msg_content.add("encrypted messages:");
        encrypted_messages.setAdapter(encrypted_msg_adapter);

        // name of chat partner
        otherDevicename = (TextView) findViewById(R.id.otherDeviceName);
        otherDevicename.setText("this is a new chat with: " + otherDeviceName);
        encyprionAlgo.setText( encyprionAlgo.getText().toString() + algo);
        myKey.setText("opkey received ?: ");

        msgToSend = "thisShitIsNull";

        try {
            if(rsaOrNot == false && aesOrdes == true) {
                secretKeyString = "1111111111111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), aesOrdes);
                Log.i("rsa - aes - AES created", rsaOrNot + " - "+aesOrdes + " in chat");
            }else if(rsaOrNot == false && aesOrdes == false){
                secretKeyString = "11111111";
                encryptionAES = new EncryptionAES(secretKeyString.getBytes(), aesOrdes);
                Log.i("rsa - aes - DES created", rsaOrNot + " - "+aesOrdes + " in chat");
            }else if(rsaOrNot == true){
                encryptionRSA = new EncryptionRSA();
                Log.i("rsa - aes - RSA created", rsaOrNot + " - "+aesOrdes + " in chat");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //rsaOrNot = true;
        if(rsaOrNot == true){
            sendKey.setVisibility(View.VISIBLE);
            sendButton.setEnabled(false);
        }

    }

    public void showMsg(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void sendKeyToOpponent(View view){
        if(serverOrClient){ // for server
            Log.i("key exchanged in","server begins");
            //boolean isExchanged = server.exchangeKey();
            if(server.exchangeKey()){
                sendButton.setEnabled(true);
                Log.i("key exchanged in","server is true");
                opKey.setText("yes");
            }else{
                Log.i("key exchanged in","server is false");
                opKey.setText("no");
            }

        }else if(!serverOrClient){    // for client
            Log.i("key exchanged in","client begins");
            //boolean isExchanged = client.exchangeKey();
            if(client.exchangeKey()){
                sendButton.setEnabled(true);
                Log.i("key exchanged in","client is true");
                opKey.setText("yes");
            }else{
                Log.i("key exchanged in","client is false");
                opKey.setText("no");
            }

        }else
            Log.i("key exchanged: ","failed");
    }

    public void sendButton(View view){
        EditText textTosend2 = (EditText) findViewById(R.id.textToSend);
        if(String.valueOf(textTosend2.getText()) != null){
            msgToSend = String.valueOf(textTosend2.getText());


            Log.i("rsa - aes", rsaOrNot + " - " + aesOrdes + " in sendbutton");
            Log.i("serverOrClient", serverOrClient + " in sendbutton");

            if(serverOrClient == true ){ // for server

                if( rsaOrNot == true){
                    Log.i("resid inja", "000 in chat");
                    server.write(msgToSend.getBytes());

                }else if(rsaOrNot == false){
                    try {
                        Log.i("resid inja", "111 in chat");
                        //encrypted_msg = encryptionAES.encryptMSG(secretKeyString, msgToSend);
                        encrypted_msg = encryptionAES.encrypt(msgToSend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    server.write(encrypted_msg.getBytes());
                }
                Log.i("msg to send server: ",msgToSend);

            }else if(serverOrClient == false ){    // for client

                if(rsaOrNot == true ){
                    client.write(msgToSend.getBytes());
                }else {
                    try {
                        encrypted_msg = encryptionAES.encrypt(msgToSend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client.write(encrypted_msg.getBytes());

                    Log.i("msg to send client: ", msgToSend);
                }
            } else {
                Log.i("encrypted message: ", "is null.");
                showMsg("encrypted message is null.");
            }


        }
        textTosend2.setText("");
        textTosend2.setHint("write here");


    }

    private void setSender(WifiP2pInfo wifiP2pInfo, boolean serverOrClient) {
        mygroupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner && serverOrClient == true) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            Log.i("server Sender: ", "criteria met.");
            server = new Server(mygroupOwnerAddress,this);
            //server.start();
            showMsg("server created from server.");
            //client = new Client(mygroupOwnerAddress, this);
            //client.start();
            //showMsg("client created from server");
//                chat.sendAsServer(groupOwnerAddress);
            server.start();
            Log.i("server setSender: ", server.toString());

        } else if (wifiP2pInfo.groupFormed && serverOrClient == false) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Log.i("client Sender: ", "criteria met.");
            client = new Client(mygroupOwnerAddress, this);
            //client.start();
            showMsg("client created from client");
            client.start();
            Log.i("client setSender: ", client.toString());
            //server = new Server(mygroupOwnerAddress, this);
            //server.start();
            //showMsg("server created from client");
            //chat.sendAsClient(groupOwnerAddress);
        }
    }

    static void updateMessagesfromServer(String encrypted_msg, String decrypted_msg){
        if(encrypted_msg != null && decrypted_msg != null){
            //messages.setText(messages.getText() + "\n" + "client: " + msgs);
            decrypted_msg_content.add( "Server: " + decrypted_msg);
            decrypted_msg_adapter.notifyDataSetChanged();
            encrypted_msg_content.add( "Server: " + encrypted_msg);
            encrypted_msg_adapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
        }

    }

    public static void updateMessagesfromClient(String encrypted_msg, String decrypted_msg) {
        if(encrypted_msg != null && decrypted_msg != null){
            //messages.setText(messages.getText() + "\n" + "Server: " + ret);
            decrypted_msg_content.add("Client: " + decrypted_msg);
            decrypted_msg_adapter.notifyDataSetChanged();
            encrypted_msg_content.add( "Client: " + encrypted_msg);
            encrypted_msg_adapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
        }
    }


    private static void scrollMyListViewToBottom() {
        encrypted_messages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                encrypted_messages.setSelection(encrypted_msg_adapter.getCount() - 1);
            }
        });

        decrypted_messages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                decrypted_messages.setSelection(decrypted_msg_adapter.getCount() - 1);
            }
        });
    }

}


