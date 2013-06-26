package com.example.Fugetive;

import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;
import java.lang.*;

import android.R.bool;


public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
	//Global variables layout
	static SeekBar seekbar;
	static TextView textview, textview2;
	static int progressbar;
	static Handler serverHandler = null;
	static Handler clientHandler = null;
	//Global variables bluetooth
	String myBlue_Name, myBlue_Mac;
	BluetoothDevice blueDevice;
	static BluetoothAdapter blueAdapter = null;
	static BluetoothServerSocket blueServer;
	static BluetoothSocket blueClient = null, blueClientS = null;
	boolean testBlue = true;
	boolean finishedDiscovering = false;
	
	//IO streams
	static InputStream input;
	static OutputStream output;
	byte[] byteStream;
	
	//initiate list which will contain paired and discovered bluetooth devices.
	ArrayList<BluetoothDevice> foundDevices;
	ArrayList<BluetoothDevice> pairedDevs;
	
	String[] blueFound = new String[3];
	
	//Hardcoded stuff to make connection easier for now..
	static String blueMac = "";
	public static String tag = "fugitive";
	public String myName = "";
	//Hardcoded service name and UUID for socket connection
	static String serviceName = "testBlue";
	static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-FFFFFFFFFFFF");


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			//Layout shizzle
			textview = (TextView) findViewById(R.id.textView1);
			textview2 = (TextView) findViewById(R.id.textView2);

			seekbar = (SeekBar) findViewById(R.id.seekBar1);
			seekbar.setOnSeekBarChangeListener(this);
			foundDevices = new ArrayList<BluetoothDevice>();
			pairedDevs = new ArrayList<BluetoothDevice>();
			
			//bluetooth
			blueAdapter = BluetoothAdapter.getDefaultAdapter();
			do{
				if(!blueAdapter.isEnabled()){
					blueAdapter.enable();
					Log.i("BlueLog", "Bluetooth is enabled");
				}else{
					testBlue = false;
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);

					startActivity(discoverableIntent);
					
				}	
			}while(testBlue);
			myBlue_Mac = blueAdapter.getAddress();
			myBlue_Name = blueAdapter.getName();		
			myName = tag + "_" + myBlue_Mac;
			blueAdapter.setName(myName);
			getPairedDevices();
			
			final BroadcastReceiver myReciever = new BroadcastReceiver() {
				Message msg = Message.obtain();
				@Override
		        public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Log.i("debug", "inside broadcastreceiver");
		            if(BluetoothDevice.ACTION_FOUND.equals(action)){
		                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		                Log.i("ActionFound","Inside action found " + device.getName());
		                if((device.getName().indexOf("fugitive") > -1) && !(foundDevices.contains(device))){
		                	foundDevices.add(device);
		                	Log.i("Devices Found", "Adds devices to foundDevices " + device.getName());
		                }
		                
		            }
		            
				}  
		    };
		    
		    final BroadcastReceiver discoveryStopped = new BroadcastReceiver() {
				
				@Override
		        public void onReceive(Context context, Intent intent) {
					Log.i("broadcast", "inside discoveryStopped broadcast");
					//whenStoppedDiscovery();
					Log.i("founddevs", "Found devices != null");
					//finishedDiscovering = true;
					whenStoppedDiscovery();
				}
		           
		    };
			
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);		 
		    registerReceiver(myReciever, filter);
		    IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);		 
		    registerReceiver(discoveryStopped, filter2);
		    
			
		    blueAdapter.startDiscovery();
				    
			byteStream = new byte[1024];
			
			
			serverHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					Log.i("BlueLog", "Trying to read data...");
					byte[] readBuf = (byte[]) msg.obj;
					int value = new Integer(msg.arg1);
					seekbar.setProgress(value);
					Log.i("BlueLog", "Progress changed to: "+value);
					serverHandler.removeMessages(1);
					
				}
			}; 
			clientHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					Log.i("BlueLog", "Trying to read data...");
					int value = (Integer) msg.obj;
					seekbar.setProgress(value);
					Log.i("BlueLog", "Progress changed to: "+value);
					clientHandler.removeMessages(1);
				}
			};
			Log.i("BlueLog", "OnCreate finished...");
	}
	
	
	
	@Override
	protected void onStart(){
		super.onStart();
		
     }
	

	public void whenStoppedDiscovery(){
		int sizeList = foundDevices.size();
		for(int i = 0; i < sizeList; i++){
			blueMac = foundDevices.get(i).getAddress();
			Thread BlueServer = new Thread(new BlueServerSock(blueAdapter));
	 		BlueServer.start();
	 		Thread BlueClient = new Thread(new BlueClientSocket(blueAdapter, blueMac));
	 		BlueClient.start();
		}
	}
	
	private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = blueAdapter.getBondedDevices();            
        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                foundDevices.add(device);
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		//Log.i("Log", "onProgressedChanged");
		textview.setText("Progress changing to: " + progress);
		progressbar = progress;
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//Log.i("Log", "onStartTrackingTouch");
		textview.setText("Progress is changing, last value: " + progressbar);
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//Log.i("Log", "onStopTrackingTouch");
		textview.setText("Progress has changed to: " + progressbar);
		byteStream = intToByteArray(progressbar);
		Log.i("BlueLog", "byteStream: " + byteStream);
		writeStream(byteStream);
	}
	
	public static byte[] intToByteArray(int value) {
	    byte[] b = new byte[1];
	    b[0] = (byte) value;
	    return b;
	}
	
	public void writeStream(byte[] writeStream){
		Log.i("BlueLog", "BlueClient trying to write...");
		try {
			Log.i("BlueLog", "bufferout "+writeStream[0]);
			output.write(writeStream[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onRestart(){
		//registerReceiver(myReciever, intentFilter);
		super.onRestart();
	}
}
