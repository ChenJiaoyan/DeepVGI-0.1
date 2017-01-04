package org.deepvgi.active_learning;

import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.deepvgi.evaluation.Predicting;
import org.deepvgi.vgi.VGI_Files;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by john on 29.12.16.
 * Actively resample tiles in order of uncertainty
 */
public class Uncertainty_Active {
    private static double filtering_t = 0.505;

    private static int max_num_per_image = 2;
    private static int max_num = 100;

    private static Predicting p;
    private static HashMap<String,Double> active_samples = new HashMap<>();


    public static void main(String args []) throws IOException {
        String model_file = args[0];
        p = new Predicting(model_file);

        ArrayList<String> images = VGI_Files.loadImageName("train");
        int num = 0;
        for(int i=0;i<images.size();i++){
            if(num+max_num_per_image > max_num){
                break;
            }
            int n = uncertainty_resampling(images.get(i));
            System.out.println(images.get(i) + ": " + n + " tiles actively sampled");
            num += n;
        }
        sort_store();
    }

    private static int uncertainty_resampling(String img_name) throws IOException {
        int num = 0;
        INDArray tiles = p.slide(img_name);
        for (int r = 0; r < tiles.shape()[0]; r++) {
            INDArray tiles_r = tiles.getRow(r);
            INDArray output = p.getModel().output(tiles_r);
            for (int c = 0; c < output.shape()[0]; c++) {
                double max_p = output.getFloat(c,0)>output.getFloat(c,1)?
                        output.getFloat(c,0):output.getFloat(c,1);
                if (max_p < filtering_t ) {
                    num += 1;
                    int pixel_x = c*p.getSlide_stride() + p.getTile_width()/2;
                    int pixel_y = r*p.getSlide_stride() + p.getTile_height()/2;
                    String [] tmp = img_name.split("_");
                    String sample_f = tmp[0] + "_" + tmp[1] + "_18.jpeg;" + pixel_x+";"+pixel_y;
                    active_samples.put(sample_f,max_p);
                    if(num>=max_num_per_image){
                        return num;
                    }
                }
            }
        }
        return num;
    }

    private static void sort_store() throws IOException {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(active_samples.entrySet());
        Collections.sort(entries, (o1, o2) -> {
            if (o1.getValue() - o2.getValue()>=0){
                return 1;
            }else{
                return -1;
            }
        });

        File f = new File(System.getProperty("user.dir"), "src/main/resources/uncertainty_active_samples");
        FileWriter writer = new FileWriter(f);
        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i).toString();
            writer.write(entry.replace("=",";") + "\n");
        }
        writer.flush();
        writer.close();
    }
}
