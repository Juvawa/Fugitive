package com.example.Fugetive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BlueClientSocket implements Runnable{
	String blueMac = "";
	BluetoothDevice remDevice;
	BluetoothSocket tmp = null;
		
	BluetoothAdapter blueAdapter = null;
	static BluetoothServerSocket blueServer;
	static BluetoothSocket blueClient = null, blueClientS = null;
	
	static InputStream input;
	static OutputStream output;
	
	public BlueClientSocket(BluetoothAdapter blueAdapter, String blueMac){
		this.blueAdapter = BluetoothAdapter.getDefaultAdapter();
		this.blueMac = blueMac;
		
	}
	@Override
	public void run() {
		blueAdapter.cancelDiscovery();
		remDevice = blueAdapter.getRemoteDevice(blueMac);
		try {
			Log.i("pairing", "aan het pairen");
			tmp = remDevice.createRfcommSocketToServiceRecord(MainActivity.myUUID);
		} catch (IOException e) {
			Log.i("BlueLog", "Client Socket could not create from remote device...");
			e.printStackTrace();
		}
		blueClient = tmp;
		Log.i("BlueLog", "Client Socket created from remote device...");
		
		try {
			blueClient.connect();
		} catch (IOException e) {
			Log.i("BlueLog", "Client Socket could not connect...");
			e.printStackTrace();
			/*try{
				//MainActivity.blueClient.close();
			}catch(IOException ex){
				ex.printStackTrace();
				return;
			}*/
		}
		Log.i("BlueLog", "Client Socket is connected...");
		manageBlueClient();
	}
	
	public void manageBlueClient(){
		try {
			input = blueClient.getInputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Client: InputStream did NOT initialize...");
			e.printStackTrace();
		}
		try {
			MainActivity.output = blueClient.getOutputStream();
		} catch (IOException e) {
			Log.i("BlueLog", "Client: OutputStream did NOT initialize...");
			e.printStackTrace();
		}
		Log.i("BlueLog", "Client: Input & Output are initialised...");
		Thread socketRead2 = new Thread(new SocketRead(input, MainActivity.output));
		socketRead2.start();
		//Thread socketWrite2 = new Thread(new SocketWrite(input, output));
		//socketWrite2.start();
	}
	
	public void cancel(){
		try{
			blueClient.close();
		}catch(IOException e){
			Log.i("BlueLog", "Client Socket could not close...");
		}
		Log.i("BlueLog", "Client Socket is closed...");
	}

}
