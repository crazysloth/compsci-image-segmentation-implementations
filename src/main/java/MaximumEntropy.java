import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by jonas on 21/01/17.
 */
public class MaximumEntropy implements PlugInFilter {

    public int setup(String arg, ImagePlus imagePlus) {
        return DOES_16;
    }

    public void run(ImageProcessor ip) {
        int[] histogram = ip.getHistogram();
        double totalPixels = ip.getWidth() * ip.getHeight();

        double[] normalisedHisto = new double[histogram.length];

        for (int i = 0; i < histogram.length; i++) {
            normalisedHisto[i] = histogram[i] / totalPixels;
        }

        double[] cumNormHistogram = new double[histogram.length];

        // Dont forget for other algorithms
        cumNormHistogram[0] = normalisedHisto[0];

        double totalMeanSum = 0;

        for (int i = 1; i < cumNormHistogram.length; i++) {
            cumNormHistogram[i] = cumNormHistogram[i - 1] + normalisedHisto[i];
            totalMeanSum += normalisedHisto[i];
        }


        double maxEntropy = -1;
        int optimalThresh = -1;

        for (int i = 0; i < histogram.length - 1; i++) {
            //TODO implement using shannon entropy formula
            double objEntropy = 0;
            for (int x = 0; x < i; x++) {
                objEntropy -= (normalisedHisto[i] / cumNormHistogram[i]) * Math.log(normalisedHisto[i] / cumNormHistogram[i]);
            }

            double bgFreq = 1 - cumNormHistogram[i];
            double bgEntropy = 0;
            for (int y = i; y < histogram.length; y++) {
                bgEntropy = -((normalisedHisto[i] / bgFreq) * Math.log(normalisedHisto[i] / bgFreq));
            }

            double sumEntropy = objEntropy + bgEntropy;

            if (sumEntropy > maxEntropy) {
                maxEntropy = sumEntropy;
                optimalThresh = i;
            }
        }

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                if (ip.getPixel(x,y) <= optimalThresh) {
                    ip.putPixel(x,y,0);
                } else {
                    ip.putPixel(x,y, histogram.length - 1);
                }
            }
        }
    }


}
