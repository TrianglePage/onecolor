package com.puzzleworld.onecolor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.puzzleworld.onecolor.ResultPreviewActivity.AuthListener;
import com.puzzleworld.onecolor.wbapi.AccessTokenKeeper;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.VoiceObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.Utility;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

import android.R.bool;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/*
 * 处理后图片添加文字心情界面
 */
public class MoodPreviewActivity extends Activity {

	protected static final String TAG = "MoodActivity";
	private boolean DEBUG = false;
	private ImageView ivPreview;
	private ImageButton btnConfirm;
	private ImageButton btnColors;
	private ImageButton btnFonts;
	private Bitmap previewBitmap;
	private Bitmap imgTemp;  //临时图
	private int width,height;   //图片的高度和宽带
	private EditText editText;
	private int TextColor = Color.WHITE;
	private Typeface TextTypeFace = Typeface.DEFAULT_BOLD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_preview);

		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		btnConfirm = (ImageButton) findViewById(R.id.btnConfirm);
		btnColors = (ImageButton) findViewById(R.id.btnColors);
		btnFonts = (ImageButton) findViewById(R.id.btnFonts);
        editText=(EditText)findViewById(R.id.edit_text);

        previewBitmap = BitmapStore.getBitmapProcessed();
        width = previewBitmap.getWidth();
        height = previewBitmap.getHeight();
		
		String MoodStr=editText.getText().toString();
		ivPreview.setBackgroundDrawable(createDrawable(MoodStr));
		
        editText.setOnEditorActionListener(new OnEditorActionListener() {  
            @Override  
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {        		
                Toast.makeText(MoodPreviewActivity.this, String.valueOf(actionId), Toast.LENGTH_SHORT).show();  
                return false;  
            }  
        });
        
		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "jump to result preview");
				final Intent intent_preview = new Intent();

				intent_preview.setClass(MoodPreviewActivity.this, ResultPreviewActivity.class);
				Bitmap image = ((BitmapDrawable) ivPreview.getBackground().getCurrent()).getBitmap();
				BitmapStore.setBitmapProcessed(image);
				//MoodPreviewActivity.this.startActivity(intent_preview);
				startActivityForResult(intent_preview, 1);

			}
		});
		
		btnColors.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "jump to colors select");
				showColorsPopwindow();
			}
		});
		
		btnFonts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "jump to colors select");
				showFontsPopwindow();
			}
		});
        editText.addTextChangedListener(watcher);

	}

	/**
	 * 显示ColorspopupWindow
	 */
	private void showColorsPopwindow() {
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_mood_colors, null);

		// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

		PopupWindow window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);

		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		window.setBackgroundDrawable(dw);

		// 设置popWindow的显示和消失动画
		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 在底部显示
		window.showAtLocation(MoodPreviewActivity.this.findViewById(R.id.ivPreview), Gravity.BOTTOM, 0, 0);

		// 这里检验popWindow里的button是否可以点击
		ImageButton btnColorsRed = (ImageButton) view.findViewById(R.id.btnColorsRed);
		btnColorsRed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "U slc Color Red");
				TextColor = Color.RED;
			}
		});

		ImageButton btnColorsYellow = (ImageButton) view.findViewById(R.id.btnColorsYellow);
		btnColorsYellow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "pls slc Color Yellow");
				TextColor = Color.YELLOW;
			}
		});

		ImageButton btnColorsBlue = (ImageButton) view.findViewById(R.id.btnColorsBlue);
		btnColorsBlue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "pls slc Color Blue");
				TextColor = Color.BLUE;
			}
		});
		
		// popWindow消失监听方法
		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				System.out.println("popWindow消失");
			}
		});

	}

	/**
	 * 显示FontspopupWindow
	 */
	private void showFontsPopwindow() {
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_mood_fonts, null);

		// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

		PopupWindow window = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);

		// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
		window.setFocusable(true);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		window.setBackgroundDrawable(dw);

		// 设置popWindow的显示和消失动画
		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		// 在底部显示
		window.showAtLocation(MoodPreviewActivity.this.findViewById(R.id.ivPreview), Gravity.BOTTOM, 0, 0);

		// 这里检验popWindow里的button是否可以点击
		ImageButton btnFontsSS = (ImageButton) view.findViewById(R.id.btnFontsSansSerif);
		btnFontsSS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "U slc Font SANS_SERIF");
				TextTypeFace = Typeface.SANS_SERIF;
			}
		});

		ImageButton btnFontsSERIF = (ImageButton) view.findViewById(R.id.btnFontsSerif);
		btnFontsSERIF.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "pls slc Font SERIF");
			}
		});

		// popWindow消失监听方法
		window.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				System.out.println("Fonts popWindow消失");
			}
		});

	}
	
	//监听EditText
	private TextWatcher watcher = new TextWatcher() {
			//private CharSequence temp;//监听前的文本  
	       //private int editStart;//光标开始位置  
	       //private int editEnd;//光标结束位置  
	       //private final int charMaxNum = 10;  
	  
	       @Override  
	       public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
	           if (DEBUG)  
	               Log.i(TAG, "输入文本之前的状态");  
	           //temp = s;  
	       }  
	  
	       @Override  
	       public void onTextChanged(CharSequence s, int start, int before, int count) {  
	           if (DEBUG)  
	               Log.i(TAG, "输入文字中的状态，count是一次性输入字符数");  
	           //editText.setText("还能输入" + (charMaxNum - s.length()) + "字符");  
	          String MoodStr=editText.getText().toString();
	   		ivPreview.setBackgroundDrawable(createDrawable(MoodStr));	  
	       }  
	  
	       @Override  
	       public void afterTextChanged(Editable s) {  
	           if (DEBUG)  
	               Log.i(TAG, "输入文字后的状态");  
	           /** 得到光标开始和结束位置 ,超过最大数后记录刚超出的数字索引进行控制 */  
	          /* editStart = editText.getSelectionStart();  
	           editEnd = editText.getSelectionEnd();  
	           if (temp.length() > charMaxNum) {  
	               Toast.makeText(getApplicationContext(), "你输入的字数已经超过了限制！", Toast.LENGTH_LONG).show();  
	               s.delete(editStart - 1, editEnd);  
	               int tempSelection = editStart;  
	               editText.setText(s);  
	               editText.setSelection(tempSelection);  
	           }*/  
	  
	       }  
	};

	// 给图片添加文字心情
    private Drawable createDrawable(String str) {
        imgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Log.d("kevin", "createDrawable imgTemp width = " + width + ", height = "+ height);
        Canvas canvas = new Canvas(imgTemp);  

        Paint paint = new Paint(); // 建立画笔  
        paint.setDither(true);  
        paint.setFilterBitmap(true);  
        Rect src = new Rect(0, 0, width, height);  
        Rect dst = new Rect(0, 0, width, height);  
        canvas.drawBitmap(previewBitmap, src, dst, paint);  // 将 previewBitmap 缩放或扩大到 dst 使用的填充区 paint

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG  | Paint.DEV_KERN_TEXT_FLAG);  // 设置画笔
        textPaint.setTextSize(60.0f); // 字体大小
        textPaint.setTypeface(TextTypeFace); // 采用默认的宽度  
        textPaint.setColor(TextColor);
        canvas.drawText(str, width/4, height*3/4,  textPaint); // 绘制上去字，开始未知x,y采用那只笔绘制 
        canvas.save(Canvas.ALL_SAVE_FLAG);  
        canvas.restore();  
        return (Drawable) new BitmapDrawable(getResources(), imgTemp);  
  
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri uri = data.getData();
			// Log.i("uri", uri.toString());
			ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
				BitmapStore.setBitmapOriginal(previewBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("MoodActivity", "pick up picture failed!");
		}
	}

}