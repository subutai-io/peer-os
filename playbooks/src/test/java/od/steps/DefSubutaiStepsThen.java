package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Then;
import org.sikuli.script.FindFailed;

import java.io.FileNotFoundException;

public class DefSubutaiStepsThen {

    @Steps
    private SubutaiSteps subutaiSteps;

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
        subutaiSteps.clickOnTitleManagement();
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
//        subutaiSteps.userShouldObservePluginMongo();
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
//        subutaiSteps.userShouldObserveNotificationsBody();
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
        subutaiSteps.userShouldObservePluginCassandra();
    }

    @Then("the user should observe web elements on: Kurjun page")
    public void user_observe_elements_on_kurjun_page() throws FindFailed {
        subutaiSteps.userShouldObserveGreenButton();
        subutaiSteps.clickOnTitleAPT();
        subutaiSteps.userShouldObserveGreenButton();
        subutaiSteps.userShouldObserveFieldSearch();
        subutaiSteps.clickOnTitleRawFiles();
        subutaiSteps.userShouldObserveButtonUploadFile();
        subutaiSteps.userShouldObserveButtonRefresh();
    }

    @Then("the user should observe web elements on: Account Settings page")
    public void user_observe_elements_on_account_settings_page() throws FindFailed {
//        subutaiSteps.userShouldObserveFieldPGP();
//        subutaiSteps.userShouldObserveButtonSetPublicKey();
        subutaiSteps.clickOnTitleChangePassword();
        subutaiSteps.userShouldObserveButtonSave();
    }

    @Then("the user should observe web elements on: Kurjun Settings page")
    public void user_observe_elements_on_kurjun_settings_page() throws FindFailed {
        subutaiSteps.userShouldObserveButtonSaveOnKurjunSettingsPage();
        subutaiSteps.userShouldObserveButtonAddOnKurjunSettingsPage();
        subutaiSteps.clickOnTitleQuotas();
        subutaiSteps.userShouldObserveButtonSaveOnKurjunSettingsQuotasPage();
        subutaiSteps.clickOnTitleUrlsList();
        subutaiSteps.userShouldObserveButtonAddUrl();
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
        subutaiSteps.clickOnButtonSaveUser();
    }

    @Then("the user should change password: '$oldpass', '$newpass', '$confpass'")
    public void change_password(String oldpass, String password, String confpass) throws FindFailed {
        subutaiSteps.inputOldPassword(oldpass);
        subutaiSteps.inputNepPassword(password);
        subutaiSteps.inputConfirmPassword(confpass);
        subutaiSteps.clickOnButtonSave();
        subutaiSteps.clickOnButtonOk();
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

    @Then("the user destroys created environment")
    public void user_delete_environment() throws FindFailed {
        subutaiSteps.clickOnIconDeleteEnvironment();
        subutaiSteps.clickOnButtonDelete();
//        subutaiSteps.clickOnButtonOkPopupEnvironmentHasBeenDestroyed();
//        subutaiSteps.waitFor(5000);
    }

    @Then("the user unregister peer")
    public void user_unregister_peer() throws FindFailed {
        subutaiSteps.clickOnButtonUnregister();
        subutaiSteps.clickOnButtonUnregisterPopup();
        subutaiSteps.clickOnButtonOkUnregisterPeer();
    }

    @Then("the user should put the First RH in workspace")
    public void user_put_the_first_rh() throws FindFailed {
        subutaiSteps.clickOnPeer1();
        subutaiSteps.clickOnResourceHost1();
    }

    @Then("the user should put the Second RH in workspace")
    public void user_put_the_second_rh() throws FindFailed {
        subutaiSteps.clickOnPeer2();
        subutaiSteps.clickOnResourceHost1();
    }

    @Then("the user should drag and drop template to RH1: Master")
    public void user_drag_and_drop_master_to_rh1() throws FindFailed {
//        subutaiSteps.userShouldObservePNGs();
        subutaiSteps.dragAndDropTemplateMasterToRH1();
    }

    @Then("the user should drag and drop template to RH2: Master")
    public void user_drag_and_drop_master_to_rh2() throws FindFailed {
//        subutaiSteps.userShouldObservePNGs();
        subutaiSteps.dragAndDropTemplateMasterToRH2();
    }

    @Then("the user should create an environment")
    public void user_create_an_environment() throws FindFailed {
        subutaiSteps.clickOnButtonApply();
        subutaiSteps.inputEnvironmentName("Test environment");
        subutaiSteps.clickOnButtonBuild();
        subutaiSteps.clickOnButtonCloseBuildPopup();
        subutaiSteps.waitFor(5000);
    }

    @Then("the user search plugin: '$plugin'")
    public void user_search_plugin(String plugin){
        subutaiSteps.inputPluginName(plugin);
    }

    @Then("the user chooses the environment")
    public void user_chooses_enviroenment() throws FindFailed {
        subutaiSteps.clickOnSelectorEnvironment();
        subutaiSteps.clickOnEnvironmentFromSelector();
    }

    @Then("the user creates profile name")
    public void user_creates_profile_name() throws FindFailed {
        subutaiSteps.inputProfileName("test profile");
        subutaiSteps.clickOnButtonCreate();
        subutaiSteps.clickOnButtonOk();
//        subutaiSteps.userShouldObserveProfileName();
    }

    @Then("the user should add an operation")
    public void user_add_operation() throws FindFailed {
        subutaiSteps.inputOperationName("ls_operation");
        subutaiSteps.inputOperation("ls");
        subutaiSteps.clickOnButtonSave();
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user should execute the ls opeartion")
    public void user_execute_ls_operation() throws FindFailed {
        subutaiSteps.clickOnButtonExecute();
        subutaiSteps.seeOutputOfLsCommand();
    }

    @Then("the user should delete a profile")
    public void delete_profile() throws FindFailed {
        subutaiSteps.clickOnIconDeleteEnvironment();
        subutaiSteps.clickOnButtonDelete();
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user uninstall plugin")
    public void user_uninstall_plugin() throws FindFailed {
        subutaiSteps.clickOnButtonUninstall();
    }

    @Then("the user clicks button: Quick install")
    public void user_clicks_quick_install() throws FindFailed {
        subutaiSteps.clickOnButtonQuickInstall();
    }

    @Then("the user clicks on the buton: Console")
    public void user_clicks_button_console() throws FindFailed {
        subutaiSteps.clickOnButtonConsole();
    }

    @Then("the user should observe button: Console")
    public void user_observe_button_console(){
        subutaiSteps.userShouldObserveButtonConsole();
    }

    @Then("the user fills out Quick Install")
    public void user_fills_quick_install() throws FindFailed {
        subutaiSteps.inputAppScaleEnvironmentName("AppScaleEnvi");
        subutaiSteps.inputDomain("test.ai");
        subutaiSteps.clickOnButtonInstall();
    }

    @Then("the user should observe 3 containers")
    public void user_observe_three_containers(){
        subutaiSteps.userShouldObserveThreeContainers();
    }

    @Then("the user should observe 2 containers")
    public void user_observe_2_containers(){
        subutaiSteps.userShouldObserveTwoContainers();
    }

    @Then("the user uploads template")
    public void user_upload_template() throws FindFailed {
        subutaiSteps.clickOnButtonBrowse();
        subutaiSteps.clickOnButtonOpen();
        subutaiSteps.clickOnButtonAdd();
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user should click on admin icon")
    public void user_click_on_admin_icon() throws FindFailed {
        subutaiSteps.waitABit(5000);
        subutaiSteps.clickOniconAdmin();
    }
    @Then("the user should click on test icon")
    public void user_click_on_test_icon() throws FindFailed {
        subutaiSteps.clickOnIconTest();
    }

    @Then("the user clicks on the button: OK")
    public void user_click_on_button_ok() throws FindFailed {
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user should observe role: iManagement")
    public void user_observe_role_imanagement(){
        subutaiSteps.userShouldObserveIManagement();
    }

    @Then("the user should delete the role: iManagement")
    public void user_delete_role_imanagement() throws FindFailed {
        subutaiSteps.inputNameInSearchField("iManagement");
        subutaiSteps.clickOnIconDeleteRole();
        subutaiSteps.clickOnButtonDelete();
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user set domain")
    public void user_set_domain() throws FindFailed {
        subutaiSteps.inputSetDomainName("test.qa");
        subutaiSteps.clickOnButtonSave();
        subutaiSteps.clickOnButtonOk();
    }

    @Then("the user should observe header apache")
    public void user_observe_header_apache(){
        subutaiSteps.userShouldObserveHeaderApache();
    }

    @Then("the user chooses the medium size of template cassandra")
    public void user_choose_the_medium_size_of_cont_cassandra() throws FindFailed {
        subutaiSteps.clickOnIconSettingsCont();
        subutaiSteps.clickOnPickerSmall();
        subutaiSteps.clickOnPickerMedium();
        subutaiSteps.clickOnButtonSave();
    }

    @Then("the user chooses the medium size of first template cassandra")
    public void user_choose_the_medium_size_of_first_cont_cassandra() throws FindFailed {
        subutaiSteps.clickOnIconSettingsFirstCont();
        subutaiSteps.clickOnPickerSmall();
        subutaiSteps.clickOnPickerMedium();
        subutaiSteps.clickOnButtonSave();
    }
    @Then("the user chooses the medium size of second template cassandra")
    public void user_choose_the_medium_size_of_second_cont_cassandra() throws FindFailed {
        subutaiSteps.clickOnIconSettingsSecondCont();
        subutaiSteps.clickOnPickerSmall();
        subutaiSteps.clickOnPickerMedium();
        subutaiSteps.clickOnButtonSave();
    }

    @Then("the user chooses the medium size of third template cassandra")
    public void user_choose_the_medium_size_of_third_cont_cassandra() throws FindFailed {
        subutaiSteps.clickOnIconSettingsThirdContCass();
        subutaiSteps.clickOnPickerSmall();
        subutaiSteps.clickOnPickerMedium();
        subutaiSteps.clickOnButtonSave();
    }

    @Then("the user gets token")
    public void user_gets_token() throws FindFailed {
        subutaiSteps.clickOnButtonShowToken();
        subutaiSteps.getToken();
    }

    @Then("the user observes Local Peer ID")
    public void user_observe_local_peer_id() throws FileNotFoundException {
        subutaiSteps.observeLocalPeerID();
    }

    @Then("the user observes Own PGP key")
    public void user_observe_own_pgp_key() throws FileNotFoundException {
        subutaiSteps.ownPGPKey();
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
}