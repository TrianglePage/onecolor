package com.puzzleworld.onecolor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;

import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/*
 * 处理后图片添加心情界面
 */
public class MoodPreviewActivity extends Activity {

	private ImageView ivPreview;
	private ImageButton btnConfirm;
	private Bitmap previewBitmap;
	private Context mContext;
	private Bitmap imgTemp;  //临时标记图
	private int width,height;   //图片的高度和宽带
	private Bitmap imgMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_preview);


		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		btnConfirm = (ImageButton) findViewById(R.id.btnConfirm);

		imgMarker = BitmapFactory.decodeResource(getResources(), R.drawable.meinv);  
        width = imgMarker.getWidth();  
        height = imgMarker.getHeight();  

		previewBitmap = BitmapStore.getBitmapProcessed();
		ivPreview.setImageBitmap(previewBitmap);
		mContext = this;  		

		ivPreview.setBackground(createDrawable('K'));
		
		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("kevin", "jump to result preview");
				final Intent intent_preview = new Intent();

				intent_preview.setClass(MoodPreviewActivity.this, ResultPreviewActivity.class);
				MoodPreviewActivity.this.startActivity(intent_preview);
				//startActivityForResult(intent_preview, 1);

			}
		});

	}
	
	// 穿件带字母的标记图片  
    private Drawable createDrawable(char letter) {  
        imgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
        Canvas canvas = new Canvas(imgTemp);  
        Paint paint = new Paint(); // 建立画笔  
        paint.setDither(true);  
        paint.setFilterBitmap(true);  
        Rect src = new Rect(0, 0, width, height);  
        Rect dst = new Rect(0, 0, width, height);  
        canvas.drawBitmap(imgMarker, src, dst, paint);  
  
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG  
                | Paint.DEV_KERN_TEXT_FLAG);  
        textPaint.setTextSize(20.0f);  
        textPaint.setTypeface(Typeface.DEFAULT_BOLD); // 采用默认的宽度  
        textPaint.setColor(Color.WHITE);  
  
        canvas.drawText(String.valueOf(letter), width /2-5, height/2+5,  
                textPaint);  
        canvas.save(Canvas.ALL_SAVE_FLAG);  
        canvas.restore();  
        return (Drawable) new BitmapDrawable(getResources(), imgTemp);  
  
    }

}