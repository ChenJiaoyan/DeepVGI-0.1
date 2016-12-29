package org.deepvgi.model;

import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
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
    private static double filtering_t = 0.55;

    private static int tile_height;
    private static int tile_width;
    private static int image_height;
    private static int image_width;
    private static int labelNum;
    private static int channels;
    private static int slide_stride;
    private static MultiLayerNetwork model;
    private static HashMap<String,Double> results = new HashMap<>();

    private static final long seed = 12345;
    private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random randNumGen = new Random(seed);

    public static void main(String args []) throws IOException {
        String model_file = "model_s1.zip";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/" + model_file);
        model = ModelSerializer.restoreMultiLayerNetwork(f);
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        image_height = Integer.parseInt(properties.getProperty("image_height"));
        image_width = Integer.parseInt(properties.getProperty("image_width"));
        labelNum = Integer.parseInt(properties.getProperty("labelNum"));
        channels = Integer.parseInt(properties.getProperty("channels"));
        slide_stride = Integer.parseInt(properties.getProperty("slide_stride"));

        ArrayList<String> images = VGI_Files.loadImageName("train");
        for(int i=0;i<images.size();i++){
            int n = uncertainty_resampling(images.get(i));
            System.out.println(images.get(i) + ": " + n + " tiles actively sampled");
        }
        sort_store();
    }

    private static int uncertainty_resampling(String img_name) throws IOException {
        int num = 0;
        INDArray tiles = VGI_Files.slide(img_name,image_height,image_width,tile_height,tile_width,slide_stride,
               channels,labelNum,allowedExtensions,randNumGen);
        for (int r = 0; r < tiles.shape()[0]; r++) {
            INDArray tiles_r = tiles.getRow(r);
            INDArray output = model.output(tiles_r);
            for (int c = 0; c < output.shape()[0]; c++) {
                double max_p = output.getFloat(c,0)>output.getFloat(c,1)?
                        output.getFloat(c,0):output.getFloat(c,1);
                if (max_p < filtering_t ) {
                    num += 1;
                    int pixel_x = c*slide_stride + tile_width/2;
                    int pixel_y = r*slide_stride + tile_height/2;
                    String [] tmp = img_name.split("_");
                    String sample_f = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
                    results.put(sample_f,max_p);
                }
            }
        }
        return num;
    }

    private static void sort_store() throws IOException {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(results.entrySet());
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
