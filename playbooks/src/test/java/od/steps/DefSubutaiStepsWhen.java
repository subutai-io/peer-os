package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.When;
import org.sikuli.script.FindFailed;

import java.io.FileNotFoundException;

public class DefSubutaiStepsWhen {

    @Steps
    SubutaiSteps subutaiSteps;

    @When("the user clicks on the menu item: Monitoring")
    public void user_clicks_monitoring() throws FindFailed {
        subutaiSteps.clickOnMenuItemMonitoring();
    }

    @When("the user clicks on the menu item: Environment")
    public void user_click_environment() throws FindFailed {
        subutaiSteps.clickOnMenuEnvironment();
    }

    @When("the user clicks on the menu item: Blueprints")
    public void user_clicks_blueprints() {
        subutaiSteps.clickOnMenuItemBlueprints();
    }

    @When("the user clicks on the menu item: Environments")
    public void user_clicks_environment() throws FindFailed {
        subutaiSteps.clickOnMenuItemEnvironments();
    }

    @When("the user clicks on the menu item: Containers")
    public void user_clicks_containers() throws FindFailed {
        subutaiSteps.clickOnMenuItemContainers();
    }

    @When("the user clicks on the menu item: Console")
    public void user_clicks_console() throws FindFailed {
        subutaiSteps.clickOnMenuItemConsole();
    }

    @When("the user chooses: Management host")
    public void user_choose_management_host() throws FindFailed {
        subutaiSteps.clickOnSelectorHostsConsolePage();
        subutaiSteps.clickOnManagementHost();
    }

    @When("the user enters console command: '$command'")
    public void executes_console_command(String command) {
        subutaiSteps.executeConsoleCommand(command);
    }

    @When("the user chooses: Local host")
    public void user_choose_local_host() throws FindFailed {
        subutaiSteps.clickOnSelectorHostsConsolePage();
        subutaiSteps.clickOnLocalHost();
    }

    @When("the user clicks on the menu item: User management")
    public void user_clicks_user_management() throws FindFailed {
       subutaiSteps.clickOnMenuItemUserManagement();
    }

    @When("the user clicks on the menu item: Role management")
    public void user_clicks_role_management() throws FindFailed {
        subutaiSteps.clickOnMenuItemRoleManagement();
    }

    @When("the user clicks on the menu item: Tokens")
    public void user_clicks_tokens() throws FindFailed {
        subutaiSteps.clickOnMenuItemTokens();
    }

    @When("the user clicks on the menu item: Peer Registration")
    public void user_clicks_peer_registration() throws FindFailed {
        subutaiSteps.clickOnMenuItemPeerRegistration();
    }

    @When("the user clicks on the menu item: Resource Hosts")
    public void user_clicks_resource_hosts() throws FindFailed {
        subutaiSteps.clickOnMenuItemResourceHosts();
    }

    @When("the user clicks on the menu item: Tracker")
    public void user_clicks_tracker() throws FindFailed {
        subutaiSteps.clickOnMenuItemTracker();
    }

    @When("the user clicks on the menu item: Bazaar")
    public void user_clicks_bazaar() throws FindFailed {
        subutaiSteps.clickOnMenuItemBazaar();
    }

    @When("the user clicks on the menu item: System Settings")
    public void user_clicks_plugin_integrator() throws FindFailed {
        subutaiSteps.clickOnMenuItemSystemSettings();
    }

    @When("the user clicks on the menu item: About")
    public void user_click_about() throws FindFailed {
        subutaiSteps.clickOnMenuItemAbout();
    }

    @When("the user sets pgp Key")
    public void user_sets_pgp_key() throws FindFailed {
        subutaiSteps.clickOnMenuItemUserManagement();
        subutaiSteps.clickOnMenuItemAccountSettings();
        subutaiSteps.waitABit(5000);
//        subutaiSteps.clickOnButtonSetPublicKey();
//        subutaiSteps.waitABit(5000);
//        subutaiSteps.inputConfirmPasswordE2E();
//        subutaiSteps.clickOnButtonOkE2EKeyPopup();
    }

    @When("the user creates environment using template: Master")
    public void user_creates_environment_using_template_master() throws FindFailed, FileNotFoundException {
        subutaiSteps.clickOnIconTemplateMaster();
        subutaiSteps.clickOnButtonApply();
        subutaiSteps.inputEnvironmentName("Test Environment Master");
        subutaiSteps.clickOnButtonBuild();
        subutaiSteps.clickOnButtonCloseBuildPopup();
        subutaiSteps.waitFor(5000);
    }

    @When("the user clicks on the upper menu item: Register Peer")
    public void user_click_on_upper_menu_item() throws FindFailed {
        subutaiSteps.clickOnUpperMenuItemRegisterPeer();
    }

    @When("the user click on the upper menu icon: Notification")
    public void user_click_on_icon_notification() throws FindFailed {
        subutaiSteps.clickOnIconNotifications();
    }

    @When("the user clicks on the Environment's mode: Advanced")
    public void user_click_on_advance() throws FindFailed {
        subutaiSteps.clickOnAdvancedMode();
    }

    @When("the user clicks on the menu item: Kurjun")
    public void user_clicks_on_kurjun() throws FindFailed {
        subutaiSteps.clickOnMenuItemKurjun();
    }

    @When("the user clicks on the menu item: User Identity")
    public void user_clicks_on_user_identity() throws FindFailed {
        subutaiSteps.clickOnMenuItemUserIdentity();
    }

    @When("the user clicks on the menu item: Account Settings")
    public void user_clicks_on_account_settings() throws FindFailed {
        subutaiSteps.clickOnMenuItemAccountSettings();
    }

    @When("the user clicks on the menu item: Peer Settings")
    public void user_clicks_on_peer_settings() throws FindFailed {
        subutaiSteps.clickOnMenuItemPeerSettings();
    }

    @When("the user clicks on the menu item: Kurjun Settings")
    public void user_clicks_on_kurjun_settings() throws FindFailed {
        subutaiSteps.clickOnMenuItemKurjunSettings();
    }

    @When("the user clicks on the menu item: Network Settings")
    public void user_clicks_on_network_settings() throws FindFailed {
        subutaiSteps.clickOnMenuItemNetworkSettings();
    }

    @When("the user clicks on the menu item: Advanced")
    public void user_clicks_on_advanced() throws FindFailed {
        subutaiSteps.clickOnMenuItemAdvanced();
    }

    @When("the user enters login for Peer Registration on the Hub: '$login'")
    public void enters_login_peer_registration_on_the_hub(String login){
        subutaiSteps.inputLoginPeerRegistrationOnHub(login);
    }

    @When("the user enters password for Peer Registration on the Hub: '$password'")
    public void enters_password_peer_registration_on_the_hub(String password){
        subutaiSteps.inputPasswordPeerRegistrationOnHub(password);
    }

    @When("the user clicks on the button: Register")
    public void click_on_button_register() throws FindFailed {
        subutaiSteps.clickOnButtonRegister();
    }

    @When("the user clicks on the button: Go to HUB")
    public void click_on_button_go_to_hub() throws FindFailed {
        subutaiSteps.clickOnButtonGoToHUBGreen();
        subutaiSteps.waitABit(5000);
    }

    @When("user user clicks on the button: Close")
    public void click_button_close() throws FindFailed {
        subutaiSteps.clickButtonClose();
    }

    @When("the user clicks on the button: Peer Registration Online")
    public void click_button_peer_registration_online() throws FindFailed {
        subutaiSteps.clickOnButtonPeerRegistrationOnline();
    }

    @When("the user clicks on the button: Send Heartbeat")
    public void click_button_send_heartbeat() throws FindFailed {
        subutaiSteps.clickOnButtonSendHeartbeat();
    }

    @When("the user clicks on the button: OK")
    public void click_button_ok() throws FindFailed {
        subutaiSteps.clickOnButtonOkHeartbeat();
    }

    @When("the user click on the buton: Go to HUB White")
    public void click_on_the_button_go_to_hub_white() throws FindFailed {
        subutaiSteps.clickOnButtonGoToHUBWhite();
        subutaiSteps.waitABit(5000);
    }

    @When("the user clicks on the button: Сreate peer")
    public void clicks_on_the_button_create_peer() throws FindFailed {
        subutaiSteps.clickOnButtonCreatePeer();
    }

    @When("the user should wait a few seconds")
    public void user_should_wait_a_few_seconds(){
        subutaiSteps.userShouldWaitAFewSeconds();
    }

    @When("the user creates environment using template: Casandra")
    public void user_creates_environment_using_template_casandra() throws FindFailed, FileNotFoundException {
        subutaiSteps.clickOnIconTemplateCasandra();
        subutaiSteps.clickOnButtonApply();
        subutaiSteps.inputEnvironmentName("Test Environment Casandra");
        subutaiSteps.clickOnButtonBuild();
        subutaiSteps.clickOnButtonCloseBuildPopup();
        subutaiSteps.waitFor(5000);
    }

    @When("the user clicks on templates")
    public void user_clicks_on_templates() throws FindFailed {
        subutaiSteps.clickOnTitleTemplates();
    }

    @When("the user should find template: Master")
    public void user_finds_template_master(){
        subutaiSteps.inputTemplateNameInSearchField("Master");
    }

    @When("the user should install plugin")
    public void user_install_plugin() throws FindFailed {
        subutaiSteps.clickOnButtonInstall();
    }

    @When("the user clicks on Launch button")
    public void user_clicks_launch_button() throws FindFailed {
        subutaiSteps.clickOnButtonLaunch();
    }

    @When("the user input the cluster name: '$cluster'")
    public void user_search_plugin(String cluster){
        subutaiSteps.inputClusterName(cluster);
    }

}