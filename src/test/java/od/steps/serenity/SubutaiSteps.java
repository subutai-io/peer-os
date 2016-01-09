package od.steps.serenity;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.classes.ReaderFromFile;
import od.pages.LoginPage;

import java.io.FileNotFoundException;

public class SubutaiSteps extends ScenarioSteps {

    //region IMPORT: Web Elements

    LoginPage loginPage;

    //endregion

    //region ACTION: Open Page

    @Step
    public void open_mng_h1() throws FileNotFoundException {
        loginPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        loginPage.open();
    }

    @Step
    public void open_mng_h2() throws FileNotFoundException {
        loginPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        loginPage.open();
    }

    //endregion

    //region ACTION: Type

    @Step
    public void inputLogin(String login){
        loginPage.inputLogin.type(login);
    }

    @Step
    public void inputPassword(String password){
        loginPage.inputPassword.type(password);
    }

    //endregion

    //region ACTION: Click

    @Step
    public void clickOnButtonLogin(){
        loginPage.buttonLogin.click();
        waitABit(5000);
    }

    //endregion

    //region ACTION: Wait

    //endregion

    //region VERIFICATION: AssertThat

    //endregion

}
