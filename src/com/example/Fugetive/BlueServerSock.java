package com.example.Fugetive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;


public class BlueServerSock implements Runnable{
	private BluetoothServerSocket tmp = null;
	
	static BluetoothAdapter blueAdapter = null;
	static BluetoothServerSocket blueServer;
	static BluetoothSocket blueClient = null, blueClientS = null;
	
	static InputStream input;
	static OutputStream output;
	Message msg = new Message();
	public BlueServerSock(BluetoothAdapter blueAdapter){
		this.blueAdapter = BluetoothAdapter.getDefaultAdapter();
		
	}
	
	@Override
	public void run() {
		
		try {
			Log.i("pairing", "aan het pairen");
			tmp = blueAdapter.listenUsingRfcommWithServiceRecord(MainActivity.serviceName, MainActivity.myUUID);
		} catch (IOException e) {
			Log.i("BlueLog", "BlueServer did NOT initialise...");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		blueServer = tmp;
		Log.i("BlueLog", "BlueServer is initialised...");
		
		while(true){
			Log.i("BlueLog", "BlueServer is listening...");
			try {
				blueClientS = blueServer.accept();
			} catch (IOException e) {
				Log.i("BlueLog", "BlueServer is NOT listening...");
				e.printStackTrace();
			}
			//Socket accepted
			if(blueClientS != null){
				try {
					manageBlueServer();
					blueServer.close();
					break;
				} catch (IOException e) {
					Log.i("BlueLog", "BlueServer did not close...");
					e.printStackTrace();
				}
				Log.i("BlueLog", "BlueServer has closed...");
			}
		}
		
		
	}

	public void manageBlueServer(){
		try {
			input = blueClientS.getInputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Server: InputStream did NOT initialize...");
			e.printStackTrace();
		}
		try {
			MainActivity.output = blueClientS.getOutputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Server: OutputStream did NOT initialize...");
			e.printStackTrace();
		}
		Log.i("BlueLog", "Server: Input & Output are initialised...");
		Thread socketRead = new Thread(new SocketRead(input, MainActivity.output));
		socketRead.start();
		//Thread socketWrite = new Thread(new SocketWrite(input, output));
		//socketWrite.start();
		
		
	}
	
	public void cancel(){
		try{
			blueServer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
}
