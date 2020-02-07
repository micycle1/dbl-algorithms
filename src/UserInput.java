import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UserInput {
    private Scanner sc;

    public UserInput(InputStream is) {
        this.sc = new Scanner(is);
    }

    public Parameters getUserInput() {
        Parameters params = new Parameters();
        List<Object> height = getHeight();
        params.heightVariant = (String) height.get(0);
        params.height = (Integer) height.get(1);
        params.rotationVariant = getRotation();
        params.rectangles = getRectangles();
        return params;
    }

    private List<Object> getHeight() {
        // Getting the height variant
        String heightVariant = readInputParameter();
        Integer height = null;
        if (heightVariant.contains("fixed")) {
            height = Integer.parseInt(heightVariant.split(" ")[1]);
            heightVariant = heightVariant.split(" ")[0];
        }
        System.out.println();
        System.out.println("Height variant: " + heightVariant + (height != null ? " with height " + height : ""));

        return Arrays.asList(heightVariant, height);
    }

    private boolean getRotation() {
        // Getting the rotations variant
        String rotationsVariant = readInputParameter();
        System.out.println("Rotations variant: " + rotationsVariant.startsWith("y"));
        return rotationsVariant.startsWith("y");
    }

    private Rectangle[] getRectangles() {
        // Getting the rectangles
        int nRectangles = Integer.parseInt(readInputParameter());
        if (nRectangles <= 0) {
            throw new IllegalArgumentException("The number of rectangles must be positive.");
        }
        System.out.println("Getting " + nRectangles + " rectangles");
        Rectangle[] rectangles = new Rectangle[nRectangles];
        for (int i = 0; i < nRectangles; i++) {
            int[] rec = Arrays.stream(sc.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            rectangles[i] = new Rectangle(rec[0], rec[1]);
        }

        for (Rectangle rec : rectangles) {
            System.out.println(" " + rec.getSize());
        }
        return rectangles;
    }

    private String readInputParameter() {
        return this.sc.nextLine().split(":")[1].trim();
    }
}