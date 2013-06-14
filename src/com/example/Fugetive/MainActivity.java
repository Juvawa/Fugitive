package com.example.Fugetive;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
    /** Called when the activity is first created. */
    ListView listViewPaired;
    ListView listViewDetected;
    SeekBar seekBar;
    TextView textSeek;
    int progressBar;    
    ArrayList<String> arrayListpaired;
    ArrayAdapter<String> adapter,detectedAdapter;
    static HandleSeacrh handleSeacrh;
    BluetoothDevice bdDevice;
    BluetoothClass bdClass;
    ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    ListItemClickedonPaired listItemClickedonPaired;
    BluetoothAdapter bluetoothAdapter = null;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    ListItemClicked listItemClicked;
    static int numberOfConnectedDevices;
  
    //String buffer for send message
    private StringBuffer outStringBuffer;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listViewDetected = (ListView) findViewById(R.id.listViewDetected);
        listViewPaired = (ListView) findViewById(R.id.listViewPaired);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        textSeek = (TextView) findViewById(R.id.textViewSeek);
        
        //Initialise outgoing stringbuffer
        outStringBuffer = new StringBuffer("");
        
        arrayListpaired = new ArrayList<String>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handleSeacrh = new HandleSeacrh();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        /*
         * the above declaration is just for getting the paired bluetooth devices;
         * this helps in the removing the bond between paired devices.
         */
        listItemClickedonPaired = new ListItemClickedonPaired();
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        adapter= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arrayListpaired);
        detectedAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_single_choice);
        listViewDetected.setAdapter(detectedAdapter);
        listItemClicked = new ListItemClicked();
        detectedAdapter.notifyDataSetChanged();
        listViewPaired.setAdapter(adapter);
        
        numberOfConnectedDevices = 0;
        
        IntentFilter connected = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter dcRequest = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter disconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        
        this.registerReceiver(myReceiver, connected);
        this.registerReceiver(myReceiver, dcRequest);
        this.registerReceiver(myReceiver, disconnected);
        
        seekBar.setOnSeekBarChangeListener(this);
    }
    
    void print(String s) {
    	System.out.println(s);
    }
    @Override
    protected void onStart() {
        super.onStart();
        onBluetooth();
        makeDiscoverable();
        startSearching();
        getPairedDevices();
        listViewDetected.setOnItemClickListener(listItemClicked);
        listViewPaired.setOnItemClickListener(listItemClickedonPaired);
    }
    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();            
        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                arrayListpaired.add(device.getName()+"\n"+device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }
    class ListItemClicked implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListBluetoothDevices.get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The dvice : "+bdDevice.toString());
            /*
             * here below we can do pairing without calling the callthread(), we can directly call the
             * connect(). but for the safer side we must use the threading object.
             */
            callThread();
            if(connect(bdDevice))
            	Log.i("Log", "Trying to connect to device: "+bdDevice.toString()+"\n");
            Boolean isBonded = false; 
            try {
                isBonded = createBond(bdDevice);
                if(isBonded)
                {
                    //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                    //adapter.notifyDataSetChanged();
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace(); 
            }//connect(bdDevice);
            Log.i("Log", "The bond is created: "+isBonded);
        }       
    }
    class ListItemClickedonPaired implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            try {
                Boolean removeBonding = removeBond(bdDevice);
                if(removeBonding)
                {
                    arrayListpaired.remove(position);
                    adapter.notifyDataSetChanged();
                }


                Log.i("Log", "Removed"+removeBonding);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void callThread() {
        new Thread(){
            public void run() {
                Boolean isBonded = false;
                try {
                    isBonded = createBond(bdDevice);
                    if(isBonded)
                    {
                        arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace(); 
                }
                connect(bdDevice);
                Log.i("Log", "The bond is created: "+isBonded);
            }           
        }.start();
    }
    private Boolean connect(BluetoothDevice bdDevice) { 
        Boolean bool = false;
        try {
        	if(MainActivity.numberOfConnectedDevices < 3) {
	            Log.i("Log", "service metohd is called ");
	            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
	            Class[] par = {};
	            Method method = cl.getMethod("createBond", par);
	            Object[] args = {};
	            bool = (Boolean) method.invoke(bdDevice, args);// this invoke creates the detected devices paired.
	            Log.i("Log", "This is: "+bool.booleanValue());
	            Log.i("Log", "devicesss: "+bdDevice.getName());
        	} else {
        		print("I can only have 3 connections at once, U MAD?!");
        	}
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    };


    public boolean removeBond(BluetoothDevice btDevice)  
    throws Exception  
    {  
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");  
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }


    public boolean createBond(BluetoothDevice btDevice)  
    throws Exception  
    {
    	Log.i("Log", "CREATING BOND\n");
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");  
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }  

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();

                try
                {
                    //device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                }
                catch (Exception e) {
                    Log.i("Log", "Inside the exception: ");
                    e.printStackTrace();
                }

                if(arrayListBluetoothDevices.size()<1) // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the arraylist.
                    detectedAdapter.add(device.getName()+"\n"+device.getAddress());
                    arrayListBluetoothDevices.add(device);
                    detectedAdapter.notifyDataSetChanged();
                }
                else
                {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for(int i = 0; i<arrayListBluetoothDevices.size();i++)
                    {
                        if(device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress()))
                        {
                            flag = false;
                        }
                    }
                    if(flag == true)
                    {
                        detectedAdapter.add(device.getName()+"\n"+device.getAddress());
                        arrayListBluetoothDevices.add(device);
                        detectedAdapter.notifyDataSetChanged();
                    }
                }
            }
            else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            	Log.i("Log", "Connected to :" + device.getName());
            	MainActivity.numberOfConnectedDevices++;
            	Log.i("Log", "Connected device number: "+numberOfConnectedDevices+"\n");            	
            }
        }
    };
    private void startSearching() {
        Log.i("Log", "in the start searching method");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.this.registerReceiver(myReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();
    }
    private void onBluetooth() {
        if(!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();
            Log.i("Log", "Bluetooth is Enabled");
        }
    }
    private void offBluetooth() {
        if(bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.disable();
        }
    }
    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }
    class HandleSeacrh extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 111:

                break;

            default:
                break;
            }
        }
    }
    
    /*
     * onProgressChanged, onStartTrackingTouch and onStopTrackingTouch
     * are functions used to implement the progress bar. 
     * If the progress is changed on the bar the function onProgressChanged
     * is called. When you start to change the bar: onStartTrackingTouch and
     * when you release the bar onStopTrackinTouch. This way you can implement
     * different things for the three states. 
     * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		textSeek.setText("Seekbar now at: " + progress);
		progressBar = progress;
		
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		textSeek.setText("Start tracking touch..");
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		textSeek.setText("Stop tracking touch...");
		textSeek.setText("Progress set at:" + progressBar);
	}
}