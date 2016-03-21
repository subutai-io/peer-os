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
import org.sikuli.script.*;


public class SubutaiSteps extends ScenarioSteps {

    //region IMPORT: Imports
    LoginPage loginPage;
    CommonPages commonPage;
    ExecuteShellCommand executeShellCommand;
    MonitoringPage monitoringPage;
    BlueprintsPage blueprintsPage;
    EnvironmentsPage environmentsPage;
    ContainersPage containersPage;
    KurjunPage kurjunPage;
    ConsolePage consolePage;
    UserManagementPage userManagementPage;
    RoleManagementPage roleManagementPage;
    AccountSettings accountSettingsPage;
    TokensPage tokensPage;
    PeerRegistrationPage peerRegistrationPage;
    ResourceHostsPage resourceHostsPage;
    TrackerPage trackerPage;
    BazaarPage bazaarPage;
    PeerSettingsPage peerSettingsPage;
    KurjunSettingsPage kurjunSettingsPage;
    NetworkSettings networkSettingsPage;
    AdvancedPage advancedPage;
    AboutPage aboutPage;
    PgpPlugin pgpPlugin;
    Screen screen = new Screen();
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

    @Step
    public void inputEnvironmentName(String env_name){
        environmentsPage.inputEnvironmentName.type(env_name);
    }

    //endregion

    //region ACTION: Click

    @Step
    public void clickOnButtonLogin() throws FindFailed {
//        loginPage.buttonLogin.click();
        screen.click(commonPage.sikuliButtonLogin);
    }

    @Step
    public void clickOnMenuItemMonitoring() throws FindFailed {
//        commonPage.linkMonitoring.click();
        screen.click(commonPage.sikuliMenuItemMonitoring);
        commonPage.waitFor(2000);
    }

    @Step
    public void clickOnSelectorHostsMonitoringPage() throws FindFailed {
//        monitoringPage.selectorHosts.click();
        screen.click(monitoringPage.sikuliMenuIconRH);
        commonPage.waitFor(2000);
    }

    @Step
    public void clickOnMenuEnvironment() throws FindFailed {
//        commonPage.linkEnvironment.click();
        screen.click(commonPage.sikuliMenuItemEnvironment);
        commonPage.waitFor(2000);
    }

    @Step
    public void clickOnMenuItemBlueprints() {
        commonPage.linkBlueprint.click();
    }

    @Step
    public void clickOnMenuItemEnvironments() throws FindFailed {
//        commonPage.linkEnvironments.click();
        screen.click(commonPage.sikuliMenuItemEnvironments);
    }

    @Step
    public void clickOnMenuItemContainers() throws FindFailed {
//        commonPage.linkContainers.click();
        screen.click(commonPage.sikuliMenuItemContainers);
    }

    @Step
    public void clickOnMenuItemConsole() throws FindFailed {
//        commonPage.linkConsole.click();
        screen.click(commonPage.sikuliMenuItemConsole);
    }

    @Step
    public void clickOnSelectorHostsConsolePage() throws FindFailed {
//        consolePage.itemSelectorHost.click();
        screen.click(consolePage.sikuliIconSelectorHost);
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
    public void clickOnMenuItemUserManagement() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemUserManagement);
    }

    @Step
    public void clickOnMenuItemRoleManagement() throws FindFailed {
//        commonPage.linkRoleManagement.click();
        screen.click(commonPage.sikuliMenuItemRoleManagement);
    }

    @Step
    public void clickOnMenuItemTokens() throws FindFailed {
//        commonPage.linkTokens.click();
        screen.click(commonPage.sikuliMenuItemTokens);
    }

    @Step
    public void clickOnMenuItemPeerRegistration() throws FindFailed {
//        commonPage.linkPeerRegistration.click();
        screen.click(commonPage.sikuliMenuItemPeerRegistration);
    }

    @Step
    public void clickOnMenuItemResourceHosts() throws FindFailed {
//        commonPage.linkResourceNode.click();
        screen.click(commonPage.sikuliMenuItemResourceHosts);
    }

    @Step
    public void clickOnMenuItemTracker() throws FindFailed {
//        commonPage.linkTracker.click();
        screen.click(commonPage.sikuliMenuItemTracker);
    }

    @Step
    public void clickOnMenuItemBazaar() throws FindFailed {
//        commonPage.linkPlugins.click();
        screen.click(commonPage.sikuliMenuItemBazaar);
    }

    @Step
    public void clickOnMenuItemSystemSettings() throws FindFailed {
//        commonPage.linkPluginIntegrator.click();
        screen.click(commonPage.sikuliMenuItemSystemSettings);
    }

    @Step
    public void clickOnMenuItemAbout() throws FindFailed {
//        commonPage.linkAbout.click();
        screen.click(commonPage.sikuliMenuItemAbout);
    }

    @Step
    public void clickOnIconTemplateMongo() throws FindFailed {
        environmentsPage.waitFor(environmentsPage.sikuliTemplateMongo);
        screen.click(environmentsPage.sikuliTemplateMongo);
    }


    @Step
    public void clickOnButtonApply() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonApply);
    }

    @Step
    public void clickOnButtonBuild() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonBuild);
    }

    @Step
    public void clickOnButtonCloseBuildPopup() throws FindFailed {
        commonPage.waitFor(environmentsPage.buttonClose);
        screen.click(environmentsPage.sikuliButtonClosePopupBuild);
    }

    @Step
    public void clickOnIconDeleteEnvironment() throws FindFailed {
        screen.click(environmentsPage.sikuliIconDeleteEnvironment);
    }

    @Step
    public void clickOnButtonDelete() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonDelete);
    }

    @Step
    public void clickOnButtonOkPopupEnvironmentHasBeenDestroyed() throws FindFailed {
        commonPage.waitFor(commonPage.textEnvironmentHasBeenDestroyed);
        screen.click(environmentsPage.sikuliButtonOk);
    }

    @Step
    public void clickOnIconPgp() throws FindFailed {
        screen.click(pgpPlugin.sikuliIconPgp);
    }

    @Step
    public void clickOnButtonOptions() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonOptions);
    }

    @Step
    public void clickOnButtonGenerate() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonGenerate);
    }

    @Step
    public void typeInFieldsPgp(){
        screen.type(pgpPlugin.sikuliFieldEnterKeyName, "test");
        screen.type(pgpPlugin.sikuliFieldEnterEmail, "test@test.com");
        screen.type(pgpPlugin.sikuliFieldEnterPassword, "secret");
        screen.type(pgpPlugin.sikuliFieldConfirmPassword, "secret");
    }

    @Step
    public void clickOnButtonSubmit() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonSubmit);
    }

    @Step
    public void clickOnButtonOk() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonOk);
        screen.wait(pgpPlugin.sikuliMessageKeyPairSuccessfullyGenerated, 60000);
        screen.click(pgpPlugin.sikuliButtonOk);
    }

    @Step
    public void clickOnSubutaiSocialTab() throws FindFailed {
        screen.click(pgpPlugin.sikuliSubutaiSocialTab);
    }

    @Step
    public void clickOnMenuItemAccountSettings() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemAccountSettings);
    }

    @Step
    public void clickOnButtonSetPublicKey() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonSetPublicKey);
    }

    @Step
    public void clickOnUpperMenuItemRegisterPeer() throws FindFailed {
        screen.click(commonPage.sikuliUpperMenuItemRegisterPeer);
    }

    @Step
    public void clickOnIconNotifications() throws FindFailed {
        screen.click(commonPage.sikuliIconNotifications);
    }

    @Step
    public void clickOnAdvancedMode() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonAdvanced);
    }

    @Step
    public void clickOnTitleTemplates() throws FindFailed {
        screen.click(environmentsPage.sikuliTitleTemplates);
    }

    @Step
    public void clickOnMenuItemKurjun() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemKurjun);
    }

    @Step
    public void clickOnTitleAPT() throws FindFailed {
        screen.click(kurjunPage.sikuliTitleAPT);
    }

    @Step
    public void clickOnMenuItemUserIdentity() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemUserIdentity);
    }

    @Step
    public void clickOnTitleChangePassword() throws FindFailed {
        screen.click(accountSettingsPage.sikuliTitleChangePassword);
    }

    @Step
    public void clickOnTitleInstalled() throws FindFailed {
        screen.click(bazaarPage.sikuliMenuTitleInstalled);
    }

    @Step
    public void clickOnTitleAdvanced() throws FindFailed {
        screen.click(bazaarPage.sikuliMenuTitleAdvanced);
    }

    @Step
    public void clickOnMenuItemPeerSettings() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemPeerSettings);
    }

    @Step
    public void clickOnTitlePolicy() throws FindFailed {
        screen.click(peerSettingsPage.sikuliTitlePolicy);
    }

    @Step
    public void clickOnMenuItemKurjunSettings() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemKurjunSettings);
    }

    @Step
    public void clickOnTitleQuotas() throws FindFailed {
        screen.click(kurjunSettingsPage.sikuliTitleQuotas);
    }

    @Step
    public void clickOnMenuItemNetworkSettings() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemNetworkSettings);
    }

    @Step
    public void clickOnMenuItemAdvanced() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemAdvanced);
    }

    @Step
    public void clickOnTitleEnvironment() throws FindFailed {
        screen.click(monitoringPage.sikuliTitleEnvironment);
    }

    @Step
    public void clickOnTitleLogs() throws FindFailed {
        screen.click(advancedPage.sikuliTitleLogs);
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
    public void userShouldObserveHeaderResourceHosts() {
        assertThat(resourceHostsPage.headerResourceHosts.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderTracker() {
        assertThat(trackerPage.headerTracker.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderPeerSettings() {
        assertThat(peerSettingsPage.headerPeerSettings.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonUploadNewPlugin() {
        assertThat(bazaarPage.buttonUploadNewPlugin.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderAbout() {
        assertThat(aboutPage.headerAbout.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonCreateEnvironment() {
        assertThat(environmentsPage.buttonCreateEnvironment.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveTitleRegisterPeer() {
        assertThat(commonPage.titleUpperMenuRegisterPeer.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveUpperMenuBody() {
        assertThat(commonPage.upperMenuLoginBody.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveLinkSignUp() {
        assertThat(commonPage.linkSignUp.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonRegister() {
        assertThat(commonPage.buttonRegister.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveNotificationsBody() {
        assertThat(commonPage.upperMenuNotificationsBody.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveLinkClear() {
        assertThat(commonPage.linkClear.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveUserNameAdmin() {
        assertThat(commonPage.linkAdmin.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonModes() {
        assertThat(environmentsPage.buttonModes.isVisible(), is(true));
    }

    @Step
    public void userShouldObservePluginMongo() {
        environmentsPage.waitFor(environmentsPage.sikuliTemplateMongo);
        assertThat(environmentsPage.templateMongo.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonApply() {
        assertThat(environmentsPage.buttonApply.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveTitlePeers() {
        assertThat(environmentsPage.titlePeers.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveGreenButton() {
        assertThat(commonPage.buttonGreen.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveFieldPGP() {
        assertThat(accountSettingsPage.fieldPGP.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSetPublicKey() {
        assertThat(accountSettingsPage.buttonSetPublicKey.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSave() {
        assertThat(accountSettingsPage.buttonSave.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSetPeerOwner() {
        assertThat(peerSettingsPage.buttonSetPeerOwner.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSaveOnPolicyPage() {
        assertThat(peerSettingsPage.buttonSave.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSaveOnKurjunSettingsPage() {
        assertThat(kurjunSettingsPage.buttonSave.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonAddOnKurjunSettingsPage() {
        assertThat(kurjunSettingsPage.buttonAdd.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderNetworkSettings() {
        assertThat(networkSettingsPage.headerNetworkSettings.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSaveOnNetworkSettingsPage() {
        assertThat(networkSettingsPage.buttonSave.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderAdvanced() {
        assertThat(advancedPage.headerAdvancedSettings.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonExport() {
        assertThat(advancedPage.buttonExport.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveSelectorEnvironment() {
        assertThat(monitoringPage.selectorSelectEnvironment.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveSelectorEnvironmentConsole() {
        assertThat(consolePage.selectorEnvironment.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonSaveOnKurjunSettingsQuotasPage() {
        assertThat(kurjunSettingsPage.buttonSaveQuotas.isVisible(), is(true));
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