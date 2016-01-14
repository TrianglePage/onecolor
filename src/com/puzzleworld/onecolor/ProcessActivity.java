package com.puzzleworld.onecolor;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ProcessActivity extends Activity {

	static {
		System.loadLibrary("img_processor");
	}

	public native int[] ImgFun(int[] buf, int w, int h);

	private ImageView ivProcess;
	private ImageButton btnRestore;
	private ImageButton btnSave;
	private ImageButton btnPickanother;
	private Bitmap tmpBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process);

		ivProcess = (ImageView) findViewById(R.id.ivProcess);
		btnRestore = (ImageButton) findViewById(R.id.btnRestore);
		btnSave = (ImageButton) findViewById(R.id.btnSave);
		btnPickanother = (ImageButton) findViewById(R.id.btnPickanother);
		
		//从前一界面获取到选择的图片地址，显示到ImageView中
		Intent intent = getIntent();
		if (intent != null) {
			ContentResolver cr = this.getContentResolver();
			Uri uri = intent.getParcelableExtra("uri");
			try {
				tmpBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
				ivProcess.setImageBitmap(tmpBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//调用native opencv处理图像
		ivProcess.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ivProcess.setDrawingCacheEnabled(true);
				// Bitmap img1 =
				// Bitmap.createBitmap(ivProcess.getDrawingCache());
				// ivProcess.setDrawingCacheEnabled(false);
				int w = tmpBitmap.getWidth(), h = tmpBitmap.getHeight();
				int[] pix = new int[w * h];
				tmpBitmap.getPixels(pix, 0, w, 0, 0, w, h);
				int[] resultInt = ImgFun(pix, w, h);
				Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
				ivProcess.setImageBitmap(resultImg);
			}
		});

		//
		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ivProcess.setImageBitmap(tmpBitmap);
			}
		});

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		btnPickanother.setOnClickListener(new OnClickListener() {
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("md", "caocaocao");
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			Log.i("uri", uri.toString());
			ContentResolver cr = this.getContentResolver();
			try {
				tmpBitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
				ivProcess.setImageBitmap(tmpBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("PickpicActivity", "pick up picture failed!");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}