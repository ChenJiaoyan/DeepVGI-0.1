package org.deepvgi.model;

import org.deepvgi.evaluation.Predicting;
import org.deepvgi.vgi.VGI_Files;

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
    private static int negative_filter_threshold = 200;

    private static HashMap<String, Integer> positive_active_samples = new HashMap<>();
    private static HashMap<String, Integer> negative_active_samples = new HashMap<>();

    private static Predicting p;
    private static HashMap<String, int []> mapswipe_labels;

    public static void main(String args []) throws IOException {

        p = new Predicting();
        mapswipe_labels = VGI_Files.loadMapSwipeLabel();

        ArrayList<String> images = VGI_Files.loadImageName("train");

        for(int i=0;i<images.size();i++){
            String img_name = images.get(i);
            boolean positive = has_building_by_mapswipe(img_name);
            int p_tile_n = p.image_predict(img_name);
            System.out.println(img_name + ", " + positive + ", " + p_tile_n);
            if(positive && p_tile_n <= positive_filter_threshold){
                positive_active_samples.put(img_name,p_tile_n);
            }
            if(!positive && p_tile_n>=negative_filter_threshold){
                negative_active_samples.put(img_name,p_tile_n);
            }
        }
        sort_store(positive_active_samples,"positive_active_samples");
        sort_store(negative_active_samples,"negative_active_samples");
    }

    private static boolean has_building_by_mapswipe(String img_name){
        String [] tmp = img_name.split("_");
        String k = tmp[0] + "_" + tmp[1];
        if(mapswipe_labels.containsKey(k)){
            int [] v = mapswipe_labels.get(k);
            int yes_count = v[0];
            int maybe_count = v[1];
            if(yes_count==0 && maybe_count <=1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    private static void sort_store(HashMap<String,Integer> samples,String file_name) throws IOException {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(samples.entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue() - o2.getValue());

        File f = new File(System.getProperty("user.dir"), "src/main/resources/"+file_name);
        FileWriter writer = new FileWriter(f);
        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i).toString();
            writer.write(entry.replace("=",";") + "\n");
        }
        writer.flush();
        writer.close();
    }

}


