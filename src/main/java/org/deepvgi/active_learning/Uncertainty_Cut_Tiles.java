package org.deepvgi.active_learning;

import org.deepvgi.vgi.VGI_Files;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by John on 1/1/17.
 */
public class Uncertainty_Cut_Tiles {
    private static int batch_num = 10;
    private static int active_batch_size;

    private static int tile_height;
    private static int tile_width;
    private static int image_height;
    private static int image_width;

    public static void main(String args[]) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("config.properties");
        properties.load(inputStream);

        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        image_height = Integer.parseInt(properties.getProperty("image_height"));
        image_width = Integer.parseInt(properties.getProperty("image_width"));
        active_batch_size = Integer.parseInt(properties.getProperty("active_batch_size"));

        ArrayList<Integer[]> pixels = MapSwipe_Cut_Tiles.read_error_images_pixels(
                "uncertainty_active_samples", image_height, image_width, tile_height, tile_width);
        int num = 0;
        for (int i = 1; i <= batch_num; i++) {
            for (int j = (i - 1) * (active_batch_size / 2);
                 j < i * (active_batch_size / 2) && j < pixels.size(); j++) {
                Integer[] pixel = pixels.get(j);
                String img_f = pixel[0] + "_" + pixel[1] + "_18.jpeg";
                File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
                File des_f = new File(System.getProperty("user.dir"),
                        "src/main/resources/uncertainty_active_tiles/" + "batch" + i + "_" + pixel[0] + "_" +
                                pixel[1] + "_" + pixel[2] + "_" + pixel[3] + ".jpeg");
                VGI_Files.cut_tile(src_f, des_f, pixel[2], pixel[3], tile_width, tile_height);
                num += 1;
            }
        }
        System.out.println("tiles #: " + num);
    }

}
