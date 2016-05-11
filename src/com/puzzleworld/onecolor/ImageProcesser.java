package com.puzzleworld.onecolor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class ImageProcesser {
	private static final int MAX_LEVEL = 100;
	private static final int MAX_POINTS_COUNT = 10;

	private static ImageProcesser instance = null;
	private int mLevel = 0;
	private int mBgColor = 0;
	private int mIsBlur = 0;
	private int[] mTouchPoints;
	private int mTouchPointsCount;

	private native int[] ImgFun(int[] buf, int w, int h, int[] touchPoints, int touchPointsCount, int value,
			int bgColor, int bgBlur);

	private ImageProcesser() {
		System.loadLibrary("img_processor");
		mLevel = mBgColor = mIsBlur = mTouchPointsCount = 0;
		mTouchPoints = new int[20];
	}

	public static ImageProcesser getInstance() {
		if (instance == null) {
			instance = new ImageProcesser();
		}
		return instance;
	}

	public int setLevel(int level) {
		if (level > MAX_LEVEL) {
			return -1;
		}
		mLevel = level;
		return 0;
	}

	public void setBgColor(int color) {
		mBgColor = color;
	}

	public void setBlur(int isBlur) {
		mIsBlur = isBlur;
	}

	public int setTouchPoints(int[] points, int count) {
		if (count > MAX_POINTS_COUNT) {
			return -1;
		}
		System.arraycopy(points, 0, mTouchPoints, 0, count * 2);
		mTouchPointsCount = count;
		return 0;
	}

	public int addTouchPoint(int x, int y) {
		if (mTouchPointsCount == MAX_POINTS_COUNT) {
			return -1;
		}

		mTouchPoints[2 * mTouchPointsCount] = x;
		mTouchPoints[2 * mTouchPointsCount + 1] = y;
		mTouchPointsCount++;
		return 0;
	}

	public int delTouchPoint() {
		if (mTouchPointsCount == 0) {
			return -1;
		}

		mTouchPointsCount--;
		return 0;
	}

	public Bitmap processImage(Bitmap img) {
		if (mTouchPointsCount == 0) {
			return img;
		}

		int w = img.getWidth(), h = img.getHeight();
		// 获取bitmap像素颜色值存入pix数组，后面传入算法
		int[] pix = new int[w * h];
		img.getPixels(pix, 0, w, 0, 0, w, h);
		int[] resultInt = ImgFun(pix, w, h, mTouchPoints, mTouchPointsCount, mLevel, mBgColor, mIsBlur);
		Bitmap resultImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		resultImg.setPixels(resultInt, 0, w, 0, 0, w, h);
		return resultImg;
	}
}
