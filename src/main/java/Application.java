import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.FolderOpener;

/**
 * Created by jonas on 29/11/16.
 */
public class Application {
    public static final String IMAGE_PATH = "~/Documents/test-image.png";

    public static void main(String[] args) {
        Class<?> clazz = SeededRegionGrowing.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        new ImageJ();

        ImagePlus seedImage = IJ.openImage(IMAGE_PATH);
        seedImage.show();

        IJ.runPlugIn(clazz.getName(), "");
    }
}
