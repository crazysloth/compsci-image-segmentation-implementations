import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by jonas on 3/12/16.
 */
public class OtsuThresholding implements PlugInFilter {

    public int setup(String arg, ImagePlus imagePlus) {
        return DOES_16 | DOES_STACKS;
    }

    public void run(ImageProcessor ip) {
        //PluginInFilter will run all stacks under the hood if setup like this
        process(ip);
    }

    private void process(ImageProcessor ip) {
        int[] histogram = ip.getHistogram();

        int[] cumHistogram = new int[histogram.length];

        long totalMeanSum = 0;

        cumHistogram[0] = histogram[0];
        for (int i = 1; i < cumHistogram.length; i++) {
            cumHistogram[i] = cumHistogram[i - 1] + histogram[i];
            totalMeanSum += i * histogram[i];
        }

        long sumMeanObg = 0;
        double maxVar = 0;
        int optimalThresh = 0;

        for (int i = 0; i < histogram.length; i++) {
            sumMeanObg += i * histogram[i];

            if (sumMeanObg == 0 || totalMeanSum == sumMeanObg) {
                continue;
            }

            long sumMeanBg = totalMeanSum - sumMeanObg;
            long bgPix = cumHistogram[cumHistogram.length -1] - cumHistogram[i];
            double betweenVar = cumHistogram[i] * bgPix * Math.pow(((sumMeanObg/cumHistogram[i]) - (sumMeanBg/bgPix)), 2);

            if (betweenVar > maxVar) {
                maxVar = betweenVar;
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
