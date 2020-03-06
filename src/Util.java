
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;

/**
 * Global Util class for commonly used function and constants.
 */
public class Util {

    /**
     * Used for checking height variants.
     */
    public enum HeightSupport {
        FIXED,
        FREE
    }

    /**
     * Creates a deep copy of a {@link Rectangle} ArrayList with the same {@link Rectangle#getId()} for each rectangle.
     *
     * @param rects Input array to copy
     * @return A deep copy ArrayList.
     */
    public static ArrayList<Rectangle> cloneRectangleState(ArrayList<Rectangle> rects) {
        ArrayList<Rectangle> rectangles = new ArrayList<>();
        for (Rectangle rect :
                rects) {
            rectangles.add(rect.copy());
        }
        return rectangles;
    }

    static boolean animate = false;

    /**
     * Used for animating the current parameters.
     *
     * @param parameters Parameters to animate.
     * @param solver     Solver that called the animation.
     *                   TODO COMMENT OUT BEFORE HANDING IN.
     */
    public static void animate(Parameters parameters, AbstractSolver solver) {
        if (animate && Animator.getInstance() != null){
            Animator.getInstance().draw();
            Animator.getInstance().drawParameter(parameters, solver);
        }
    }

    public static void animate() {
        if (animate && Animator.getInstance() != null) {
            Animator.getInstance().draw();
        }
    }

    /**
     * Times how long it takes to find a solution, used for debugging and test cases.
     *
     * @param parameters the Parameters to be packed
     * @param solver     the Solver to do the packing
     */
    public static boolean timedPacker(Parameters parameters, AbstractSolver solver) {
        long startTime = System.nanoTime();

        Solution solution = solver.getSolution(parameters);

        long endTime = System.nanoTime();

        //divide by 1000000 to get milliseconds
        long duration = (endTime - startTime) / 1000000;
        System.out.println("Packing took " + duration + "ms");

        if (duration > 30000) {
            System.err.println("Packing took longer than 30 seconds");
            return false;
        }

        return isValidSolution(solution);
    }

    /**
     * Sweepline algorithm to check if the solution has overlap.
     *
     * @param solution the Solution to check for overlap
     * @return boolean value representing if there is overlap
     */
    public static boolean sweepline(Solution solution) {
        boolean overlap = false;

        class segment {
            boolean left;

            segment() {

            }

        }

        // Deep copy to prevent modification of the solution
        Solution solutionCopy = solution.copy();

        return overlap;
    }

    /**
     * Check if the solution found by the solver is valid.
     *
     * @param solution the solution to be checked
     * @return a boolean that is true if the solution is valid.
     */
    public static boolean isValidSolution(Solution solution) {
        double rate = solution.getRate();

        // Test report
        System.out.println(solution);

        if (hasOverlapping(solution.parameters.rectangles)) {
            System.err.println("There are overlapping rectangles");
            return false;
        }

        for (Rectangle rectangle :
                solution.parameters.rectangles) {
            if (rectangle.x < 0 || rectangle.y < 0) {
                System.err.println("Negative coordinates found");
                return false;
            }
        }

        if (solution.parameters.heightVariant == Util.HeightSupport.FIXED) {
            for (Rectangle rectangle : solution.parameters.rectangles) {
                if (rectangle.y + rectangle.height > solution.parameters.height) {
                    System.err.println("The height limit is not maintained");
                    return false;
                }
            }
        }

        if (rate < 1) {
            System.err.println("Impossible result.");
            return false;
        }

        return true;
    }

    /**
     * Test to see if any of the rectangles in the list overlap.
     *
     * @return a boolean that is true if there is overlap.
     * TODO Improve runtime
     */
    public static boolean hasOverlapping(ArrayList<Rectangle> rectangles) {
        for (Rectangle rectangle1 :
                rectangles) {
            for (Rectangle rectangle2 :
                    rectangles) {
                if (!rectangle1.equals(rectangle2)) {
                    if (rectangle1.intersects(rectangle2)) {
                        System.out.println(rectangle1.getId());
                        System.out.println(rectangle2.getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

}