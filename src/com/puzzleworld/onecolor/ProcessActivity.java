package com.puzzleworld.onecolor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private ImageButton btnConfirm;
	private ImageButton btnPickanother;
	private Bitmap showBitmap;
	private SeekBar seekBar;
	private TextView textView;
	private TextView textView1;
	private RatingBar ratingBar;
	private int value2jni;
	private int align;
	private float touchX=0;
	private float touchY=0;

	final float PIC_MAX_WIDTH = 1920;
	final float PIC_MAX_HEIGHT = 1080;
	final int seekbarLevel=4;
	
	private enum textView_e
	{
		TV_SELECT_PIC,
		TV_CHANGE_LEVEL,
		TV_TOUCH_POINT,
		TV_CONFIRM,
		TV_MAX_NUM
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process);

		ivProcess = (ImageView) findViewById(R.id.ivProcess);
		btnRestore = (ImageButton) findViewById(R.id.btnRestore);
		btnConfirm = (ImageButton) findViewById(R.id.btnConfirm);
		btnPickanother = (ImageButton) findViewById(R.id.btnPickanother);
		textView = (TextView) findViewById(R.id.textView);
		textView1 = (TextView) findViewById(R.id.textView1);

		//textView 点击更新
		textView.getPaint().setFakeBoldText(true);
		textView.setTextColor(Color.rgb(255, 255, 255));
		textView_e tv_0 = textView_e.TV_SELECT_PIC;
		fresh_textView(tv_0);
		
		// 从前一界面获取到选择的图片地址，显示到ImageView中
		Intent intent = getIntent();
		if (intent != null) {
			//textView 点击更新
			textView_e tv_1 = textView_e.TV_CHANGE_LEVEL;
			fresh_textView(tv_1);
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
				//textView 点击更新
				textView_e tv_3 = textView_e.TV_CONFIRM;
				fresh_textView(tv_3);

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
				textView1.getPaint().setFakeBoldText(true);
				textView1.setTextColor(Color.rgb(255, 255, 255));
				//textView 点击更新
				textView_e tv_2 = textView_e.TV_TOUCH_POINT;
				fresh_textView(tv_2);

				textView1.setText(	String.format("Level[0~4] %d", progress));
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
				//textView 点击更新
				textView_e tv_1 = textView_e.TV_CHANGE_LEVEL;
				fresh_textView(tv_1);

				ivProcess.setImageBitmap(showBitmap);
			}
		});

		btnConfirm.setOnClickListener(new OnClickListener() {

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
				//textView 点击更新
				textView_e tv_1 = textView_e.TV_CHANGE_LEVEL;
				fresh_textView(tv_1);

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

	private void fresh_textView(textView_e tv_type)
	{
		Log.d("kevin", "fresh textView type : " + tv_type);

		textView.getPaint().setFakeBoldText(true);
		textView.setTextColor(Color.rgb(255, 255, 255));
		
		switch (tv_type)
		{
		case TV_CONFIRM:
			textView.setText(	String.format("请点击确认按钮"));
			break;
		case TV_TOUCH_POINT:
			textView.setText(	String.format("请点击需要突出显示的区域"));
			break;
		case TV_CHANGE_LEVEL:
			textView.setText(	String.format("请选择处理强度级别"));
			break;
		case TV_SELECT_PIC:
		default:
			textView.setText(	String.format("请选择一张图片"));
			break;
		}
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
		Bitmap scaledBitmap  = bgimage;

		
		// 如果图片过大，压缩处理
		if (bgimage.getWidth() > PIC_MAX_WIDTH || bgimage.getHeight() > PIC_MAX_HEIGHT) {
			float wRatio = PIC_MAX_WIDTH / (float) (bgimage.getWidth());
			float hRatio = PIC_MAX_HEIGHT / (float) (bgimage.getHeight());
			float scaleRatio = wRatio > hRatio ? hRatio : wRatio;
			matrix = new Matrix();
			matrix.postScale(scaleRatio, scaleRatio);	
			Log.wtf("chz scaleAndAlignBitmap", "w="+bgimage.getWidth()+",h="+bgimage.getHeight()+",ratio="+scaleRatio);
			scaledBitmap = Bitmap.createBitmap(bgimage, 0, 0, bgimage.getWidth(), bgimage.getHeight(), matrix, true);	
		}
		
		//scaledBitmap = compressImage(bgimage);

		//对齐
		alignedWidth = (scaledBitmap.getWidth()/align)*align;
		alignedHeight = (scaledBitmap.getHeight()/align)*align;
		
		Log.wtf("chz scaleAndAlignBitmap", "w="+alignedWidth+",h="+alignedHeight);	
		return Bitmap.createBitmap(scaledBitmap, 0, 0, alignedWidth, alignedHeight, null, true);
	}

	//// 图像压缩-质量压缩法-只支持JPG
	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		int i = 0;
		
		Log.d("kevin", "before compressImage Byte Count = " + baos.toByteArray().length + "Bytes");
		while ( baos.toByteArray().length / 1024 > 31) {	//循环判断如果压缩后图片是否大于31kb,大于继续压缩
			Log.d("kevin", "图像质量压缩处理" + i + " 次。");
			++i;
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		Log.d("kevin", "after compressImage Byte Count = " + baos.toByteArray().length + "Bytes");
		
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		Log.d("kevin", "after compressImage bitmap Byte Count = " + bitmap.getByteCount() + "Bytes");
		return bitmap;
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		showDialog(this, item.getItemId());
		return true;
	}
	
	private void showDialog(Context context, int ItemId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		switch (ItemId) {
		case R.id.action_about:
			builder.setTitle("关于");
			builder.setMessage("声明：\n本应用特别牛逼，请谨慎使用！");
			builder.setPositiveButton("还是确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// setTitle("你懂了");
				}
			});
			builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// setTitle("你真懂了");
				}
			});
			builder.show();
			break;
		case R.id.action_update:
			builder.setTitle("更新");
			builder.setMessage("更新文件大小100G，继续下载？？？");
			builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			builder.setNeutralButton("下载", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			builder.show();
			break;
		default:
			break;
		}
	}

}
