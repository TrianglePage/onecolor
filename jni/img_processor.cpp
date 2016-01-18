#include<jni.h>
#include<android/log.h>
#include<opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/legacy/legacy.hpp>
#include "stdlib.h"
#include "stdio.h"

#define TAG    "jni_opencv"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

using namespace cv;

extern "C" {
JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ProcessActivity_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h, int value);
JNIEXPORT jintArray JNICALL Java_com_puzzleworld_onecolor_ProcessActivity_ImgFun(
		JNIEnv* env, jobject obj, jintArray buf, int w, int h, int value) {
	jint *cbuf;
	cbuf = env->GetIntArrayElements(buf, NULL);
	if (cbuf == NULL) {
		return 0;
	}
	LOGD("kevin jni value = %d", value);

	Mat imgData(h, w, CV_8UC4, (unsigned char*) cbuf);
	int flags = 4 + (255 << 8) + (CV_FLOODFILL_FIXED_RANGE);
	IplImage src_data;
	src_data = IplImage(imgData);

	IplImage *src = cvCloneImage(&src_data);

	IplImage* dst = cvCreateImage(cvGetSize(src), src->depth, 3);
	IplImage* show = cvCreateImage(cvGetSize(src), src->depth, src->nChannels);
	IplImage* hsv = cvCreateImage(cvGetSize(src), src->depth, 3);
	CvSize size_src = cvGetSize(src);
	IplImage* maskImage = cvCreateImage(
			cvSize(size_src.width + 2, size_src.height + 2), src->depth,
			1);
	IplImage* mask = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* mask_inv = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray_dst = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* gray_mask = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* r = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* g = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* b = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);

	IplImage* h_plane = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* s = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);
	IplImage* v = cvCreateImage(cvSize(size_src.width, size_src.height),
			src->depth, 1);



	cvZero(dst);
	cvZero(maskImage);
	cvZero(mask);

	CvSeq* comp = NULL;
	int level = 1; //进行n层采样
	double threshold1 = 120;
	double threshold2 = 50; //
	int comp_count = 0;
	CvMemStorage* storage = cvCreateMemStorage(0);
	CvSeq* contours = 0;

	cvCvtColor(src, gray, CV_RGB2GRAY);

	cvCvtColor(src, hsv, CV_RGB2HSV);

	cvSplit(hsv, h_plane, s, v, 0);

	cvPyrSegmentation(v, gray_dst, storage, &contours, level, threshold1,
			threshold2);
	cvZero(maskImage);
	cvZero(show);
	cvZero(gray_mask);
	Mat mat_dst(gray_dst, 0);
	Mat mat_mask(maskImage, 0);
	Mat mat_r(r, 0);
	Mat mat_g(g, 0);
	Mat mat_b(b, 0);
	Mat mat_gray_inv(gray_mask, 0);
	Mat mat_show(show, 0);

	Rect ccomp;

	cvThreshold(maskImage, maskImage, 1, 128, CV_THRESH_BINARY);

	int area = floodFill(mat_dst, mat_mask, cvPoint(w>>1,h>>1), 1, &ccomp,
			Scalar(20, 20, 20), Scalar(20, 20, 20), flags);

	cvSetImageROI(maskImage, cvRect(1, 1, size_src.width, size_src.height));

	cvFindContours(maskImage, storage, &contours, sizeof(CvContour),
			CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE); //CV_RETR_CCOMP,
	cvZero(maskImage);

	for (int i = 0; contours != 0; contours = contours->h_next) {
		cvDrawContours(maskImage, contours, cvScalar(255), cvScalar(255), 0,
				CV_FILLED, 8);
	}

	cvCopy(src, show, maskImage);

	cvThreshold(maskImage, mask_inv, 1, 128, CV_THRESH_BINARY_INV);

	cvCopy(gray, gray_mask, mask_inv);
	//cvShowImage("mask_inv",gray_mask);

	cvResetImageROI(maskImage);

	cvSplit(show, r, g, b, 0);
	mat_r = mat_gray_inv + r;
	mat_g = mat_gray_inv + g;
	mat_b = mat_gray_inv + b;

    cvMerge(r,g,b,0,show);

	uchar* ptr = imgData.ptr(0);
	int step = show->widthStep/sizeof(uchar);
	int channels = show->nChannels;
	for (int i = 0; i < h; i++)
	{
		for(int j = 0; j < w ; j++)
		{
//		int grayScale = (int) (ptr[4 * i + 2] * 0.299 + ptr[4 * i + 1] * 0.587
//				+ ptr[4 * i + 0] * 0.114);

//		memcpy((uchar *)src->imageData[i*step+j*src->nChannels+0],&ptr[4 * (i*w+j) + 0],sizeof(uchar));
		ptr[4 * (i*w+j) + 0] = cvGet2D(show,i,j).val[0];
		ptr[4 * (i*w+j) + 1] = cvGet2D(show,i,j).val[1];
		ptr[4 * (i*w+j) + 2] = cvGet2D(show,i,j).val[2];


		}
	}

	int size = w * h;
	jintArray result = env->NewIntArray(size);
	env->SetIntArrayRegion(result, 0, size, cbuf);
	env->ReleaseIntArrayElements(buf, cbuf, 0);

	cvReleaseImage(&src);
	cvReleaseImage(&dst);
	cvReleaseImage(&show);
	cvReleaseImage(&hsv);
	cvReleaseImage(&maskImage);
	cvReleaseImage(&mask);
	cvReleaseImage(&mask_inv);
	cvReleaseImage(&gray);
	cvReleaseImage(&gray_dst);
	cvReleaseImage(&gray_mask);
	cvReleaseImage(&r);
	cvReleaseImage(&g);
	cvReleaseImage(&b);
	cvReleaseImage(&h_plane);
	cvReleaseImage(&s);
	cvReleaseImage(&v);


	return result;
}
}




