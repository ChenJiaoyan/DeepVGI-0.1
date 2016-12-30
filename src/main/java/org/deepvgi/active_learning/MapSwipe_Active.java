package org.deepvgi.active_learning;

import org.deepvgi.evaluation.Predicting;
import org.deepvgi.vgi.VGI_Files;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by John on 12/22/16.
 * actively sampling according to mapswipe labels
 */

public class MapSwipe_Active {

    private static int positive_filter_threshold = 1;
    private static int negative_filter_threshold = 60;

    private static int num_per_negative_image = 5;

    private static HashMap<String, Integer> positive_active_samples = new HashMap<>();
    private static HashMap<String, Integer> negative_active_samples = new HashMap<>();

    private static Predicting p;
    private static HashMap<String, int[]> mapswipe_labels;

    public static void main(String args[]) throws IOException {

        p = new Predicting();
        mapswipe_labels = VGI_Files.loadMapSwipeLabel();

        ArrayList<String> images = VGI_Files.loadImageName("train");

        for (int i = 0; i < images.size(); i++) {
            String img_name = images.get(i);
            boolean positive = has_building_by_mapswipe(img_name);
            int p_tile_n = p.image_predict(img_name);
            System.out.println(i + "," + img_name + ", " + positive + ", " + p_tile_n);
            if (positive && p_tile_n <= positive_filter_threshold) {
                positive_active_samples.put(img_name, p_tile_n);
            }
            if (!positive && p_tile_n >= negative_filter_threshold) {
                negative_active_samples.put(img_name, p_tile_n);
            }
        }
        sort_store(positive_active_samples, "positive_active_samples", true);
        sort_store(negative_active_samples, "negative_active_samples", false);
        System.out.println("positive active_learning samples #: " + positive_active_samples.size());
        System.out.println("negative active_learning samples #: " + negative_active_samples.size());

        System.out.println("#### Sampling " + num_per_negative_image + " pixels from each type I error image ####");
        negative_active_sampling_pixels();
    }

    private static void negative_active_sampling_pixels() throws IOException {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(negative_active_samples.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue() - o1.getValue());
        File f = new File(System.getProperty("user.dir"), "src/main/resources/uncertainty_active_samples_pixels");
        FileWriter writer = new FileWriter(f);

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            String img_name = entry.getKey();
            INDArray tiles = p.slide(img_name);
            HashMap<String,Double> tmp_samples = new HashMap<>();
            for (int r = 0; r < tiles.shape()[0]; r++) {
                INDArray tiles_r = tiles.getRow(r);
                INDArray output = p.getModel().output(tiles_r);
                for (int c = 0; c < output.shape()[0]; c++) {
                    double positive_p = output.getFloat(c,1);
                    if(positive_p>=p.getDecision_threshold()) {
                        int pixel_x = c * p.getSlide_stride() + p.getTile_width() / 2;
                        int pixel_y = r * p.getSlide_stride() + p.getTile_height() / 2;
                        String[] tmp = img_name.split("_");
                        String sample_f = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
                        tmp_samples.put(sample_f, positive_p);
                    }
                }
            }
            List<Map.Entry<String, Double>> entries2 = new ArrayList<>(tmp_samples.entrySet());
            Collections.sort(entries2, (o1, o2) -> {
                if (o2.getValue() - o1.getValue()>=0){
                    return 1;
                }else{
                    return -1;
                }
            });
            for(int j=0;j<entries2.size() && j<num_per_negative_image;j++){
                writer.write(entries2.get(j).getKey() + ";" + entries2.get(j).getValue());
            }
        }
        writer.flush();
        writer.close();
    }

    private static boolean has_building_by_mapswipe(String img_name) {
        String[] tmp = img_name.split("_");
        String k = tmp[0] + "_" + tmp[1];
        if (mapswipe_labels.containsKey(k)) {
            int[] v = mapswipe_labels.get(k);
            int yes_count = v[0];
            int maybe_count = v[1];
            if (yes_count == 0 && maybe_count <= 1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private static void sort_store(HashMap<String, Integer> samples, String file_name, boolean ascending) throws IOException {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(samples.entrySet());
        if (ascending) {
            Collections.sort(entries, (o1, o2) -> o1.getValue() - o2.getValue());
        } else {
            Collections.sort(entries, (o1, o2) -> o2.getValue() - o1.getValue());

        }

        File f = new File(System.getProperty("user.dir"), "src/main/resources/" + file_name);
        FileWriter writer = new FileWriter(f);
        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i).toString();
            writer.write(entry.replace("=", ";") + "\n");
        }
        writer.flush();
        writer.close();
    }

}


