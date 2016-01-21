package com.puzzleworld.onecolor;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;

/*
 * logo欢迎首页
 */
//TODO:如何只在第一次安装显示
public class MainActivity extends Activity {
	private ImageView ivLogo;
	private ImageButton btnLightOn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnLightOn = (ImageButton) findViewById(R.id.btnLightOn);
		ivLogo = (ImageView) findViewById(R.id.ivLogo);

		btnLightOn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap lightlogo = ((BitmapDrawable) getResources().getDrawable(R.drawable.onecolorlogo_2)).getBitmap();

				// ivLogo.setImageBitmap(lightlogo);
				onClick_fadeout(ivLogo);
				final Intent intent = new Intent();
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						intent.setClass(MainActivity.this, PickpicActivity.class);
						MainActivity.this.startActivity(intent);
					}
				};
				// 点击按钮后延时给用户看logo点亮效果。
				timer.schedule(task, 1200);
			}
		});
	}

	protected void onPause() {
		super.onPause();
		onClick_fadein(ivLogo);
		Log.i("chz", "onPause");
	}

	public void onClick_fadeout(ImageView view) {
		TransitionDrawable drawable = (TransitionDrawable) view.getDrawable();
		// 从第一个图像切换到第2个图像。其中使用1秒(1000毫秒)时间完成淡入淡出效果
		drawable.startTransition(1000);
	}

	public void onClick_fadein(ImageView view) {
		TransitionDrawable drawable = (TransitionDrawable) view.getDrawable();
		// 从第2个图像切换到第1个图像。其中使用1秒(1000毫秒)时间完成淡入淡出效果
		drawable.reverseTransition(1000);
	}

}