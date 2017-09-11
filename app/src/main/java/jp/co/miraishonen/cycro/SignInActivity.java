package jp.co.miraishonen.cycro;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jp.co.miraishonen.cycro.helper.BitmapHelper;
import jp.co.miraishonen.cycro.helper.PreferenceUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class SignInActivity extends Activity { 
	public static final int REQUEST_CODE_PICK_IMAGE = 100;
	
	EditText nameEditText;
	
	ImageView avatarImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		nameEditText = (EditText)findViewById(R.id.signin_name_edittext);
		nameEditText.setText(PreferenceUtil.getUsername(SignInActivity.this));
		
		findViewById(R.id.signin_ok_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = nameEditText.getText().toString().trim();
				if (name.isEmpty()) {
					nameEditText.setError(getResources().getString(R.string.name_error));
				} else {
					PreferenceUtil.putUsername(SignInActivity.this, name);
					startActivity(new Intent(SignInActivity.this, MainActivity.class));
					finish();
				}
			}
		});
		
		findViewById(R.id.signin_plus_imagebutton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);

				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
				Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
				chooserIntent.putExtra
				(
				  Intent.EXTRA_INITIAL_INTENTS, 
				  new Intent[] { takePhotoIntent }
				);

				startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
			}
		});
		
		findViewById(R.id.signin_telll_a_friend_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "Check out Cycro ! Download it now from http://cycro.me/dl/ \nFrom" + PreferenceUtil.getUsername(SignInActivity.this));
				startActivity(Intent.createChooser(intent, "Invite via"));
			}
		});
		
		avatarImageView = ((ImageView)findViewById(R.id.signin_avatar_imageview));
		updateAvatarImage();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
	            try {
	            	Bitmap selectedBitmap;
	            	Uri uri = data.getData();
	            	if (uri != null) {
	            		selectedBitmap = BitmapHelper.decodeUri(data.getData(), this, 256);
	            	} else {
	            		selectedBitmap = (Bitmap)data.getExtras().get("data");
	            	}
	            	
	            	if (selectedBitmap != null) {
	            		selectedBitmap = BitmapHelper.createRegularBitmap(selectedBitmap, 256);
	            		PreferenceUtil.putMyPhoto(SignInActivity.this, selectedBitmap);
	            	} else {
	            		PreferenceUtil.putMyPhoto(SignInActivity.this, null);
	            	}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					PreferenceUtil.putMyPhoto(SignInActivity.this, null);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					PreferenceUtil.putMyPhoto(SignInActivity.this, null);
					e.printStackTrace();
				}
	            updateAvatarImage();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void updateAvatarImage() {
		Bitmap bm = PreferenceUtil.getMyPhoto(SignInActivity.this);
		if (bm == null) {
			avatarImageView.setImageResource(R.drawable.default_avatar);
		} else {
			avatarImageView.setImageBitmap(bm);
		}
	}
}
