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

    private static int positive_filter_threshold = 1;
    private static int negative_filter_threshold = 200;



    public static void main(String args []) throws IOException {


        ArrayList<String> images = VGI_Files.loadImageName("train");
        for(int i=0;i<images.size();i++){
            String img_name = images.get(i);

        }
    }

}


