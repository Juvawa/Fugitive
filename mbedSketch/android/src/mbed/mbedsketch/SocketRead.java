package mbed.mbedsketch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SocketRead implements Runnable{
	InputStream input;
	OutputStream output;
	String serviceName = "testBlue";
	static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-FFFFFFFFFFFF");
	
	public SocketRead(InputStream input, OutputStream output){
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void run(){
		
		byte[] buffer = new byte[200];
		int bytes;

		while(true) {
			Log.i("BlueLog", "BlueClient trying to read...");
			try {
				bytes = input.read(buffer);
				Log.i("BlueLog", "bytes: " + buffer[0]);
				if(bytes < 0) {
					// EOF, close the socket
					output.close();
					input.close();
					input = null;
					output = null;
				}
				final int send_bytes = buffer[0];
				if(bytes >= 0) {
					// Update progressbar
					MbedADKSketchActivity.clientHandler.obtainMessage(1, bytes, -1 ,(buffer)).sendToTarget();
					//seekbar.setProgress(send_bytes & 0xFF);
					//textview2.setText("Progress changed to: " + (send_bytes & 0xFF));
				}
			} catch(IOException e) {
				Log.e("BlueLog", "Could not read from conn", e);
				break;
			}
		}
	}
}
