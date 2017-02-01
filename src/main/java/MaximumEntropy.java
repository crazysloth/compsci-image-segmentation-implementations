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
        long totalPixels = ip.getWidth() * ip.getHeight();

        double[] normalisedHisto = new double[histogram.length];

        for (int i = 0; i < histogram.length; i++) {
            normalisedHisto[i] = histogram[i] / totalPixels;
        }

        double[] cumNormHistogram = new double[histogram.length];

        // Dont forget for other algorithms
        cumNormHistogram[0] = normalisedHisto[0];

        for (int i = 1; i < cumNormHistogram.length; i++) {
            cumNormHistogram[i] = cumNormHistogram[i - 1] + normalisedHisto[i];
        }

        int optimalThresh = 0;
        for (int i = 0; i < histogram.length - 1; i++) {
            //TODO implement using shannon entropy formula

        }




    }


}
