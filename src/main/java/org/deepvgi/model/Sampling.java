package org.deepvgi.model;

import org.deepvgi.vgi.VGI_Files;

import java.io.*;
import java.util.*;

/**
 * Created by john on 23.12.16.
 * Sample positive tiles (resources/vig_tiles/negative) from OSM and negative tiles (resources/vgi_tiles/positive) from MapSwipe
 */
public class Sampling {

    private static int tile_height;
    private static int tile_width;
    private static int image_height;
    private static int image_width;

    public static void main(String args[]) throws IOException {
        System.out.println("----------------loading-----------------");
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        image_height = Integer.parseInt(properties.getProperty("image_height"));
        image_width = Integer.parseInt(properties.getProperty("image_width"));
        ArrayList<String> train_images = VGI_Files.loadImageName("train");

        System.out.println("----------------positive tiles-----------------");
        int p_n = positive_tiles(train_images);
        System.out.println("----------------negative tiles-----------------");
        int n_n = negative_tiles(train_images);
        System.out.println("Positive tiles #: " + p_n);
        System.out.println("Negative tiles #: " + n_n);
    }

    private static int positive_tiles(ArrayList<String> train_images) throws IOException {
        ArrayList<int[]> OSM_buildings = VGI_Files.loadOSMBuilding();
        int n = 0;
        for (int i = 0; i < OSM_buildings.size(); i++) {
            int[] building = OSM_buildings.get(i);
            int task_x = building[0];
            int task_y = building[1];
            int pixel_x = building[2];
            int pixel_y = building[3];
            String img_f = task_x + "_" + task_y + "_18.jpeg";
            if (train_images.contains(img_f) && tileInImage(pixel_x, pixel_y)) {
                File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
                File des_f = new File(System.getProperty("user.dir"),
                        "src/main/resources/vgi_tiles/positive/" + task_x + "_" + task_y + "_" + pixel_x + "_" + pixel_y + ".jpeg");
                VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
                n++;
            }
        }
        return n;
    }

    private static int negative_tiles(ArrayList<String> train_images) throws IOException {
        HashMap<String, int[]> MapSwipe_labels = VGI_Files.loadMapSwipeLabel();
        int n_n = 0;
        Set<String> keys = MapSwipe_labels.keySet();

        for (int i = 0; i < train_images.size(); i++) {
            String img_f = train_images.get(i);
            String[] tmp = img_f.split("_");
            String key = tmp[0] + "_" + tmp[1];
            if (!keys.contains(key)) {
                int pixel_x = image_width / 2;
                int pixel_y = image_height / 2;
                File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
                File des_f = new File(System.getProperty("user.dir"),
                        "src/main/resources/vgi_tiles/negative/" + key + "_" + pixel_x + "_" + pixel_y + ".jpeg");
                VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
                n_n++;
            }
        }
        return n_n;
    }

    private static boolean tileInImage(int pixel_x, int pixel_y) {
        if (pixel_x - tile_width / 2 >= 0 && pixel_y - tile_height / 2 >= 0
                && pixel_x + tile_width / 2 <= image_width && pixel_y + tile_height / 2 <= image_height) {
            return true;
        } else {
            return false;
        }
    }

}
