package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Given;

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
    public void enter_login_and_password(String login, String password){
        subutaiSteps.inputLogin(login);
        subutaiSteps.inputPassword(password);
    }

    @Given("the user clicks on the button: Login")
    public void clicks_button_login(){
        subutaiSteps.clickOnButtonLogin();
    }


}