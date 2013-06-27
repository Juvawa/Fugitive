/*
* mbedADKSketch
* 
* Written by p07gbar
* 
* This library allows the mbed to be used for a controller of an "etch-a-sketch" clone
* 
* 
*/

package mbed.mbedsketch;


import mbed.adkPort.AdkPort;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;



public class MbedADKSketchActivity extends Activity {
   
   AdkPort mbed;						// Instance of the ADK Port class
   boolean mbed_attached = false;
   Button butfor;
   Button butback;
   Button butlef;
   Button butret;
   Button butsto;
   Button butforlef;
   Button butforRig;
   Button butbakLef;
   Button butbakRig;
    
   @Override
   public void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      butfor = (Button) findViewById(R.id.forw);
      butback = (Button) findViewById(R.id.bak);
      butlef = (Button) findViewById(R.id.lef);
      butret = (Button) findViewById(R.id.rig);
      butsto = (Button) findViewById(R.id.sto);
      butforlef = (Button) findViewById(R.id.forlef);
      butforRig = (Button) findViewById(R.id.forrig);
      butbakLef = (Button) findViewById(R.id.baklef);
      butbakRig = (Button) findViewById(R.id.bakrig);
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View v) {
            switch (v.getId()) {
            case R.id.forw:
               mbed.sendString("MF");
               break;
            case R.id.lef:
               mbed.sendString("ML");
               break;
            case R.id.rig:
               mbed.sendString("MR");
               break;
            case R.id.bak:
               mbed.sendString("MB");
               break;
            case R.id.sto:
               mbed.sendString("MS");
               break;
            case R.id.forlef:
               mbed.sendString("MQ");
               break;
            case R.id.forrig:
               mbed.sendString("ME");
               break;
            case R.id.baklef:
               mbed.sendString("M005005005");
               break;
            case R.id.bakrig:
               mbed.sendString("M001001001");
               break;
            }   
         }
      };
      butfor.setOnClickListener(listener);
      butback.setOnClickListener(listener);
      butlef.setOnClickListener(listener);
      butret.setOnClickListener(listener);
      butsto.setOnClickListener(listener);
      butforlef.setOnClickListener(listener);
      butforRig.setOnClickListener(listener);
      butbakLef.setOnClickListener(listener);
      butbakRig.setOnClickListener(listener);
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
      // Initialises the instance of ADKPort with the context
      
      
      
      try {
         mbed = new AdkPort(this);
      } catch (IOException e) {
         return;
      }
      // Attaches a function which is called on new, as a MessageNotifier interface, onNew called when new bytes are recived
      mbed.attachOnNew(new AdkPort.MessageNotifier(){ 
         @Override
         public void onNew()
         {
            //byte[] in = mbed.readB();
         }
      });
      Thread thread = new Thread(mbed);			// Set up an instance of the mbed thread
      thread.start();								// start it
      //mbed.sendString("GO");						// Tell it to send "go" to the mbed, so the mbed starts sending pot values
   }

   @Override
   public void onDestroy() {
      if (mbed != null)
      mbed.onDestroy(this);

      super.onDestroy();

   }
   
   @Override
   public void onPause() {
      if (mbed != null)
      mbed.closeAccessory();

      super.onPause();
   }
   
   @Override
   public void onResume() {
      if (mbed != null)
      mbed.openAccessory();
      
      super.onResume();
   }

   private int bytetoint(byte b) {				// Deals with the twos complement problem that bit packing presents
      int t = ((Byte)b).intValue();
      if(t < 0)
      {
         t += 256;
      }
      return t;
   }
}
