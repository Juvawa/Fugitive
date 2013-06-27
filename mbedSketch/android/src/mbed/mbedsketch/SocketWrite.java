package mbed.mbedsketch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SocketWrite implements Runnable {
	InputStream input;
	OutputStream output;
	String serviceName = "testBlue";
	static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-FFFFFFFFFFFF");
	
	public SocketWrite(InputStream input, OutputStream output){
		this.input = input;
		this.output = output;
	}
	byte[] bufferOut = new byte[1];
	int bytesOut;

	@Override
	public void run() {
		Log.i("BlueLog", "BlueClient trying to write...");
		while(true){
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			//bufferOut = intToByteArray(MainActivity.progressbar);
			try {
				Log.i("BlueLog", "bufferout "+bufferOut[0]);
				output.write(bufferOut[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static byte[] intToByteArray(int value) {
	    byte[] b = new byte[1];
	    b[0] = (byte) value;
	    return b;
	}
}
