package org.deepvgi.evaluation;

import org.datavec.api.util.ClassPathResource;
import org.deepvgi.vgi.VGI_Files;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by john on 24.12.16.
 * Manually label the ground truth of the testing data
 */
public class GroundTruth {

    private String gt_file;
    private ArrayList<String> test_images;
    private ArrayList<String> p_test_images;
    private ArrayList<String> n_test_images;
    private ArrayList<String []> p_pixels;

    public GroundTruth(String file_name) throws IOException {
        this.gt_file = new ClassPathResource(file_name).getFile().getPath();
        this.test_images = VGI_Files.loadImageName("test");
        this.p_test_images = new ArrayList<>();
        this.n_test_images = new ArrayList<>();
        this.p_pixels = new ArrayList<>();

        File f = new File(this.gt_file);
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String [] items = line.split(";");
            this.p_test_images.add(items[0]);
            this.p_pixels.add(items);
        }
        bufferedReader.close();
        read.close();
        for(int i=0;i<this.test_images.size();i++){
            if(!this.p_test_images.contains(test_images.get(i))){
                this.n_test_images.add(test_images.get(i));
            }
        }
    }

    public void cut_gt_tiles(){

    }


    public ArrayList<String> getP_test_images(){
        return this.getP_test_images();
    }

    public ArrayList<String> getN_test_images(){
        return this.getN_test_images();
    }

    public static void main(String args []){

    }
}
