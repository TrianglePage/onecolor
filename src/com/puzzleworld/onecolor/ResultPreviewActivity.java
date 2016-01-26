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
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;

import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

/*
 * 处理后图片确认界面
 */
public class ResultPreviewActivity extends Activity {

	private ImageView ivPreview;
	private ImageButton btnSave;
	private ImageButton btnShare;
	private Bitmap previewBitmap;
	private Bitmap ShareBitmap;
	private Context mContext;
	private IWXAPI wxApi;
	private final int THUMB_SIZE = 200;

	/** 显示认证后的信息，如 AccessToken */
    private TextView mTokenText;
	/** 微博 Web 授权类，提供登陆等功能  */
    private WeiboAuth mWeiboAuth;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

    /** 微博微博分享接口实例 */
    private IWeiboShareAPI  mWeiboShareAPI = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_preview);

		//Create WeChat Instantiation
		wxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

        // Create WeiBo Instantiation
        mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
            mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
                @Override
                public void onCancel() {
                    Toast.makeText(ResultPreviewActivity.this,
                            R.string.weibosdk_demo_cancel_download_weibo,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		btnSave = (ImageButton) findViewById(R.id.btnSave);
		btnShare = (ImageButton) findViewById(R.id.btnShare);

		previewBitmap = BitmapStore.getBitmap();
		ivPreview.setImageBitmap(previewBitmap);
		mContext = this;

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("chz", "save to gallery");
				saveImageToGallery(mContext, previewBitmap);
			}
		});

		btnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("chz", "pop menu");
				showPopwindow();
			}
		});
	}

	public static void saveImageToGallery(Context context, Bitmap bmp) {
		// 首先保存图片
		String pathname = "OneColor";
		File appDir = new File(Environment.getExternalStorageDirectory(), pathname);
		if (!appDir.exists()) {
			appDir.mkdir();
		}

		String savePath = "file:/" + Environment.getExternalStorageDirectory() + "/" + pathname;

		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 其次把文件插入到系统图库
		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 最后通知图库更新
		// context.sendBroadcast(new
		// Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(savePath)));
		MediaScannerConnection.scanFile(context, new String[] { savePath }, null, null);
		Toast.makeText(context, "图片保存至：" + savePath, Toast.LENGTH_LONG).show();
	}

	/**
	 * 显示popupWindow
	 */
	private void showPopwindow() {
		// 利用layoutInflater获得View
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_share, null);

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
		window.showAtLocation(ResultPreviewActivity.this.findViewById(R.id.ivPreview), Gravity.BOTTOM, 0, 0);

		// 这里检验popWindow里的button是否可以点击
		ImageButton btnWeChat = (ImageButton) view.findViewById(R.id.btnWeChat);
		btnWeChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wxApi.registerApp(Constants.APP_ID);
				wechatShare(0);// 分享到微信好友
			}
		});

		ImageButton btnFriend = (ImageButton) view.findViewById(R.id.btnFriend);
		btnFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wxApi.registerApp(Constants.APP_ID);
				wechatShare(1);// 分享到微信朋友圈
			}
		});

		ImageButton btnWeibo = (ImageButton) view.findViewById(R.id.btnWeibo);
		btnWeibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAccessToken = AccessTokenKeeper.readAccessToken(ResultPreviewActivity.this);

				if (mAccessToken.isSessionValid()) {
					mWeiboShareAPI.registerApp();

					// TODO发微博
					Bitmap bmp, WaterMarkbmp;
					ShareBitmap = BitmapStore.getBitmap();
					WaterMarkbmp = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_small_70);
					// 加水印
					bmp = createBitmap(ShareBitmap, WaterMarkbmp);

					reqMsg(bmp);
				} else {
					/** 不使用SSO方式进行授权验证 */
					// mWeibo.anthorize(AppMain.this, new AuthDialogListener());

					/** 使用SSO方式进行授权验证 */
					mSsoHandler = new SsoHandler(ResultPreviewActivity.this, mWeiboAuth);
					mSsoHandler.authorize(new AuthListener());
				}

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
	 * 向weibo 客户端注册发送一个携带：文字、图片等数据
	 *
	 * @param bitmap
	 */
	public void reqMsg(Bitmap bitmap) {

		/*图片对象*/
		ImageObject imageobj = new ImageObject();

		if (bitmap != null) {
			imageobj.setImageObject(bitmap);
		}

		/*微博数据的message对象*/
		WeiboMultiMessage multmess = new WeiboMultiMessage();
		TextObject textobj = new TextObject();
		textobj.text = "异彩你生活！";

		multmess.textObject = textobj;
		multmess.imageObject = imageobj;
		/*微博发送的Request请求*/
		SendMultiMessageToWeiboRequest multRequest = new SendMultiMessageToWeiboRequest();
		multRequest.multiMessage = multmess;
		//以当前时间戳为唯一识别符
		multRequest.transaction = String.valueOf(System.currentTimeMillis());
		mWeiboShareAPI.sendRequest(multRequest);
	}

	/**
	 * 微信分享 （这里仅提供一个分享本地图片的示例，其它请参看官网示例代码）
	 * 
	 * @param flag(0:分享到微信好友，1：分享到微信朋友圈)
	 */
	private void wechatShare(int flag) {
		Bitmap bmp, WaterMarkbmp;

		ShareBitmap = BitmapStore.getBitmap();
		WaterMarkbmp = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_small_70);
		// 加水印
		bmp = createBitmap(ShareBitmap, WaterMarkbmp);

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
	private Bitmap createBitmap(Bitmap src, Bitmap watermark) {
		String tag = "createBitmap";
		Log.d(tag, "kevin add watermark");

		if (src == null) {
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

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
                updateTokenView(false);

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(ResultPreviewActivity.this, mAccessToken);
                Toast.makeText(ResultPreviewActivity.this,
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(ResultPreviewActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(ResultPreviewActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(ResultPreviewActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

    /**
     * @see {@link Activity#onNewIntent}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        //mWeiboShareAPI.handleWeiboResponse(intent, ResultPreviewActivity.this);
    }
    
    /**
     * 接收微客户端博请求的数据。
     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
     *
     * @param baseRequest 微博请求数据对象
     * @see {@link IWeiboShareAPI#handleWeiboRequest}
     */
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
        case WBConstants.ErrorCode.ERR_OK:
            Toast.makeText(this, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_CANCEL:
            Toast.makeText(this, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_FAIL:
            Toast.makeText(this,
                    getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: " + baseResp.errMsg,
                    Toast.LENGTH_LONG).show();
            break;
        }
    }


    /**
     * 显示当前 Token 信息。
     *
     * @param hasExisted 配置文件中是否已存在 token 信息并且合法
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
        mTokenText.setText(String.format(format, mAccessToken.getToken(), date));

        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
        }
        mTokenText.setText(message);
    }

}