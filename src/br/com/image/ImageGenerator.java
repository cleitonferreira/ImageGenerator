package br.com.image;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageGenerator extends JPanel {

	private static class Pixel implements Comparable<Pixel> {
		private final int x, y;
		private int r, g, b;
		private int fitness;

		private Pixel(int x, int y, int r, int g, int b) {
			this.x = x;
			this.y = y;
			this.r = r;
			this.g = g;
			this.b = b;
			this.fitness = 0;
		}

		private void cloneColors(Pixel original) {
			this.r = original.r;
			this.g = original.g;
			this.b = original.b;
		}

		@Override
		public int compareTo(Pixel o) {
			return Integer.compare(fitness, o.fitness);
		}
	}

	private static ImageGenerator imgGenerator;

	private static final long serialVersionUID = 8241401773325067709L;

	private static final int SIZE = 700;

	private static final int N = (SIZE * SIZE) / 10000;

	private static final int MUTATION_CHANCE = 10; // 5%

	private static final int MUTATION_DELTA_MAX = 32;

	private static final Color[] COLORS = new Color[] { Color.BLACK, Color.BLUE, Color.GRAY, Color.GREEN, Color.CYAN,
			Color.RED, Color.WHITE, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.LIGHT_GRAY, Color.PINK };

	private static final Random rand = new Random();

	private final synchronized Color getSetColor(boolean set) {
		if (set) {
			currentColor = COLORS[rand.nextInt(COLORS.length)];
		}
		return currentColor;
	}

	private Color currentColor;

	private final List<Pixel> pixels = new ArrayList<>(SIZE * SIZE);

	private BufferedImage image;

	private ImageGenerator() {
		getSetColor(true);
		image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		ImageIcon icon = new ImageIcon(image);
		add(new JLabel(icon));
		initialize();
		refresh();
	}

	private static void createAndShowGUI() {
		imgGenerator = new ImageGenerator();
		JFrame frame = new JFrame("ImageGenerator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(imgGenerator);
		frame.setLocationByPlatform(true);
		frame.pack();
		frame.setVisible(true);
		new Thread(() -> {
			while (true) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
					imgGenerator.refresh();
					imgGenerator.repaint();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static void showColorChangeButton() {
		new Thread(() -> {
			BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			ImageIcon icon = new ImageIcon(img);
			JFrame frame = new JFrame("Color changer");
			JPanel panel = new JPanel();
			JButton button = new JButton("Change");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			panel.add(new JLabel(icon));
			panel.add(button);
			frame.add(panel);
			setColorChangeImage(img);
			button.addActionListener(e -> {
				imgGenerator.getSetColor(true);
				setColorChangeImage(img);
				panel.repaint();
			});
			frame.setLocationByPlatform(true);
			frame.pack();
			frame.setVisible(true);
		}).start();
	}

	private static synchronized void setColorChangeImage(BufferedImage img) {
		Color newColor = imgGenerator.getSetColor(false);
		for (int x = 0; x < 30; x++) {
			for (int y = 0; y < 30; y++) {
				img.setRGB(x, y, newColor.getRGB());
			}
		}
	}

	private synchronized void refresh() {
		for (Pixel p : pixels) {
			Color color = getSetColor(false);
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
			p.fitness = (Math.abs(p.r - r) + Math.abs(p.g - g) + Math.abs(p.b - b)) / 3;
		}
		Collections.sort(pixels);
		List<Pixel> topN = pixels.subList(0, N);
		List<Pixel> middle = pixels.subList(N, pixels.size() - N);
		List<Pixel> worstN = pixels.subList(pixels.size() - N, pixels.size());
		for (Pixel p : middle) {
			Pixel randTop = topN.get(rand.nextInt(topN.size()));
			p.r = (3 * p.r + randTop.r) / 4;
			p.g = (3 * p.g + randTop.g) / 4;
			p.b = (3 * p.b + randTop.b) / 4;
			if (rand.nextInt(100) <= MUTATION_CHANCE) {
				p.r = p.r + (rand.nextBoolean() ? 1 : -1) * rand.nextInt(MUTATION_DELTA_MAX);
				p.g = p.g + (rand.nextBoolean() ? 1 : -1) * rand.nextInt(MUTATION_DELTA_MAX);
				p.b = p.b + (rand.nextBoolean() ? 1 : -1) * rand.nextInt(MUTATION_DELTA_MAX);
			}
		}
		for (Pixel p : worstN) {
			p.cloneColors(middle.get(rand.nextInt(middle.size())));
		}
		for (Pixel p : pixels) {
			p.r = Math.min(Math.max(p.r, 0), 255);
			p.g = Math.min(Math.max(p.g, 0), 255);
			p.b = Math.min(Math.max(p.b, 0), 255);
			image.setRGB(p.x, p.y, new Color(p.r, p.g, p.b).getRGB());
		}
	}

	private void initialize() {
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				int r = rand.nextInt(256);
				int g = rand.nextInt(256);
				int b = rand.nextInt(256);
				pixels.add(new Pixel(x, y, r, g, b));
			}
		}
	}

	public static void main(String[] args) {
		createAndShowGUI();
		showColorChangeButton();
	}
}
