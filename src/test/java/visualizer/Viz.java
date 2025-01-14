package visualizer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import jacenre.dbla.AbstractSolver;
import jacenre.dbla.BottomUpSolver;
import jacenre.dbla.CompoundSolver;
import jacenre.dbla.CompressionSolver;
import jacenre.dbla.FirstFitSolver;
import jacenre.dbla.GeneticSolver;
import jacenre.dbla.Parameters;
import jacenre.dbla.Rectangle;
import jacenre.dbla.ReverseFitSolver;
import jacenre.dbla.SimpleTopLeftSolver;
import jacenre.dbla.SkylineSolver;
import jacenre.dbla.Solution;
import jacenre.dbla.TopLeftSolver;
import jacenre.dbla.UserInput;
import jacenre.dbla.Util;
import jacenre.dbla.Util.HeightSupport;
import net.jafama.FastMath;
import processing.core.PApplet;

@SuppressWarnings("Duplicates")
public class Viz extends PApplet {

	private static final double zoomFactor = 2;

	private Viewport activeView = null;
	private ArrayList<Viewport> viewports = new ArrayList<>();
	private int active = 0;

	// Due to concurrency P3 width and height dont work as expected.
	private int DEFAULT_WIDTH = 1000;
	private int DEFAULT_HEIGHT = 1000;

	static boolean USER_INPUT;

	private int range = 0;

	public static void main(String[] args) {
		System.setProperty("sun.java2d.uiScale", "1.0");
		System.setProperty("prism.allowhidpi", "false");
		PApplet.main(Viz.class, args);
	}

	@Override
	public void settings() {
		SplittableRandom r = new SplittableRandom();
		List<Rectangle> rects = Stream.generate(() -> new Rectangle(r.nextInt(15, 300), r.nextInt(15, 300))).limit(500)
				.collect(Collectors.toList());
		Parameters params = new Parameters(3000, HeightSupport.FIXED, true, rects);

		List<AbstractSolver> solvers = new ArrayList<>();
		solvers.add(new FirstFitSolver());
		solvers.add(new TopLeftSolver());
		solvers.add(new CompressionSolver());
		solvers.add(new ReverseFitSolver());
		solvers.add(new SimpleTopLeftSolver());
		solvers.add(new BottomUpSolver());
		solvers.add(new GeneticSolver(new TopLeftSolver(), true));
//        solvers.add(new CompoundSolver().addSolver(new FirstFitSolver()).addSolver(new SkylineSolver()));
//        solvers.add(new SkylineSolver());

		range = r.nextInt(90, 180);

//        XYChart chart = new XYChartBuilder().xAxisTitle("Height").yAxisTitle("Area").theme(Styler.ChartTheme.Matlab).width(1600).height(900).build();
//        chart.getStyler().setYAxisMin(0.0);

		solvers.parallelStream().forEach(solver -> {
			if (solver.canSolveParameters(params)) {
				Solution solution = solver.getSolution(params.copy());
				viewports.add(new Viewport(solution));
//				if (solution.parameters.heightVariant.equals(Util.HeightSupport.FREE)) {
//					ChartMaker.addSeries(chart, solver, solution);
//				}
			}
		});

//        if (params.copy().heightVariant.equals(Util.HeightSupport.FREE)) {
//            new SwingWrapper<>(chart).displayChart();
//        }

		activeView = viewports.get(0);

		size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	@Override
	public void setup() {
		colorMode(HSB, 360, 100, 100);
	}

	@Override
	public void draw() {
		background(0, 0, 100);
		if (activeView != null) {
			// If the view has overlap turn the text red.
			int TEXT_X = 50;
			int TEXT_Y = 50;
			if (activeView.overlap.size() != 0) {
				fill(0, 100, 100);
				text("OVERLAP!", TEXT_X, TEXT_Y - 15);
			} else {
				fill(0, 0, 0);
			}
			text(activeView.solution.solvedBy.getClass().getSimpleName(), TEXT_X, TEXT_Y);
			text("Size = " + activeView.solution.getArea(), TEXT_X, TEXT_Y + 15);
			activeView.draw();
		}
	}

	public void drawParameter(Parameters parameters) {
		activeView = new Viewport(parameters);
	}

	private int oldX;
	private int oldY;
	private boolean moving = false;

	@Override
	public void mousePressed() {
		oldX = mouseX;
		oldY = mouseY;
	}

	@Override
	public void mouseReleased() {
		moving = false;
	}

	@Override
	public void keyPressed() {
		if (keyCode == RIGHT) {
			if (active < viewports.size() - 1) {
				active++;
			} else {
				active = 0;
			}
			activeView.reset();
		} else if (keyCode == LEFT) {
			if (active >= 1) {
				active--;
			} else {
				active = viewports.size() - 1;
			}
			activeView.reset();
		} else if (keyCode == UP) {
			// zoom in
			zoom(zoomFactor);
		} else if (keyCode == DOWN) {
			// zoom out
			zoom(1 / zoomFactor);
		}
		activeView = viewports.get(active);
	}

	/* Zooms view centered at mouse pointer. */
	private void zoom(double factor) {
		activeView.maxSize *= factor;
		activeView.setScale(factor);
	}

	@Override
	public void mouseDragged() {
		// If you click on the strip, move it
		if (activeView.boundingBox.contains(new Point(mouseX, mouseY)) || moving) {
			moving = true;
			activeView.setX(activeView.x - oldX + mouseX);
			activeView.setY(activeView.y - oldY + mouseY);
		}
		oldX = mouseX;
		oldY = mouseY;
	}

	/**
	 * Object responsible for drawing parameters as rectangles
	 */
	class Viewport {

		// Max size of either the width or height.
		int maxSize = 800;

		int x = 100;

		public void setX(int x) {
			this.x = x;
			this.boundingBox.x = x;
		}

		int y = 100;

		public void setY(int y) {
			this.y = y;
			this.boundingBox.y = y;
		}

		int solutionWidth;
		int solutionHeight;

		Rectangle boundingBox;

		// Scale factor
		int drawWidth;
		int drawHeight;

		Solution solution;

		void reset() {
			this.maxSize = 800;
			this.x = 100;
			this.y = 100;
			setScale();
		}

		// Arraylist filled with all the ID's of overlapping rectangles.
		ArrayList<Rectangle> overlap = new ArrayList<>();

		void setup() {
			for (Rectangle rectangle : this.solution.parameters.rectangles) {
				if (rectangle.x < 0 || rectangle.y < 0) {
					overlap.add(rectangle);
				}
				smallest = Math.min(smallest, rectangle.width * rectangle.height);
				largest = Math.max(largest, rectangle.width * rectangle.height);
			}
			for (Rectangle rectangle1 : this.solution.parameters.rectangles) {
				for (Rectangle rectangle2 : this.solution.parameters.rectangles) {
					if (rectangle1 != rectangle2) {
						if (rectangle1.intersects(rectangle2)) {
							overlap.add(rectangle1);
							overlap.add(rectangle2);
						}
					}
				}
			}
			setScale();
		}

		private void updateSizes() {
			this.solutionHeight = (int) this.solution.getHeight();
			this.solutionWidth = (int) this.solution.getWidth();

			if (this.solutionWidth > this.solutionHeight) {
				drawWidth = maxSize;
				drawHeight = solutionHeight * maxSize / this.solutionWidth;
			} else {
				drawWidth = solutionWidth * maxSize / solutionHeight;
				drawHeight = maxSize;
			}
		}

		void setScale() {
			updateSizes();
			// translate rectangle position
			this.x = (DEFAULT_WIDTH - drawWidth) / 2;
			this.y = (DEFAULT_HEIGHT - drawHeight) / 2;

			boundingBox = new Rectangle(this.x, this.y, drawWidth, drawHeight);
		}

		void setScale(double factor) {
			// update sizes
			updateSizes();

			double scaleChange = 1 - factor;

			double dx = (mouseX - this.x) * scaleChange;
			double dy = (mouseY - this.y) * scaleChange;

			// translate rectangle position
			this.x = this.x + (int) dx;
			this.y = this.y + (int) dy;

			boundingBox = new Rectangle(this.x, this.y, drawWidth, drawHeight);

		}

		int smallest = Integer.MAX_VALUE;
		int largest = 0;

		Viewport(Parameters parameters) {
			// Wrap the parameters in a solution object.
			this.solution = new Solution(parameters);
			setup();
		}

		Viewport(Solution solution) {
			this.solution = solution;
			setup();
		}

		void draw() {
			fill(0, 0, 100, 100);
			for (Rectangle rectangle : solution.parameters.rectangles) {
				float hue;
				// If rectangle is overlapping draw it bright red.
				if (activeView.overlap.contains(rectangle)) {
					stroke(0, 100, 100);
					fill(0, 0, 0);
				} else {
					hue = map((float) FastMath.log(rectangle.width * rectangle.height), (float) FastMath.log(activeView.smallest),
							(float) FastMath.log(activeView.largest), range, range + 180);
					fill(hue, 100, 100);
					stroke(0, 0, 0);
				}
				float x = map(rectangle.x, 0, solution.getWidth(), 0, drawWidth);
				float y = map(rectangle.y, 0, solution.getHeight(), 0, drawHeight);
				float rectw = map(rectangle.width, 0, solution.getWidth(), 0, drawWidth);
				float recth = map(rectangle.height, 0, solution.getHeight(), 0, drawHeight);
				rect(x + this.x, y + this.y, rectw, recth);
			}
			stroke(0, 0, 0);
			fill(0, 0, 0, 0);
			rect(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
		}

	}

}
