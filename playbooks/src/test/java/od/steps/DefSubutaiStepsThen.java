package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Then;
import org.sikuli.script.FindFailed;
import org.yecht.Data;

import java.io.FileNotFoundException;

public class DefSubutaiStepsThen {

    @Steps
    SubutaiSteps subutaiSteps;

    @Then("the user should observe web elements on: Login page")
    public void user_observe_login_page(){
        subutaiSteps.userShouldObserveHeaderLogin();
        subutaiSteps.userShouldObserveFieldLogin();
        subutaiSteps.userShouldObserveFieldPassword();
    }

    @Then("the user should observe web elements on: Monitoring page")
    public void user_obseve_monitoring_page() throws FindFailed {
//      subutaiSteps.userShouldObserveHeaderMonitoring();
        subutaiSteps.clickOnSelectorHostsMonitoringPage();
//        subutaiSteps.userShouldObserveManagementHost();
        subutaiSteps.userShouldObserveResourceHost();
        subutaiSteps.clickOnSelectorHostsMonitoringPage();
        subutaiSteps.clickOnTitleEnvironment();
        subutaiSteps.userShouldObserveSelectorEnvironment();
    }

    @Then("the user should observe web elements on: Blueprints page")
    public void user_observe_blueprints(){
        subutaiSteps.userShouldObserveHeaderBlueprints();
        subutaiSteps.userShouldObserveButtonCreateBlueprints();
    }

    @Then("the user should observe web elements on: Environments page")
    public void user_observe_environments(){
        subutaiSteps.userShouldObserveButtonModes();
        subutaiSteps.userShouldObserveButtonApply();
        subutaiSteps.userShouldObservePluginMongo();
    }

    @Then("the user should create the local environment")
    public void user_should_create_local_environment() throws FindFailed {
        subutaiSteps.clickOnTemplateWebdemo();
        subutaiSteps.clickOnButtonApply();
        subutaiSteps.inputEnvironmentName("Test Environment Webdemo");
        subutaiSteps.clickOnButtonBuild();
        subutaiSteps.clickOnButtonCloseBuildPopup();
        subutaiSteps.waitFor(5000);
        subutaiSteps.clickOnIconDeleteEnvironmentNotSikuli();
        subutaiSteps.clickOnButtonDelete();
        subutaiSteps.clickOnButtonOkPopupEnvironmentHasBeenDestroyed();
        subutaiSteps.waitFor(5000);
    }

    @Then("the user should observe web elements on: Containers page")
    public void user_observe_containers(){
        subutaiSteps.userShouldObserveHeaderContainers();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe output of the pwd command")
    public void user_observes_output_of_pwd_command() throws FindFailed {
        subutaiSteps.seeOutputOfPwdCommand();
        subutaiSteps.clickOnSelectorHostsConsolePage();
        subutaiSteps.clickOnTitleEnvironment();
        subutaiSteps.userShouldObserveSelectorEnvironmentConsole();
    }

    @Then("the user should observe web elements on: User management page")
    public void user_observe_user_management() throws FindFailed {
        subutaiSteps.userShouldObserveHeaderUserManagement();
//        subutaiSteps.userShouldObserveButtonAddUser();
    }

    @Then("the user should observe web elements on: Role management page")
    public void user_observe_role_management() {
        subutaiSteps.userShouldObserveHeaderRoleManagement();
        subutaiSteps.userShouldObserveButtonAddRole();
    }

    @Then("the user should observe web elements on: Tokens page")
    public void user_observe_tokens() {
        subutaiSteps.userShouldObserveHeaderTokens();
        subutaiSteps.userShouldObserveButtonAddToken();
    }

    @Then("the user should observe web elements on: Peer Registration page")
    public void user_observe_peer_registration() {
        subutaiSteps.userShouldObserveHeaderPeerRegistration();
        subutaiSteps.userShouldObserveButtonCreatePeer();
    }

    @Then("the user should observe web elements on: Resource Hosts page")
    public void user_observe_resource_hosts() {
        subutaiSteps.userShouldObserveHeaderResourceHosts();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Tracker page")
    public void user_observe_tracker() {
        subutaiSteps.userShouldObserveHeaderTracker();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Bazaar page")
    public void user_observe_bazaar() throws FindFailed {
//        subutaiSteps.userShouldObserveHeaderPlugins();
//        subutaiSteps.userShouldObservePluginItems();
        subutaiSteps.clickOnTitleInstalled();
        subutaiSteps.clickOnTitleAdvanced();
        subutaiSteps.userShouldObserveButtonUploadNewPlugin();
    }

    @Then("the user should observe web elements on: Peer Settings page")
    public void user_observe_peer_settings() throws FindFailed {
//        subutaiSteps.userShouldObserveHeaderPeerSettings();
        subutaiSteps.userShouldObserveButtonSetPeerOwner();
        subutaiSteps.clickOnTitlePolicy();
        subutaiSteps.userShouldObserveButtonSaveOnPolicyPage();
    }

    @Then("the user should observe web elements on: About page")
    public void user_observe_about(){
        subutaiSteps.userShouldObserveHeaderAbout();
    }

    @Then("the user should observe web elements on drop down menu: Register Peer")
    public void user_observe_upper_menu_elements(){
        subutaiSteps.userShouldObserveTitleRegisterPeer();
        subutaiSteps.userShouldObserveUpperMenuBody();
        subutaiSteps.userShouldObserveLinkSignUp();
        subutaiSteps.userShouldObserveButtonRegister();
    }

    @Then("the user should observe web elements on drop down menu: Notifications")
    public void user_observe_elements_of_notifications(){
        subutaiSteps.userShouldObserveNotificationsBody();
        subutaiSteps.userShouldObserveLinkClear();
    }

    @Then("the user should observe user name: admin")
    public void user_observe_name_admin(){
        subutaiSteps.userShouldObserveUserNameAdmin();
    }

    @Then("the user should observe web elements on: Advanced mode page")
    public void user_observe_elements_on_advanced_page() throws FindFailed {
        subutaiSteps.userShouldObserveTitlePeers();
        subutaiSteps.clickOnTitleTemplates();
        subutaiSteps.userShouldObservePluginMongo();
    }

    @Then("the user should observe web elements on: Kurjun page")
    public void user_observe_elements_on_kurjun_page() throws FindFailed {
        subutaiSteps.userShouldObserveGreenButton();
        subutaiSteps.clickOnTitleAPT();
        subutaiSteps.userShouldObserveGreenButton();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Account Settings page")
    public void user_observe_elements_on_account_settings_page() throws FindFailed {
        subutaiSteps.userShouldObserveFieldPGP();
        subutaiSteps.userShouldObserveButtonSetPublicKey();
        subutaiSteps.clickOnTitleChangePassword();
        subutaiSteps.userShouldObserveButtonSave();
    }

    @Then("the user should observe web elements on: Kurjun Settings page")
    public void user_observe_elements_on_kurjun_settings_page() throws FindFailed {
        subutaiSteps.userShouldObserveButtonSaveOnKurjunSettingsPage();
        subutaiSteps.userShouldObserveButtonAddOnKurjunSettingsPage();
        subutaiSteps.clickOnTitleQuotas();
        subutaiSteps.userShouldObserveButtonSaveOnKurjunSettingsQuotasPage();
    }

    @Then("the user should observe web elements on: Network Settings page")
    public void user_observe_elements_on_network_settings(){
        subutaiSteps.userShouldObserveHeaderNetworkSettings();
        subutaiSteps.userShouldObserveButtonSaveOnNetworkSettingsPage();
    }

    @Then("the user should observe web elements on: Advanced page")
    public void user_observe_elements_on_advanced() throws FindFailed {
//        subutaiSteps.userShouldObserveHeaderAdvanced();
        subutaiSteps.clickOnTitleLogs();
        subutaiSteps.userShouldObserveButtonExport();
    }

    @Then("the user should register a new user: '$username', '$fullname', '$email', '$password', '$confirmpassword'")
    public void user_register_new_user(String username, String fullname, String email,
                                       String password, String confirmpassword) throws FindFailed {
        subutaiSteps.clickOnButtonAddUser();
        subutaiSteps.inputNewUserUsername(username);
        subutaiSteps.inputNewUserFullName(fullname);
        subutaiSteps.clickOnTrustedLevel();
        subutaiSteps.clickOnUltimateTrust();
        subutaiSteps.inputNewUserEmail(email);
        subutaiSteps.inputNewUserPassword(password);
        subutaiSteps.inputNewUserConfirmPassword(confirmpassword);
        subutaiSteps.clickOnButtonChooseAll();
        subutaiSteps.clickOnButtonSave();
    }

    @Then("the user should observe a new user")
    public void user_observe_a_new_user(){
        subutaiSteps.userShouldObserveANewUsersEmail();
    }

    @Then("the user should delete the role: Internal-System")
    public void user_delete_the_role_internal_system() throws FindFailed {
        subutaiSteps.clickOnIconDeleteRoleInternalSystem();
        subutaiSteps.clickOnRemoveRoleButton();
        subutaiSteps.clickOnButtonOkRoleRemoved();
        subutaiSteps.userShouldNotObserveRoleInternalSystem();
    }

    @Then("the user should delete a new user")
    public void user_delete_a_new_user() throws FindFailed {
        subutaiSteps.clickOnButtonRemoveUser();
        subutaiSteps.clickOnButtonDelete();
        subutaiSteps.clickOnButtonOkDeleteUser();
    }

    @Then("the user stop record video and save the file")
    public void stop_record_video() throws FileNotFoundException {
        subutaiSteps.run_bash_script("src/test/resources/files/recordScreenStop.sh");
    }

    @Then("the user should observe button: Go To HUB Green")
    public void should_see_button_go_to_hub(){
        subutaiSteps.assertButtonGoToHUB();
    }

    @Then("the user should observe message: Heartbeat sent successfully")
    public void should_observe_message_heartbeat_sent(){
        subutaiSteps.assertMessageHeartbeatSentSuccessfully();
    }

    @Then("the user should observe button: Send Heartbeat")
    public void should_observe_button_send_heartbeat(){
        subutaiSteps.assertButtonSendHeartbeat();
    }

    @Then("the user should create a peer request with: '$ip', '$key'")
    public void should_create_a_peer_request(String ip, String key) throws FindFailed {
        subutaiSteps.inputPeerIP(ip);
        subutaiSteps.inputPeerKeyPhrase(key);
        subutaiSteps.clickOnButtonCreate();
    }

    @Then("the user should observe button: Cancel")
    public void should_observe_button_cancel_request(){
        subutaiSteps.assertButtonCancelPeerRequest();
    }

    @Then("the user should approve the peer with: '$key'")
    public void should_approve_peer(String key) throws FindFailed {
        subutaiSteps.clickbuttonApprove();
        subutaiSteps.inputApprovePeerKeyPhrase(key);
        subutaiSteps.clickbuttonApprovePopUp();
        subutaiSteps.assertButtonUnregister();
    }
}