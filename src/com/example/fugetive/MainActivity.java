package com.example.fugetive;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.ArrayList;


public class MainActivity extends Activity implements OnSeekBarChangeListener {
	private SeekBar bar1;		
	private SeekBar bar2;				
	private SeekBar bar3;
	private TextView text1;
	private TextView text2;
	private TextView text3;
	private ArrayList<String> devices = new ArrayList<String>();
	
	
	void print(String s) {
		System.out.println(s);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// Register the BroadcastReceiver
    	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	this.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bar1 = (SeekBar)findViewById(R.id.seekBar1); //make seekbar object
		bar1.setOnSeekBarChangeListener(this); //set seekbar listener
		bar2 = (SeekBar)findViewById(R.id.seekBar2); //make seekbar object
		bar2.setOnSeekBarChangeListener(this); //set seekbar listener
		bar3 = (SeekBar)findViewById(R.id.seekBar3); //make seekbar object
		bar3.setOnSeekBarChangeListener(this); //set seekbar listener
		text1 = (TextView)findViewById(R.id.textView1);
		text2 = (TextView)findViewById(R.id.textView2);
		text3 = (TextView)findViewById(R.id.textView3);


        
        BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(mBluetooth == null)
        	print("BLuetooth is not supported.");
        if (!mBluetooth.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        if(mBluetooth.isEnabled()) {
        	String mydeviceaddress = mBluetooth.getAddress();
        	String mydevicename= mBluetooth.getName();
        	text1.setText(mydevicename + " : " + mydeviceaddress);
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        
        if(mBluetooth.isDiscovering())
        	mBluetooth.cancelDiscovery();
        
        if(mBluetooth.startDiscovery()){
        	print("Searching has started!");
        }
        else
        	print("Search did not start!");

    }
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
    
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	print("Received");
            text2.setText("");
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	print("IN IF\n");
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            text2.setText(device.getName() + " : " + device.getAddress());
	            System.out.println("test1\n");
	            devices.add(device.getName() + "\n" + device.getAddress());
	            System.out.println("test2\n");
	            for(String item: devices)
	            	print(item + "\n");
	        }
	    }
	};
}