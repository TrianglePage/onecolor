package com.puzzleworld.onecolor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.tencent.mm.sdk.openapi.IWXAPI;  
import com.tencent.mm.sdk.openapi.SendMessageToWX;  
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;  
import com.tencent.mm.sdk.openapi.WXTextObject; 
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;

/*
 * 微信分享界面
 */
public class ShareActivity extends Activity {

	private ImageButton btnWechatShare, btnWechatShare1;
	private IWXAPI wxApi;
	private final int THUMB_SIZE = 200;
	private Bitmap ShareBitmap;

	//private static final String APP_ID = "wxe288bcf07e6c4a2d";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		wxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID);  
		btnWechatShare = (ImageButton) findViewById(R.id.btnWechatShare);
		btnWechatShare1 = (ImageButton) findViewById(R.id.btnWechatShare1);
		
		btnWechatShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				wxApi.registerApp(Constants.APP_ID);  
				wechatShare(0);//分享到微信好友
			}
		});

		btnWechatShare1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				wxApi.registerApp(Constants.APP_ID);  
				wechatShare(1);//分享到微信朋友圈
			}
		});
		
	}

	/** 
	 * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码） 
	 * @param flag(0:分享到微信好友，1：分享到微信朋友圈) 
	 */  
	private void wechatShare(int flag){
		Bitmap bmp, WaterMarkbmp;

		ShareBitmap = BitmapStore.getBitmap();
		WaterMarkbmp = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_small_70);
		// 加水印
		bmp = createBitmap(ShareBitmap, WaterMarkbmp);

		//初始化WXImageObject和WXMediaMessage对象
		WXImageObject imgObj = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;

		//设置缩略图
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		//初始化WXImageObject和WXMedia
	    SendMessageToWX.Req req = new SendMessageToWX.Req();  
	    req.transaction = String.valueOf(System.currentTimeMillis());  
	    req.message = msg;  
	    req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;  
	    wxApi.sendReq(req);  
	}

	// 加水印
	/**
　　* create the bitmap from a byte array
　　*
　　* @param src the bitmap object you want proecss
　　* @param watermark the water mark above the src
　　* @return return a bitmap object ,if paramter's length is 0,return null
　　*/
	private Bitmap createBitmap( Bitmap src, Bitmap watermark ) {
	String tag = "createBitmap";
	Log.d( tag, "kevin add watermark" );

	if( src == null )
	{
		return null;
	}

	int w = src.getWidth();
	int h = src.getHeight();
	int ww = watermark.getWidth();
	int wh = watermark.getHeight();

	Log.d( tag, "kevin src w = "+w+", h = "+h+", ww = "+ww+", wh = "+wh );

	//create the new blank bitmap
	Bitmap newb = Bitmap.createBitmap( w, h, Config.ARGB_8888 );//创建一个新的和SRC长度宽度一样的位图
	Canvas cv = new Canvas( newb );

	//draw src into
	cv.drawBitmap( src, 0, 0, null );//在 0，0坐标开始画入src

	//draw watermark into
	int dw, dh;
	dw = w - ww;
	dh =  h - wh;
	Log.d( tag, "kevin draw watermark,  w = "+dw+", h = "+dh );
	cv.drawBitmap( watermark, dw, dh, null );//在src的右下角画入水印

	//save all clip
	cv.save( Canvas.ALL_SAVE_FLAG );//保存

	//store
	cv.restore();
	return newb;
	}
	
}