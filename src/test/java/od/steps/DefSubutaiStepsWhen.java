package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.When;

public class DefSubutaiStepsWhen {

    @Steps
    SubutaiSteps subutaiSteps;

    @When("the user clicks on the menu item: Monitoring")
    public void user_clicks_monitoring() {
        subutaiSteps.clickOnMenuItemMonitoring();
    }

    @When("the user clicks on the menu item: Blueprints")
    public void user_clicks_blueprints() {
        subutaiSteps.clickOnMenuEnvironment();
        subutaiSteps.clickOnMenuItemBlueprints();
    }

    @When("the user clicks on the menu item: Environments")
    public void user_clicks_environment() {
        subutaiSteps.clickOnMenuItemEnvironments();
    }

    @When("the user clicks on the menu item: Containers")
    public void user_clicks_containers() {
        subutaiSteps.clickOnMenuItemContainers();
    }

    @When("the user clicks on the menu item: Console")
    public void user_clicks_console() {
        subutaiSteps.clickOnMenuItemConsole();
    }

    @When("the user chooses: Management host")
    public void user_choose_management_host() {
        subutaiSteps.clickOnSelectorHostsConsolePage();
        subutaiSteps.clickOnManagementHost();
    }

    @When("the user enters console command: '$command'")
    public void executes_console_command(String command) {
        subutaiSteps.executeConsoleCommand(command);
    }

    @When("the user chooses: Local host")
    public void user_choose_local_host() {
        subutaiSteps.clickOnSelectorHostsConsolePage();
        subutaiSteps.clickOnLocalHost();
    }

    @When("the user clicks on the menu item: User management")
    public void user_clicks_user_management() {
        subutaiSteps.clickOnMenuUserIdentity();
        subutaiSteps.clickOnMenuItemUserManagement();
    }

    @When("the user clicks on the menu item: Role management")
    public void user_clicks_role_management() {
        subutaiSteps.clickOnMenuItemRoleManagement();
    }

    @When("the user clicks on the menu item: Tokens")
    public void user_clicks_tokens() {
        subutaiSteps.clickOnMenuItemTokens();
    }

    @When("the user clicks on the menu item: Peer Registration")
    public void user_clicks_peer_registration() {
        subutaiSteps.clickOnMenuItemPeerRegistration();
    }

    @When("the user clicks on the menu item: Resource Nodes")
    public void user_clicks_resource_nodes() {
        subutaiSteps.clickOnMenuItemResourceNodes();
    }

    @When("the user clicks on the menu item: Tracker")
    public void user_clicks_tracker() {
        subutaiSteps.clickOnMenuItemTracker();
    }

    @When("the user clicks on the menu item: Plugins")
    public void user_clicks_plugins() {
        subutaiSteps.clickOnMenuItemPlugins();
    }
}