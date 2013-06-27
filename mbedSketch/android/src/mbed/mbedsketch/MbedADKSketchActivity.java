/*
* mbedADKSketch
* 
* Written by p07gbar
* 
* This library allows the mbed to be used for a controller of an "etch-a-sketch" clone
* 
* 
*/

package mbed.mbedsketch;


import mbed.adkPort.AdkPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.os.Message;
import android.view.Menu;
import android.bluetooth.*;
import android.content.*;
import android.util.Log;
import android.view.*;
import android.widget.*;



public class MbedADKSketchActivity extends Activity {
   
   AdkPort mbed;						// Instance of the ADK Port class
   boolean mbed_attached = false;
   
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
	
   Button butfor;
   Button butback;
   Button butlef;
   Button butret;
   Button butsto;
   Button butforlef;
   Button butforRig;
   Button butbakLef;
   Button butbakRig;
    
   @Override
   public void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
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
				try {
					String bla = new String(readBuf,"UTF-8");
					Log.i("BlueLog", "DATA: "+ bla);
					mbed.sendString("bla");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Log.i("BlueLog", "Progress changed to: "+value);
				serverHandler.removeMessages(1);
				
			}
		}; 
		clientHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				Log.i("BlueLog", "Trying to read data...");
				byte[] readBuf = (byte[]) msg.obj;
				try {
					String bla = new String(readBuf,"UTF-8");
					Log.i("BlueLog", "DATA: "+ bla);
					mbed.sendString("bla");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Log.i("BlueLog", "Progress changed to: "+value);
				clientHandler.removeMessages(1);
			}
		};
		Log.i("BlueLog", "OnCreate finished...");
		
      butfor = (Button) findViewById(R.id.forw);
      butback = (Button) findViewById(R.id.bak);
      butlef = (Button) findViewById(R.id.lef);
      butret = (Button) findViewById(R.id.rig);
      butsto = (Button) findViewById(R.id.sto);
      butforlef = (Button) findViewById(R.id.forlef);
      butforRig = (Button) findViewById(R.id.forrig);
      butbakLef = (Button) findViewById(R.id.baklef);
      butbakRig = (Button) findViewById(R.id.bakrig);
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View v) {
        	 String command;
        	 byte[] bCommand;
            switch (v.getId()) {
            case R.id.forw:
            	command = "MF";
            	try {
					bCommand = command.getBytes("UTF-8");
					writeStream(bCommand);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
               //mbed.sendString("MF");
               break;
            case R.id.lef:
            	command = "ML";
            	try {
					bCommand = command.getBytes("UTF-8");
					writeStream(bCommand);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}            	
               //mbed.sendString("ML");            	
               break;
            case R.id.rig:
               //mbed.sendString("MR");
               break;
            case R.id.bak:
               //mbed.sendString("MB");
               break;
            case R.id.sto:
            	command = "MS";
            	try {
					bCommand = command.getBytes("UTF-8");
					writeStream(bCommand);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
               //mbed.sendString("MS");
               break;
            case R.id.forlef:
               //mbed.sendString("MQ");
               break;
            case R.id.forrig:
               //mbed.sendString("ME");
               break;
            case R.id.baklef:
               //mbed.sendString("M005005005");
               break;
            case R.id.bakrig:
               //mbed.sendString("M001001001");
               break;
            }   
         }
      };
      butfor.setOnClickListener(listener);
      butback.setOnClickListener(listener);
      butlef.setOnClickListener(listener);
      butret.setOnClickListener(listener);
      butsto.setOnClickListener(listener);
      butforlef.setOnClickListener(listener);
      butforRig.setOnClickListener(listener);
      butbakLef.setOnClickListener(listener);
      butbakRig.setOnClickListener(listener);
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
      // Initialises the instance of ADKPort with the context
      
      
      try {
         mbed = new AdkPort(this);
      } catch (IOException e) {
         return;
      }
      // Attaches a function which is called on new, as a MessageNotifier interface, onNew called when new bytes are recived
      mbed.attachOnNew(new AdkPort.MessageNotifier(){ 
         @Override
         public void onNew()
         {
            //byte[] in = mbed.readB();
         }
      });
      Thread thread = new Thread(mbed);			// Set up an instance of the mbed thread
      thread.start();								// start it
      //mbed.sendString("GO");						// Tell it to send "go" to the mbed, so the mbed starts sending pot values
   }

   @Override
   public void onDestroy() {
      //if (mbed != null)
      //mbed.onDestroy(this);

      super.onDestroy();

   }
   
   @Override
   public void onPause() {
      //if (mbed != null)
      //mbed.closeAccessory();

      super.onPause();
   }
   
   @Override
   public void onResume() {
      //if (mbed != null)
      //mbed.openAccessory();
      
      super.onResume();
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
	
   private int bytetoint(byte b) {				// Deals with the twos complement problem that bit packing presents
      int t = ((Byte)b).intValue();
      if(t < 0)
      {
         t += 256;
      }
      return t;
   }
   
	public void writeStream(byte[] writeStream){
		Log.i("BlueLog", "BlueClient trying to write...");
		try {
			Log.i("BlueLog", "bufferout "+writeStream[0]);
			output.write(writeStream);
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
