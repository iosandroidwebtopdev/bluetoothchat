package jp.co.miraishonen.cycro;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix.ScaleToFit;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

public class HelpActivity extends Activity {
	public static final int TIMER_INTERVAL = 10 * 1000;
	
	ViewPager viewPager;
	PagerAdapter pagerAdapter;
	Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		findViewById(R.id.help_signin_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(HelpActivity.this, SignInActivity.class));
				finish();
			}
		});
		
		viewPager = (ViewPager)findViewById(R.id.help_viewpager);
		viewPager.setAdapter(pagerAdapter = new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == (View)arg1;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 7;
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				// TODO Auto-generated method stub
				container.removeView((View)object);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				// TODO Auto-generated method stub
				int imgResId = getResources().getIdentifier("page" + String.valueOf(position + 1), "drawable", getPackageName());
				ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				ImageView iv = new ImageView(HelpActivity.this);
				iv.setImageResource(imgResId);
				iv.setScaleType(ScaleType.FIT_XY);
				container.addView(iv, 0, lp);
				return iv;
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						viewPager.setCurrentItem((viewPager.getCurrentItem() + 1) % pagerAdapter.getCount(), true);
					}
				});
			}
		}, TIMER_INTERVAL, TIMER_INTERVAL);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		timer.cancel();
		super.onPause();
	}
}
