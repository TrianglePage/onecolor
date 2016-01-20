package com.puzzleworld.onecolor;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

	public native int[] ImgFun(int[] buf, int w, int h, int touchX, int touchY, int value);

	private ImageView ivProcess;
	private ImageButton btnRestore;
	private ImageButton btnSave;
	private ImageButton btnPickanother;
	private Bitmap showBitmap;
	private SeekBar seekBar;
	private TextView textView;
	private RatingBar ratingBar;
	private int value2jni;
	private int align;
	private float touchX=0;
	private float touchY=0;

	final float PIC_MAX_WIDTH = 1920;
	final float PIC_MAX_HEIGHT = 1080;
	final int seekbarLevel=4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process);

		ivProcess = (ImageView) findViewById(R.id.ivProcess);
		btnRestore = (ImageButton) findViewById(R.id.btnRestore);
		btnSave = (ImageButton) findViewById(R.id.btnConfirm);
		btnPickanother = (ImageButton) findViewById(R.id.btnPickanother);
		textView = (TextView) findViewById(R.id.textView1);

		// 从前一界面获取到选择的图片地址，显示到ImageView中
		Intent intent = getIntent();
		if (intent != null) {
			align = 2<<(seekbarLevel+1);
			ContentResolver cr = this.getContentResolver();
			Uri uri = intent.getParcelableExtra("uri");
			try {
				Bitmap bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
				showBitmap = scaleAndAlignBitmap(bm, align);
				ivProcess.setImageBitmap(showBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ivProcess.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	            //当按下时获取到屏幕中的xy位置
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                	touchX = event.getX();
                	touchY = event.getY();
                    Log.e("chz", "touch info: "+event.getX() +","+event.getY());
                }
				return false;
			}
        });

		// 滑动条
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setMax(seekbarLevel);

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
				textView.getPaint().setFakeBoldText(true);
				textView.setTextColor(Color.rgb(255, 255, 255));
				textView.setText(	String.format("%d", progress)+"%");
				value2jni = progress;
			}
		});

		// 调用native opencv处理图像
		ivProcess.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ivProcess.setDrawingCacheEnabled(true);
				// Bitmap img1 =
				// Bitmap.createBitmap(ivProcess.getDrawingCache());
				// ivProcess.setDrawingCacheEnabled(false);
				int w = showBitmap.getWidth(), h = showBitmap.getHeight();
				int[] pix = new int[w * h];
				showBitmap.getPixels(pix, 0, w, 0, 0, w, h);
				Log.i("chz", "img w="+ivProcess.getWidth()+", h="+ivProcess.getHeight()+", bitmap w="+showBitmap.getWidth()+",h="+showBitmap.getHeight());
				//touchX和touchY是相对imageView控件的，而内部的bitmap宽高与imageView是不同的
				//这里换算成相对图片的坐标tx，ty
				int tx = (int) (touchX/ivProcess.getWidth()*showBitmap.getWidth());
				int ty = (int) (touchY/ivProcess.getHeight()*showBitmap.getHeight());
				Log.i("chz", "img x="+ivProcess.getX()+",y="+ivProcess.getY()+",touchX="+touchX+",touchY="+touchY+",tx="+tx+",ty="+ty);
				int[] resultInt = ImgFun(pix, w, h, tx, ty, value2jni);
				Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
				ivProcess.setImageBitmap(resultImg);
			}
		});

		//
		btnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ivProcess.setImageBitmap(showBitmap);
			}
		});

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Intent intent_preview = new Intent();

				intent_preview.setClass(ProcessActivity.this, ResultPreviewActivity.class);
				Bitmap image = ((BitmapDrawable)ivProcess.getDrawable()).getBitmap();
				BitmapStore.setBitmap(image);
				ProcessActivity.this.startActivity(intent_preview);

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
				Bitmap bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
				showBitmap = scaleAndAlignBitmap(bm, align);
				ivProcess.setImageBitmap(showBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("PickpicActivity", "pick up picture failed!");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * 压缩和对齐图片，便于算法处理
	 */
	private Bitmap scaleAndAlignBitmap(Bitmap bgimage, int align) {
		int alignedWidth = bgimage.getWidth();
		int alignedHeight = bgimage.getHeight();
		Matrix matrix = null;
		Bitmap scaledBitmap = bgimage;
		// 如果图片过大，压缩处理
		if (bgimage.getWidth() > PIC_MAX_WIDTH || bgimage.getHeight() > PIC_MAX_HEIGHT) {
			float wRatio = PIC_MAX_WIDTH / (float) (bgimage.getWidth());
			float hRatio = PIC_MAX_HEIGHT / (float) (bgimage.getHeight());
			float scaleRatio = wRatio > hRatio ? hRatio : wRatio;
			matrix = new Matrix();
			matrix.postScale(scaleRatio, scaleRatio);	
			Log.wtf("chz", "w="+bgimage.getWidth()+",h="+bgimage.getHeight()+",ratio="+scaleRatio);
			scaledBitmap = Bitmap.createBitmap(bgimage, 0, 0, bgimage.getWidth(), bgimage.getHeight(), matrix, true);	
		}

		//对齐
		alignedWidth = (scaledBitmap.getWidth()/align)*align;
		alignedHeight = (scaledBitmap.getHeight()/align)*align;
		
		Log.wtf("chz", "w="+alignedWidth+",h="+alignedHeight);	
		return Bitmap.createBitmap(scaledBitmap, 0, 0, alignedWidth, alignedHeight, null, true);
	}


}
