import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Main class for the test cases
 */
@DisplayName("Concrete solver test cases")
abstract class AbstractPackingSolverTest {

    // The solver to be used for the test cases
    abstract AbstractSolver getSolver();

    /**
     * Test the solver against the gigantic library.
     */
    @TestFactory
    @DisplayName("Solver Test Factory")
    @Tag("library")
    Stream<DynamicTest> dynamicSolverTests() throws FileNotFoundException {
        List<DynamicTest> dynamicTests = new ArrayList<>();

        String path = "./test/input/Non-perfect fit/Martello, 1998";
        ArrayList<Double> average = new ArrayList<>();
        File folder = new File(path);
        File[] files = folder.listFiles();
        assert files != null;
        files = Arrays.stream(files).filter(File::isFile).toArray(File[]::new);

        for (File file : files) {

            Parameters params = (new UserInput(new FileInputStream(file))).getUserInput();

            AbstractSolver solver = this.getSolver();
            if (!solver.getHeightSupport().contains(params.heightVariant)) {
                continue;
            }

            Solution solution = solver.getSolution(params);
            DynamicTest dynamicTest = dynamicTest(file.getName(), () -> assertTrue(Util.isValidSolution(solution)));
            average.add(solution.getRate());
            dynamicTests.add(dynamicTest);
        }
        int av = 0;
        for (int i = 0; i < average.size(); i++) {
            av += average.get(i);
        }
        System.out.println("Average OPT rate of " + (double) av / (double) average.size());
        return dynamicTests.stream();
    }

    /**
     * All momotor test cases
     */
    @TestFactory
    @DisplayName("Momotor Test Cases")
    @Tag("momotor")
    Stream<DynamicTest> momotorTests() throws IOException {
        List<DynamicTest> dynamicTests = new ArrayList<>();

        ArrayList<String> paths = new ArrayList<>();

        paths.add("./test/momotor/prototype-1");
//        paths.add("./test/momotor/prototype-2");

        for (String path : paths) {
            // Get all files from the momotor folder
            File folder = new File(path);
            File[] files = folder.listFiles();
            assert files != null;
            files = Arrays.stream(files).filter(File::isFile).toArray(File[]::new);

            // Add a test for each input
            for (File file : files) {
                Parameters params = (new UserInput(new FileInputStream(file))).getUserInput();

                AbstractSolver solver = this.getSolver();
                if (!solver.getHeightSupport().contains(params.heightVariant)) {
                    continue;
                }

                Solution solution = solver.getSolution(params);

                DynamicTest dynamicTest = dynamicTest(file.getName(), () -> assertTrue(Util.isValidSolution(solution)));

                dynamicTests.add(dynamicTest);
            }
        }

        return dynamicTests.stream();

    }

}