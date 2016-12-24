package org.deepvgi.evaluation;

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
public class Prediction {

    private static int tile_height;
    private static int tile_width;
    private static int labelNum;
    private static int channels;

    private static MultiLayerNetwork model;

    public static void main(String args[]) throws IOException {
        String model_file = "model_s1.zip";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/Body/" + model_file);
        model = ModelSerializer.restoreMultiLayerNetwork(f);

        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        labelNum = Integer.parseInt(properties.getProperty("labelNum"));
        channels = Integer.parseInt(properties.getProperty("channels"));
    }

    private static void tile_evaluation(){

    }

    private static void image_evaluation(){

    }


}
