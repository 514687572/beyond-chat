package com.stip.net.dl4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class RecognitionPornographic {
    protected static int height = 100;
    protected static int width = 100;
    protected static int channels = 3;
    protected static long seed = 42;
    protected static Random rng = new Random(seed);
    static String netWorkPath = FilenameUtils.concat(System.getProperty("user.dir"), "src/main/resources/model.bin");
	
	public static void main(String[] args) throws Exception {
		List<String> labelList = Arrays.asList("normal","pornographic");
	    MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(netWorkPath);
	    
	    // FileChose is a string we will need a file
	    File file = new File("D:\\images\\newsex\\172.jpeg");

	    // Use NativeImageLoader to convert to numerical matrix
	    NativeImageLoader loader = new NativeImageLoader(height, width, channels);

	    // Get the image into an INDarray
	    INDArray image = loader.asMatrix(file);

	    // 0-255
	    // 0-1
	    DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
	    scaler.transform(image);
	    
	    // Pass through to neural Net
	    INDArray output = model.output(image);
	    
	    //log.info("## List of Labels in Order## ");
	    // In new versions labels are always in order
	    System.out.println(output.toString());
	    System.out.println(labelList.toString());

	}
}
