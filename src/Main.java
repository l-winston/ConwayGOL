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
	String name, filename;

	// window dimensions
	int width, height;

	// each square's dimensions
	int tile_width;
	int tile_height;

	// stores board state
	boolean[][] grid;

	// objects used for display
	JFrame frame;
	BufferedImage image;
	Canvas canvas = new Canvas();
	GraphicsEnvironment ge;
	GraphicsConfiguration gc;
	GraphicsDevice gd;
	Graphics graphics;
	Graphics2D g2d;
	BufferStrategy buffer;

	// timer to update each tick (turn) of the game
	Timer gameTick;
	// timer to register held down mouse as multiple clicks
	Timer clicker;

	// the last cell that was toggled (by mouse)
	Point lastOn = new Point(-1, -1);

	// when true, execute turns
	// when false, freeze
	boolean pause = true;

	public static void main(String args[]) {
		Main m = new Main("cgol", "save", 750, 750, 5, 5);
	}

	// constructor initializes objects for display and displays empty board
	public Main(String name, String filename, int width, int height, int board_width, int board_height) {
		this.width = width;
		this.height = height;
		this.name = name;
		this.filename = filename;

		this.tile_width = width / board_width;
		this.tile_height = height / board_height;

		grid = new boolean[board_height][board_width];
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		initFrame();
		draw();
	}

	// starts a timer that runs each turn of the game
	private void start() {
		gameTick = new Timer();
		gameTick.schedule(new TimerTask() {

			@Override
			public void run() {
				update();
				draw();
			}

		}, 0, 100);
	}

	// ends the timer
	private void stop() {
		gameTick.cancel();
	}

	// displays the board
	private void draw() {
		g2d = image.createGraphics();

		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, width, height);

		g2d.setColor(Color.white);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j])
					g2d.fillRect(j * tile_width, i * tile_height, tile_width, tile_height);
			}
		}

		graphics = buffer.getDrawGraphics();
		graphics.drawImage(image, 0, 0, null);
		g2d.dispose();
		graphics.dispose();
		buffer.show();
	}

	// increments the boardstate by 1 turn
	private void update() {
		boolean[][] temp = new boolean[grid.length][grid[0].length];

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				int n = count(i, j);
				temp[i][j] = (grid[i][j] && n >= 2 && n <= 3) || !grid[i][j] && n == 3;
			}
		}

		grid = temp;
	}

	// returns the number of adjacent live cells
	private int count(int i, int j) {
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

	// outputs the boardstate to a file
	private void saveToFile() {
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

	// reads boardstate from a file
	private void readFromFile() {
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

	// Initialization of objects needed for display
	private void initFrame() {

		frame = new JFrame(name);
		frame.setFocusable(false);
		frame.setVisible(true);
		canvas.setSize(width, height);
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
		image = gc.createCompatibleImage(width, height);
		// Objects needed for rendering...
		graphics = null;
		g2d = null;
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setFocusable(true);

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

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
					saveToFile();
					break;
				case KeyEvent.VK_L:
					readFromFile();
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				clicker = new Timer();
				clicker.schedule(new TimerTask() {

					@Override
					public void run() {
						Point currentMousePos = canvas.getMousePosition();
						if (currentMousePos == null)
							return;
						Point pos = new Point((int) (currentMousePos.getY() / tile_height),
								(int) (currentMousePos.getX() / tile_width));
						if (!pos.equals(lastOn))
							grid[pos.x][pos.y] ^= true;
						lastOn = pos;
						draw();
					}
				}, 0, 10);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				clicker.cancel();
				clicker = new Timer();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		});
	}
}
