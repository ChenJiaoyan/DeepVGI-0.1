package org.deepvgi.model;

import org.datavec.image.loader.BaseImageLoader;

import java.util.Random;
/**
 * Created by John on 12/22/16.
 * S1: Train
 */
public class Train1 {
    private static final long seed = 12345;
    private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random randNumGen = new Random(seed);

    private static int height = 50;
    private static int width = 50;
    private static int channels = 3;
    private static int outputNum = 3;
    private static int numEpochs = 10;
    private static int batchSize = 5;
    private static int iterations = 1;

    public static void main(String args[]){

    }
}
