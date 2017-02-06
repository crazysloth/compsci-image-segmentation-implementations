import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import util.Pixel;

import java.util.*;

/**
 * Created by jonas on 3/02/17.
 */
public class SeededRegionGrowing implements PlugInFilter {

    private static final String TITLE = "Seeded Region Growing";
    private static final String RESULT_WINDOW_TITLE = "Region Growing Result";
    private static final int MAX_ITERATIONS = 20;
    private static final int MAX_REGIONS = 5;
    private boolean eightConnected = true;
    private boolean allowUnassignedRegion = false;

    private int width = 0;
    private int height = 0;
    private int[][] seedPixels;
    private ImageProcessor ip;
    private Region currentRegionStats;
    private boolean unassignedPixels = true;
    private int currentIteration = 1;
    private double stdDevMultiplier = 1;

    public int setup(String arg, ImagePlus imagePlus) {
        this.width = imagePlus.getWidth();
        this.height = imagePlus.getHeight();
        return DOES_16 | DOES_8G;
    }

    public void run(ImageProcessor ip) {
        this.ip = ip;
        ImagePlus seedImage = getSeedImage();

        if (seedImage == null) {
            return;
        }

        ImageProcessor seedIp = seedImage.getProcessor();
        this.seedPixels = seedIp.getIntArray();

        Map<Integer, List<Pixel>> regions;

        while (unassignedPixels && currentIteration <= MAX_ITERATIONS) {
            IJ.log("Starting iteration " + this.currentIteration + " unassigned pixels: " + this.unassignedPixels + " std dev multiply: " + this.stdDevMultiplier);
            regions = findRegions(seedIp);
            for (Map.Entry<Integer, List<Pixel>> entry : regions.entrySet()) {
                IJ.log("Growing region with grey level: " + entry.getKey());
                List<Pixel> pixels = entry.getValue();
                this.currentRegionStats = new Region();
                this.currentRegionStats.numPixels = pixels.size();
                this.currentRegionStats.regionGreyLevel = entry.getKey();
                this.currentRegionStats.mean = Pixel.calculateMean(pixels);
                this.currentRegionStats.rollingSum = Pixel.calculateSum(pixels);
                this.currentRegionStats.rollingDeviations = Pixel.calculateDeviationDifferenceSum(pixels, this.currentRegionStats.mean);
                this.currentRegionStats.stdDev = (int)Math.round((double)Pixel.calculateStdDev(pixels, this.currentRegionStats.mean) * this.stdDevMultiplier);

                Queue<Pixel> pixelQ = new LinkedList<>();

                for (Pixel regionPixel : pixels) {
                    pixelQ = addNeighboursToQueue(pixelQ, regionPixel);

                    while (!pixelQ.isEmpty()) {
                        Pixel p = pixelQ.remove();

                        if ( isInBounds(p) && meetsGrowCriteria(p) && !belongsToARegion(p)) {
                            this.seedPixels[p.x][p.y] = this.currentRegionStats.regionGreyLevel;

                            this.currentRegionStats.numPixels += 1;
                            this.currentRegionStats.rollingSum = Math.addExact(this.currentRegionStats.rollingSum, (long)p.getGreyLevel());
                            this.currentRegionStats.mean = Math.round(this.currentRegionStats.rollingSum / this.currentRegionStats.numPixels);
                            this.currentRegionStats.rollingDeviations = Math.addExact(this.currentRegionStats.rollingDeviations, (long)Math.pow((p.getGreyLevel() - this.currentRegionStats.mean), 2));
                            double var = this.currentRegionStats.rollingDeviations / this.currentRegionStats.numPixels;
                            this.currentRegionStats.stdDev = (int)(Math.round(Math.sqrt(var) * this.stdDevMultiplier));

                            pixelQ = addNeighboursToQueue(pixelQ, p);

                        }

                    }

                }
            }
            IJ.log("Completed Iteration " + this.currentIteration);
            this.currentIteration += 1;

            if (!this.allowUnassignedRegion) {
                this.stdDevMultiplier += 1;
            }
        }

        ImagePlus resultImage = NewImage.createByteImage(RESULT_WINDOW_TITLE, this.width, this.height, 1, NewImage.FILL_WHITE);
        ImageProcessor resultProcessor = resultImage.getProcessor();

        for (int y = 0; y < this.ip.getHeight(); y++) {
            for (int x = 0; x < this.ip.getWidth(); x++) {
                resultProcessor.putPixel(x,y, this.seedPixels[x][y] * 75);
            }
        }

        resultImage.show();
        IJ.selectWindow(RESULT_WINDOW_TITLE);
    }

    private Queue<Pixel> addNeighboursToQueue(Queue<Pixel> pixelQ, Pixel p) {
        pixelQ.add(new Pixel(p.x+1, p.y, this.ip.getPixel(p.x+1, p.y)));
        pixelQ.add(new Pixel(p.x-1, p.y, this.ip.getPixel(p.x-1, p.y)));
        pixelQ.add(new Pixel(p.x, p.y+1, this.ip.getPixel(p.x, p.y+1)));
        pixelQ.add(new Pixel(p.x, p.y-1, this.ip.getPixel(p.x, p.y-1)));

        if (this.eightConnected) {
            pixelQ.add(new Pixel(p.x+1, p.y+1, this.ip.getPixel(p.x+1, p.y+1)));
            pixelQ.add(new Pixel(p.x-1, p.y-1, this.ip.getPixel(p.x-1, p.y-1)));
            pixelQ.add(new Pixel(p.x+1, p.y-1, this.ip.getPixel(p.x+1, p.y-1)));
            pixelQ.add(new Pixel(p.x-1, p.y+1, this.ip.getPixel(p.x-1, p.y+1)));
        }

        return pixelQ;
    }

    private Map<Integer, List<Pixel>> findRegions(ImageProcessor seedIp) {
        int unassignedPixelCount = 0;
        Map<Integer, List<Pixel>> regions = new HashMap<>();

        for (int x = 0; x < seedIp.getWidth(); x++) {
            for (int y = 0; y < seedIp.getHeight(); y++) {
                int value = this.seedPixels[x][y];
                if (value > 0) {
                    if (regions.containsKey(value)) {
                        regions.get(value).add(new Pixel(x, y, this.ip.getPixel(x,y)));
                    } else {
                        if (regions.size() <= MAX_REGIONS) {
                            regions.put(value, new ArrayList<>());
                        } else {
                            IJ.error(TITLE, "Too many regions, only " + MAX_REGIONS + " supported");
                            return null;
                        }
                    }
                } else {
                   unassignedPixelCount += 1;
                }
            }
        }

        if (unassignedPixelCount == 0) {
            this.unassignedPixels = false;
        }

        IJ.log("Unassigned pixels in region: " + unassignedPixelCount);
        return regions;
    }

    private boolean meetsGrowCriteria(Pixel p) {
        int min = this.currentRegionStats.mean - this.currentRegionStats.stdDev;
        int max = this.currentRegionStats.mean + this.currentRegionStats.stdDev;
        return p.getGreyLevel() >= min && p.getGreyLevel() <= max;
    }

    private boolean belongsToARegion(Pixel p) {
        return this.seedPixels[p.x][p.y] > 0;
    }

    private boolean isInBounds(Pixel p) {
        return p.x > 0 && p.y > 0 && p.x < this.width && p.y < this.height;
    }

    private ImagePlus getSeedImage() {
        final int[] wList = WindowManager.getIDList();
        if (wList == null) {
            IJ.noImage();
            return null;
        }

        final List<String> seedTitleList = new ArrayList<>();
        for (final int id : wList) {
            final ImagePlus imp = WindowManager.getImage(id);
            final int type = imp.getType();
            if (!imp.getTitle().trim().isEmpty()) {
                if (type == ImagePlus.GRAY8) {
                    seedTitleList.add(imp.getTitle());
                }
            }
        }

        if (seedTitleList.size() < 1) {
            IJ.error(TITLE, "No supported seed images open.");
            return null;
        }

        final String[] seedTitles = seedTitleList.toArray(new String[seedTitleList.size()]);

        //Dialog begins here
        final GenericDialog gd = new GenericDialog(TITLE, IJ.getInstance());
        gd.addChoice("Seeds:", seedTitles, seedTitles[0]);
        gd.addCheckbox("8-Connected Neighbourhood", this.eightConnected);
        gd.addCheckbox("Allow Unassigned Region (Don't allow X*stdDev to increase)", this.allowUnassignedRegion);
        gd.showDialog();

        //Retrieve dialog values
        final ImagePlus seeds = WindowManager.getImage(seedTitles[gd.getNextChoiceIndex()]);
        this.eightConnected = gd.getNextBoolean();
        this.allowUnassignedRegion = gd.getNextBoolean();

        if (seeds.getHeight() != this.height || seeds.getWidth() != this.width) {
            IJ.error(TITLE, "Seed image has different size to image. This is the wrong seed for this image.");
            return null;
        }

        return seeds;
    }

    class Region {
        public long rollingSum = 0;
        public long rollingDeviations = 0;
        public int regionGreyLevel = -1;
        public int numPixels = 0;
        public int mean = 0;
        public int stdDev = 0;
    }
}
