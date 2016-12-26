package org.deepvgi.evaluation;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by John on 12/22/16.
 * 1) evaluate the model's performance on tiles
 * 2) predict the label of image with sliding window, and evaluate the model's performance
 */
public class Predicting {

    private static int tile_height;
    private static int tile_width;
    private static int labelNum;
    private static int channels;

    private static MultiLayerNetwork model;
    private static GroundTruth gt;

    public static void main(String args[]) throws IOException {

        System.out.println("######loading model and ground truths######");
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        labelNum = Integer.parseInt(properties.getProperty("labelNum"));
        channels = Integer.parseInt(properties.getProperty("channels"));

        String model_file = "model_s1.zip";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/Body/" + model_file);
        model = ModelSerializer.restoreMultiLayerNetwork(f);

        String gt_file = new ClassPathResource("ground_truths").getFile().getPath();
        gt = new GroundTruth(new File(gt_file));

        System.out.println("######task (1): evaluate with tiles######");
        tile_evaluation();

        System.out.println("######task (2): evaluate with images######");
        image_evaluation();
    }

    private static void tile_evaluation() throws IOException {
    }

    private static void image_evaluation(){

    }


}
