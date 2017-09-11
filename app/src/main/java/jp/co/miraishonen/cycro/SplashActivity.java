package jp.co.miraishonen.cycro;

import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashActivity extends Activity {
	AsyncTask<Void, Integer, Void> timerTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		timerTask = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				// TODO Auto-generated method stub
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
				super.onPostExecute(result);
			}
		};
		timerTask.execute();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		timerTask.cancel(true);
		super.onPause();
	}
}
