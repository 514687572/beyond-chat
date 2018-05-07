package tutorial_code.ImgProc.tutorial_template_matching;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class MatchTemplateDemoRun implements ChangeListener {

	// ! [declare]
	/// Global Variables
	Boolean use_mask = false;
	Mat img = new Mat(), templ = new Mat();
	Mat mask = new Mat();

	int match_method;

	JLabel imgDisplay = new JLabel(), resultDisplay = new JLabel();
	// ! [declare]

	public void run(String[] args) {

		if (args.length < 2) {
			System.out.println("Not enough parameters");
			System.out.println("Program arguments:\n<image_name> <template_name> [<mask_name>]");
			System.exit(-1);
		}

		// ! [load_image]
		/// Load image and template
		img = Imgcodecs.imread(args[0], Imgcodecs.IMREAD_COLOR);
		templ = Imgcodecs.imread(args[1], Imgcodecs.IMREAD_COLOR);
		// ! [load_image]

		if (args.length > 2) {
			use_mask = true;
			mask = Imgcodecs.imread(args[2], Imgcodecs.IMREAD_COLOR);
		}

		if (img.empty() || templ.empty() || (use_mask && mask.empty())) {
			System.out.println("Can't read one of the images");
			System.exit(-1);
		}

		//matchImages();
		//createJFrame();
		openCVHistCompare(img,templ,true);
	}

	private void matchingMethod() {
		Mat result = new Mat();
		
		// ! [copy_source]
		/// Source image to display
		Mat img_display = new Mat();
		img.copyTo(img_display);
		// ! [copy_source]

		// ! [create_result_matrix]
		/// Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;

		result.create(result_rows, result_cols, CvType.CV_32FC1);
		// ! [create_result_matrix]

		// ! [match_template]
		/// Do the Matching and Normalize
		Boolean method_accepts_mask = (Imgproc.TM_SQDIFF == match_method || match_method == Imgproc.TM_CCORR_NORMED);
		if (use_mask && method_accepts_mask) {
			Imgproc.matchTemplate(img, templ, result, match_method, mask);
		} else {
			Imgproc.matchTemplate(img, templ, result, match_method);
		}
		// ! [match_template]

		// ! [normalize]
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		// ! [normalize]

		// ! [best_match]
		/// Localizing the best match with minMaxLoc
		double minVal;
		double maxVal;
		Point matchLoc;

		Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
		// ! [best_match]

		// ! [match_loc]
		/// For SQDIFF and SQDIFF_NORMED, the best matches are lower values.
		// For all the other methods, the higher the better
		if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
			matchLoc = mmr.minLoc;
		} else {
			matchLoc = mmr.maxLoc;
		}
		// ! [match_loc]

		// ! [imshow]
		/// Show me what you got
		Imgproc.rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),new Scalar(0, 0, 0), 2, 8, 0);
		Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),new Scalar(0, 0, 0), 2, 8, 0);
		
		Image tmpImg = toBufferedImage(img_display);
		ImageIcon icon = new ImageIcon(tmpImg);
		imgDisplay.setIcon(icon);

		result.convertTo(result, CvType.CV_8UC1, 255.0);
		tmpImg = toBufferedImage(result);
		icon = new ImageIcon(tmpImg);
		resultDisplay.setIcon(icon);
		// ! [imshow]
	}
	
	
	private void matchImages() {
		Mat result = new Mat();
		
		double minVal;
		double maxVal;
		Point matchLoc;

		Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
		maxVal=mmr.maxVal;
		minVal=mmr.minVal;
		
		if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
			matchLoc = mmr.minLoc;
		} else {
			matchLoc = mmr.maxLoc;
		}
		
		System.out.println(minVal);
		System.out.println(maxVal);
	}
	
	public static double openCVHistCompare(Mat image1,Mat image2,boolean isGray){
	      //Mat m = Highgui.imread(image1,Highgui.CV_LOAD_IMAGE_COLOR);
	     // Mat m2 = Highgui.imread(image2,Highgui.CV_LOAD_IMAGE_COLOR);
	      List<Mat> images = new ArrayList<Mat>();
	      List<Mat> images2 = new ArrayList<Mat>();
	      if(isGray){
	    	  //使用灰色图比较
	    	  //System.out.println("Use Gray!");
	    	  Mat gray1 = new Mat(image1.size(),CvType.CV_8UC1);
	    	  Imgproc.cvtColor(image1,gray1,Imgproc.COLOR_RGB2GRAY);
	    	  Mat gray2 = new Mat(image2.size(),CvType.CV_8UC1);
	    	  Imgproc.cvtColor(image2,gray2,Imgproc.COLOR_RGB2GRAY);
	    	  images.add(gray1);
	    	  images2.add(gray2);
	      }else{
	        images.add(image1);
	        images2.add(image2); 
	      }
	      MatOfInt channels= new MatOfInt(0);
	      MatOfInt histSize = new MatOfInt(256);
	      MatOfFloat ranges= new MatOfFloat(0,256);
	      Mat hist = new Mat();
	      Mat hist2 = new Mat();
	      //计算直方图  (似乎是将多张图(list)计算出一个直方图，再跟另一个多个图计算出来的直方图进行比较)
	     
	      Imgproc.calcHist(images, channels, new Mat(), hist, histSize, ranges);
	      Imgproc.calcHist(images2, channels, new Mat(), hist2, histSize, ranges); 
	      double d=Imgproc.compareHist(hist, hist2, Imgproc.CV_COMP_CORREL);
	      
	      return d;
	  }

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			match_method = (int) source.getValue();
			matchingMethod();
		}
	}

	public Image toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		
		return image;
	}

	private void createJFrame() {
		String title = "Source image; Control; Result image";
		JFrame frame = new JFrame(title);
		frame.setLayout(new GridLayout(2, 2));
		frame.add(imgDisplay);

		// ! [create_trackbar]
		int min = 0, max = 5;
		JSlider slider = new JSlider(JSlider.VERTICAL, min, max, match_method);
		// ! [create_trackbar]

		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		// Set the spacing for the minor tick mark
		slider.setMinorTickSpacing(1);

		// Customizing the labels
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("0 - SQDIFF"));
		labelTable.put(new Integer(1), new JLabel("1 - SQDIFF NORMED"));
		labelTable.put(new Integer(2), new JLabel("2 - TM CCORR"));
		labelTable.put(new Integer(3), new JLabel("3 - TM CCORR NORMED"));
		labelTable.put(new Integer(4), new JLabel("4 - TM COEFF"));
		labelTable.put(new Integer(5), new JLabel("5 - TM COEFF NORMED : (Method)"));
		slider.setLabelTable(labelTable);

		slider.addChangeListener(this);

		frame.add(slider);

		frame.add(resultDisplay);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
}


public class MatchTemplateDemo {
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		//args = new String[] { "D:\\opencv\\ouput115.jpg", "D:\\opencv\\ouput162.jpg", "D:\\opencv\\ouput162.jpg" };
		args = new String[] { "D:\\opencv\\bbb162.jpg", "D:\\opencv\\bbb162.jpg"};
		// run code
		new MatchTemplateDemoRun().run(args);
	}
}
