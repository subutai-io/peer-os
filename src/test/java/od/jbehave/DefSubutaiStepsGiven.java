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

public class DefSubutaiStepsGiven {

    @Steps
    SubutaiSteps subutaiSteps;

    @Given("the first user is on the home page of Subutai")
    public void first_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_mng_h1();
    }

    @Given("the second user is on the home page of Subutai")
    public void second_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_mng_h2();
    }

    @Given("the ARM user is on the home page of Subutai")
    public void arm_user_open_home_page(){
        subutaiSteps.open_mng_h_arm();
    }

    @Given("the user enters login and password: '$login', '$password'")
    public void enter_login_and_password(String login, String password){
        subutaiSteps.inputLogin(login);
        subutaiSteps.inputPassword(password);
    }

    //-------3578

    @Given("the first AWS user is on the home page of Subutai")
    public void first_aws_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_aws_mng_h1();
    }

    @Given("the second AWS user is on the home page of Subutai")
    public void second_aws_user_open_home_page() throws FileNotFoundException {
        subutaiSteps.open_aws_mng_h2();
    }

    @Given("the user clicks on the button: Login")
    public void clicks_button_login(){
        subutaiSteps.clickOnButtonLogin();
    }

    //-------3023

    @Given("the user should observes blueprint header")
    public void see_blueprint_header(){
        subutaiSteps.seeHeaderBlueprint();
    }

    @Given("the vagrant user is on the home page of Subutai")
    public void opens_vagrant_mghost() throws FileNotFoundException {
        subutaiSteps.waitSleep(120000);
        subutaiSteps.open_vagrant_mgh();
    }

    //------3298

    @Given("the user opens pgp plugin")
    public void opens_pgp_plugin() throws FileNotFoundException {
        subutaiSteps.open_mng_h1();
        subutaiSteps.run_bash_script("src/test/resources/files/pgpStart.sh");
    }

    //-------3117

    @Given("the user is on subut.ai page")
    public void opens_subutai_local_page(){
        subutaiSteps.open_local_subutai_page();
    }
}