package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.When;
import org.sikuli.script.FindFailed;

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
        subutaiSteps.waitFor(5000);
        subutaiSteps.clickOnButtonSetPublicKey();
    }

    @When("the user creates environment using template: Mongo")
    public void user_creates_environment_using_template_mongo() throws FindFailed {

        subutaiSteps.clickOnIconTemplateMongo();
        subutaiSteps.clickOnButtonApply();
        subutaiSteps.inputEnvironmentName("Test Environment Mongo");
        subutaiSteps.clickOnButtonBuild();
        subutaiSteps.clickOnButtonCloseBuildPopup();
        subutaiSteps.waitFor(5000);
        subutaiSteps.clickOnIconDeleteEnvironment();
        subutaiSteps.clickOnButtonDelete();
        subutaiSteps.clickOnButtonOkPopupEnvironmentHasBeenDestroyed();
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

}