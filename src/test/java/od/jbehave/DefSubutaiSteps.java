package od.jbehave;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.annotations.Steps;
import od.pages.ReaderFromFile;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DefSubutaiSteps {

    @Step
    public String getMngHost1() throws FileNotFoundException {
        return ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1");
    }

    @Step
    public String getMngHost2() throws FileNotFoundException {
        return ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2");
    }
}