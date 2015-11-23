package od.steps;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.pages.ReaderFromFile;
import od.pages.SubutaiPage;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SubutaiSteps extends ScenarioSteps {

    @Step
    public String getMngHost1() throws FileNotFoundException {
        return ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1");
    }

    @Step
    public String getMngHost2() throws FileNotFoundException {
        return ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2");
    }
}