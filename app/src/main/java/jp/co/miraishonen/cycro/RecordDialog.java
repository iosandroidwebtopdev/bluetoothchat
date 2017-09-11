package jp.co.miraishonen.cycro;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class RecordDialog extends Dialog {
	ChatActivity chatActivity;
	
	public RecordDialog(Context context) {
		super(context, android.R.style.Theme_Holo_DialogWhenLarge_NoActionBar);
		// TODO Auto-generated constructor stub
		chatActivity = (ChatActivity)context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_record);
		
		findViewById(R.id.record_stop_imagebutton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chatActivity.stopRecorder();
				dismiss();
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		chatActivity.releaseRecorder();
		super.onBackPressed();
	}
}
