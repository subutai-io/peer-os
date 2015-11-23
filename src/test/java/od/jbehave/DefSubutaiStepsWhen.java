package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;

import java.io.FileNotFoundException;

public class DefSubutaiStepsWhen {

    @Steps
    SubutaiSteps subutaiSteps;

    @When("the user click on the menu item: Environment")
    public void click_on_link_environment(){
        subutaiSteps.clickOnMenuItemEnvironment();
    }

    @When("the user click on the menu item: Blueprint")
    public void click_on_link_blueprint(){
        subutaiSteps.clickOnMenuItemBlueprint();
    }

    @When("the user click on the menu item: Environments")
    public void click_on_link_environments(){
        subutaiSteps.clickOnMenuItemEnvironments();
    }

    @When("the user click on the menu item: Containers")
    public void click_on_link_Containers(){
        subutaiSteps.clickOnMenuItemContainers();
    }
}