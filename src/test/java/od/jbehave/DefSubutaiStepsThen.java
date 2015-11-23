package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;

import java.io.FileNotFoundException;

public class DefSubutaiStepsThen {

    @Steps
    SubutaiSteps subutaiSteps;

    @Then("the user observe items of Environment menu")
    public void see_items_Environment_menu(){
        subutaiSteps.seeEnvironmentMenuItemBlueprint();
        subutaiSteps.seeEnvironmentMenuItemEnvironments();
        subutaiSteps.seeEnvironmentMenuItemContainers();
    }
}