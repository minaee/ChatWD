package com.shmj.chatwd;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    Button startButton;
    TextView devicesList;
    ListView listView;
    static TextView msg;
    Intent chatPage;
    RadioGroup radioAlgoGroup;
    RadioButton radioaes, radiodes,radiorsa;

    boolean serverOrClient;


    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter;
    //private ArrayList peers = new ArrayList();
    public static ArrayList<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDeviceAdapter adapter;

    private ArrayAdapter myAdapter;
    ArrayList<String> deviceNames;

    String otherDeviceName = "";

    boolean rsaOrNot, aesOrdes;

    public void showMsg (String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.searchButton);
        listView = (ListView) findViewById(R.id.listitem);
        //msg = (TextView) findViewById(R.id.msg);

        initFilter();

        //adapter = new WifiP2pDeviceAdapter(this,peers);
        //listView.setAdapter(adapter);
        deviceNames = new ArrayList<>();
        for(int i = 0 ; i < peers.size() ; i++){
            deviceNames.add(peers.get(i).deviceName.toString());
        }

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceNames );
        listView.setAdapter(myAdapter);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);



//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position = i;//Integer.parseInt(view.getTag().toString());
                WifiP2pDevice device = peers.get(position);
                switch (device.status){
                    case WifiP2pDevice.AVAILABLE:
                    case WifiP2pDevice.CONNECTED:
                    case WifiP2pDevice.INVITED:
                        connect(device);
                        otherDeviceName = device.deviceName;
                        break;
                    case WifiP2pDevice.FAILED:
                    case WifiP2pDevice.UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(),"status=%d",device.status),Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        radioAlgoGroup = (RadioGroup) findViewById(R.id.radioAlgo);
        radioaes = (RadioButton) findViewById(R.id.radioAES);
        radiodes = (RadioButton) findViewById(R.id.radioDES);
        radiorsa = (RadioButton) findViewById(R.id.radioRSA);

        radioaes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("view tag", v.getTag().toString() +" "+"AES" );
                rsaOrNot = false;
                aesOrdes = true;
                Log.i("rsa - aes", rsaOrNot + " - " + aesOrdes );
            }
        });
        radiodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("v tag", v.getTag().toString()+" "+"DES" );
                rsaOrNot = false;
                aesOrdes = false;
                Log.i("rsa - aes", rsaOrNot + " - " + aesOrdes );
            }
        });
        radiorsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("v tag", v.getTag().toString() +" "+"RSA");
                rsaOrNot = true;
                aesOrdes = false;
                Log.i("rsa - aes", rsaOrNot + " - " + aesOrdes );
            }
        });

        /*radioAlgoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String radioID = String.valueOf(v.getTag());
                Log.i("radioID", radioID );
                switch (radioID){
                    case "AES":
                        rsaOrNot = false;
                        aesOrdes = true;
                        Log.i("rsa - aes", String.valueOf(rsaOrNot) + " - " + String.valueOf(aesOrdes));
                        break;
                    case "DES":
                        rsaOrNot = false;
                        aesOrdes = false;
                        Log.i("rsa - aes", String.valueOf(rsaOrNot) + " - " + String.valueOf(aesOrdes));
                        break;
                    case "RSA":
                        rsaOrNot = true;
                        Log.i("rsa - aes", String.valueOf(rsaOrNot) + " - " + String.valueOf(aesOrdes));
                        break;
                }
            }
        });*/

    }


    private void initFilter() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
        discoverPeers();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        stopPeerDiscovery();
    }

    private void discoverPeers(){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("discover","onSuccess");
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
    private void stopPeerDiscovery(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    showMsg("stopPeerDiscovery: onSuccess");
                    Log.e("stopPeerDiscovery","onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    switch (reason){
                        case WifiP2pManager.ERROR:
                            Log.e("stopPeerDiscovery","ERROR");
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            Log.e("stopPeerDiscovery","P2P_UNSUPPORTED");
                            break;
                        case WifiP2pManager.BUSY:
                            Log.e("stopPeerDiscovery","BUSY");
                            break;
                        case WifiP2pManager.NO_SERVICE_REQUESTS:
                            Log.e("stopPeerDiscovery","NO_SERVICE_REQUESTS");
                            break;
                    }
                }
            });
        }
    }

    public void connect(final WifiP2pDevice device) {
        // Picking the selected device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.i("deviceAddress connect", String.valueOf(device.deviceAddress));

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

                Log.e("connect","onSuccess");
                showMsg("connected.");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason){
                    case WifiP2pManager.ERROR:
                        Log.e("connect","ERROR");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e("connect","P2P_UNSUPPORTED");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e("connect","BUSY");
                        break;
                    case WifiP2pManager.NO_SERVICE_REQUESTS:
                        Log.e("connect","NO_SERVICE_REQUESTS");
                        break;
                }
            }
        });
    }

    public void openChat(WifiP2pInfo wifiP2pInfo, boolean serverOrClient){  //false for client and vice versa
        Log.i("wifiP2pinfo MActivity", String.valueOf(wifiP2pInfo));
        //serverOrClient = false;
        Log.i("rsaOrNot - aesordes", rsaOrNot +" "+ aesOrdes + " in main");
        chatPage = new Intent(getApplicationContext(), Chat.class).putExtra("serverOrClient",serverOrClient);
        chatPage.putExtra("WifiP2pInfo",wifiP2pInfo);
        chatPage.putExtra("otherDeviceName", otherDeviceName);
        chatPage.putExtra("rsaOrNot", rsaOrNot);
        chatPage.putExtra("aesOrdes", aesOrdes);
        startActivity(chatPage);
    }

    public void search(View v) {
        onResume();
        deviceNames.clear();
        Log.e("peers",peers.toString());
        for(int i = 0 ; i < peers.size() ; i++){
            deviceNames.add(peers.get(i).deviceName.toString());
        }
        myAdapter.notifyDataSetChanged();
    }

}

