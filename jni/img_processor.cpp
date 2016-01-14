#include<jni.h>
#include<opencv2/opencv.hpp>

using namespace cv;

extern "C" {
JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ProcessActivity_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h);
JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ProcessActivity_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h) {
	jint *cbuf;
	cbuf = env->GetIntArrayElements(buf, NULL);
	if (cbuf == NULL) {
		return 0;
	}

	Mat imgData(h, w, CV_8UC4, (unsigned char*) cbuf);

	uchar* ptr = imgData.ptr(0);
	for (int i = 0; i < w * h; i++) {
		int grayScale = (int) (ptr[4 * i + 2] * 0.299 + ptr[4 * i + 1] * 0.587
				+ ptr[4 * i + 0] * 0.114);
		ptr[4 * i + 1] = grayScale;
		ptr[4 * i + 2] = grayScale;
		ptr[4 * i + 0] = grayScale;
	}

	int size = w * h;
	jintArray result = env->NewIntArray(size);
	env->SetIntArrayRegion(result, 0, size, cbuf);
	env->ReleaseIntArrayElements(buf, cbuf, 0);
	return result;
}
}
