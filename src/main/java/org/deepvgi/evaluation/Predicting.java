package org.deepvgi.evaluation;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.util.ClassPathResource;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/**
 * Created by John on 12/22/16.
 * task 1) evaluate the model's performance on tiles
 * task 2) predict the label of image with sliding window, and evaluate the model's performance
 */
public class Predicting {
    //private static String task_type = "tile";
    private static String task_type = "image";
    private static int batchSize = 20;

    private static int tile_height;
    private static int tile_width;
    private static int image_height;
    private static int image_width;
    private static int labelNum;
    private static int channels;
    private static int slide_stride;

    private static MultiLayerNetwork model;

    private static final long seed = 12345;
    private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random randNumGen = new Random(seed);

    public static void main(String args[]) throws IOException {

        System.out.println("###### loading model and ground truths ######");
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        image_height = Integer.parseInt(properties.getProperty("image_height"));
        image_width = Integer.parseInt(properties.getProperty("image_width"));
        labelNum = Integer.parseInt(properties.getProperty("labelNum"));
        channels = Integer.parseInt(properties.getProperty("channels"));

        slide_stride = Integer.parseInt(properties.getProperty("slide_stride"));

        String model_file = "model_s1.zip";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/" + model_file);
        model = ModelSerializer.restoreMultiLayerNetwork(f);

        if (task_type.equals("tile")) {
            System.out.println("###### task (1): evaluate with tiles ######");
            tile_evaluation();
        } else {
            System.out.println("###### task (2): evaluate with images ######");
            image_evaluation();
        }
    }

    private static void tile_evaluation() throws IOException {
        File parentDir = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/");
        FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions, randNumGen);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImageRecordReader recordReader = new ImageRecordReader(tile_height, tile_width, channels, labelMaker);
        recordReader.initialize(filesInDir);
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(testIter);
        testIter.setPreProcessor(scaler);

        Evaluation eval = new Evaluation(labelNum);
        while (testIter.hasNext()) {
            DataSet next = testIter.next();
            INDArray features = next.getFeatures();
            INDArray output = model.output(features);
            eval.eval(next.getLabels(), output);
        }
        System.out.println(eval.stats());
    }

    private static void image_evaluation() throws IOException {
        String gt_file = new ClassPathResource("ground_truths").getFile().getPath();
        GroundTruth gt = new GroundTruth(new File(gt_file));
        ArrayList<String> p_test_images = gt.getP_test_images();
        ArrayList<String> n_test_images = gt.getN_test_images();
        System.out.println("positive images #: " + p_test_images.size());
        System.out.println("negative images #: " + n_test_images.size());
        System.exit(0);
        int TP = 0;
        int FN = 0;
        for (int i = 0; i < p_test_images.size(); i++) {
            int p_tile_n = image_predict(p_test_images.get(i));
            if (p_tile_n > 0) {
                TP += 1;
            } else {
                FN += 1;
            }
            System.out.print("Positive " + p_test_images.get(i) + ": " + p_tile_n);
        }

        int TN = 0;
        int FP = 0;
        for (int i = 0; i < n_test_images.size(); i++) {
            int p_tile_n = image_predict(n_test_images.get(i));
            if (p_tile_n == 0) {
                TN += 1;
            } else {
                FP += 1;
            }
            System.out.print("Negative " + n_test_images.get(i) + ": " + p_tile_n);
        }

        float acc = (float) (TP + TN) / (float) (TP + TN + FP + FN);
        float precision = (float) TP / (float) (TP + FP);
        float recall = (float) TP / (float) (TP + FN);
        float f1 = 2 * precision * recall / (precision + recall);
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + f1);
        System.out.println("Accuracy: " + acc);
    }

    private static int image_predict(String img_name) throws IOException {
        INDArray tiles = slide(img_name);
        int p_tile_n = 0;
        for (int r = 0; r < tiles.shape()[0]; r++) {
            INDArray tiles_r = tiles.getRow(r);
            int[] label_int = model.predict(tiles_r);
            for (int j = 0; j < label_int.length; j++) {
                if (label_int[j] > 0) {
                    p_tile_n++;
                }
            }
        }
        return p_tile_n;
    }

    private static INDArray slide(String predict_f) throws IOException {
        File img = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + predict_f);
        FileSplit filesInDir = new FileSplit(img, allowedExtensions, randNumGen);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        ImageRecordReader recordReader = new ImageRecordReader(image_height, image_width, channels, labelMaker);
        recordReader.initialize(filesInDir);
        DataSetIterator it = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);
        scaler.fit(it);
        it.setPreProcessor(scaler);
        DataSet ds = it.next();

        int row_n = (int) Math.ceil((image_height - tile_height) / (double) slide_stride);
        int col_n = (int) Math.ceil((image_width - tile_width) / (double) slide_stride);
        INDArray m = ds.getFeatureMatrix().getRow(0);
        INDArray out = Nd4j.zeros(row_n, col_n, channels, tile_height, tile_width);
        for (int y = 0, r = 0; y < image_height - tile_height; y = y + slide_stride, r = r + 1) {
            for (int x = 0, c = 0; x < image_width - tile_width; x = x + slide_stride, c = c + 1) {
                INDArray tile = m.get(NDArrayIndex.all(), NDArrayIndex.interval(y, y + tile_height),
                        NDArrayIndex.interval(x, x + tile_width));
                out.get(NDArrayIndex.point(r), NDArrayIndex.point(c)).assign(tile);
            }
        }
        return out;
    }
}
