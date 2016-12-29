package org.deepvgi.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;

/**
 * Created by John on 12/22/16.
 *
 */
public class MapSwipe_Active {
    private static String sampling_type = "mapswipe";
   // private static String sampling_type = "uncertainty";

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

    }
}
