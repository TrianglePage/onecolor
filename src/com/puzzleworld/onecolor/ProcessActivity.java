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
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ProcessActivity extends Activity {

	static {
		System.loadLibrary("img_processor");
	}

	public native int[] ImgFun(int[] buf, int w, int h, int value);

	private ImageView ivProcess;
	private ImageButton btnRestore;
	private ImageButton btnSave;
	private ImageButton btnPickanother;
	private Bitmap tmpBitmap;
	private SeekBar seekBar;
	private TextView textView;
	private RatingBar ratingBar;
	private int value2jni;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process);

		ivProcess = (ImageView) findViewById(R.id.ivProcess);
		btnRestore = (ImageButton) findViewById(R.id.btnRestore);
		btnSave = (ImageButton) findViewById(R.id.btnSave);
		btnPickanother = (ImageButton) findViewById(R.id.btnPickanother);
		textView = (TextView) findViewById(R.id.textView1);
		
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

		//滑动条
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setMax(100);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				System.out.println("kevin Start Tracking Touch-->");
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				System.out.println("kevin Stop Tracking Touch-->");
			}
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				System.out.println("kevin progress changed-->"+progress);
				textView.setText(	String.format("%d", progress)+"%");
				value2jni = progress;
			}
		});

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
				int[] resultInt = ImgFun(pix, w, h, value2jni);
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
				final Intent intent_share = new Intent();
				
				intent_share.setClass(ProcessActivity.this, ShareActivity.class);
				ProcessActivity.this.startActivity(intent_share);

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