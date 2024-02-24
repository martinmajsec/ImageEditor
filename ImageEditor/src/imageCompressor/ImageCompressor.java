package imageCompressor;


import java.awt.BorderLayout;
import java.awt.Color; // sRGB color space
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.text.Highlighter.Highlight;



import java.lang.Math;
import java.nio.file.Path;
import java.util.Random;

public class ImageCompressor extends JFrame {
	
	
	/**
	 * Image that's open in the editor.
	 */
	private static BufferedImage BI;
	/**
	 * Label corresponding to the image in editor.
	 */
	private static JLabel picLabel;
	/**
	 * default image
	 */
	final static String myPath = "img.jpg"; 
	
	/**
	 * Main menu.
	 */
	JMenuBar menuBar = new JMenuBar();
	/**
	 * Unorthodox methods.
	 */
	private JMenu menu1 = new JMenu("Other");
	/**
	 * Common filters, such as min, max, median, opening, closing, Sobel and GND.
	 */
	private JMenu menu2 = new JMenu("Filters");
	/**
	 * Photo effects: Constant Increase a certain color, invert, hard and soft sharpening.
	 */
	private JMenu menu3 = new JMenu("Effects");
	/**
	 * Save image in PNG format. IMPORTANT: add extension when saving the image.
	 */
	private JMenu menu4 = new JMenu("Save");
	
	// see a function foo's description at Methods.foo call
	
	JMenuItem round = new JMenuItem("Round");
	JMenuItem randomNoise = new JMenuItem("Random noise");
	JMenuItem increaseRGBrandom = new JMenuItem("Random Increase");
	JMenuItem increaseRGBcyclic = new JMenuItem("Cyclic Increase");
	JMenuItem plotData = new JMenuItem("Plot data");
	
	JMenuItem increaseRGBconst = new JMenuItem("Constant Increase");
	JMenuItem invert = new JMenuItem("Invert");
	JMenuItem sharpenHard = new JMenuItem("Sharpen hard");
	JMenuItem sharpenSoft = new JMenuItem("Sharpen soft");
	
	JMenuItem minFilter = new JMenuItem("Minimum filter");
	JMenuItem maxFilter = new JMenuItem("Maximum filter");
	JMenuItem medFilter = new JMenuItem("Median filter");
	JMenuItem openFilter = new JMenuItem("Opening filter");
	JMenuItem closeFilter = new JMenuItem("Closing filter");
	JMenuItem sobelFilter = new JMenuItem("Sobel filter");
	JMenuItem gndFilter = new JMenuItem("GND filter");
		
	JMenuItem saveItem = new JMenuItem("Save image");
	JMenuItem undoItem = new JMenuItem("Undo"); // TODO
	

	
	public ImageCompressor() throws IOException {
		super("Compress");
		new Methods(myPath);
		BI = Methods.loadImage(myPath);
		picLabel = new JLabel(new ImageIcon(BI));
		Methods.setBI(BI);
		Methods.setPicLabel(picLabel);
		add(picLabel); // default image
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		createMenus();
		addListeners();
		pack();
		
	}

	private void addListeners() {
		invert.addActionListener(e -> Methods.invert());
		increaseRGBconst.addActionListener(e -> constIncreaseListener());
		sharpenHard.addActionListener(e -> Methods.sharpenHard());
		sharpenSoft.addActionListener(e -> Methods.sharpenSoft());
		
		
		round.addActionListener(e -> roundActionListener());
		randomNoise.addActionListener(e -> Methods.randomNoise());
		increaseRGBrandom.addActionListener(e -> randomIncreaseListener());
		increaseRGBcyclic.addActionListener(e -> cyclicIncreaseListener());
		plotData.addActionListener(e -> Methods.plotImageData());
		
		minFilter.addActionListener(e -> factorFilterActionListener("minimum"));
		maxFilter.addActionListener(e -> factorFilterActionListener("maximum"));
		medFilter.addActionListener(e -> factorFilterActionListener("median"));
		openFilter.addActionListener(e -> Methods.openingFilter());
		closeFilter.addActionListener(e -> Methods.closingFilter());
		sobelFilter.addActionListener(e -> Methods.SobelFilter());
		gndFilter.addActionListener(e -> gndFilterListener());

		saveItem.addActionListener(e -> FileSave());
	}

	/**
	 * Saves image via JFileChooser.
	 */
	public void FileSave() {
    	/*
		String writerNames[] = ImageIO.getWriterFormatNames();
    	for (int i = 0;i < writerNames.length;i++) {
    		System.out.println(writerNames[i]);
    	}
    	*/
		JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showDialog(null, "Save");
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
				ImageIO.write(BI, "png", file);
//				System.out.println(file.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
        } 
    }
	/**
	 * Parses input and applies GND filter.
	 */
	private void gndFilterListener() {
			JTextField rField = new JTextField(3);
			JTextField gField = new JTextField(3);
			JTextField bField = new JTextField(3);
			JTextField hardField = new JTextField(5);
			
			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel("R:"));
			myPanel.add(rField);
			myPanel.add(Box.createHorizontalStrut(5)); // a spacer
			myPanel.add(new JLabel("G:"));
			myPanel.add(gField);
			myPanel.add(Box.createHorizontalStrut(5)); // a spacer
			myPanel.add(new JLabel("B:"));
			myPanel.add(bField);
			myPanel.add(Box.createHorizontalStrut(5)); // a spacer
			myPanel.add(new JLabel("hard? true/false"));
			myPanel.add(hardField);
			
			int result = JOptionPane.showConfirmDialog(null, myPanel, 
			        "Enter normalized RGB values and hardness", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
		          try {
		        	  String in1 = rField.getText(), in2 = gField.getText(), in3 = bField.getText(), in4 = hardField.getText();
		        	  boolean hard = Boolean.parseBoolean(in4);
		        	  if (in1.equals("") && in2.equals("") && in3.equals("")) {
		        		  Methods.GND(hard);
		        		  return;
		        	  }
		        	  double red = Double.parseDouble(in1);
		        	  double green = Double.parseDouble(in2);
		        	  double blue = Double.parseDouble(in3);
		        	  
		        	  if (!(red >= 0.0 && red <= 1.0 && green >= 0.0 && green <= 1.0 && blue >= 0.0 && blue <= 1.0)) {
		        		  throw new IOException();
		        	  }
		        	  
		        	  Methods.GND(hard, red, green, blue);
		        	  
		          }
		          catch (Exception exc) {
		        	  JOptionPane.showMessageDialog(this, "Wrong input! RGB values must"
		        	  		+ " be normalized to [0.0, 1.0].", "Input error", JOptionPane.ERROR_MESSAGE);
		          }
		       }
		
	}
	
	/**
	 * Parses input and applies filter specified at {@code which}.
	 * @param which minimum/maximum/median
	 */
	private void factorFilterActionListener(String which) {
		JTextField factorField = new JTextField(3);
		JTextArea description = new JTextArea("Enter number of times to succesively apply the filter");
		description.setEditable(false);
		description.setOpaque(false);
		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(2,1));
		myPanel.add(description);
		JPanel bottomPane = new JPanel();
		
		bottomPane.add(new JLabel("factor:"));
		bottomPane.add(factorField);
		myPanel.add(bottomPane);
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
		        "Enter values", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
		      try {
		    	  String in1 = factorField.getText();
		    	  int factor = Integer.parseInt(in1);

		    	  if (which == "minimum") Methods.minimumFilter(factor);
		    	  else if (which == "maximum") Methods.maximumFilter(factor);
		    	  else if (which == "median") Methods.medianFilter(factor);
		    	  else assert(false);
		      }
		      catch (Exception exc) {
		    	  JOptionPane.showMessageDialog(this, "Wrong input! Enter a number.", "Input error", JOptionPane.ERROR_MESSAGE);
		      }
		   }
	}
	/**
	 * 
	 * Parses input and applies GND filter.
	 * @param which const/cyclic
	 */
	void increaseParser(String which) { // constant and cyclic
		JTextField rField = new JTextField(3);
		JTextField gField = new JTextField(3);
		JTextField bField = new JTextField(3);
		JTextField factorField = new JTextField(5);
		
		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("R:"));
		myPanel.add(rField);
		myPanel.add(Box.createHorizontalStrut(5)); // a spacer
		myPanel.add(new JLabel("G:"));
		myPanel.add(gField);
		myPanel.add(Box.createHorizontalStrut(5)); // a spacer
		myPanel.add(new JLabel("B:"));
		myPanel.add(bField);
		myPanel.add(Box.createHorizontalStrut(5)); // a spacer
		myPanel.add(new JLabel("factor:"));
		myPanel.add(factorField);
		
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
		        "Enter normalized RGB values and boost percent", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
	          try {
	        	  String in1 = rField.getText(), in2 = gField.getText(), in3 = bField.getText(), in4 = factorField.getText();
	        	  double red = Double.parseDouble(in1);
	        	  double green = Double.parseDouble(in2);
	        	  double blue = Double.parseDouble(in3);
	        	  int factor = Integer.parseInt(in4);
	        	  if (!(red >= 0.0 && red <= 1.0 && green >= 0.0 && green <= 1.0 && blue >= 0.0 && blue <= 1.0 && factor >= 0 && factor <= 100)) {
	        		  throw new IOException();
	        	  }
	        	  if (which == "const") {
	        		  System.out.println("const");
	        		  Methods.increaseRGBconstant(red, green, blue, factor);
	        	  }
	        	  else if (which == "cyclic") {
	        		  System.out.println("cyclic");
	        		  Methods.increaseRGBcyclic(red, green, blue, factor);
	        	  }
	        	  else assert(false);
	        	  
	          }
	          catch (Exception exc) {
	        	  JOptionPane.showMessageDialog(this, "Wrong input! RGB values must"
	        	  		+ " be normalized to [0.0, 1.0].", "Input error", JOptionPane.ERROR_MESSAGE);
	          }
	       }
	}

	private void constIncreaseListener() {
		increaseParser("const");
	}
	private void cyclicIncreaseListener() {
		increaseParser("cyclic");
	}
	/**
	 * Parses input and applies Random Increase effect.
	 */
	private void randomIncreaseListener() {
		JTextField rField = new JTextField(3);
		JTextField gField = new JTextField(3);
		JTextField bField = new JTextField(3);
		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("R:"));
		myPanel.add(rField);
		myPanel.add(Box.createHorizontalStrut(5)); // a spacer
		myPanel.add(new JLabel("G:"));
		myPanel.add(gField);
		myPanel.add(Box.createHorizontalStrut(5)); // a spacer
		myPanel.add(new JLabel("B:"));
		myPanel.add(bField);
		
		int result = JOptionPane.showConfirmDialog(null, myPanel, 
		        "Enter normalized RGB values and boost percent", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
	          try {
	        	  String in1 = rField.getText(), in2 = gField.getText(), in3 = bField.getText();
	        	  double red = Double.parseDouble(in1);
	        	  double green = Double.parseDouble(in2);
	        	  double blue = Double.parseDouble(in3);

	        	  if (!(red >= 0.0 && red <= 1.0 && green >= 0.0 && green <= 1.0 && blue >= 0.0 && blue <= 1.0)) {
	        		  throw new IOException();
	        	  }
	        	  Methods.increaseRGBrandom(red, green, blue);
	          }
	          catch (Exception exc) {
	        	  JOptionPane.showMessageDialog(this, "Wrong input! RGB values must"
	        	  		+ " be normalized to [0.0, 1.0] and factor has to be in [0, 100].", "Input error", JOptionPane.ERROR_MESSAGE);
	          }
	       }
	}
	
	/**
	 * Fills the 4 menus and adds accelerators for saving and inverting the image.
	 */
	void createMenus () {
		
		menu1.add(randomNoise);		
		menu1.add(increaseRGBrandom);
		menu1.add(increaseRGBcyclic);
		menu1.add(round);
		menu1.add(plotData);
		
		menu2.add(minFilter);
		menu2.add(maxFilter);
		menu2.add(medFilter);
		menu2.add(openFilter);
		menu2.add(closeFilter);
		menu2.add(sobelFilter);
		menu2.add(gndFilter);
		
		menu3.add(increaseRGBconst);
		menu3.add(invert);
		menu3.add(sharpenHard);
		menu3.add(sharpenSoft);
		
		menuBar.add(menu2);
		menuBar.add(menu3);
		menuBar.add(menu1);
		menuBar.add(menu4);
		
		menu4.add(saveItem);
	
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
		saveItem.setAccelerator(ctrlS);
		KeyStroke ctrlX = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
		invert.setAccelerator(ctrlX);
		
		
		setJMenuBar(menuBar);
		
		
		
	}
	/**
	 * Parses input and applies Round effect.
	 */
	private void roundActionListener() {
		String prompt = JOptionPane.showInputDialog("How many digits of RGB values do you want to round off? 1-3");
		try {
			if (prompt == null || prompt == "" ) return;
			int x = Integer.parseInt(prompt);
			if (x < 1 || x > 3) throw new IOException();
			Methods.round(x);
		}
		catch (Exception exc) {
			JOptionPane.showMessageDialog(this, "Wrong input! Write a number between 1 and 3", "Input error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void main (String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	try {
					new ImageCompressor().setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
	}
	
}
