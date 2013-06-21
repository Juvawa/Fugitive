package nl.vdweegen.fugetivetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Simple activity used to start other activities
 *
 * @author Peter Abeles
 */
public class FugetiveTracker extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void clickedVideo( View view ) {
		Intent intent = new Intent(this, VideoActivity.class);
		startActivity(intent);
	}
}