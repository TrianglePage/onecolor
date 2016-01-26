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

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		setImageMatrix(matrix);
		if (!processed) {
			// 不是处理后的图片，是重新选择的，在此做缩放处理以适合控件
			matrix.reset();
			float[] values = new float[9];
			matrix.getValues(values);
			float offsetX = values[2];
			float offsetY = values[5];
			float scaleX = (float) this.getWidth() / (float) bm.getWidth();
			float scaleY = (float) this.getHeight() / (float) bm.getHeight();
			float scale = scaleX < scaleY ? scaleX : scaleY;

			matrix.postTranslate(-offsetX, -offsetY);
			// Log.i("chz", "init scale = "+scale+",w = " + this.getWidth() +",h
			// = " +this.getHeight()+ ",bw =
			// "+bm.getWidth()+",bh="+bm.getHeight());
			matrix.postScale(scale, scale, 0, 0);

			setImageMatrix(matrix);
		}
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
			Log.d("Infor", "多点操作");
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
				Log.d("Infor", "oldDist" + oldDist);
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
			if (start.x == event.getX() && start.y == event.getY()) {
				float[] values = new float[9];
				matrix.getValues(values);
				float offsetX = values[2];
				float offsetY = values[5];
				float scaleX = values[0];
				float scaleY = values[4];

				float actualX = (event.getX() - offsetX) / scaleX;
				float actualY = (event.getY() - offsetY) / scaleY;
				Log.i("chz", "actualX = " + actualX + ",actualY = " + actualY);

				Log.i("chz",
						"offx = " + offsetX + ",offy = " + offsetY + ",scaleX = " + scaleX + ",scaleY = " + scaleY);
				// ivProcess.setDrawingCacheEnabled(true);
				// Bitmap img1 =
				// Bitmap.createBitmap(ivProcess.getDrawingCache());
				// ivProcess.setDrawingCacheEnabled(false);
				Bitmap showBitmap = ((BitmapDrawable) this.getDrawable()).getBitmap();
				int w = showBitmap.getWidth(), h = showBitmap.getHeight();
				int[] pix = new int[w * h];
				showBitmap.getPixels(pix, 0, w, 0, 0, w, h);
				Log.i("chz", "img w=" + this.getWidth() + ", h=" + this.getHeight() + ", bitmap w="
						+ showBitmap.getWidth() + ",h=" + showBitmap.getHeight());
				// touchX和touchY是相对imageView控件的，而内部的bitmap宽高与imageView是不同的
				// 这里换算成相对图片的坐标tx，ty
				// int tx = (int)
				// (start.x/this.getWidth()*showBitmap.getWidth());
				// int ty = (int)
				// (start.y/this.getHeight()*showBitmap.getHeight());
				// Log.i("chz", "img
				// x="+this.getX()+",y="+this.getY()+",touchX="+start.x+",touchY="+start.y+",tx="+tx+",ty="+ty);
				if (actualX > 0 && actualY > 0 && actualX < w && actualY < h) {
					int[] resultInt = ImgFun(pix, w, h, (int) actualX, (int) actualY, 1);// TODO
					Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
					resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
					processed = true;
					this.setImageBitmap(resultImg);
					processed = false;
				}
			}
		}
		setImageMatrix(matrix);
		return true;
	}
}