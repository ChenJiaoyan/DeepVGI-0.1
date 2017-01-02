package org.deepvgi.model;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
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
    private static int batchSize = 45;
    private static String ann_type = "lenet";
//    private static String ann_type = "alexnet";
//    private static String ann_type = "";

    private static int tile_height;
    private static int tile_width;
    private static int labelNum;
    private static int channels;

    private static String model_file;

    private static final long seed = 12345;
    private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    private static final Random randNumGen = new Random(seed);

    public static void main(String args[]) throws IOException {
        model_file = "model_s4_batch4_5.zip";
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
        recordReader.initialize(filesInDir);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(trainIter);
        trainIter.setPreProcessor(scaler);

        System.out.println("****** Build Model ******");
        MultiLayerConfiguration conf = ANN_config();
        if(conf==null){
            System.err.println("ANN model not configured!");
            System.exit(0);
        }
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.setListeners(new ScoreIterationListener(100));
        for (int i = 0; i < numEpochs; i++) {
            model.fit(trainIter);
        }
        storeModel(model);

        System.out.println("******EVALUATE MODEL ON TRAIN DATA******");
        recordReader.reset();
        recordReader.initialize(filesInDir);
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);
        scaler.fit(testIter);
        testIter.setPreProcessor(scaler);

        Evaluation eval = new Evaluation(labelNum);
        while (testIter.hasNext()) {
            DataSet next = testIter.next();
            INDArray output = model.output(next.getFeatures());

            eval.eval(next.getLabels(), output);
        }
        System.out.println(eval.stats());
    }

    private static MultiLayerConfiguration ANN_config() {
        MultiLayerConfiguration conf = null;
        double nonZeroBias;
        double dropOut;
        switch (ann_type) {
            case "alexnet":
                nonZeroBias = 1;
                dropOut = 0.5;
                conf = new NeuralNetConfiguration.Builder()
                        .seed(seed)
                        .weightInit(WeightInit.DISTRIBUTION)
                        .dist(new NormalDistribution(0.0, 0.01))
                        .activation("relu").updater(Updater.NESTEROVS).iterations(1)
                        .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .learningRate(1e-2).biasLearningRate(1e-2 * 2)
                        .learningRateDecayPolicy(LearningRatePolicy.Step)
                        .lrPolicyDecayRate(0.1).lrPolicySteps(100000)
                        .regularization(true).l2(5 * 1e-4)
                        .momentum(0.9).miniBatch(false).list()
                        .layer(0, convInit("cnn1", channels, 96, new int[]{11, 11}, new int[]{4, 4}, new int[]{3, 3}, 0))
                        .layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
                        .layer(2, maxPool("maxpool1", new int[]{3, 3}))
                        .layer(3, conv5x5("cnn2", 256, new int[]{1, 1}, new int[]{2, 2}, nonZeroBias))
                        .layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
                        .layer(5, maxPool("maxpool2", new int[]{3, 3}))
                        .layer(6, conv3x3("cnn3", 384, 0))
                        .layer(7, conv3x3("cnn4", 384, nonZeroBias))
                        .layer(8, conv3x3("cnn5", 256, nonZeroBias))
                        .layer(9, maxPool("maxpool3", new int[]{3, 3}))
                        .layer(10, fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
                        .layer(11, fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
                        .layer(12, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .name("output").nOut(labelNum).activation("softmax").build())
                        .setInputType(InputType.convolutionalFlat(tile_height,tile_width,channels))
                        .backprop(true).pretrain(false).build();
                break;
            case "lenet":
                nonZeroBias = 1;
                dropOut = 0.5;
                conf = new NeuralNetConfiguration.Builder()
                        .seed(seed)
                        .iterations(1)
                        .regularization(true).l2(0.0005)
                        .learningRate(.01)//.biasLearningRate(0.02)
                        //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                        .weightInit(WeightInit.XAVIER)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .updater(Updater.NESTEROVS).momentum(0.9)
                        .list()
                        .layer(0, new ConvolutionLayer.Builder(5, 5)
                                .nIn(channels)
                                .stride(1, 1)
                                .nOut(50)
                                .activation("identity")
                                .build())
                        .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2,2)
                                .stride(2,2)
                                .build())
                        .layer(2, new ConvolutionLayer.Builder(5, 5)
                                //Note that nIn need not be specified in later layers
                                .stride(1, 1)
                                .nOut(50)
                                .activation("identity")
                                .build())
                        .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                                .kernelSize(2,2)
                                .stride(2,2)
                                .build())
                        .layer(4, new DenseLayer.Builder().activation("relu")
                                .nOut(500).build())
                        .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nOut(labelNum)
                                .activation("softmax")
                                .build())
                        .setInputType(InputType.convolutionalFlat(tile_height,tile_width,channels)) //See note below
                        .backprop(true).pretrain(false).build();
            default:
                conf = new NeuralNetConfiguration.Builder()
                        .seed(seed)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .iterations(1)
                        .learningRate(0.006)
                        .updater(Updater.NESTEROVS).momentum(0.9)
                        .regularization(true).l2(1e-4)
                        .list()
                        .layer(0, new DenseLayer.Builder()
                                .nIn(tile_height * tile_width * 3).nOut(100).activation("relu")
                                .weightInit(WeightInit.XAVIER).build())
                        .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(100).nOut(labelNum).activation("softmax")
                                .weightInit(WeightInit.XAVIER).build())
                        .pretrain(false).backprop(true)
                        .setInputType(InputType.convolutional(tile_height, tile_width, channels))
                        .build();
                break;
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
        File f = new File(System.getProperty("user.dir"), "src/main/resources/" + model_file);
        boolean saveUpdater = true;
        ModelSerializer.writeModel(net, f, saveUpdater);
    }

}
