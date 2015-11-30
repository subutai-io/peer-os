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

    @Then("the user observe header: Deleted")
    public void see_header_blueprint_deleted(){
        subutaiSteps.waitHeaderBlueprintDeleted();
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

    //------3023


    @Then("the user observe console module UI with select menu of available resource hosts")
    public void see_console_and_select_menu_resource_hosts(){
        subutaiSteps.seeConsole();
        subutaiSteps.seeSelectMenuResourceHosts();
    }

    @Then("the user should observe output of the command")
    public void see_output_of_the_test_command(){
        subutaiSteps.seeOutputOfTestCommand();
    }

    @Then("the user should observe public Key of the resource host in GPG Armored text")
    public void see_resource_host_pgp_header(){
        subutaiSteps.seePGP();
    }

    @Then("the user should observe public Key of the container in GPG Armored text")
    public void see_container_pgp_header(){
        subutaiSteps.seePGP();
    }

    @Then("the user observe button: Start")
    public void user_observe_button_start(){
        subutaiSteps.seeButtonStart();
    }

    @Then("the user observe button: Stop")
    public void user_observe_button_stop(){
        subutaiSteps.seeButtonStop();
    }

    @Then("the user should observe output of the pwd command")
    public void user_observe_output_of_pwd_command(){
        subutaiSteps.seeOutputOfPwdCommand();
    }

    @Then("the user observe text: Token")
    public void user_observe_text_token(){
        subutaiSteps.seeTextToken();
    }

    @Then("the user get Token")
    public void user_get_token(){
        subutaiSteps.getToken();
    }

    @Then("the user observe Local Peer ID")
    public void user_observe_local_peer_id() throws FileNotFoundException {
        subutaiSteps.observeLocalPeerID();
    }

    @Then("the user observe Own PGP key")
    public void user_observe_own_pgp_key() throws FileNotFoundException {
        subutaiSteps.ownPGPKey();
        subutaiSteps.observeOwnPGPKey();
    }

    @Then("the user observe Remote PGP key")
    public void user_observe_remote_pgp_key() throws FileNotFoundException {
        subutaiSteps.remotePGPKey();
        subutaiSteps.observeOwnPGPKey();
    }

    @Then("the user observe Environment data")
    public void user_observe_environment_id() throws FileNotFoundException {
        subutaiSteps.observeEnvironmentData();
    }

    @Then("the user observe Environment PGP key")
    public void user_observe_environment_pgp_key() throws FileNotFoundException {
        subutaiSteps.environmentPGPKey();
        subutaiSteps.waitABit(10000);
        subutaiSteps.observeEnvironmentPGPKey();
    }
}