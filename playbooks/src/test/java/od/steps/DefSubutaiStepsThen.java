package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Then;
import org.sikuli.script.FindFailed;

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
    public void user_observe_user_management(){
        subutaiSteps.userShouldObserveHeaderUserManagement();
        subutaiSteps.userShouldObserveButtonAddUser();
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
}