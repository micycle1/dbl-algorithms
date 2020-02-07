/**
 * Data type to store all the paramaters of the algorithm.
 * @see <a href="https://canvas.tue.nl/files/1978093/download?download_frd=1"> Problem description 2.1 Input Format</a>
 */
public class Parameters {

    /**
     * Describes the variant that needs to be solved.
     * Where varient \in {free, fixed}
     */
    public String heightVariant;

    /**
     *  Describes the height of {@link Parameters#heightVariant} if {@code heightVarient.equals("fixed")}
     */
    public Integer height = null;

    /**
     * True if rotation of rectangles is allowed.
     */
    public boolean rotationVariant;

    /**
     * Arraylist that stores all the rectangles.
     */
    public Rectangle[] rectangles;
}
