package com.example.lightcontrol;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {


    private Button switchRelay;
    private Button switchLight;
    private BluetoothAdapter mBTAdapter;
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final static String MODULE_MAC = "58:BF:25:A1:C3:56";
    public final static int REQUEST_ENABLE_BT = 1;


    BluetoothAdapter bta;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    public Handler mHandler;
    TextView response;
    boolean lightflag = false;
    boolean relayFlag = true;
    ConnectedThread btt = null;
    Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("[BLUETOOTH]", "Creating listeners");
        response = (TextView) findViewById(R.id.response);
        switchRelay = (Button) findViewById(R.id.relay);
        switchLight = (Button) findViewById(R.id.switchlight);
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        //Spinner options=findViewById(R.id.spinner_time);

        switchLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Perform on click Vibration
                //Set Vibration Timing
                vibrator.vibrate(40);
                Log.i("[BLUETOOTH]", "Attempting to send data");
                if (mmSocket.isConnected() && btt != null) { //if we have connection to the bluetoothmodule
                    if (!lightflag) {
                        String sendtxt = ("hhh/n");
                        btt.write(sendtxt.getBytes());
                        Toast.makeText(MainActivity.this, "Defroster Turn On", Toast.LENGTH_LONG).show();
                        lightflag = true;
                    } else {
                        String sendtxt = ("qqq/n");
                        btt.write(sendtxt.getBytes());
                        Toast.makeText(MainActivity.this, "Defroster Turn Off", Toast.LENGTH_LONG).show();
                        lightflag = false;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        });

        switchRelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Allow the user in to your app by going into the next activity */
                startActivity(new Intent(MainActivity.this, SettingActivity2.class));

           }
        });

        bta = BluetoothAdapter.getDefaultAdapter();
        //if bluetooth is not enabled then create Intent for user to turn it on
        if(!bta.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }else{
            initiateBluetoothProcess();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT){
            initiateBluetoothProcess();
        }
    }


    public void initiateBluetoothProcess(){
        if(bta.isEnabled()){
            //attempt to connect to bluetooth module
            BluetoothSocket tmp = null;
            mmDevice = bta.getRemoteDevice(MODULE_MAC);

            //create socket
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(mDeviceUUID);
                mmSocket = tmp;
                mmSocket.connect();
                Log.i("[BLUETOOTH]","Connected to: "+mmDevice.getName());
            }catch(IOException e){
                try{mmSocket.close();}catch(IOException c){return;}
            }

            Log.i("[BLUETOOTH]", "Creating handler");
            mHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    //super.handleMessage(msg);
                    if(msg.what == ConnectedThread.RESPONSE_MESSAGE){
                        String txt = (String)msg.obj;
                        if(response.getText().toString().length() >= 30){
                            response.setText("");
                            response.append(txt);
                        }
                        else{
                            response.append("\n" + txt);
                        }
                    }
                }
            };

            Log.i("[BLUETOOTH]", "Creating and running Thread");
            btt = new ConnectedThread(mmSocket,mHandler);
            btt.start();

        }
    }

}