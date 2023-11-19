package com.example.lightcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class SettingActivity2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button Reset;
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
    boolean fifteenflag = false;
    boolean tenflag = false;
    boolean fiveflag = false;
    ConnectedThread btt = null;
    //Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);
        Reset = (Button) findViewById(R.id.reset);
        Spinner spinner = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        Log.i("[BLUETOOTH]", "Creating listeners");
        response = (TextView) findViewById(R.id.response);
        //switchRelay = (Button) findViewById(R.id.relay);
        //switchLight = (Button) findViewById(R.id.switchlight);
        bta = BluetoothAdapter.getDefaultAdapter();
        //if bluetooth is not enabled then create Intent for user to turn it on

        if(!bta.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }else

        {
            initiateBluetoothProcess();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      String text = parent.getItemAtPosition(position).toString();

      switch (text) {

          case "15min":
              Toast.makeText(parent.getContext(), text, Toast.LENGTH_LONG).show();
              Log.i("[BLUETOOTH]", "Attempting to send data");
              if (mmSocket.isConnected() && btt != null) { //if we have connection to the bluetoothmodule
                  if (!fifteenflag) {
                      String sendtxta = ("a/n");
                      btt.write(sendtxta.getBytes());
                      Toast.makeText(SettingActivity2.this, "Time set to 15mn", Toast.LENGTH_LONG).show();
                      fifteenflag = true;
                  }
                 else {
                     // String sendtxt = ("z/n");
                      //btt.write(sendtxt.getBytes());
                      //Toast.makeText(SettingActivity2.this, "Time reset", Toast.LENGTH_LONG).show();
                      fifteenflag = false;
                }
          } else {
                  Toast.makeText(SettingActivity2.this, "Something went wrong", Toast.LENGTH_LONG).show();
              }
              break;
          case "10min":
              Toast.makeText(parent.getContext(), text, Toast.LENGTH_LONG).show();
              Log.i("[BLUETOOTH]", "Attempting to send data");
              if (mmSocket.isConnected() && btt != null) { //if we have connection to the bluetoothmodule
                  if (!tenflag) {
                      String sendtxtb = ("b/n");
                      btt.write(sendtxtb.getBytes());
                      Toast.makeText(SettingActivity2.this, "Time set to 10mn", Toast.LENGTH_LONG).show();
                      tenflag = true;
                  }
                  else {
                      //String sendtxt = "q";
                      //btt.write(sendtxt.getBytes());
                      //Toast.makeText(SettingActivity2.this, "Time reset", Toast.LENGTH_LONG).show();
                      tenflag = false;
                  }
              } else {
                  Toast.makeText(SettingActivity2.this, "Something went wrong", Toast.LENGTH_LONG).show();
              }
              break;
          case "05min":
              Toast.makeText(parent.getContext(), text, Toast.LENGTH_LONG).show();
              Log.i("[BLUETOOTH]", "Attempting to send data");
              if (mmSocket.isConnected() && btt != null) { //if we have connection to the bluetoothmodule
                  if (!fiveflag) {
                      String sendtxtc = ("c/n");
                      btt.write(sendtxtc.getBytes());
                      Toast.makeText(SettingActivity2.this, "Time set to 05mn", Toast.LENGTH_LONG).show();
                      fiveflag = true;
                  }
                 else {
                      //String sendtxt = "q";
                      //btt.write(sendtxt.getBytes());
                      //Toast.makeText(SettingActivity2.this, "Time reset", Toast.LENGTH_LONG).show();
                      fiveflag = false;
                  }
              } else {
                  Toast.makeText(SettingActivity2.this, "Something went wrong", Toast.LENGTH_LONG).show();
              }
              break;
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
