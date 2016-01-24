package com.puzzleworld.onecolor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_preview);

		wxApi = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

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

				System.out.println("第三个按钮被点击了");
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

}