package com.beyond.net;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class PeopleDetection {
	
	/**
	 * 人脸检测(正脸和侧脸)
	 * @param args
	 * @return
	 */
	public static Mat facesDetect(Mat image) {
		CascadeClassifier faceDetector = new CascadeClassifier();
		faceDetector.load("E:\\tools\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");

		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		
		for (Rect rect : faceDetections.toArray()) {
			saveImage(image,rect,"D:\\opencv\\bbb"+rect.x+".jpg");
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),new Scalar(0, 255, 0));
		}
		
		if(faceDetections.empty()) {
			faceDetector.load("E:\\tools\\opencv\\sources\\data\\haarcascades\\haarcascade_profileface.xml");
			faceDetector.detectMultiScale(image, faceDetections);
			
			for (Rect rect : faceDetections.toArray()) {
				saveImage(image,rect,"D:\\opencv\\bbb"+rect.x+".jpg");
				Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),new Scalar(0, 255, 0));
			}
		}

		return image;
	}
	
	/**
	 * 人眼检测，包括戴眼镜和不戴眼镜两种情况
	 * @param image
	 * @return
	 */
	public static Mat eyeDetect(Mat image) {
		MatOfRect eyeDetections = new MatOfRect();
		CascadeClassifier EyeDetector = new CascadeClassifier();
		
		EyeDetector.load("E:\\tools\\opencv\\sources\\data\\haarcascades\\haarcascade_eye.xml");
    	EyeDetector.detectMultiScale(image, eyeDetections);
		// 检测出人眼，一人眼中心为圆点画圆圈
		Scalar eyeColor = new Scalar(0, 255, 255);

		for (Rect rect : eyeDetections.toArray()) {
			Imgproc.circle(image, new Point(rect.x+rect.width/2, rect.y+rect.height/2),rect.width/2, eyeColor);
		}
		
		if(eyeDetections.empty()) {
			EyeDetector.load("E:\\tools\\opencv\\sources\\data\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml");
			EyeDetector.detectMultiScale(image, eyeDetections);
			
			for (Rect rect : eyeDetections.toArray()) {
				Imgproc.circle(image, new Point(rect.x+rect.width/2, rect.y+rect.height/2),rect.width/2, eyeColor);
			}
		}
		
		return image;
	}
	
	/**
	 * @description 比对两张人脸个的相似度
	 * @param image1
	 * @param image2
	 * @param isGray 是否采用灰度化方式
	 * @return
	 */
	public static double facesCompare(Mat image1, Mat image2, boolean isGray) {
		List<Mat> images = new ArrayList<Mat>();
		List<Mat> images2 = new ArrayList<Mat>();
		if (isGray) {
			Mat gray1 = new Mat(image1.size(), CvType.CV_8UC1);
			Imgproc.cvtColor(image1, gray1, Imgproc.COLOR_RGB2GRAY);
			Mat gray2 = new Mat(image2.size(), CvType.CV_8UC1);
			Imgproc.cvtColor(image2, gray2, Imgproc.COLOR_RGB2GRAY);
			images.add(gray1);
			images2.add(gray2);
		} else {
			images.add(image1);
			images2.add(image2);
		}
		MatOfInt channels = new MatOfInt(0);
		MatOfInt histSize = new MatOfInt(256);
		MatOfFloat ranges = new MatOfFloat(0, 256);
		Mat hist = new Mat();
		Mat hist2 = new Mat();
		// 计算直方图 (似乎是将多张图(list)计算出一个直方图，再跟另一个多个图计算出来的直方图进行比较)

		Imgproc.calcHist(images, channels, new Mat(), hist, histSize, ranges);
		Imgproc.calcHist(images2, channels, new Mat(), hist2, histSize, ranges);
		double d = Imgproc.compareHist(hist, hist2, Imgproc.CV_COMP_CORREL);
		
		return d;
	}
	
	/**
	 * 将识别出的人脸剪切后保存
	 *
	 * @param image    Mat
	 * @param rect     人脸信息
	 * @param fileName 文件名字
	 * @return 保存是否成功
	 */
	public static boolean saveImage(Mat image, Rect rect, String path) {
	    try {
	        // 把检测到的人脸重新定义大小后保存成文件
	        Mat sub = image.submat(rect);
	        Mat mat = new Mat();
	        Size size = new Size(100, 100);
	        Imgproc.resize(sub, mat, size);
	        Imgcodecs.imwrite(path, mat);
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public static void main(String[] args) throws Exception, InterruptedException{  
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat image = Imgcodecs.imread("D:\\opencv\\img_270.jpg");
		image=facesDetect(image);
		image=eyeDetect(image);
		
		Imgcodecs.imwrite("D:\\opencv\\jieguo999.jpg", image);
	}  
	
}
