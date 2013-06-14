package com.example.Fugetive;

import android.widget.TextView;

public class MessagePoster implements Runnable {
	   	private TextView textView;
	    private String message;

	    public MessagePoster(TextView textView, String message) {
	      this.textView = textView;
	      this.message = message;
	    }

	    public void run() {
	      textView.setText(message);
	    }  

}
