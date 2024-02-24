package imageCompressor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class Methods {

	private static BufferedImage BI;
	private static JLabel picLabel;
	private static String myPath; // default image
	private static Color[][] pom;
	
	public Methods(String myPath) {
		Methods.myPath = myPath;
	}	
		
	
// 		EXAMPLE USAGE					-------
		
//		invert();
//		round();
//		randomNoise();
//		increaseRGBconstant(1, 0, 0, 80); // slika je 80% crvenija
//		increaseRGBrandom(1, 0.5, 1); 
//		increaseRGBconstant(1, 1, 0, 30);
//		increaseRGBcyclic(0.5, 0.5, 0, 50);
//		increaseRGBcyclic(2.0, 0.5, 0, 50); // error
		
		
//		minimumFilter(2);
//		maximumFilter(50);
//		medianFilter(3); 
//		openingFilter();
//		closingFilter();
		
		
//		plotImageData();
		
//		sharpenHard();
//		sharpenSoft();
		
//		SobelFilter();
//		GND(true, 0.5, 0.0, 0.5);
	
	
	/**
	 * Checks whether given pair of coordinates in inside of buffered image bounds. 
	 * @param x row coordinate
	 * @param y column coordinate
	 * @return {@code True} if they are inside, {@code False} otherwise
	 */
	static private boolean in(int x, int y) {
		if (x >= 0 && x < BI.getWidth() && y >= 0 && y < BI.getHeight()) return true;
		return false;
	}
	/**
	 * Fills {@code pom} with RGB values from image at {@code BI}.
	 */
	private static void updatePom() {
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
		pom = new Color[width][height]; 
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
					pom[i][j] = new Color(BI.getRGB(i, j));
				} catch (Exception exc) {
					exc.printStackTrace();
				}
	    	}
	    }
	}
	
	// TODO add undo
	
	/**
	 * Helper class for black neutral-density filter. <p> 
	 * Overlays dark layer with decaying transparecy from top to bottom over the original image. <p>
	 * Equivalent to {@code GND (hard,0,0,0)}.
	 * @param hard
	 * @throws IOException If an input exception occurred
	 */
	public static void GND (boolean hard) throws IOException {
		GND(hard, 0, 0, 0);
	}
	
	/**
	 * Graduated colored neutral-density filter. 
	 * <p> Overlays colored layer with decaying transparecy from top to bottom over the original image.
	 * @param hard {@code True} for hard edge filter, {@code False} for soft edge.
	 * @param r normalized R value ( to [0, 1.0] )
	 * @param g normalized G value
	 * @param b normalized B value 
	 * @throws IOException If an input exception occurred
	 */
	public static void GND (boolean hard, double r, double g, double b) throws IOException {
		if (!(r >= 0 && r <= 1 && g >= 0 && g <= 1 && b >= 0 && b <= 1)) throw new IOException("parameters aren't normalized to [0.0, 1.0]");
		r = 1.0 - r; // color is subtracted, so calculation should be done with inverse
		g = 1.0 - g;
		b = 1.0 - b;
		updatePom();
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    int step;
	    if (hard) step = height / 2 / 128;
	    else step = height / 128;
	    int cnt = 128;
	    for (int j = 0;j < height;j++) {
	    	for (int i = 0;i < width;i++) {
	    	
	    		int red = cutoff(  (int) (pom[i][j].getRed() - cnt * r));
	    		int green =  cutoff((int) (pom[i][j].getGreen() - cnt*g));
	    		int blue = cutoff(  (int) (pom[i][j].getBlue() - cnt*b));
	    		int rgb = new Color(red, green, blue).getRGB();
				BI.setRGB(i,j,rgb);
	    	}
	    	if (j % step == 0) cnt--;
	    	if (cnt < 0) break;
	    }
	    picLabel.setIcon(new ImageIcon(BI));
	}
	
	/**
	 * Filter used for edge detection.
	 */
	public static void SobelFilter() {
		
		updatePom();
		
		int[] transform1 = {1,0,-1,2,0,-2,1,0,-1};
		Color[][] Gx = applyTransform(transform1);
		int[] transform2 = {-1,-2,-1,0,0,0,1,2,1};
		Color[][] Gy = applyTransform(transform2);
		int width = BI.getWidth();
	    int height = BI.getHeight();
		for (int i = 1;i < width-1;i++) {
	    	for (int j = 1;j < height-1;j++) {
	    		int red = cutoff(Gx[i][j].getRed() + Gy[i][j].getRed());
	    		int green = cutoff(Gx[i][j].getGreen() + Gy[i][j].getGreen());
	    		int blue = cutoff(Gx[i][j].getBlue() + Gy[i][j].getBlue());
	    		int rgb = new Color(red, green, blue).getRGB();
				BI.setRGB(i,j,rgb);
	    	}
	    }
		picLabel.setIcon(new ImageIcon(BI));
	}
	
	/**
	 * Helper function for filters whose convolution kernel can be written as 3x3 matrix.
	 */
	static private Color[][] applyTransform(int[] transform) {
		int dx[] = {-1,-1,-1,0,0,0,1,1,1}, dy[] = {-1,0,1,-1,0,1,-1,0,1};
		updatePom();
	    final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    
		for (int i = 1;i < width-1;i++) {
	    	for (int j = 1;j < height-1;j++) {
//	    		Color curr = new Color(BI.getRGB(i, j));
	    		int newRed = 0;
	    		int newGreen = 0;
	    		int newBlue = 0;
	    		for (int ind = 0;ind < 9;ind++) {
	    			int x = i + dx[ind], y = j + dy[ind];
	    			if (!in(x,y)) continue;
	    			newRed += pom[x][y].getRed() * transform[ind];
	    			newGreen += pom[x][y].getGreen() * transform[ind];
	    			newBlue += pom[x][y].getBlue() * transform[ind];
	    		}
	    		newRed = cutoff(newRed);
	    		newGreen = cutoff(newGreen);
	    		newBlue = cutoff(newBlue);
//	    		newRed = (i == 0 || j == 0 || i == width-1 || j == height-1) ? pom[i][j].getRed() : cutoff(newRed);
//	    		newGreen = (i == 0 || j == 0 || i == width-1 || j == height-1) ? pom[i][j].getGreen() : cutoff(newGreen);
//	    		newBlue = (i == 0 || j == 0 || i == width-1 || j == height-1) ? pom[i][j].getBlue() : cutoff(newBlue);
//	    		System.out.printf("%d %d %d\n", newRed, newGreen, newBlue);
	    		try {
		    		int rgb = new Color(newRed, newGreen, newBlue).getRGB();
					BI.setRGB(i,j,rgb);
	    		}
	    		catch (Exception exc) {
					exc.printStackTrace();
				}
	    	}
	    }
		picLabel.setIcon(new ImageIcon(BI));
		return pom;
	}
	
	/**
	 * Sharpens image using Laplacian operator (considers all neighbours). <p>
	 * Can be called successively. 
	 * Depending on the image, it can remain usable after at most 3 calls.
	 */
	public static void sharpenHard () {
		int[] transform = {-1,-1,-1,-1,9,-1,-1,-1,-1};
		applyTransform(transform);
	}
	
	/**
	 * Sharpens image using Laplacian operator. It doesn't consider diagonal neighbours, only row and column adjacent. <p>
	 * Can be called successively.
	 * Depending on the image, it can remain usable after at most 3 calls.
	 */
	public static void sharpenSoft () {
		int[] transform = {0,-1,0,-1,5,-1,0,-1,0};
		applyTransform(transform);    
	}
	
	/**
	 * Plots loaded image to Cartesian coordinate system rotated by 90 degrees clockwise, with values from [0, 255]. 
	 * Output at (x,y) has the greatest B value of all pixels with R value = x and G value = y.
	 * This value is displayed as the intensity of blue at (x,y). <p>
	 * This is a somewhat different, non-unique representation of the image's data.
	 */
	public static void plotImageData() {
		updatePom();
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    
	    
	    BI = loadImage("empty256.png");
	    
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		int red = pom[i][j].getRed();
	    		int green = pom[i][j].getBlue();
	    		int blue = pom[i][j].getBlue();
	    		if (blue > BI.getRGB(red, green)) BI.setRGB(red, green, new Color(0, 0, blue).getRGB()); // x-axis is R, y-axis is G, and 
	    	}
	    }
	    JFrame dataFrame = new JFrame();
	    JLabel dataImgLabel = new JLabel();
	    dataFrame.setTitle("Data plot");
	    dataImgLabel.setIcon(new ImageIcon(BI));
	    dataFrame.add(dataImgLabel);
	    dataFrame.pack();
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	dataFrame.setVisible(true);
            }
        });
//	    picLabel.setIcon(new ImageIcon(BI));
	    BI = loadImage(myPath); // reload default image
	}
	
	/**
	 * Applies a minimum filter multiple times in a row.
	 * That is, the R value of each pixel is set to the minimum R value of its 8 neighbours, same for G and B.
	 * @param factor number of times to succesively apply the filter
	 */
	public static void minimumFilter(int factor) {
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    int dx[] = {-1,-1,-1,0,0,1,1,1}, dy[] = {-1,0,1,-1,1,-1,0,1};
	    
	    for (int epoch = 0;epoch < factor;epoch++) {
		    
	    	updatePom();
	
		    
		    for (int i = 0;i < width;i++) {
		    	for (int j = 0;j < height;j++) {
		    		int red = 255, green = 255, blue = 255;
		    		for (int ind = 0;ind < 8;ind++) {
		    			int x = i + dx[ind], y = j + dy[ind];
		    			if (!in(x,y)) continue;
	
		    			if (pom[x][y].getRed() < red) {
		    				red = pom[x][y].getRed();
		    			}
		    			if (pom[x][y].getGreen() < green) {
		    				green = pom[x][y].getGreen();
		    			}
		    			if (pom[x][y].getBlue() < blue) {
		    				blue = pom[x][y].getBlue();
		    			}
		    		}
		    		try {
//		    			System.out.printf("%d %d %d | ", pom[i][j].getRed(), pom[i][j].getGreen(), pom[i][j].getBlue());
		    			int rgb = new Color(red,green,blue).getRGB();
						BI.setRGB(i,j,rgb);
//			    		System.out.printf("%d %d %d \n", red, green, blue);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
		    	}
		    }
	    }
	    picLabel.setIcon(new ImageIcon(BI));
	}
	/**
	 * Applies a maximum filter multiple times in a row. 
	 * That is, the R value of each pixel is set to the maximum R value of its 8 neighbours, same for G and B.
	 * @param factor number of times to succesively apply the filter
	 */
	public static void maximumFilter(int factor) {
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    int dx[] = {-1,-1,-1,0,0,1,1,1}, dy[] = {-1,0,1,-1,1,-1,0,1};
	    for (int epoch = 0;epoch < factor;epoch++) {
	    	updatePom();
	//	    System.out.printf("w|h is %d %d", width, height);
		    
		    for (int i = 0;i < width;i++) {
		    	for (int j = 0;j < height;j++) {
		    		int red = 0, green = 0, blue = 0;
		    		for (int ind = 0;ind < 8;ind++) {
		    			int x = i + dx[ind], y = j + dy[ind];
		    			if (!in(x,y)) continue;
	
		    			if (pom[x][y].getRed() > red) {
		    				red = pom[x][y].getRed();
		    			}
		    			if (pom[x][y].getGreen() > green) {
		    				green = pom[x][y].getGreen();
		    			}
		    			if (pom[x][y].getBlue() > blue) {
		    				blue = pom[x][y].getBlue();
		    			}
		    		}
		    		try {
//		    			System.out.printf("%d %d %d | ", pom[i][j].getRed(), pom[i][j].getGreen(), pom[i][j].getBlue());
		    			int rgb = new Color(red,green,blue).getRGB();
						BI.setRGB(i,j,rgb);
//			    		System.out.printf("%d %d %d \n", red, green, blue);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
		    	}
		    }
		}
	    picLabel.setIcon(new ImageIcon(BI));
	}
	/**
	 * Preserves edges and removes noise.
	 * Applies median filter {@code factor} times in a row. 
	 * @param factor number of times to succesively apply the filter
	 */
	public static void medianFilter(int factor) {
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    int dx[] = {-1,-1,-1,0,0,1,1,1}, dy[] = {-1,0,1,-1,1,-1,0,1};
	    for (int epoch = 0;epoch < factor;epoch++) {
		    
	    	updatePom();
		    
		    for (int i = 0;i < width;i++) {
		    	for (int j = 0;j < height;j++) {
		    		int red = 0, green = 0, blue = 0, cnt = 0;
		    		for (int ind = 0;ind < 8;ind++) {
		    			int x = i + dx[ind], y = j + dy[ind];
		    			if (!in(x,y)) continue;
		    			cnt++;
		    			red += pom[x][y].getRed();
		    			green += pom[x][y].getGreen();
		    			blue += pom[x][y].getBlue();
		    		}
		    		if (cnt == 0) continue;
		    		try {
		    			red /= cnt;
		    			green /= cnt;
		    			blue /= cnt;
//		    			System.out.printf("%d %d %d | ", pom[i][j].getRed(), pom[i][j].getGreen(), pom[i][j].getBlue());
		    			int rgb = new Color(red,green,blue).getRGB();
						BI.setRGB(i,j,rgb);
//			    		System.out.printf("%d %d %d \n", red, green, blue);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
		    	}
		    }
	    }
	    picLabel.setIcon(new ImageIcon(BI));
	}
	// open lightens, close darkens. open+close is lighter than close+open
	/**
	 * Removes small objects.
	 */
	public static void openingFilter () { 
		maximumFilter(1);
		minimumFilter(1);
	}
	/**
	 * Smoothes image.
	 */
	public static void closingFilter () {
		minimumFilter(1);
		maximumFilter(1);
	}
	
	/**
	 * Rounds off digits at the end of RGB values of each pixel. 
	 * @param ROUND number of digits to round off
	 */
	public static void round(int ROUND) {
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
		    		int red = mycolor.getRed();
		    		red = red - red % (int) Math.pow(10, ROUND);
		    		int blue = mycolor.getBlue();
		    		blue = blue - blue % (int) Math.pow(10, ROUND);
		    		int green = mycolor.getGreen();
		    		green = green - green % (int) Math.pow(10, ROUND);
//		    		System.out.printf("%d %d %d | ", red, green, blue);
		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    		}
	    		catch (Exception exc) {
	    			exc.printStackTrace();
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		
	    picLabel.setIcon(new ImageIcon(BI));
	}
	
	static private int abs (int x) {
		if (x < 0) x *= -1;
		return x;
	}
	
	static private int modabs (int x, int mod) {
		if (mod == 0) {
			if (x < 0) x *= -1;
			return x;
		}
		x = (x + mod) % mod;
		return x;
	}
	
	
	static private int min(int x, int y) {
		if (x < y) return x;
		return y;
	}
	
	/**
	 * Returns value in interval [0, 255].
	 */
	static private int cutoff(int x) {
		if (x > 255) x = 255;
		if (x < 0) x = 0;
		return x;
	}

	/**
	 * Increases intensity of the given color by a factor. Large values are cut off at 255.
	 * @param r normalized R value ( to [0, 1.0] )
	 * @param g normalized G value
	 * @param b normalized B value 
	 * @param factor increase percentage in range [0, 100]
	 * @throws IOException If an input exception occurred
	 */
	public static void increaseRGBconstant (double r, double g, double b, int factor) throws IOException {
		updatePom();
		// add r * factor/100 * 256 to red RGB value and return to [0, 255]. Repeat for g and b
		if (!(r >= 0 && r <= 1 && g >= 0 && g <= 1 && b >= 0 && b <= 1)) throw new IOException("parameters aren't normalized to [0.0, 1.0]");
		System.out.printf("factor is %d", factor);
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
	    			int red = mycolor.getRed();
	    			int blue = mycolor.getBlue();
	    			int green = mycolor.getGreen();
//	    			System.out.printf("%d %d %d | ", red, green, blue)
	    			red = min(red + (int) (r*factor/100*256), 255);
	    			green = min(green + (int) (g*factor/100*256), 255);
	    			blue = min(blue + (int) (b*factor/100*256), 255);

		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    			
	    		}
	    		catch (Exception exc) {
	    			System.out.println(exc.toString());
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		
	    picLabel.setIcon(new ImageIcon(BI));
	}
	/**
	 * Increases intensity of the given color by a factor, all with MOD 255.
	 * @param r normalized R value ( to [0, 1.0] )
	 * @param g normalized G value
	 * @param b normalized B value
	 * @param factor a measure of filter's intensity
	 * @throws IOException If an input exception occurred
	 */
	public static void increaseRGBcyclic (double r, double g, double b, int factor) throws IOException {
		updatePom();
		// add r * factor/100 * 256 to red RGB value and return to [0, 255]. Repeat for g and b
		if (!(r >= 0 && r <= 1 && g >= 0 && g <= 1 && b >= 0 && b <= 1)) throw new IOException("parameters aren't normalized to [0.0, 1.0]");
		System.out.printf("factor is %d", factor);
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
	    			int red = mycolor.getRed();
	    			int blue = mycolor.getBlue();
	    			int green = mycolor.getGreen();
//	    			System.out.printf("%d %d %d | ", red, green, blue)
	    			red = modabs(red + (int) (r*factor/100*256), 256);
	    			green = modabs(green + (int) (g*factor/100*256), 256);
	    			blue = modabs(blue + (int) (b*factor/100*256), 256);
//	    			System.out.printf("%d %d %d \n", red, green, blue);
		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    			
	    		}
	    		catch (Exception exc) {
	    			System.out.println(exc.toString());
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		
	    picLabel.setIcon(new ImageIcon(BI));
	}
	/**
	 * Adds random noise to color specified by args.
	 * @param r normalized R value ( to [0, 1.0] )
	 * @param g normalized G value
	 * @param b normalized B value
	 * @throws IOException If an input exception occurred
	 */
	public static void increaseRGBrandom (double r, double g, double b) throws IOException {
		updatePom();
		if (!(r >= 0 && r <= 1 && g >= 0 && g <= 1 && b >= 0 && b <= 1)) throw new IOException("parameters aren't normalized to [0.0, 1.0]");
		Random rand = new Random();
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
	    			int x = rand.nextInt(256);
	    			int y = rand.nextInt(256);
	    			int z = rand.nextInt(256);
	    			int red = mycolor.getRed();
	    			int blue = mycolor.getBlue();
	    			int green = mycolor.getGreen();
//		    		System.out.printf("%d %d %d | ", red, green, blue);
//	    			red = abs(red - x);
//	    			blue = abs(blue - y);
//	    			green = abs(green - z);
//		    		System.out.printf("%d %d %d \n", red, green, blue);
	    			red = modabs(red + (int)(r*x), 256);
	    			green = modabs(green + (int)(g*y), 256);
	    			blue = modabs(blue + (int)(b*z), 256);

		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    			
	    		}
	    		catch (Exception exc) {
	    			System.out.println(exc.toString());
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		
	    picLabel.setIcon(new ImageIcon(BI));
	}

	/**
	 * Adds random noise to each pixel. Not equivalent to increaseRGBrandom(1,1,1).
	 */
	public static void randomNoise() { // 
		updatePom();
		Random rand = new Random();
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
	    			int x = rand.nextInt(256);
	    			int y = rand.nextInt(256);
	    			int z = rand.nextInt(256);
	    			int red = mycolor.getRed();
	    			int blue = mycolor.getBlue();
	    			int green = mycolor.getGreen();
//		    		System.out.printf("%d %d %d | ", red, green, blue);
	    			red = abs(red - x);
	    			blue = abs(blue - y);
	    			green = abs(green - z);
//		    		System.out.printf("%d %d %d \n", red, green, blue);

	    			
		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    			
	    		}
	    		catch (Exception exc) {
	    			System.out.println(exc.toString());
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		
	    picLabel.setIcon(new ImageIcon(BI));
	}
	/**
	 * Inverts RGB value of each pixel.
	 */
	public static void invert() {
		updatePom();
		
		final int width = BI.getWidth();
	    final int height = BI.getHeight();
//	    System.out.printf("%d %d\n", width, height);
	    for (int i = 0;i < width;i++) {
	    	for (int j = 0;j < height;j++) {
	    		try {
	    			Color mycolor = new Color(BI.getRGB(i, j));
//	    			result[i][j] = BI.getRGB(i, j);
		    		int red = mycolor.getRed();
		    		red = 255 - red;
		    		int blue = mycolor.getBlue();
		    		blue = 255 - blue;
		    		int green = mycolor.getGreen();
		    		green = 255 - green;
//		    		System.out.printf("%d %d %d | ", red, green, blue);
		    		int rgb = new Color(red,green,blue).getRGB();
		    		BI.setRGB(i, j, rgb);
	    		}
	    		catch (Exception exc) {
	    			break;
	    		}
	    		
	    	}System.out.println();
	    }
		picLabel.setIcon(new ImageIcon(BI));
//		System.out.printf("%d %d\n", BI.getWidth(), BI.getHeight());
		

	}
	
	

	public static void setBI(BufferedImage bI) {
		BI = bI;
	}

	public static void setPicLabel(JLabel picLabel) {
		Methods.picLabel = picLabel;
	}

	/**
	 * Loads image from file given by {@code pathToFile}.
	 * @param pathToFile relative path to image file. The image must be in the data folder.
	 */
	static public BufferedImage loadImage(String pathToFile) {
		String myPath = "data/" + pathToFile;
		try {
			
//			myPath = "data/miller.jpg";
			return ImageIO.read(new File(myPath));
			
		} catch (Exception exc) {
			System.out.println(exc);
		}
		return null;

	}
	
}
