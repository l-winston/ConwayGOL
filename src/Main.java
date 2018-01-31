import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class Main {
	static final String saveFile = "save";

	// window size
	static final int X = 750;
	static final int Y = 750;

	static final int TILE_WIDTH = 10;
	static final int TILE_HEIGHT = 10;

	static boolean[][] grid = new boolean[Y / TILE_HEIGHT][X / TILE_WIDTH];

	static JFrame frame;
	static BufferedImage image = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
	static Canvas canvas = new Canvas();
	static GraphicsEnvironment ge;
	static GraphicsConfiguration gc;
	static GraphicsDevice gd;
	static Graphics graphics;
	static Graphics2D g2d;
	static BufferStrategy buffer;

	static Timer gameTick;
	static Timer clicker;

	static Point lastOn = new Point(-1, -1);

	static boolean pause = true;

	public static void main(String args[]) {
		initFrame();
		draw();
	}

	private static void start() {
		gameTick = new Timer();
		gameTick.schedule(new TimerTask() {

			@Override
			public void run() {
				update();
				draw();
			}

		}, 0, 100);
	}

	private static void stop() {
		gameTick.cancel();
	}

	private static void draw() {
		g2d = image.createGraphics();

		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, X, Y);

		g2d.setColor(Color.white);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j])
					g2d.fillRect(j * TILE_WIDTH, i * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
			}
		}

		graphics = buffer.getDrawGraphics();
		graphics.drawImage(image, 0, 0, null);
		g2d.dispose();
		graphics.dispose();
		buffer.show();
	}

	private static void update() {
		boolean[][] temp = new boolean[grid.length][grid[0].length];

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				int n = count(i, j);
				temp[i][j] = (grid[i][j] && n >= 2 && n <= 3) || !grid[i][j] && n == 3;
			}
		}

		grid = temp;
	}

	private static int count(int i, int j) {
		int ret = 0;
		try {
			if (grid[i - 1][j - 1])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i - 1][j])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i - 1][j + 1])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i][j - 1])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i][j + 1])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i + 1][j - 1])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i + 1][j])
				ret++;
		} catch (Exception e) {
		}

		try {
			if (grid[i + 1][j + 1])
				ret++;
		} catch (Exception e) {
		}

		return ret;
	}

	private static void saveToFile(String filename) {
		PrintWriter pw = null;

		try {
			pw = new PrintWriter(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		pw.println(grid.length + " " + grid[0].length);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				pw.print((grid[i][j] ? 1 : 0) + " ");
			}
			pw.println();
		}

		pw.close();
	}

	private static void readFromFile(String filename) {
		Scanner scan = null;

		try {
			scan = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int i_m = scan.nextInt();
		int j_m = scan.nextInt();

		if (i_m != grid.length || j_m != grid[0].length) {
			System.err.println("save file dimensions incompatible!");
			return;
		}

		for (int i = 0; i < i_m; i++) {
			for (int j = 0; j < j_m; j++) {
				grid[i][j] = scan.nextInt() == 0 ? false : true;
			}
		}
		
		draw();
	}

	private static void initFrame() {

		frame = new JFrame("GOL");
		frame.setFocusable(false);
		frame.setVisible(true);
		canvas.setSize(X, Y);
		frame.add(canvas);
		frame.pack();
		frame.setIgnoreRepaint(true);
		canvas.createBufferStrategy(2);
		buffer = canvas.getBufferStrategy();
		// Get graphics configuration...
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		// Create off-screen drawing surface
		image = gc.createCompatibleImage(X, Y);
		// Objects needed for rendering...
		graphics = null;
		g2d = null;
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setFocusable(true);

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {

				case KeyEvent.VK_SPACE:
					pause ^= true;
					if (!pause)
						start();
					else
						stop();
					break;
				case KeyEvent.VK_S:
					saveToFile(saveFile);
					break;
				case KeyEvent.VK_L:
					readFromFile(saveFile);
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
		});

		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				clicker = new Timer();
				clicker.schedule(new TimerTask() {

					@Override
					public void run() {
						Point currentMousePos = canvas.getMousePosition();
						if (currentMousePos == null)
							return;
						Point pos = new Point((int) (currentMousePos.getY() / TILE_HEIGHT),
								(int) (currentMousePos.getX() / TILE_WIDTH));
						if (!pos.equals(lastOn))
							grid[pos.x][pos.y] ^= true;
						lastOn = pos;
						draw();
					}
				}, 0, 10);
			}

			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}

		});
	}
}
