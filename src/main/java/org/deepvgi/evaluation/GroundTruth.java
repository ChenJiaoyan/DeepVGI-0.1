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

    private File gt_file;
    private ArrayList<String> test_images;
    private ArrayList<String> p_test_images;
    private ArrayList<String> n_test_images;
    private ArrayList<String[]> p_pixels;

    private int tile_height;
    private int tile_width;
    private int image_height;
    private int image_width;
    private int border_size;

    public GroundTruth(File f) throws IOException {
        this.gt_file = f;
        this.test_images = VGI_Files.loadImageName("test");
        this.p_test_images = new ArrayList<>();
        this.n_test_images = new ArrayList<>();
        this.p_pixels = new ArrayList<>();

        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("config.properties");
        properties.load(inputStream);
        this.tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        this.tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        this.image_height = Integer.parseInt(properties.getProperty("image_height"));
        this.image_width = Integer.parseInt(properties.getProperty("image_width"));
        this.border_size = Integer.parseInt(properties.getProperty("border_size"));

        InputStreamReader read = new InputStreamReader(new FileInputStream(gt_file));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] items = line.split(";");
            this.p_pixels.add(items);
            if(!this.p_test_images.contains(items[0])) {
                this.p_test_images.add(items[0]);
            }
        }
        bufferedReader.close();
        read.close();
        for (int i = 0; i < this.test_images.size(); i++) {
            if (!this.p_test_images.contains(test_images.get(i))) {
                this.n_test_images.add(test_images.get(i));
            }
        }
        System.out.println("before filtering, p_test_images: " + this.p_test_images.size());
        System.out.println("before filtering, n_test_images: " + this.n_test_images.size());
        filter_border_test_samples();
    }

    private void filter_border_test_samples(){
        for(int i=p_pixels.size()-1;i>=0;i--){
            String [] pixel = p_pixels.get(i);
            int pixel_x = Integer.parseInt(pixel[1]);
            int pixel_y = Integer.parseInt(pixel[2]);
            if(pixel_x<border_size || pixel_x >= image_width-border_size ||
                    pixel_y<border_size || pixel_y >= image_height-border_size){
                p_pixels.remove(i);
            }
        }

        for(int i=p_test_images.size()-1;i>=0;i--){
            String img_name = p_test_images.get(i);
            boolean remove = true;
            for(int j=0;j<p_pixels.size();j++){
                if(p_pixels.get(j)[0].equals(img_name)){
                    remove = false;
                    break;
                }
            }
            if(remove){
                p_test_images.remove(i);
            }
        }
    }


    public void cut_gt_tiles() throws IOException {
        File p_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/positive/");
        if (!p_f.exists()) {
            p_f.createNewFile();
        }
        File n_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/");
        if (!n_f.exists()) {
            n_f.createNewFile();
        }
        if (p_f.listFiles().length > 0 || n_f.listFiles().length > 0) {
            System.err.println("ground truth tiles already exit");
            return;
        }
        System.out.println("### generate positive tiles ###");
        for (int i = 0; i < p_pixels.size(); i++) {
            String[] pixel = p_pixels.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + pixel[0]);
            int pixel_x = Integer.parseInt(pixel[1]);
            int pixel_y = Integer.parseInt(pixel[2]);
            String[] tmp = pixel[0].split("_");
            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            System.out.println(des_filename);
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/positive/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
        }
        System.out.println("### cut negative tiles ###");
        for (int i = 0; i < n_test_images.size(); i++) {
            String filename = n_test_images.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + filename);
            int pixel_x = image_width / 3;
            int pixel_y = image_height / 3;
            String[] tmp = filename.split("_");

            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);

            des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x*2 + "_" + pixel_y + ".jpeg";
            des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x*2, pixel_y, tile_width, tile_height);

            des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y*2 + ".jpeg";
            des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y*2, tile_width, tile_height);

            des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x*2 + "_" + pixel_y*2 + ".jpeg";
            des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x*2, pixel_y*2, tile_width, tile_height);
        }

    }


    public ArrayList<String> getP_test_images() {
        return this.p_test_images;
    }

    public ArrayList<String> getN_test_images() {
        return this.n_test_images;
    }

    public static void main(String args []) throws IOException {
        String gt_file = new ClassPathResource("ground_truths").getFile().getPath();
        GroundTruth gt = new GroundTruth(new File(gt_file));
        gt.cut_gt_tiles();
    }
}
