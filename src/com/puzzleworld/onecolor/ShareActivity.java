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

	private ImageButton btnWechatShare;
	private IWXAPI wxApi;
	private static final String APP_ID = "wxcee085215ede750a";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		wxApi = WXAPIFactory.createWXAPI(this, APP_ID);  
		btnWechatShare = (ImageButton) findViewById(R.id.btnWechatShare);
		btnWechatShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				wxApi.registerApp(APP_ID);  
				wechatShare(0);//分享到微信好友  
				//wechatShare(1);//分享到微信朋友圈
			}
		});
	}

	/** 
	 * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码） 
	 * @param flag(0:分享到微信好友，1：分享到微信朋友圈) 
	 */  
	private void wechatShare(int flag){  
	    WXWebpageObject webpage = new WXWebpageObject();  
	    webpage.webpageUrl = "www.baidu.com";  
	    WXMediaMessage msg = new WXMediaMessage(webpage);  
	    msg.title = "onecolor_puzzorld";  
	    msg.description = "an app by puzzorld";  
	    //这里替换一张自己工程里的图片资源  
	    Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.sharewithwechat);  
	    msg.setThumbImage(thumb);  
	      
	    SendMessageToWX.Req req = new SendMessageToWX.Req();  
	    req.transaction = String.valueOf(System.currentTimeMillis());  
	    req.message = msg;  
	    req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;  
	    wxApi.sendReq(req);  
	}  
	
}