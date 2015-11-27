package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
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

    @Then("the user observe created blueprint")
    public void see_created_blueprint(){
        subutaiSteps.seeCreatedBlueprint();
    }

    @Then("the user observe build environment")
    public void see_build_environment(){
        subutaiSteps.seeBuildEnvironment();
    }

    @Then("the user observe icon: two containers")
    public void see_icon_two_containers(){
        subutaiSteps.seeIconTwoContainers();
    }

    @Then("the user observe popup: Build Environment")
    public void see_popup_build_environment(){
        subutaiSteps.seePopup();
    }

    @Then("the user observe header: Success!")
    public void see_header_success(){
        subutaiSteps.seeHeaderSuccess();
    }

    @Then("the user observe text: Your environment start creation.")
    public void see_text_your_environment_start_creation(){
        subutaiSteps.seeTextYourEnvironmentStartCreation();
    }

    @Then("the user observe text: Your environment has been created.")
    public void see_text_your_environment_has_been_created(){
        subutaiSteps.waitTextYourEnvironmentHasBeenCreated();
        subutaiSteps.seeTextYourEnvironmentHasBeenCreated();
    }

    @Then("the user observe selector: Environment")
    public void see_selector_environment(){
        subutaiSteps.seeSelectorEnvironment();
    }

    @Then("the user observe text: Your environment start growing.")
    public void see_text_your_environment_start_growing(){
        subutaiSteps.seeTextYourEnvironmentStartGrowing();
    }

    @Then("the user observe text: You successfully grow environment.")
    public void see_you_successfully_grow_environment(){
        subutaiSteps.waitTextYouSuccessfullyGrowEnvironment();
        subutaiSteps.seeTextYouSuccessfullyGrowEnvironment();
    }

    @Then("the user observe popup: Are you sure?")
    public void see_popup_are_sure_yes(){
        subutaiSteps.seePopupAreYouSure();
    }

    @Then("the user observe header: Deleted!")
    public void see_header_blueprint_deleted(){
        subutaiSteps.seeHeaderBlueprintDeleted();
    }

    @Then("the user observe text: Your environment start deleting!")
    public void see_text_your_environment_start_deleting(){
        subutaiSteps.seeTextYourEnvironmentStartDeleting();
    }

    @Then("the user observe text: Your environment has been destroyed.")
    public void see_text_your_environment_has_been_destroyed(){
        subutaiSteps.waitTextYourEnvironmentHasBeenDestroyed();
        subutaiSteps.seeTextYourEnvironmentHasBeenDestroyed();
    }

    @Then("the user observe button: Create Peer")
    public void see_button_create_peer(){
        subutaiSteps.waitButtonCreatePeer();
        subutaiSteps.seeButtonCreatePeer();
    }

    @Then("the user observe field: Enter IP")
    public void see_input_peer_ip(){
        subutaiSteps.seeInputPeerIP();
    }

    @Then("the user observe field: Key phrase")
    public void see_input_peer_key_phrase(){
        subutaiSteps.seeInputPeerKeyPhrase();
    }

    @Then("the user observe: First user's IP")
    public void see_text_cross_peer_1_ip() throws FileNotFoundException {
        subutaiSteps.seeCrossPeer1Ip();
    }

    @Then("the user observe: Second user's IP")
    public void see_text_cross_peer_2_ip() throws FileNotFoundException {
        subutaiSteps.seeCrossPeer2Ip();
    }

    @Then("the user observe button: Cancel")
    public void see_button_cancel(){
        subutaiSteps.seeButtonCancel();
    }

    @Then("the user observe button: Approve")
    public void see_button_approve(){
        subutaiSteps.seeButtonApprove();
    }

    @Then("the user observe button: Reject")
    public void see_button_reject(){
        subutaiSteps.seeButtonReject();
    }

    @Then("the user observe field: Approve Key phrase")
    public void see_input_peer_approve_key_phrase(){
        subutaiSteps.seePeerApproveKeyPhrase();
    }

    @Then("the user observe button: Unregister")
    public void see_button_unregister(){
        subutaiSteps.waitButtonUnregister();
        subutaiSteps.seeButtonUnregister();
    }

    @Then("the user observe header: Unregistered!")
    public void see_text_unregistered(){
        subutaiSteps.waitTextUnregistered();
        subutaiSteps.seeTexUnregistered();
    }

    @Then("the user observe text: No data available in table")
    public void see_text_no_data_available_in_table(){
        subutaiSteps.seeTextNoDataAvailableInTable();
    }

    @Then("the user verify output console command and observe expected phrase: '$expectedPhrase'")
    public void verify_output_console_command(String expectedPhrase){
        subutaiSteps.verifyOutputConsoleCommand(expectedPhrase);
    }

    @Then("the user look all data")
    public void get_all_data(){
        subutaiSteps.getAllData();
    }
}