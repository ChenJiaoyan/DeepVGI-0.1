package org.deepvgi.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.deepvgi.vgi.VGI_Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by John on 12/22/16.
 * actively sampling according to mapswipe labels
 */

public class MapSwipe_Active {

    private static int tile_height;
    private static int tile_width;
    private static int image_height;
    private static int image_width;
    private static int labelNum;
    private static int channels;
    private static int slide_stride;

    private static MultiLayerNetwork model;

    public static void main(String args []) throws IOException {

        String model_file = "model_s1.zip";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/" + model_file);
        model = ModelSerializer.restoreMultiLayerNetwork(f);

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

        ArrayList<String> images = VGI_Files.loadImageName("train");
        for(int i=0;i<images.size();i++){

        }
    }

}


