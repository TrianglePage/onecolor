package com.puzzleworld.onecolor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.tencent.mm.sdk.openapi.IWXAPI;  
import com.tencent.mm.sdk.openapi.SendMessageToWX;  
import com.tencent.mm.sdk.openapi.WXAPIFactory;  
import com.tencent.mm.sdk.openapi.WXMediaMessage;  
import com.tencent.mm.sdk.openapi.WXTextObject; 
import com.tencent.mm.sdk.openapi.WXWebpageObject;

/*
 * 微信分享界面
 */
public class ShareActivity extends Activity {

	private ImageButton btnWechatShare, btnWechatShare1;
	private IWXAPI wxApi;
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
				wechatShare(1);//分享到微信好友  
			}
		});

		btnWechatShare1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				wxApi.registerApp(Constants.APP_ID);  
				wechatShare(0);//分享到微信朋友圈
			}
		});
		
	}

	/** 
	 * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码） 
	 * @param flag(0:分享到微信好友，1：分享到微信朋友圈) 
	 */  
	private void wechatShare(int flag){  
	    WXWebpageObject webpage = new WXWebpageObject();  
	    webpage.webpageUrl = "http://mobile.baidu.com/#/item?docid=8747088";  
	    WXMediaMessage msg = new WXMediaMessage(webpage);  
	    msg.title = "OneColor";  
	    msg.description = "Color Your Life. An App By PuzzleWorld";  
	    //这里替换一张自己工程里的图片资源  
	    Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.onecolorlogo_2);  
	    msg.setThumbImage(thumb);  
	      
	    SendMessageToWX.Req req = new SendMessageToWX.Req();  
	    req.transaction = String.valueOf(System.currentTimeMillis());  
	    req.message = msg;  
	    req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;  
	    wxApi.sendReq(req);  
	}  
	
}