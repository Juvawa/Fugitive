package org.opencv.samples.colorblobdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import mbed.adkPort.AdkPort;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
	//Mbed
	AdkPort mbed;						// Instance of the ADK Port class
	boolean mbed_attached = false;
	   
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    List<Point> 				 foo;
    private int					 dir;
    int maxX, maxY, minX, minY;
    Display display;
    int width;
    int height;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        foo = new ArrayList<Point>();
        display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        minX = mOpenCvCameraView.getWidth();
        minY = mOpenCvCameraView.getHeight();
        maxX = 0;
        maxY = 0; 
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
    public void onPause()
    {
        if (mbed != null)
            mbed.closeAccessory();
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        if (mbed != null)
            mbed.openAccessory();
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        if (mbed != null)
        mbed.onDestroy(this);
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        //mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        
        //mBlobColorHsv = new Scalar(6, 222, 106); //Red
        mBlobColorHsv = new Scalar(77, 148, 119); //Green
        mDetector.setHsvColor(mBlobColorHsv);

        mIsColorSelected = true;
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    /*public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")"); 
        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }*/

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            int result = 0;
            if(contours.size() > 0) {
            	foo = contours.get(0).toList();
            	for(Point p : foo){
            		if(minX > p.x)
            			minX = (int)p.x;
            		if(minY > p.y)
            			minY = (int)p.y;
            		if(maxX < p.x)
            			maxX = (int)p.x;
            		if(maxY < p.y)
            			maxY = (int)p.y;
            		//Log.i("POINTS", "WIDTH "+width+" HEIGHT "+height);
            		Log.i("POINTS", "maxX "+maxX + " maxY "+maxY+ " minX " +minX + " minY "+minY);	
            	}
            	// && minY > (height/3) && maxY < (height/3*2))
            	// && minY > (height/3) && maxY < (height/3*2))
            	// && minY > (height/3) && maxY < (height/3*2))
            	result = (minX+maxX)/2;
            	if(result >= (width/3) && result < (width/3*2)) {
            		if(dir != 1) {
            		   mbed.sendString("MF");
            		}
            		dir = 1;
            	}
            	else if(result >= (width/3*2) && result < width) {
            		if(dir != 2) {
             		   mbed.sendString("MR");
             		}
             		dir = 2;
            	}
            	else if(result >= 0 && result < (width/3)) {
            		if(dir != 3) {
             		   mbed.sendString("ML");
             		}
             		dir = 3;
            	}
            	else {
            		mbed.sendString("MS");
            		dir = 0;
            	}
                minX = mOpenCvCameraView.getWidth();
                minY = mOpenCvCameraView.getHeight();
                maxX = 0;
                maxY = 0; 
            } else {
            	if(dir != 0)
            	   mbed.sendString("MS");
            	dir = 0;
            }
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
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