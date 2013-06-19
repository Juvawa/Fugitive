package com.example.Fugetive;

import android.os.Bundle;
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
	BluetoothDevice blueDevice;
	static BluetoothAdapter blueAdapter = null;
	static BluetoothServerSocket blueServer;
	static BluetoothSocket blueClient = null, blueClientS = null;
	
	//IO streams
	static InputStream input;
	static OutputStream output;
	byte[] byteStream;
	
	//Hardcoded stuff to make connection easier for now..
	static String myBlue_Name, myBlue_Mac;
	static String myBlue_NameH= "projectThief1"; //16
	static String nmyBlue_Name = "projectThief2"; //5
	static String myBlue_MacH = "A0:F4:50:CC:19:38"; //16
	static String nmyBlue_Mac = "A0:F4:50:9F:FC:1A"; //5
	
	//Hardcoded service name and UUID for socket connection
	static String serviceName = "testBlue";
	static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-FFFFFFFFFFFF");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Layout shizzle
		textview = (TextView) findViewById(R.id.textView1);
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setOnSeekBarChangeListener(this);
		
		//bluetooth
		blueAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!blueAdapter.isEnabled()){
			blueAdapter.enable();
			Log.i("BlueLog", "Bluetooth is enabled");
		}
		myBlue_Name = blueAdapter.getName();
		myBlue_Mac = blueAdapter.getAddress();
		Log.i("BlueLog", "myBlue_Name: "+ myBlue_Name + " MyBlue_Mac: " + myBlue_Mac);
		if(myBlue_Mac.equals(myBlue_MacH)){
			Log.i("BlueLog", "Variables do not change...");
		}
		else if(myBlue_Mac.equals(nmyBlue_Mac)){
			nmyBlue_Mac = myBlue_MacH;
			nmyBlue_Name = myBlue_NameH;
			Log.i("BlueLog", "Variables DO change...");
		}
		byteStream = new byte[1024];
		Log.i("BlueLog", "OnCreate finished...");
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
	}
	
	@Override
    protected void onStart(){
		super.onStart();
		Thread BlueServer = new Thread(new BlueServerSock(blueAdapter));
		BlueServer.start();
		Thread BlueClient = new Thread(new BlueClientSocket(blueAdapter));
		BlueClient.start();
		
	}
	/*
	 * Function that implements to listen for a incoming client connection.
	 * 
	public void manageBlueServer(){
		try {
			input = blueClientS.getInputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Server: InputStream did NOT initialize...");
			e.printStackTrace();
		}
		try {
			output = blueClientS.getOutputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Server: OutputStream did NOT initialize...");
			e.printStackTrace();
		}
		Log.i("BlueLog", "Server: Input & Output are initialised...");
		Thread socketRead = new Thread(new SocketRead());
		socketRead.start();
		Thread socketWrite = new Thread(new SocketWrite());
		socketWrite.start();
		
		
	}
	
	public void manageBlueClient(){
		try {
			input = blueClient.getInputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Client: InputStream did NOT initialize...");
			e.printStackTrace();
		}
		try {
			output = blueClient.getOutputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Client: OutputStream did NOT initialize...");
			e.printStackTrace();
		}
		Log.i("BlueLog", "Client: Input & Output are initialised...");
		Thread socketRead2 = new Thread(new SocketRead());
		socketRead2.start();
		Thread socketWrite2 = new Thread(new SocketWrite());
		socketWrite2.start();
	}*/
	

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

}
