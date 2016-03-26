package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Given;
import org.junit.Before;
import org.sikuli.script.FindFailed;

import java.io.FileNotFoundException;

public class DefSubutaiStepsGiven {

    @Steps
    SubutaiSteps subutaiSteps;

    @Given("the first user is on the Home page of Subutai")
    public void first_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_mng_h1();
    }

    @Given("the second user is on the Home page of Subutai")
    public void second_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_mng_h2();
    }

    @Given("the user enters login and password: '$login', '$password'")
    public void enter_login_and_password(String login, String password) throws FileNotFoundException {

        subutaiSteps.inputLogin(login);
        subutaiSteps.inputPassword(password);
    }

    @Given("the user clicks on the button: Login")
    public void clicks_button_login() throws FindFailed {
        subutaiSteps.clickOnButtonLogin();
    }

    @Given("the user configure pgp plugin")
    public void configurePgpPlugin() throws FindFailed, InterruptedException {
        subutaiSteps.clickOnIconPgp();
        subutaiSteps.clickOnButtonOptions();
        subutaiSteps.typeInFieldsPgp();
        subutaiSteps.clickOnButtonSubmit();
        subutaiSteps.waitGeneratedE2EKey();
        subutaiSteps.clickOnSubutaiSocialTab();
    }

    @Before
    @Given("the user start record a video")
    public void recordStartVideo() throws FileNotFoundException {
        subutaiSteps.run_bash_script("src/test/resources/files/recordScreenStart.sh");
    }



}