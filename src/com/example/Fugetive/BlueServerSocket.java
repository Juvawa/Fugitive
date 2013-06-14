package com.example.Fugetive;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class BlueServerSocket extends MainActivity implements Runnable {
	// Request code of MbedNetwork.REQUEST_ENABLE_BT + 1
	public static final int REQUEST_DISCOVERABLE_BT = 5379;
	
	private BluetoothServerSocket serverSock;
	private boolean isRunning;
	private BluetoothSocket sock;
	private InputStream in;
	private OutputStream out;
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#onStart()
	 */
	@Override
	public void onStart() {
		Intent discoverableIntent =
			new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
	}
	
	/**
	 * Called when this device is bluetooth discoverable
	 */
	public void onDiscoverable() {
		new Thread(this).start();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int nread;
		byte[] buf = new byte[1];
		
		while(isRunning) {
			try {
				// Open a new bluetooth server socket
				serverSock =
					MainActivity.bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
							MainActivity.SERVICE_NAME, MainActivity.myUUID);
			} catch(IOException e) {
				Log.e("SCA2", "Could not open server socket", e);
				MainActivity.onError("Network error", "Could not open the server.");
				return;
			}
			
			Log.i("SCA2", "Opened bluetooth server socket with adress "
					+ MainActivity.bluetoothAdapter.getAddress());
			
			try {
				sock = serverSock.accept();
				in = sock.getInputStream();
				out = sock.getOutputStream();
				// Close the server socket, only one connection per server
				// socket is supported by bluetooth
				serverSock.close();
			} catch(IOException e) {
				Log.e("SCA2", "Could not open accept conn", e);
				continue;
			}
			Log.i("SCA2", "Client connected");
			while(isRunning) {
				try {
					nread = in.read(buf);
					if(nread < 0) {
						// EOF, close the socket
						out.close();
						in.close();
						sock.close();
						in = null;
						out = null;
					}
					if(nread > 0) {
						// Update Mbed & progressbar
						MainActivity.newValue(buf[0] & 0xFF);
					}
				} catch(IOException e) {
					Log.e("SCA2", "Could not read from conn", e);
				}
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#stop()
	 */
	@Override
	public void stop() {
		isRunning = false;
		if(serverSock != null) {
			try {
				serverSock.close();
			} catch(IOException idontcare) {
			}
		}
		
		if(sock != null && in != null && out != null) {
			try {
				in.close();
				out.close();
				sock.close();
			} catch(IOException idontcare) {
			}
		}
	}
}
