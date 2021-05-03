# ImageJ Plugin - Thresholding Based Image Segmentation Algorithms

Image segmentation describes a set of techniques that provide a way of labeling pixels based on statistically significant patterns found within the image.
This repo contains an ImageJ plugin with a collection of global thresholding techniques. Thresholding methods use statistical information present in the image histogram to determine a single threshold that separates the region of interest from the background. 
Most of these techniques depend on a specific pattern being present in the histogram, such as the histogram having, uni, bi or multi-modal distribution, where the peaks represent the pixels with the highest likelihood of belonging to an object of interest. 
A threshold may be placed somewhere between the peaks to represent the boundary of the region of interest. Global thresholding algorithms, aim to determine the location of this threshold in the histogram.

## Algorithms

This repository contains 3 global thresholding techniques and 1 local region growing approach:
- Iterative thresholding (Ridler & Calvard, 1978)
- Otsu's method (Otsu, 1979)
- Entropy Maximisation (Kapur et al, 1985)
- Seeded Region Growing

### Iterative Thresholding

Iterative thresholding was introduced by Ridler & Calvard (1978). This method determines the optimal threshold by iteratively calculating the midpoint between the two means of each class as separated by the threshold.

### Otsu Method

The Otsu method determines the location of the threshold, by maximising the inter (between) class variance, which has the same effect as minimising the intra (within) class variance, because the total variance is the sum of both the inter & intra class variance. Otsu is one the most popular techniques and is often used as a starting benchmark with which to compare other techniques.

### Entropy Maximisation

The Maximum Entropy method finds the global threshold by calculating the maximum sum of the calculated entropys across the two classes by iterating over the histogram, calculating the entropys of both classes. In other words, this method aims to find the maximum intra class entropy.

### Seeded Region Growing

A novel non-thresholding based image segmentation approach that takes an unsegmented input image along with a seed image, that is the same size as the input image with regions highlighted with a region grey level value greater than zero. Black (value zero) is reserved as an undefined region.

Other input parameters include an option to select eight or four pixel neighbourhood connectivity and a flag to leave an unassigned region. in this case the user provides a constant z-score that will be used in the uniformity predicate. If this flag is not checked the algorithm increments the z-score value by 0.5 within the uniformity predicate at each iteration until no unassigned regions exist. The uniformity predicate, adds a neighbour to the growing region if the pixel's grey value falls within the defined number of standard deviations from the mean.

At each iteration regions are grown out sequentially, meaning that the first region encountered when parsing the seed image is the first region grown. Each neighbour of each region pixel is then grown out until one of three conditions is met:

1. The neighbouring pixel is outside the image size boundary
2. The neighbouring pixel does not meet the uniformity predicate
3. The neighbouring pixel is already assigned to a region

## Building & Installing

This is a maven project so after cloning this repo, run a maven install:

`mvn clean install`

There are two ways to get this plugin running inside ImageJ:

1. By running the `Application.java` entrypoint, which will start up a bare ImageJ application with the plugin pre-installed. This will run the chosen algorithm on a pre-defined image.
2. Importing the maven built _jar_ file in the `target` directory, into your local installation of [ImageJ](https://imagej.net/Fiji]). In the case of Fiji ImageJ, this can be done by copying the _jar_ file into Fiji's plugins directory.

Once the plugin is installed the algorithms can be found under _Thresholding Image Segmentation_ under the _plugins_ menu.

## Running The Algorithms

Most of these will simply just execute on the currently selected image in ImageJ, once clicked in plugins menu. 

The seeded region growing algorithm requires two images to be open - the seed image, and the focal image to be processed. See the method's section above for details on what the seed image should look like.

## References

Otsu, N. (1979). A threshold selection method from gray-level histograms. IEEE transactions on systems, man, and cybernetics, 9(1), 62-66.

Kapur, J. N., Sahoo, P. K., & Wong, A. K. (1985). A new method for gray-level picture thresholding using the entropy of the histogram. Computer vision, graphics, and image processing, 29(3), 273-285.

Ridler, T. W., & Calvard, S. (1978). Picture thresholding using an iterative selection method. IEEE trans syst Man Cybern, 8(8), 630-632.

## Other Resources

- [University of Auckland IVS lab](https://ivs.wordpress.fos.auckland.ac.nz)
- [Image Segmentation Wiki](https://en.wikipedia.org/wiki/Image_segmentation)
- [Download ImageJ](https://imagej.net/Downloads)