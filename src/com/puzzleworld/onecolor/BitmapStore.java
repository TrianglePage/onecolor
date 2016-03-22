package com.puzzleworld.onecolor;

import android.graphics.Bitmap;


public class BitmapStore {
	static private Bitmap bmpOriginal = null;// 一定要是static的才行..
	static private Bitmap bmpProcessed = null;

	static void setBitmapOriginal(Bitmap bmp) {
		BitmapStore.bmpOriginal = bmp;
	}
	
	static void setBitmapProcessed(Bitmap bmp) {
		BitmapStore.bmpProcessed = bmp;
	}

	static Bitmap getBitmapOriginal() {
		return BitmapStore.bmpOriginal;
	}
	
	static Bitmap getBitmapProcessed() {
		return BitmapStore.bmpProcessed;
	}
}
