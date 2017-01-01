package org.deepvgi.evaluation;

import org.datavec.api.util.ClassPathResource;
import org.deepvgi.vgi.VGI_Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by John on 12/28/16.
 * Evaluate the performance of MapSwipe volunteers
 */
public class MapSwipe_Eva {
    public static void main(String args[]) throws IOException {
        String gt_file = new ClassPathResource("ground_truths").getFile().getPath();
        GroundTruth gt = new GroundTruth(new File(gt_file));
        ArrayList<String> p_test_images = gt.getP_test_images();
        ArrayList<String> n_test_images = gt.getN_test_images();
        System.out.println("positive images #: " + p_test_images.size());
        System.out.println("negative images #: " + n_test_images.size());
        HashMap<String, int[]> mapswipe = VGI_Files.loadMapSwipeLabel();
        int TP = 0;
        int FN = 0;
        int TN = 0;
        int FP = 0;
        for (int i = 0; i < p_test_images.size(); i++) {
            String img_file = p_test_images.get(i);
            String[] tmp = img_file.split("_");
            String k = tmp[0] + "_" + tmp[1];
            if (!mapswipe.containsKey(k)) {
                FN += 1;
            } else {
                int[] v = mapswipe.get(k);
                System.out.println("Positive " + img_file + ": " + Arrays.toString(v));
                int yes_count = v[0];
                int maybe_count = v[1];
                int bay_imagery_count = v[2];
                if (yes_count >= 2 && bay_imagery_count == 0) {
                    TP += 1;
                } else {
                    FN += 1;
                }
            }
        }

        for (int i = 0; i < n_test_images.size(); i++) {
            String img_file = n_test_images.get(i);
            String[] tmp = img_file.split("_");
            String k = tmp[0] + "_" + tmp[1];
            System.out.println("Negative " + img_file + ": " + Arrays.toString(mapswipe.get(k)));
            if (!mapswipe.containsKey(k)) {
                TN += 1;
            }else{
                int[] v = mapswipe.get(k);
                int yes_count = v[0];
                int maybe_count = v[1];
                if(yes_count==0 && maybe_count <=2){
                    TN += 1;
                }else{
                    FP += 1;
                }
            }
        }

        float acc = (float) (TP + TN) / (float) (TP + TN + FP + FN);
        float precision = (float) TP / (float) (TP + FP);
        float recall = (float) TP / (float) (TP + FN);
        float f1 = 2 * precision * recall / (precision + recall);
        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + f1);
        System.out.println("Accuracy: " + acc);
    }

}
