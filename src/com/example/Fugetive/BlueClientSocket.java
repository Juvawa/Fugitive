package com.example.Fugetive;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * @author Ren� Aparicio Saez
 * @author Tom Peerdeman
 * 
 */
public class BlueClientSocket extends MainActivity {
	private InputStream in;
	private OutputStream out;
	private BluetoothSocket sock;
	private boolean isRunning;
	private BroadcastReceiver mReceiver;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#onStart()
	 */
	@Override
	public void onStart() {
		Log.i("SCA2", "Started bluetooth search");
		// Create a BroadcastReceiver for ACTION_FOUND
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device =
						intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if(device.getName().indexOf("17") > 0) {
						Log.i("SCA2", "Found server " + device.getName());
						onServerFound(device);
					}
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		main.registerReceiver(mReceiver, filter);
		// Start device discovery
		adapter.startDiscovery();
	}
	
	/**
	 * Called when the bluetooth discovery has found the server
	 * 
	 * @param dev
	 *            The device of the server
	 */
	public void onServerFound(BluetoothDevice dev) {
		try {
			// Create the socket
			sock = dev.createRfcommSocketToServiceRecord(MbedNetwork.SD2_UUID);
			// Stop the discovery, it slow the connecting down
			adapter.cancelDiscovery();
			// Connect to the device
			sock.connect();
			in = sock.getInputStream();
			out = sock.getOutputStream();
		} catch(IOException e) {
			main.onError("Network error", "Could not connect to the server.");
		}
		Log.i("SCA2",
				"I connected to " + dev.getAddress() + "/" + dev.getName());
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
					// Update progressbar
					main.newValue(buf[0] & 0xFF);
				}
			} catch(IOException e) {
				Log.e("SCA2", "Could not read from conn", e);
				return;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#newValue(int)
	 */
	@Override
	public void newValue(int value) {
		if(out != null) {
			try {
				out.write(value);
				out.flush();
			} catch(IOException e) {
				e.printStackTrace();
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
		// Close the socket if open
		if(sock != null && in != null && out != null) {
			try {
				in.close();
				out.close();
				sock.close();
			} catch(IOException idontcare) {
			}
		}
		
		// Unregister the bluetooth discovery receiver
		if(mReceiver != null) {
			main.unregisterReceiver(mReceiver);
		}
	}
}
