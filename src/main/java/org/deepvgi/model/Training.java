package org.deepvgi.model;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.filters.RandomPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.util.ClassPathResource;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

/**
 * Created by John on 12/22/16.
 * Train CNN model for tile prediction
 */
public class Training {

    private static int numEpochs = 30;
    private static int batchSize = 32;
    private static int iterations = 1;

    private static int tile_height;
    private static int tile_width;
    private static int labelNum;
    private static int channels;

    private static String model_file;

    private static final long seed = 12345;
    private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random randNumGen = new Random(seed);

    public static void main(String args []) throws IOException {
        model_file = "model_s1.zip";
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));
        labelNum = Integer.parseInt(properties.getProperty("labelNum"));
        channels = Integer.parseInt(properties.getProperty("channels"));

        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        ImageRecordReader recordReader = new ImageRecordReader(tile_height, tile_width, channels, labelMaker);

        String filename = new ClassPathResource("/vgi_tiles/").getFile().getPath();
        File parentDir = new File(filename);
        FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions, randNumGen);
//        BalancedPathFilter pathFilter = new BalancedPathFilter(randNumGen, allowedExtensions, labelMaker);
        RandomPathFilter pathFilter = new RandomPathFilter(randNumGen,allowedExtensions);
        InputSplit trainData = filesInDir.sample(pathFilter)[0];

        recordReader.initialize(trainData);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);

        System.out.println("****** Build Model ******");
        MultiLayerNetwork model = new MultiLayerNetwork(ANN_config(1));
        model.setListeners(new ScoreIterationListener(100));
        for (int i = 0; i < numEpochs; i++) {
            model.fit(trainIter);
        }
        storeModel(model);

        System.out.println("******EVALUATE MODEL ON TRAIN DATA******");
        RandomPathFilter pathFilter2 = new RandomPathFilter(randNumGen,allowedExtensions);
        InputSplit trainData2 = filesInDir.sample(pathFilter2)[0];
        recordReader.reset();
        recordReader.initialize(trainData2);
        DataSetIterator iter2 = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);
        scaler.fit(iter2);
        iter2.setPreProcessor(scaler);

        Evaluation eval = new Evaluation(labelNum);
        while (iter2.hasNext()) {
            DataSet next = iter2.next();
            INDArray output = model.output(next.getFeatures());
            eval.eval(next.getLabels(), output);
        }
        System.out.println(eval.stats());
    }

    private static MultiLayerConfiguration ANN_config(int type){
        MultiLayerConfiguration conf = null;
        if(type==0) {
            conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .regularization(false).l2(0.005) // tried 0.0001, 0.0005
                    .activation("relu")
                    .learningRate(0.0001) // tried 0.00001, 0.00005, 0.000001
                    .weightInit(WeightInit.XAVIER)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(Updater.RMSPROP).momentum(0.9)
                    .list()
                    .layer(0, convInit("cnn1", channels, 50, new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0))
                    .layer(1, maxPool("maxpool1", new int[]{2, 2}))
                    .layer(2, conv5x5("cnn2", 75, new int[]{5, 5}, new int[]{1, 1}, 0))
                    .layer(3, maxPool("maxool2", new int[]{2, 2}))
                    .layer(4, new DenseLayer.Builder().nOut(100).build())
                    .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nOut(labelNum)
                            .activation("softmax")
                            .build())
                    .backprop(true).pretrain(false)
                    .cnnInputSize(tile_height, tile_width, channels).build();
        }
        if(type==1){
            conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .iterations(1)
                    .learningRate(0.006)
                    .updater(Updater.NESTEROVS).momentum(0.9)
                    .regularization(true).l2(1e-4)
                    .list()
                    .layer(0, new DenseLayer.Builder()
                            .nIn(tile_height * tile_width * 3)
                            .nOut(100)
                            .activation("relu")
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nIn(100)
                            .nOut(labelNum)
                            .activation("softmax")
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .pretrain(false).backprop(true)
                    .setInputType(InputType.convolutional(tile_height,tile_width,channels))
                    .build();

        }
        return conf;
    }

    private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private static ConvolutionLayer conv3x3(String name, int out, double bias) {
        return new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1}).name(name).nOut(out).biasInit(bias).build();
    }

    private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5, 5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private static SubsamplingLayer maxPool(String name, int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2, 2}).name(name).build();
                                                                }

    private static DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
    }

    private static void storeModel(MultiLayerNetwork net) throws IOException {
        File f = new File(System.getProperty("user.dir"),"src/main/resources/"+model_file);
        boolean saveUpdater = true;
        ModelSerializer.writeModel(net, f, saveUpdater);
    }

}
