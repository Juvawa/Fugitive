package com.example.Fugetive;

import java.io.IOException;
import java.io.InputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class BluetoothSocketListener implements Runnable {
	BluetoothSocket socket;
	TextView textView;
	Handler handler;
	
	public BluetoothSocketListener(BluetoothSocket socket, Handler handler, TextView textView) {
			this.socket = socket;
			this.textView = textView;
			this.handler = handler;
	} 

	public void run() {
		  int bufferSize = 1024;
		  byte[] buffer = new byte[bufferSize];      
		  try {
		    InputStream instream = socket.getInputStream();
		    int bytesRead = -1;
		    String message = "";
		    while (true) {
		      message = "";
		      bytesRead = instream.read(buffer);
		      if (bytesRead != -1) {
		        while ((bytesRead==bufferSize)&&(buffer[bufferSize-1] != 0)) {
		          message = message + new String(buffer, 0, bytesRead);
		          bytesRead = instream.read(buffer);
		        }
		        message = message + new String(buffer, 0, bytesRead - 1); 

		        handler.post(new MessagePoster(textView, message));              
		        socket.getInputStream();
		      }
		    }
		  } catch (IOException e) {
		    Log.d("BLUETOOTH_COMMS", e.getMessage());
		  } 
		}
}
