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

    @Then("the user observe button: Create Blueprint")
    public void see_button_create_blueprint(){
        subutaiSteps.seeButtonCreateEnvironment();
    }

    @Then("the user observe field: Enter blueprint name")
    public void see_field_enter_blueprint_name(){
        subutaiSteps.seeFieldEnterBlueprintName();
    }

    @Then("the user observe node list item")
    public void see_node_list_item()
    {
        subutaiSteps.seeNodeListItem();
    }
}