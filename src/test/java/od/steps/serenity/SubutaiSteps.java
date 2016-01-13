package od.steps.serenity;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.classes.ExecuteShellCommand;
import od.classes.ReaderFromFile;
import od.pages.*;
import org.hamcrest.Matchers;

import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class SubutaiSteps extends ScenarioSteps {

    //region IMPORT: Imports
    LoginPage loginPage;
    CommonPages commonPage;
    ExecuteShellCommand executeShellCommand;
    MonitoringPage monitoringPage;
    BlueprintsPage blueprintsPage;
    EnvironmentsPage environmentsPage;
    ContainersPage containersPage;
    ConsolePage consolePage;
    UserManagementPage userManagementPage;
    RoleManagementPage roleManagementPage;
    TokensPage tokensPage;
    PeerRegistrationPage peerRegistrationPage;
    ResourceNodesPage resourceNodesPage;
    TrackerPage trackerPage;
    PluginsPage pluginsPage;
    //endregion

    //region ACTION: Open Page

    @Step
    public void open_mng_h1() throws FileNotFoundException {
        loginPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        loginPage.open();
    }

    @Step
    public void open_mng_h2() throws FileNotFoundException {
        loginPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        loginPage.open();
    }

    //endregion

    //region ACTION: Type

    @Step
    public void inputLogin(String login){
        loginPage.inputLogin.type(login);
    }

    @Step
    public void inputPassword(String password){
        loginPage.inputPassword.type(password);
    }

    //endregion

    //region ACTION: Click

    @Step
    public void clickOnButtonLogin(){
        loginPage.buttonLogin.click();
        waitABit(5000);
    }

    @Step
    public void clickOnMenuItemMonitoring() {
        commonPage.linkMonitoring.click();
    }

    @Step
    public void clickOnSelectorHostsMonitoringPage() {
        monitoringPage.selectorHosts.click();
    }

    @Step
    public void clickOnMenuEnvironment() {
        commonPage.linkEnvironment.click();
    }

    @Step
    public void clickOnMenuItemBlueprints() {
        commonPage.linkBlueprint.click();
    }

    @Step
    public void clickOnMenuItemEnvironments() {
        commonPage.linkEnvironments.click();
    }

    @Step
    public void clickOnMenuItemContainers() {
        commonPage.linkContainers.click();
    }

    @Step
    public void clickOnMenuItemConsole() {
        commonPage.linkConsole.click();
    }

    @Step
    public void clickOnSelectorHostsConsolePage() {
        consolePage.itemSelectorHost.click();
    }

    @Step
    public void clickOnManagementHost() {
        consolePage.selectorHostsItemManagementHost.click();
    }

    @Step
    public void clickOnLocalHost() {
        consolePage.selectorHostsItemRecourceHost.click();
    }

    public void clickOnMenuUserIdentity() {
        commonPage.linkUserIdentity.click();
    }

    @Step
    public void clickOnMenuItemUserManagement() {
        commonPage.linkUserManagement.click();
    }

    @Step
    public void clickOnMenuItemRoleManagement() {
        commonPage.linkRoleManagement.click();
    }

    @Step
    public void clickOnMenuItemTokens() {
        commonPage.linkTokens.click();
    }

    @Step
    public void clickOnMenuItemPeerRegistration() {
        commonPage.linkPeerRegistration.click();
    }

    @Step
    public void clickOnMenuItemResourceNodes() {
        commonPage.linkResourceNode.click();
    }

    @Step
    public void clickOnMenuItemTracker() {
        commonPage.linkTracker.click();
    }

    @Step
    public void clickOnMenuItemPlugins() {
        commonPage.linkPlugins.click();
    }

    //endregion

    //region ACTION: Wait

    //endregion

    //region VERIFICATION: AssertThat

    @Step
    public void userShouldObserveHeaderLogin() {
        assertThat(loginPage.headerLogin.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveFieldLogin() {
        assertThat(loginPage.inputLogin.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveFieldPassword() {
        assertThat(loginPage.inputPassword.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderMonitoring() {
        assertThat(monitoringPage.headerMonitoring.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveManagementHost() {
        assertThat(monitoringPage.selectorHostsItemManagementHost.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveResourceHost() {
        assertThat(monitoringPage.selectorHostsItemRecourceHost.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderBlueprints() {
        assertThat(blueprintsPage.headerBlueprints.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonCreateBlueprints() {
        assertThat(blueprintsPage.buttonCreateBlueprint.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderEnvironments() {
        assertThat(environmentsPage.headerEnvironments.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveFieldSearch() {
        assertThat(commonPage.fieldSearch.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderContainers() {
        assertThat(containersPage.headerContainers.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderUserManagement() {
        assertThat(userManagementPage.headerUserManagement.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonAddUser() {
        assertThat(userManagementPage.buttonAddUser.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderRoleManagement() {
        assertThat(roleManagementPage.headerRoleManagement.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonAddRole() {
        assertThat(roleManagementPage.buttonAddRole.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderTokens() {
        assertThat(tokensPage.headerTokens.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonAddToken() {
        assertThat(tokensPage.buttonAddToken.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderPeerRegistration() {
        assertThat(peerRegistrationPage.headerPeerRegistration.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonCreatePeer() {
        assertThat(peerRegistrationPage.buttonCreatePeer.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderResourceNodes() {
        assertThat(resourceNodesPage.headerNodes.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderTracker() {
        assertThat(trackerPage.headerTracker.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderPlugins() {
        assertThat(pluginsPage.headerPlugins.isVisible(), is(true));
    }

    @Step
    public void userShouldObservePluginItems() {
        assertThat(pluginsPage.pluginItems.isVisible(), is(true));
    }
    //endregion

    //region ACTION: Shell command

    @Step
    public void executeConsoleCommand(String command){
        executeShellCommand.executeConsoleCommand(command);
    }

    @Step
    public void seeOutputOfPwdCommand() {
        assertThat(consolePage.outputOfPwdCommand.isVisible(), Matchers.is(true));
    }


    //endregion
}