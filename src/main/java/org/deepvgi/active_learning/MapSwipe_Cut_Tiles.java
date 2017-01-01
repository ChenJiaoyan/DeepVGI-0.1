package org.deepvgi.active_learning;

import org.deepvgi.vgi.VGI_Files;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by John on 12/30/16.
 * Generate batches of actively sampled tiles
 */
public class MapSwipe_Cut_Tiles {
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

        ArrayList<Integer[]> error_I_pixels = read_error_images_pixels("I");
        ArrayList<Integer[]> error_II_pixels = read_error_images_pixels("II");
        System.out.println("error I pixels #: " + error_I_pixels.size());
        System.out.println("error II pixels #: " + error_II_pixels.size());

        for (int i = 1; i <= batch_num; i++) {
            int num_I = cut_batch(i, "negative", error_I_pixels);
            int num_II = cut_batch(i, "positive", error_II_pixels);
            System.out.println("batch " + i + ": " + num_I + ", " + num_II);
        }

    }

    private static int cut_batch(int i, String type, ArrayList<Integer[]> pixels) throws IOException {
        int num = 0;
        for (int j = (i - 1) * (active_batch_size / 2); j < i * (active_batch_size / 2) && j < pixels.size(); j++) {
            Integer[] pixel = pixels.get(j);
            String img_f = pixel[0] + "_" + pixel[1] + "_18.jpeg";
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
            File des_f = new File(System.getProperty("user.dir"),
                    "src/main/resources/mapswipe_active_tiles/" + type + "/" + "batch" + i + "_" + pixel[0] + "_" +
                            pixel[1] + "_" + pixel[2] + "_" + pixel[3] + ".jpeg");
            VGI_Files.cut_tile(src_f, des_f, pixel[2], pixel[3], tile_width, tile_height);
            num += 1;
        }
        return num;
    }


    private static ArrayList<Integer[]> read_error_images_pixels(String error_type) throws IOException {
        ArrayList<Integer[]> pixels = new ArrayList<>();
        File f = new File(System.getProperty("user.dir"), "src/main/resources/error_" + error_type + "_images_pixels");
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] items = line.split(";");
            String[] tmp = items[0].split("_");
            int task_x = Integer.parseInt(tmp[0]);
            int task_y = Integer.parseInt(tmp[1]);
            int pixel_x = Integer.parseInt(items[1]);
            int pixel_y = Integer.parseInt(items[2]);
            if (pixel_x - tile_width / 2 >= 0 && pixel_y - tile_height / 2 >= 0
                    && pixel_x + tile_width / 2 <= image_width &&
                    pixel_y + tile_height / 2 <= image_height) {
                Integer[] pixel = {task_x, task_y, pixel_x, pixel_y};
                pixels.add(pixel);
            }
        }
        bufferedReader.close();
        read.close();
        return pixels;
    }

}
