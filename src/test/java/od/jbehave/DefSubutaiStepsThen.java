package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.Then;

import java.io.FileNotFoundException;

public class DefSubutaiStepsThen {

    @Steps
    SubutaiSteps subutaiSteps;

    @Then("the user observes items of Environment menu")
    public void see_items_Environment_menu(){
        subutaiSteps.seeEnvironmentMenuItemBlueprint();
        subutaiSteps.seeEnvironmentMenuItemEnvironments();
        subutaiSteps.seeEnvironmentMenuItemContainers();
    }

    @Then("the user observes button: Create Blueprint")
    public void see_button_create_blueprint(){
        subutaiSteps.seeButtonCreateEnvironment();
    }

    @Then("the user observes field: Enter blueprint name")
    public void see_field_enter_blueprint_name(){
        subutaiSteps.seeFieldEnterBlueprintName();
    }

    @Then("the user observes node list item")
    public void see_node_list_item()
    {
        subutaiSteps.seeNodeListItem();
    }

    @Then("the user observes created blueprint")
    public void see_created_blueprint(){
        subutaiSteps.seeCreatedBlueprint();
    }

    @Then("the user observes build environment")
    public void see_build_environment(){
        subutaiSteps.seeBuildEnvironment();
    }

    @Then("the user observes icon: two containers")
    public void see_icon_two_containers(){
        subutaiSteps.seeIconTwoContainers();
    }

    @Then("the user observes popup: Build Environment")
    public void see_popup_build_environment(){
        subutaiSteps.seePopup();
    }

    @Then("the user observes header: Success!")
    public void see_header_success(){
        subutaiSteps.seeHeaderSuccess();
    }

    @Then("the user observes text: Your environment start creation.")
    public void see_text_your_environment_start_creation(){
        subutaiSteps.seeTextYourEnvironmentStartCreation();
    }

    @Then("the user observes text: Your environment has been created.")
    public void see_text_your_environment_has_been_created(){
        subutaiSteps.waitTextYourEnvironmentHasBeenCreated();
        subutaiSteps.seeTextYourEnvironmentHasBeenCreated();
    }

    @Then("the user observes selector: Environment")
    public void see_selector_environment(){
        subutaiSteps.seeSelectorEnvironment();
    }

    @Then("the user observes text: Your environment start growing.")
    public void see_text_your_environment_start_growing(){
        subutaiSteps.seeTextYourEnvironmentStartGrowing();
    }

    @Then("the user observes text: You successfully grow environment.")
    public void see_you_successfully_grow_environment(){
        subutaiSteps.waitTextYouSuccessfullyGrowEnvironment();
        subutaiSteps.seeTextYouSuccessfullyGrowEnvironment();
    }

    @Then("the user observes popup: Are you sure?")
    public void see_popup_are_sure_yes(){
        subutaiSteps.seePopupAreYouSure();
    }

    @Then("the user observes header: Deleted")
    public void see_header_blueprint_deleted(){
//        subutaiSteps.waitHeaderBlueprintDeleted();
        subutaiSteps.seeHeaderBlueprintDeleted();
    }

    @Then("the user observes text: Your environment is being deleted!")
    public void see_text_your_environment_start_deleting(){
        subutaiSteps.seeTextYourEnvironmentStartDeleting();
    }

    @Then("the user observes text: Your environment has been destroyed.")
    public void see_text_your_environment_has_been_destroyed(){
        subutaiSteps.waitTextYourEnvironmentHasBeenDestroyed();
        subutaiSteps.seeTextYourEnvironmentHasBeenDestroyed();
    }

    @Then("the user observes button: Create Peer")
    public void see_button_create_peer(){
        subutaiSteps.waitButtonCreatePeer();
        subutaiSteps.seeButtonCreatePeer();
    }

    @Then("the user observes field: Enter IP")
    public void see_input_peer_ip(){
        subutaiSteps.seeInputPeerIP();
    }

    @Then("the user observes field: Key phrase")
    public void see_input_peer_key_phrase(){
        subutaiSteps.seeInputPeerKeyPhrase();
    }

    @Then("the user observes: First user's IP")
    public void see_text_cross_peer_1_ip() throws FileNotFoundException {
        subutaiSteps.seeCrossPeer1Ip();
    }

    @Then("the user observes: Second user's IP")
    public void see_text_cross_peer_2_ip() throws FileNotFoundException {
        subutaiSteps.seeCrossPeer2Ip();
    }

    @Then("the user observes button: Cancel")
    public void see_button_cancel(){
        subutaiSteps.seeButtonCancel();
    }

    @Then("the user observes button: Approve")
    public void see_button_approve(){
        subutaiSteps.seeButtonApprove();
    }

    @Then("the user observes button: Reject")
    public void see_button_reject(){
        subutaiSteps.seeButtonReject();
    }

    @Then("the user observes field: Approve Key phrase")
    public void see_input_peer_approve_key_phrase(){
        subutaiSteps.seePeerApproveKeyPhrase();
    }

    @Then("the user observes button: Unregister")
    public void see_button_unregister(){
        subutaiSteps.waitButtonUnregister();
        subutaiSteps.seeButtonUnregister();
    }

    @Then("the user observes header: Unregistered!")
    public void see_text_unregistered(){
        subutaiSteps.waitTextUnregistered();
        subutaiSteps.seeTexUnregistered();
    }

    @Then("the user observes text: No data available in table")
    public void see_text_no_data_available_in_table(){
        subutaiSteps.seeTextNoDataAvailableInTable();
    }

    @Then("the user verifies output console command and observe expected phrase: '$expectedPhrase'")
    public void verifies_output_console_command(String expectedPhrase){
        subutaiSteps.verifyOutputConsoleCommand(expectedPhrase);
    }

    @Then("the user looks all data")
    public void gets_all_data(){
        subutaiSteps.getAllData();
    }

    //------3023


    @Then("the user observes console module UI with select menu of available resource hosts")
    public void see_console_and_select_menu_resource_hosts(){
        subutaiSteps.seeConsole();
        subutaiSteps.seeSelectMenuResourceHosts();
    }

    @Then("the user should observe output of the command")
    public void see_output_of_the_test_command(){
        subutaiSteps.seeOutputOfTestCommand();
    }

    @Then("the user should observe output of the command three received")
    public void see_output_of_the_test_command_thre_receved(){
        subutaiSteps.seeOutputOfTestCommandThreeReceived();
    }

    @Then("the user should observe public Key of the resource host in GPG Armored text")
    public void see_resource_host_pgp_header(){
        subutaiSteps.seePGP();
    }

    @Then("the user should observe public Key of the container in GPG Armored text")
    public void see_container_pgp_header(){
        subutaiSteps.seePGP();
    }

    @Then("the user observes button: Start")
    public void user_observes_button_start(){
        subutaiSteps.waitButtonStartContainer();
        subutaiSteps.seeButtonStart();
    }

    @Then("the user observes button: Stop")
    public void user_observes_button_stop(){
        subutaiSteps.waitButtonStopContainer();
        subutaiSteps.seeButtonStop();
    }

    @Then("the user should observe output of the pwd command")
    public void user_observes_output_of_pwd_command(){
        subutaiSteps.seeOutputOfPwdCommand();
    }

    @Then("the user should observe output of the ping command")
    public void user_observes_output_of_ping_command(){
        subutaiSteps.seeOutputOfPingCommand();
    }

    @Then("the user should observe output of the wrong ping command")
    public void user_observes_output_of_wrong_ping_command(){
        subutaiSteps.seeOutputOfWrongPingCommand();
    }

    @Then("the user observes text: Token")
    public void user_observes_text_token(){
        subutaiSteps.seeTextToken();
    }

    @Then("the user gets Token")
    public void user_gets_token(){
        subutaiSteps.getToken();
    }

    @Then("the user observes Local Peer ID")
    public void user_observes_local_peer_id() throws FileNotFoundException {
        subutaiSteps.observeLocalPeerID();
    }

    @Then("the user observes Remote Peer ID")
    public void user_observes_remote_peer_id() throws FileNotFoundException {
        subutaiSteps.observeRemotePeerID();
    }

    @Then("the user observes Own PGP key")
    public void user_observes_own_pgp_key() throws FileNotFoundException {
        subutaiSteps.ownPGPKey();
        subutaiSteps.observeOwnPGPKey();
    }

    @Then("the user observes Remote PGP key")
    public void user_observes_remote_pgp_key() throws FileNotFoundException {
        subutaiSteps.remotePGPKey();
        subutaiSteps.observeOwnPGPKey();
    }

    @Then("the user observes Environment data")
    public void user_observes_environment_id() throws FileNotFoundException {
        subutaiSteps.observeEnvironmentData();
    }

    @Then("the user observes Environment PGP key")
    public void user_observes_environment_pgp_key() throws FileNotFoundException {
        subutaiSteps.environmentPGPKey();
        subutaiSteps.observeEnvironmentPGPKey();
    }

    @Then("the user runs bash script: resets virtual box and deletes vagrant directory")
    public void user_resets_virual_box_and_remove_vagrant_directory() throws FileNotFoundException {
        subutaiSteps.run_bash_script("src/test/resources/files/resetVirtualBox.sh");
    }

    //-------3117

    @Then("the user observes icon: three containers")
    public void see_icon_three_containers(){
        subutaiSteps.seeIconThreeContainers();
    }

    @Then("the user observes empty Environment domain")
    public void see_empty_environment_domain(){
        subutaiSteps.seeEmptyInputDomain();
    }

    @Then("the user should observe success message")
    public void see_success_message(){
        subutaiSteps.seeHeaderSuccess();
    }

    @Then("the user observes domain name '$domain' assigned to environment")
    public void see_binding_text(String text){
        subutaiSteps.clickOnButtonConfigure();
        subutaiSteps.seeDomainBindingText(text);
    }

    @Then("the user observes domain strtegy set to round-robin")
    public void see_domain_strategy_round_robin(){
        subutaiSteps.roundRobinSelected();
    }

    @Then("the user checks Is container in domain check-box")
    public void see_checked_checkbox_container(){
        subutaiSteps.seeCheckboxCheced();
    }

    @Then("the user observes page with IP that received request appear")
    public void see_container_ip_in_domain_page(){
        subutaiSteps.seeDomainContainerIP();
    }

    @Then("the user should see diferents IP")
    public void should_see_diferent_ip(){
        subutaiSteps.compareContainerIP();
    }

    @Then("the user should see that container stopped")
    public void see_that_container_stopped(){
        subutaiSteps.seeContainerButtonStart();
    }

    @Then("the user should not observe stopped container IP")
    public void should_not_observes_stopped_ip(){
        subutaiSteps.checkForOutOfIP();
    }

    @Then("the user observes container removed from subut.ai domain")
    public void see_container_removed(){
        subutaiSteps.seeCheckboxUncheck();
    }

    @Then("the user should not observe disabled container IP")
    public void should_not_observes_disabled_ip(){
        subutaiSteps.checkForOutOfIP();
    }

    @Then("the user observes domain name empty to environment")
    public void not_see_domain_name(){
        subutaiSteps.notSeeDomainName();
    }

    @Then("the user should observe web page with container IP won't be available")
    public void should_observe_page_not_found_message() {
        subutaiSteps.pageNotFoundMessage();
    }

    @Then("the user should observe 'ng-login', 'ng-password'")
    public void user_observe_ng_login_ng_password(){
        subutaiSteps.observeLogin();
        subutaiSteps.observePassword();
    }

    @Then("the user should observe 'ng-create blueprint'")
    public void user_observes_ng_create_blueprint(){
        subutaiSteps.observeNgCreateBlueprint();
    }

    @Then("the user observes button 'ng-add', 'ng-remove'")
    public void user_observe_ng_add_ng_remove(){
        subutaiSteps.observeNgAdd();
        subutaiSteps.observeNgRevove();
    }

    @Then("the user observes 'ng-Environment selector', 'ng-Containers type selector'")
    public void user_observes_ng_envi_ng_containers(){
        subutaiSteps.observeNgEnvironmentSelector();
        subutaiSteps.observeNgContainersSelector();
    }

    @Then("the user observes 'ng-Peer', 'ng-Select peer'")
    public void user_observes_ng_peer_ng_select_peer(){
        subutaiSteps.observeNgPeer();
        subutaiSteps.observeNgSelectPeer();
    }

    @Then("the user observes button: 'ng-Add User'")
    public void user_observes_button_add_user(){
        subutaiSteps.observeNgAddUser();
    }

    @Then("the user observes button: 'ng-Add Role'")
    public void user_observes_button_ng_add_role(){
        subutaiSteps.observeNgAddRole();
    }

    @Then("the user observes button: 'ng-Add Token', name: 'ng-Token name'")
    public void user_observes_button_ng_add_token_ng_token_name(){
        subutaiSteps.observeNgAddToken();
        subutaiSteps.observeNgTokenName();
    }

    @Then("the user observes button: 'ng-Create Peer'")
    public void user_observes_button_ng_create_peer(){
        subutaiSteps.observeNgCreatePeer();
    }

    @Then("the user should observe 'ng-click peer', 'ng-click environment'")
    public void user_observes_ng_peer_ng_environment(){
        subutaiSteps.observeNgPeerMonitoring();
        subutaiSteps.observeNgEnvironmentMonitoring();
    }

    @Then("the user observes 'ng-Source selector'")
    public void user_observes_ng_source_selector(){
        subutaiSteps.observeNgSourceSelector();
    }

    @Then("the user gets weight of Web UI")
    public void user_gets_weight_of_webui(){
        subutaiSteps.getWebUiWeght1();
        subutaiSteps.getWebUiWeght2();
        subutaiSteps.getWeightOfWeight1Weight2();
    }

    @Then("the user gets weight of Web UI .war")
    public void user_gets_weight_of_webui_war(){
        subutaiSteps.getWebUiWeight3();
        subutaiSteps.waitABit(2000);
    }

    @Then("the user gets weight of all files")
    public void gets_weight_of_all_files(){
        subutaiSteps.getWeightOfWeight1Weight2Weight3();
    }

    @Then("the user should observe that Web UI is less than 10Mb")
    public void user_observe_that_web_ui_less_than_10mb(){
        subutaiSteps.observeWebUiLessThan10Mb();
    }
}