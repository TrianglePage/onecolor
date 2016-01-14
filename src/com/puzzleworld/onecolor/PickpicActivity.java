package com.puzzleworld.onecolor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
/*
 * 选择图片界面
 */
public class PickpicActivity extends Activity {

	private ImageButton btnPick;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pickuppic);
		btnPick = (ImageButton) findViewById(R.id.btnPickPic);
		btnPick.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				/* 开启Pictures画面Type设定为image */
				intent.setType("image/*");
				/* 使用Intent.ACTION_GET_CONTENT这个Action */
				intent.setAction(Intent.ACTION_GET_CONTENT);
				/* 取得相片后返回本画面 */
				startActivityForResult(intent, 1);
			}
		});
	}
	
	/*
	 * 选择图片后返回本界面的回调函数，在此把得到的图片地址传给ProcessActivity然后跳转。
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			// Log.e("uri", uri.toString());
			// Bitmap bitmap =
			// BitmapFactory.decodeStream(cr.openInputStream(uri));
			
			Intent intent = new Intent(PickpicActivity.this, ProcessActivity.class);
			intent.putExtra("uri", uri);
			startActivity(intent);
			Log.e("PickpicActivity", "pick up picture success!");
		} else {
			Log.e("PickpicActivity", "pick up picture failed!");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}