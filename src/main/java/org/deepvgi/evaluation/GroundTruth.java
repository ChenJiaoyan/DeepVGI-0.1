package org.deepvgi.evaluation;

import org.datavec.api.util.ClassPathResource;
import org.deepvgi.vgi.VGI_Files;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by john on 24.12.16.
 * Interfaces for reading ground truths from the files
 */
public class GroundTruth {

    private String gt_file;
    private ArrayList<String> test_images;
    private ArrayList<String> p_test_images;
    private ArrayList<String> n_test_images;
    private ArrayList<String[]> p_pixels;

    private static int tile_height;
    private static int tile_width;

    public GroundTruth(String file_name) throws IOException {
        this.gt_file = new ClassPathResource(file_name).getFile().getPath();
        this.test_images = VGI_Files.loadImageName("test");
        this.p_test_images = new ArrayList<>();
        this.n_test_images = new ArrayList<>();
        this.p_pixels = new ArrayList<>();

        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));

        File f = new File(this.gt_file);
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] items = line.split(";");
            this.p_test_images.add(items[0]);
            this.p_pixels.add(items);
        }
        bufferedReader.close();
        read.close();
        for (int i = 0; i < this.test_images.size(); i++) {
            if (!this.p_test_images.contains(test_images.get(i))) {
                this.n_test_images.add(test_images.get(i));
            }
        }
    }

    public void cut_gt_tiles() throws IOException {
        String p_filename = new ClassPathResource("/gt_tiles/positive/").getFile().getPath();
        File p_f = new File(p_filename);
        if (!p_f.exists()) {
            p_f.createNewFile();
        }
        String n_filename = new ClassPathResource("/gt_tiles/negative/").getFile().getPath();
        File n_f = new File(n_filename);
        if (!n_f.exists()) {
            n_f.createNewFile();
        }
        if (p_f.listFiles().length > 0 || n_f.listFiles().length > 0) {
            System.err.println("ground truth tiles already exit");
            System.exit(0);
        }
        for (int i = 0; i < p_pixels.size(); i++) {
            String[] pixel = p_pixels.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + pixel[0]);
            int pixel_x = Integer.parseInt(pixel[1]);
            int pixel_y = Integer.parseInt(pixel[2]);
            String[] tmp = pixel[0].split("_");
            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/positive/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
        }
        for (int i = 0; i < n_test_images.size(); i++) {
            String filename = n_test_images.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + filename);
            int pixel_x = tile_width / 2;
            int pixel_y = tile_height / 2;
            String[] tmp = filename.split("_");
            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
        }

    }


    public ArrayList<String> getP_test_images() {
        return this.getP_test_images();
    }

    public ArrayList<String> getN_test_images() {
        return this.getN_test_images();
    }



}
