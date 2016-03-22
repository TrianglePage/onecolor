package com.puzzleworld.onecolor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("FloatMath")
public class ScaleImageView extends ImageView {

	static {
		System.loadLibrary("img_processor");
	}

	public native int[] ImgFun(int[] buf, int w, int h, int touchX, int touchY, int value);

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private boolean processed = false;
	private boolean misForProcessPic = false;
	private float mTouchX = 0;
	private float mTouchY = 0;
	private int mLevel = 1;

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	/*
	 * isForProcessPic表示设置的图片是否用于处理，第一次显示的是添加图片，不用于处理
	 * 注意不能再oncreate函数中调用，因为函数内部获取控件宽高会变成0
	 */
	public void setImageBitmapEx(Bitmap bm, boolean isForProcessPic) {
		misForProcessPic = isForProcessPic;

		float scaleX;
		float scaleY;
		float scale;

		super.setImageBitmap(bm);
		// setImageMatrix(matrix);
		matrix.reset();
		float[] values = new float[9];
		matrix.getValues(values);
		float offsetX = values[2];
		float offsetY = values[5];

		if (isForProcessPic) {
			if (!processed) {
				// 不是处理后的图片，是重新选择的，在此做缩放处理以适合控件
				scaleX = (float) this.getWidth() / (float) bm.getWidth();
				scaleY = (float) this.getHeight() / (float) bm.getHeight();
				// scale = scaleX < scaleY ? scaleX : scaleY;
				if (scaleX < scaleY) {
					scale = scaleX;
					offsetY = (this.getHeight() - bm.getHeight() * scale) / 2;
				} else {
					scale = scaleY;
					offsetX = (this.getWidth() - bm.getWidth() * scale) / 2;
				}
				matrix.postTranslate(offsetX, offsetY);
//				Log.i("chz", "init scale = " + scale + ",w = " + this.getWidth() + ",h = " + this.getHeight() + ",bw ="
//						+ bm.getWidth() + ",bh=" + bm.getHeight() + "offX=" + offsetX + "offy=" + offsetY);
				matrix.postScale(scale, scale, 0, 0);
			}
		} else {
//			Log.i("chz", "w = " + this.getWidth() + ",h = " + this.getHeight() + ",bw =" + bm.getWidth() + ",bh="
//					+ bm.getHeight());
			offsetX = (this.getWidth() - bm.getWidth()) / 2;
			offsetY = (this.getHeight() - bm.getHeight()) / 2;
			matrix.postTranslate(offsetX, offsetY);
		}

		setImageMatrix(matrix);
	}

	public ScaleImageView(Context context) {
		super(context);
	}

	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	// public boolean onTouchEvent(MotionEvent event) {
	// performClick();//显式调用这个函数，才会调到注册的onClick函数。
	// return true;
	// }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.i("chz", "eventid=" + event.getActionMasked() + ",actionDown=" + MotionEvent.ACTION_DOWN);
//		if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
//			Log.d("Infor", "多点操作");
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			matrix.set(getImageMatrix());
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d("Infor", "触摸了...");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: // 多点触控
			oldDist = this.spacing(event);
			if (oldDist > 10f) {
//				Log.d("Infor", "oldDist" + oldDist);
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) { // 此实现图片的拖动功能...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM) {// 此实现图片的缩放功能...
				float newDist = spacing(event);
				if (newDist > 10) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		case MotionEvent.ACTION_UP:// 这里计算坐标，目前还有问题
//			Log.i("chz",
//					"startx=" + start.x + "eventx=" + event.getX() + "starty=" + start.y + "eventy=" + event.getY());
			if (start.x == event.getX() && start.y == event.getY()) {
				float[] values = new float[9];
				// 图片可能被缩放和移动，获取图片移动偏移和缩放比例
				matrix.getValues(values);
				float offsetX = values[2];
				float offsetY = values[5];
				float scaleX = values[0];
				float scaleY = values[4];

				// 计算点击坐标相对原始图片的实际坐标
				float actualX = (event.getX() - offsetX) / scaleX;
				float actualY = (event.getY() - offsetY) / scaleY;
					mTouchX = actualX;
					mTouchY = actualY;
					processPicture();
			}
		}
		setImageMatrix(matrix);
		if (misForProcessPic) {
			return true;
		} else {
			performClick();// 显式调用这个函数，才会调到注册的onClick函数。
			return false;
		}
	}

	public void setLevel(int level) {
		if (level > 0 && level < 256) {
			mLevel = level;
		}
	}

	public void processPicture() {
		//Bitmap showBitmap = ((BitmapDrawable) this.getDrawable()).getBitmap();
		Bitmap showBitmap = BitmapStore.getBitmapOriginal();
		int w = showBitmap.getWidth(), h = showBitmap.getHeight();
		// 获取bitmap像素颜色值存入pix数组，后面传入算法
		int[] pix = new int[w * h];
		showBitmap.getPixels(pix, 0, w, 0, 0, w, h);
		Log.i("chz", "img w=" + this.getWidth() + ", h=" + this.getHeight() + ", bitmap w=" + showBitmap.getWidth()
				+ ",h=" + showBitmap.getHeight());

		if ((int) mTouchX > 0 && (int) mTouchY > 0 && (int) mTouchX < w && (int) mTouchY < h) {
			int[] resultInt = ImgFun(pix, w, h, (int) mTouchX, (int) mTouchY, mLevel);// TODO
			Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
			processed = true;
			this.setImageBitmap(resultImg);
			processed = false;
		}
	}
}