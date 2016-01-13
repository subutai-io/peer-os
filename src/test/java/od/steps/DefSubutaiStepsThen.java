package od.steps;

import net.thucydides.core.annotations.Steps;
import od.steps.serenity.SubutaiSteps;
import org.jbehave.core.annotations.Then;

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
    public void user_obseve_monitoring_page(){
        subutaiSteps.userShouldObserveHeaderMonitoring();
        subutaiSteps.clickOnSelectorHostsMonitoringPage();
        subutaiSteps.userShouldObserveManagementHost();
        subutaiSteps.userShouldObserveResourceHost();
    }

    @Then("the user should observe web elements on: Blueprints page")
    public void user_observe_blueprints(){
        subutaiSteps.userShouldObserveHeaderBlueprints();
        subutaiSteps.userShouldObserveButtonCreateBlueprints();
    }

    @Then("the user should observe web elements on: Environments page")
    public void user_observe_environments(){
        subutaiSteps.userShouldObserveHeaderEnvironments();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Containers page")
    public void user_observe_containers(){
        subutaiSteps.userShouldObserveHeaderContainers();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe output of the pwd command")
    public void user_observes_output_of_pwd_command(){
        subutaiSteps.seeOutputOfPwdCommand();
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

    @Then("the user should observe web elements on: Resource Nodes page")
    public void user_observe_resource_nodes() {
        subutaiSteps.userShouldObserveHeaderResourceNodes();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Tracker page")
    public void user_observe_tracker() {
        subutaiSteps.userShouldObserveHeaderTracker();
        subutaiSteps.userShouldObserveFieldSearch();
    }

    @Then("the user should observe web elements on: Plugins page")
    public void user_observe_plugins() {
        subutaiSteps.userShouldObserveHeaderPlugins();
        subutaiSteps.userShouldObservePluginItems();
    }
}