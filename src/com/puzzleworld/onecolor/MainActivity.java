package com.puzzleworld.onecolor;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
				ivLogo.setImageBitmap(lightlogo);
				final Intent intent = new Intent();
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						intent.setClass(MainActivity.this, PickpicActivity.class);
						MainActivity.this.startActivity(intent);
					}
				};
				//点击按钮后延时给用户看logo点亮效果。
				timer.schedule(task, 500);
			}
		});
	}
}