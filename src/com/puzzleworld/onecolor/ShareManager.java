package com.puzzleworld.onecolor;

import java.text.SimpleDateFormat;

import com.puzzleworld.onecolor.wbapi.AccessTokenKeeper;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ShareManager {
	private IWXAPI wxApi;
	private Bitmap ShareBitmap;
	private Activity mActivity;

	/** 显示认证后的信息，如 AccessToken */
	private TextView mTokenText;
	/** 微博 Web 授权类，提供登陆等功能 */
	private WeiboAuth mWeiboAuth;
	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessToken mAccessToken;
	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
	private SsoHandler mSsoHandler;

	/** 微博微博分享接口实例 */
	private IWeiboShareAPI mWeiboShareAPI = null;

	private final int THUMB_SIZE = 200;

	public ShareManager(Activity activity) {
		mActivity = activity;
		// Create WeChat Instantiation
		wxApi = WXAPIFactory.createWXAPI(activity, Constants.APP_ID);
		// Create WeiBo Instantiation
		mWeiboAuth = new WeiboAuth(activity, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
		// 创建微博分享接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, Constants.APP_KEY);

		// 如果未安装微博客户端，设置下载微博对应的回调
		if (!mWeiboShareAPI.isWeiboAppInstalled()) {
			mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
				@Override
				public void onCancel() {
					Toast.makeText(ShareManager.this.mActivity, R.string.weibosdk_demo_cancel_download_weibo,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	public void shareToWechat() {
		wxApi.registerApp(Constants.APP_ID);
		wechatShare(0);// 分享到微信好友
	}

	public void shareToWechatFriends() {
		wxApi.registerApp(Constants.APP_ID);
		wechatShare(1);// 分享到微信朋友圈
	}

	public void shareToWeibo() {
		ShareBitmap = BitmapStore.getFinalProcessedBitmap();
		if (ShareBitmap == null) {
			Log.i("ShareManager", "no bitmap to be shared in shareToWeibo");
			return;
		}

		mAccessToken = AccessTokenKeeper.readAccessToken(mActivity);

		if (mAccessToken.isSessionValid()) {
			mWeiboShareAPI.registerApp();

			// TODO发微博
			Bitmap bmp, WaterMarkbmp;
			WaterMarkbmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.watermark_small_70);
			// 加水印
			bmp = addWaterMark(ShareBitmap, WaterMarkbmp);

			reqMsg(bmp);
		} else {
			/** 不使用SSO方式进行授权验证 */
			// mWeibo.anthorize(AppMain.this, new AuthDialogListener());

			/** 使用SSO方式进行授权验证 */
			mSsoHandler = new SsoHandler(mActivity, mWeiboAuth);
			mSsoHandler.authorize(new AuthListener());
		}
	}

	/**
	 * 微信分享 （这里仅提供一个分享本地图片的示例，其它请参看官网示例代码）
	 *
	 * @param flag(0:分享到微信好友，1：分享到微信朋友圈)
	 */
	private void wechatShare(int flag) {
		Bitmap bmp, WaterMarkbmp;

		ShareBitmap = BitmapStore.getFinalProcessedBitmap();
		if (ShareBitmap == null) {
			Log.i("ShareManager", "no bitmap to be shared in wechatShare");
			return;
		}

		WaterMarkbmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.watermark_ms);

		// 加水印
		bmp = addWaterMark(ShareBitmap, WaterMarkbmp);
		Log.d("chz", "[" + Thread.currentThread().getStackTrace()[2].getFileName() + ","
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");

		// 确保发送给微信图片大小<=32K
		Log.d("kevin", "before bmp Byte Count = " + bmp.getByteCount() + "Bytes");
		/*
		 * int i = 0; while ((2320*1024) <= bmp.getByteCount()) { Log.d("kevin",
		 * "图像压缩处理" + i + " 次。"); ++i; bmp = createBitmapThumbnail(bmp); }
		 */
		bmp = createBitmapThumbnail(bmp);
		// shareCompBitmap = compressImage(bmp);
		Log.d("kevin", "after bmp Byte Count = " + bmp.getByteCount() + "Bytes");

		// 初始化WXImageObject和WXMediaMessage对象
		WXImageObject imgObj = new WXImageObject(bmp);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		Log.i("chz", "shareWechat:" + flag);
		// 设置缩略图
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
		bmp.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		// 初始化WXImageObject和WXMedia
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
		wxApi.sendReq(req);
	}

	// 加水印
	/**
	 * create the bitmap from a byte array
	 *
	 * @param src
	 *            the bitmap object you want proecss @param watermark the water
	 *            mark above the src @return return a bitmap object ,if
	 *            paramter's length is 0,return null
	 */
	private Bitmap addWaterMark(Bitmap src, Bitmap watermark) {
		String tag = "createBitmap";
		Log.d(tag, "kevin add watermark");

		if (src == null) {
			Log.d("chz", "[" + Thread.currentThread().getStackTrace()[2].getFileName() + ","
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "]");
			return null;
		}

		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();

		Log.d(tag, "kevin src w = " + w + ", h = " + h + ", ww = " + ww + ", wh = " + wh);

		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);

		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src

		// draw watermark into
		int dw, dh;
		dw = w - ww;
		dh = h - wh;
		Log.d(tag, "kevin draw watermark,  w = " + dw + ", h = " + dh);
		cv.drawBitmap(watermark, dw, dh, null);// 在src的右下角画入水印

		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存

		// store
		cv.restore();

		return newb;
	}

	// 图像压缩-压缩分辨率法-损失部分细节
	public Bitmap createBitmapThumbnail(Bitmap bitMap) {
		int width = bitMap.getWidth();
		int height = bitMap.getHeight();
		// 设置想要的大小
		int newWidth = 480;
		int newHeight = 480;

		Log.d("kevin", "kevin createBitmapThumbnail,  w = " + width + ", h = " + height);
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		float scaleRatio = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleRatio, scaleRatio);
		// 得到新的图片
		Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, true);
		return newBitMap;
	}

	/**
	 * 向weibo 客户端注册发送一个携带：文字、图片等数据
	 *
	 * @param bitmap
	 */
	public void reqMsg(Bitmap bitmap) {

		/* 图片对象 */
		ImageObject imageobj = new ImageObject();

		if (bitmap != null) {
			imageobj.setImageObject(bitmap);
		}

		/* 微博数据的message对象 */
		WeiboMultiMessage multmess = new WeiboMultiMessage();
		TextObject textobj = new TextObject();
		textobj.text = "异彩你生活！";

		multmess.textObject = textobj;
		multmess.imageObject = imageobj;
		/* 微博发送的Request请求 */
		SendMultiMessageToWeiboRequest multRequest = new SendMultiMessageToWeiboRequest();
		multRequest.multiMessage = multmess;
		// 以当前时间戳为唯一识别符
		multRequest.transaction = String.valueOf(System.currentTimeMillis());
		mWeiboShareAPI.sendRequest(multRequest);
	}

	/**
	 * 微博认证授权回调类。 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用
	 * {@link SsoHandler#authorizeCallBack} 后， 该回调才会被执行。 2. 非 SSO
	 * 授权时，当授权结束后，该回调就会被执行。 当授权成功后，请保存该 access_token、expires_in、uid 等信息到
	 * SharedPreferences 中。
	 */
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			// 从 Bundle 中解析 Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				// 显示 Token
				// updateTokenView(false);//delete 2016-03-18 by kevin for fixed
				// weibo share crash issue.

				// 保存 Token 到 SharedPreferences
				AccessTokenKeeper.writeAccessToken(mActivity, mAccessToken);
				Toast.makeText(mActivity, R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
			} else {
				// 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
				String code = values.getString("code");
				String message = mActivity.getString(R.string.weibosdk_demo_toast_auth_failed);
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onCancel() {
			Toast.makeText(mActivity, R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(mActivity, "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 *
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			Toast.makeText(mActivity, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			Toast.makeText(mActivity, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(mActivity, mActivity.getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: "
					+ baseResp.errMsg, Toast.LENGTH_LONG).show();
			break;
		}
	}

	/**
	 * 显示当前 Token 信息。
	 *
	 * @param hasExisted
	 *            配置文件中是否已存在 token 信息并且合法
	 */
	private void updateTokenView(boolean hasExisted) {
		String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new java.util.Date(mAccessToken.getExpiresTime()));
		String format = mActivity.getString(R.string.weibosdk_demo_token_to_string_format_1);
		mTokenText.setText(String.format(format, mAccessToken.getToken(), date));

		String message = String.format(format, mAccessToken.getToken(), date);
		if (hasExisted) {
			message = mActivity.getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
		}
		mTokenText.setText(message);
	}
}
