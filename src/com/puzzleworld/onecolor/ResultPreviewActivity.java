package com.puzzleworld.onecolor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;

/*
 * 处理后图片确认界面，未实现
 */
public class ResultPreviewActivity extends Activity {

	private ImageView ivPreview;
	private ImageButton btnSave;
	private ImageButton btnShare;
	private Bitmap previewBitmap;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_preview);

		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		btnSave = (ImageButton) findViewById(R.id.btnSave);
		btnShare=(ImageButton) findViewById(R.id.btnShare);

		previewBitmap = BitmapStore.getBitmap();
		ivPreview.setImageBitmap(previewBitmap);
		mContext = this;

		btnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveImageToGallery(mContext, previewBitmap);
			}
		});
		
		btnShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Intent intent_share = new Intent();

				intent_share.setClass(ResultPreviewActivity.this, ShareActivity.class);
				ResultPreviewActivity.this.startActivity(intent_share);

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
		//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(savePath)));
		MediaScannerConnection.scanFile(context, new String[]{savePath}, null, null);
		Toast.makeText(context, "图片保存至："+savePath, Toast.LENGTH_LONG).show();
	}
}