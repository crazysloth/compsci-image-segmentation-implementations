import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by jonas on 21/01/17.
 */
public class MaximumEntropy implements PlugInFilter {

    public int setup(String arg, ImagePlus imagePlus) {
        return DOES_16 | DOES_STACKS;
    }

    public void run(ImageProcessor ip) {
        //PluginInFilter will run all stacks under the hood if setup like this
        process(ip);
    }

    private void process(ImageProcessor ip) {
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

        for (int i = 0; i < histogram.length; i++) {

            double objEntropy = 0;
            if (cumNormHistogram[i] > Double.MIN_VALUE) {
                for (int x = 0; x <= i; x++) {
                    if (normalisedHisto[x] > Double.MIN_VALUE) {
                        objEntropy -= (normalisedHisto[x] / cumNormHistogram[i]) * Math.log(normalisedHisto[x] / cumNormHistogram[i]);
                    }
                }
            }

            double bgFreq = 1 - cumNormHistogram[i];
            double bgEntropy = 0;
            if (bgFreq > Double.MIN_VALUE) {
                for (int x = i; x < histogram.length; x++) {
                    if (normalisedHisto[x] > Double.MIN_VALUE) {
                        bgEntropy -= (normalisedHisto[x] / bgFreq) * Math.log(normalisedHisto[x] / bgFreq);
                    }
                }
            }

            double sumEntropy = objEntropy + bgEntropy;

            if (sumEntropy > maxEntropy) {
                maxEntropy = sumEntropy;
                optimalThresh = i;
            }
        }

        IJ.log("threshold: " + optimalThresh);

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
