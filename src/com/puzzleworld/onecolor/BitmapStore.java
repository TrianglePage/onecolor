package com.puzzleworld.onecolor;

import android.graphics.Bitmap;


public class BitmapStore {
	static private Bitmap bmp = null;// 一定要是static的才行..

	static void setBitmap(Bitmap bmp) {
		BitmapStore.bmp = bmp;
	}

	static Bitmap getBitmap() {
		return BitmapStore.bmp;
	}
}
