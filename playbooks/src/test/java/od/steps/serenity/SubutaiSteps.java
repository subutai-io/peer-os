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
    PluginsPage pluginsPage;
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

    @Step
    public void open_domain_test() throws FileNotFoundException {
        loginPage.setDefaultBaseUrl(String.format(ReaderFromFile.readDataFromFile("src/test/resources/parameters/test")));
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

    @Step
    public void inputNewUserUsername(String username) {
        userManagementPage.fieldUsername.type(username);
    }

    @Step
    public void inputNewUserFullName(String fullname) {
        userManagementPage.fieldFullName.type(fullname);
    }

    @Step
    public void inputNewUserEmail(String email) {
        userManagementPage.fieldEmail.type(email);
    }

    @Step
    public void inputNewUserPassword(String password) {
        userManagementPage.fieldPassword.type(password);
    }

    @Step
    public void inputNewUserConfirmPassword(String confirmPassword) {
        userManagementPage.fieldConfirmPassword.type(confirmPassword);
    }

    @Step
    public void inputLoginPeerRegistrationOnHub(String login){
        commonPage.fieldLogin.type(login);
    }

    @Step
    public void inputPasswordPeerRegistrationOnHub(String password){
        commonPage.fieldPassword.type(password);
    }

    @Step
    public void inputPeerIP(String ip) {
        peerRegistrationPage.fieldPeerIp.type(ip);
    }

    @Step
    public void inputPeerKeyPhrase(String key) {
        peerRegistrationPage.fieldPeerKeyPhrase.type(key);

    }

    @Step
    public void inputApprovePeerKeyPhrase(String key) {
        peerRegistrationPage.fieldPeerApprove.type(key);
    }

    @Step
    public void inputTemplateNameInSearchField(String name) {
//        environmentsPage.waitTemplateCassandra();
        waitABit(3000);
        environmentsPage.fieldSearch.type(name);
    }

    @Step
    public void inputPluginName(String plugin) {
        bazaarPage.fieldSearch.type(plugin);
    }

    @Step
    public void inputClusterName(String cluster) {
        pluginsPage.fieldClusterName.type(cluster);
    }

    @Step
    public void inputProfileName(String profile) {
        pluginsPage.fieldProfile.type(profile);
    }

    @Step
    public void inputDomainName(String name) {
        pluginsPage.fieldDomainName.type(name);
    }

    @Step
    public void inputOperationName(String operation) {
        pluginsPage.fieldOperationName.type(operation);
    }

    @Step
    public void inputOperation(String ls) {
        pluginsPage.fieldOperation.type(ls);
    }

    @Step
    public void inputDomain(String name) {
        pluginsPage.fieldDomain.type(name);
    }

    @Step
    public void inputAppScaleEnvironmentName(String name) {
        pluginsPage.fieldEnvironmentName.type(name);
    }

    @Step
    public void inputOldPassword(String oldpass) {
        accountSettingsPage.fieldOldPassword.type(oldpass);
    }

    @Step
    public void inputNepPassword(String password) {
        accountSettingsPage.fieldNewPassword.type(password);
    }

    @Step
    public void inputConfirmPassword(String confpass) {
        accountSettingsPage.fieldConfirmPassword.type(confpass);
    }

    @Step
    public void inputTheRoleName(String role) {
        roleManagementPage.fieldRoleName.type(role);
    }

    @Step
    public void inputNameInSearchField(String iManagement) {
        commonPage.fieldSearch.type(iManagement);
    }

    @Step
    public void inputSetDomainName(String s) {
        environmentsPage.fieldDomainName.type(s);
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
        waitABit(4000);
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
//        waitABit(20000);
    }

    @Step
    public void clickOnButtonCloseBuildPopup() throws FindFailed {
//        commonPage.waitFor(environmentsPage.buttonClose);
        environmentsPage.waitForCloseButton();
        screen.click(environmentsPage.sikuliButtonClosePopupBuild);
    }

    @Step
    public void clickOnIconDeleteEnvironment() throws FindFailed {
        screen.click(environmentsPage.sikuliIconDeleteEnvironment);
    }

    @Step
    public void clickOnButtonDelete() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonDelete);
//        waitABit(1000000);
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
    public void typeInFieldsPgp() throws FindFailed {
//        screen.type(pgpPlugin.sikuliFieldEnterKeyName, "test");
        screen.type(pgpPlugin.sikuliFieldEnterEmail, "test@test.com");
        screen.click(pgpPlugin.sikuliCheckBoxProtectYourKeyWithPassword);
        screen.type(pgpPlugin.sikuliFieldEnterPassword, "239668a");
        screen.type(pgpPlugin.sikuliFieldConfirmPassword, "239668a");
    }

    @Step
    public void typeInFieldConfirmPasswordE2EKey(){
        screen.type(pgpPlugin.sikuliFieldConfirmPasswordPopup, "test@test.com");
    }

    @Step
    public void clickOnButtonSubmit() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonSubmit);
    }

    @Step
    public void clickOnButtonOkPGP() throws FindFailed {
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
        //screen.click(environmentsPage.sikuliButtonAdvanced);
        environmentsPage.checkboxEnviromentMode.click();
    }

    @Step
    public void clickOnTitleTemplates() throws FindFailed {
        screen.click(environmentsPage.sikuliTitleTemplates);
    }

    @Step
    public void clickOnMenuItemKurjun() throws FindFailed {
        screen.click(commonPage.sikuliMenuItemKurjun);
        waitABit(5000);
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

    @Step
    public void clickOnTemplateWebdemo() throws FindFailed {
       screen.click(environmentsPage.sikuliTemplateWebdemo);
    }

    @Step
    public void clickOnIconDeleteEnvironmentNotSikuli() {
        environmentsPage.iconDeleteEnvironment.click();
    }

    @Step
    public void clickOnButtonAddUser() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonAddUser);
    }

    @Step
    public void clickOnTrustedLevel() {
        userManagementPage.selectorTrustedLevel.click();
    }

    @Step
    public void clickOnUltimateTrust() {
        userManagementPage.pickerUltimateTrust.click();
        userManagementPage.pickerUltimateTrust.click();
    }

    @Step
    public void clickOnButtonChooseAll() {
        userManagementPage.buttonChooseAll.click();
    }

    @Step
    public void clickOnButtonSaveUser() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonSaveUser);
    }

    @Step
    public void clickOnIconDeleteRoleInternalSystem() {
        userManagementPage.roleInternalSystem.click();
    }

    @Step
    public void clickOnRemoveRoleButton() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonRemove);
    }

    @Step
    public void clickOnButtonOkRoleRemoved() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonOk);
//        waitABit(5000);
    }

    @Step
    public void clickOnButtonRemoveUser() {
        userManagementPage.buttonRemoveUser.click();
    }

    @Step
    public void clickOnButtonOkDeleteUser() throws FindFailed {
        screen.click(userManagementPage.sikuliButtonOk);
    }

    @Step
    public void clickOnButtonCreatePeer() throws FindFailed {
        screen.click(peerRegistrationPage.sikuliButtonCreatePeer);
    }

    @Step
    public void clickOnButtonCreate() throws FindFailed {
        screen.click(peerRegistrationPage.sikuliButtonCreate);
    }

    @Step
    public void clickbuttonApprovePopUp() throws FindFailed {
        screen.click(peerRegistrationPage.sikuliButtonApprovePopUp);
    }

    @Step
    public void clickOnIconTemplateCasandra() throws FindFailed {
        screen.click(environmentsPage.sikuliTemplateCasandra);
    }

    @Step
    public void clickbuttonApprove() throws FindFailed {
        screen.click(peerRegistrationPage.sikuliButtonApprove);
    }

    @Step
    public void clickOnButtonUnregister() throws FindFailed {
        peerRegistrationPage.waitButtonUnregister();
        screen.click(peerRegistrationPage.sikuliButtonUnregister);
    }

    @Step
    public void clickOnButtonUnregisterPopup() throws FindFailed {
        screen.click(peerRegistrationPage.sikuliButtonUnregisterPopup);
    }

    @Step
    public void clickOnButtonOkUnregisterPeer() throws FindFailed {
        screen.click(commonPage.sikuliButtonOk);
    }

    @Step
    public void clickOnPeer1() throws FindFailed {
        environmentsPage.waitPeer1();
        screen.click(environmentsPage.sikuliPeer1);
    }

    @Step
    public void clickOnPeer2() throws FindFailed {
        screen.click(environmentsPage.sikuliPeer2);
    }

    @Step
    public void clickOnResourceHost1() throws FindFailed {
        screen.click(environmentsPage.sikuliResourceHost1);
    }

    @Step
    public void clickOnTitleManagement() throws FindFailed {
        screen.click(monitoringPage.sikuliTitleManagement);
    }

    @Step
    public void clickOnMenuButtonInstall() throws FindFailed {
        screen.click(bazaarPage.sikuliMenuButtonInstall);
    }

    @Step
    public void clickOnIconTemplateMaster() throws FindFailed {
        screen.click(environmentsPage.sikuliTemplateMaster);
    }

    @Step
    public void clickOnButtonLaunch() throws FindFailed {
        bazaarPage.waitButtonLaunch();
        screen.click(bazaarPage.sikuliButtonLaunch);
    }

    @Step
    public void clickOnTitleRawFiles() throws FindFailed {
        screen.click(kurjunPage.sikuliTitleRawFiles);
    }

    @Step
    public void clickOnTitleUrlsList() throws FindFailed {
        screen.click(kurjunSettingsPage.sikuliTitleUrlsList);
    }

    @Step

    public void clickOnSelectorEnvironment() throws FindFailed {
        screen.click(pluginsPage.sikuliSelectorEnvironment);
    }

    @Step
    public void clickOnEnvironmentFromSelector() {
        pluginsPage.selectorEnvironmentMaster.click();
    }

    @Step
    public void clickOnTitleManage() throws FindFailed {
        screen.click(pluginsPage.sikuliTitleManage);
    }

    @Step
    public void clickOnButtonConfigureOperations() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonConfigureOperations);
    }

    @Step
    public void clickOnButtonAddOperation() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonAddOperation);
    }

    @Step
    public void clickOnButtonExecute() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonExecute);
    }

    @Step
    public void clickOnTitleCreate() throws FindFailed {
        screen.click(pluginsPage.sikuliTitleCreate);
    }

    @Step
    public void clickOnButtonUninstall() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonUninstall);
    }

    @Step
    public void clickOnButtonQuickInstall() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonQuickInstall);
    }

    @Step
    public void clickOnButtonConsole() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonConsole);
    }

    @Step
    public void clickOnButtonInstall() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonInstall);
        waitABit(50000);
    }

    @Step
    public void clickOnButtonOk() throws FindFailed {
        screen.click(commonPage.sikuliButtonOk);
    }

    @Step
    public void clickOnButtonSave() throws FindFailed {
        screen.click(pluginsPage.sikuliButtonSave);
    }

    @Step
    public void clickOnButtonCheck() throws FindFailed {
        screen.click(containersPage.sikuliButtonCheck);
    }

    @Step
    public void clickOnButtonStop() throws FindFailed {
        screen.click(containersPage.sikuliButtonStop);
    }

    @Step
    public void clickOnButtonRemove() throws FindFailed {
        screen.click(containersPage.sikuliButtonRemove);
    }

    @Step
    public void clickOnButtonDestroy() throws FindFailed {
        screen.click(containersPage.sikuliButtonDestroy);
    }

    @Step
    public void clickOnButtonStart() throws FindFailed {
        screen.click(containersPage.sikuliButtonStart);
        waitABit(4000);
    }

    @Step
    public void clickOnButtonAddTemplate() throws FindFailed {
        screen.click(kurjunPage.sikuliButtonAddTemplate);
    }

    @Step
    public void clickOnButtonBrowse() throws FindFailed {
        screen.click(kurjunPage.sikuliButtonBrowse);
    }
    @Step
    public void clickOnButtonOpen() throws FindFailed {
        screen.click(commonPage.sikuliButtonOpen);
    }

    @Step
    public void clickOnButtonAdd() throws FindFailed {
        screen.click(commonPage.sikuliButtonAdd);
        waitABit(8000);
    }

    @Step
    public void clickOnButtonEdit() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonEdit);
    }

    @Step
    public void clickOnButtonNext() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonNext);
    }

    @Step
    public void clickOniconAdmin() throws FindFailed {
        screen.click(commonPage.sikuliIconAdmin);
    }

    @Step
    public void clickOnIconTest() throws FindFailed {
        screen.click(commonPage.sikuliIconTest);
    }

    @Step
    public void clickOnButtonShare() throws FindFailed {
        screen.click(environmentsPage.sikuliIconShare);
    }

    @Step
    public void clickOnAddTheUserTest() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonAddUserTest);
    }

    @Step
    public void clickOnCheckBoxDeleteRoleFromUser() throws FindFailed {
        screen.click(environmentsPage.sikuliCheckBoxDeleteInShareEnvi);
    }

    @Step
    public void clickOnIconAddIdentityManagement() throws FindFailed {
        screen.click(roleManagementPage.sikuliIconAddIdentityManagement);
    }

    @Step
    public void clickOnIconDeleteRole() throws FindFailed {
        screen.click(roleManagementPage.sikuliIconDeleteRole);
    }

    @Step
    public void clickOnIconTemplateApache() throws FindFailed {
        screen.click(environmentsPage.sikuliTemplateApache);
    }

    @Step
    public void clickOnButtonConfigure() throws FindFailed {
        screen.click(environmentsPage.sikuliButtonConfigure);
    }

    @Step
    public void clickOnCheckboxAddDomain() throws FindFailed {
        screen.click(environmentsPage.sikuliCheckBoxAddDomain);
    }
    //endregion

    //region Action: Drag And Drop

    @Step
    public void dragAndDropTemplateCassandra() throws FindFailed {
        screen.dragDrop(environmentsPage.sikuliTemplateCasandra, environmentsPage.sikuliPeerRH1);
//        waitABit(5000);
    }

    @Step
    public void dragAndDropTemplateMasterToRH1() throws FindFailed {
        screen.dragDrop(environmentsPage.sikuliTemplateMaster, environmentsPage.sikuliPeerRH1);
    }

    @Step
    public void dragAndDropTemplateMasterToRH2() throws FindFailed {
        screen.dragDrop(environmentsPage.sikuliTemplateMaster, environmentsPage.sikuliPeerRH2);
    }

    //endregion

    //region ACTION: Wait

    @Step
    public void waitGeneratedE2EKey() throws InterruptedException {
        waitABit(30000);
    }

    @Step
    public void userShouldWaitAFewSeconds() {
        waitABit(3000);
    }
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
        environmentsPage.waitTemplateMongo();
        assertThat(environmentsPage.templateMongo.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonApply() {
        assertThat(environmentsPage.buttonApply.isVisible(), is(true));
        waitABit(20000);
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

    @Step
    public void userShouldObserveANewUsersEmail() {
        assertThat(userManagementPage.usersEmail.isVisible(), is(true));
    }

    @Step
    public void userShouldNotObserveRoleInternalSystem() {
        assertThat(userManagementPage.roleInternalSystem.isVisible(), is(false));
    }

    @Step
    public void assertButtonCancelPeerRequest() {
        assertThat(peerRegistrationPage.buttonCancelPeerRequest.isVisible(), is(true));
    }

    @Step
    public void assertButtonUnregister() {
        assertThat(peerRegistrationPage.buttonUnregister.isVisible(), is(true));
    }

    @Step
    public void userShouldObservePNGs() throws FindFailed {
        environmentsPage.waitTemplateCassandra();
        screen.find(environmentsPage.sikuliTemplateMaster);
        screen.find(environmentsPage.sikuliPeerRH1);
    }

    @Step
    public void userShouldObserveButtonUploadFile() {
        assertThat(kurjunPage.buttonUploadFile.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonRefresh() {
        assertThat(kurjunPage.buttonRefresh.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonAddUrl() {
        assertThat(kurjunSettingsPage.buttonAddUrl.isVisible(), is(true));
    }

    @Step
    public void userShouldObservePluginCassandra() {
        assertThat(environmentsPage.templateCassandra.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveProfileName() {
        assertThat(pluginsPage.titleOfProfileName.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveButtonConsole() {
        assertThat(pluginsPage.buttonConsole.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveThreeContainers() {
        assertThat(containersPage.containersThree.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveTwoContainers() {
        assertThat(containersPage.containersTwo.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveIManagement() {
        assertThat(roleManagementPage.roleIManagement.isVisible(), is(true));
    }

    @Step
    public void userShouldObserveHeaderApache() {
        assertThat(environmentsPage.headerApache.isVisible(), is(true));
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

    @Step
    public void seeOutputOfLsCommand() {
        assertThat(pluginsPage.outputOfLsCommand.isVisible(), Matchers.is(true));
    }


    //endregion

    @Step
    public void inputConfirmPasswordE2E() {
        screen.type(pgpPlugin.sikuliFieldConfirmPasswordPopup, "239668a");
    }

    @Step
    public void clickOnButtonOkE2EKeyPopup() throws FindFailed {
        screen.click(pgpPlugin.sikuliButtonOkE2EPopup);
    }

    @Step
    public void run_bash_script(String file) throws FileNotFoundException {
        ExecuteShellCommand executeShellCommand = new ExecuteShellCommand();
        System.out.println(executeShellCommand.executeCommand(file));
    }
    @Step
    public void clickOnButtonRegister() throws FindFailed {
        screen.click(commonPage.sikuliButtonRegister);
    }

    @Step
    public void assertButtonGoToHUB(){
        commonPage.waitFor(commonPage.buttonGoToHUB);
        assertThat(commonPage.buttonGoToHUB.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonGoToHUBGreen() throws FindFailed {
        screen.click(commonPage.sikuliButtonGoToHUBGreen);
    }

    @Step
    public void assertMessageHeartbeatSentSuccessfully(){
        commonPage.waitFor(commonPage.textEnvironmentHasBeenBuiltSuccessfully);
        assertThat(commonPage.textEnvironmentHasBeenBuiltSuccessfully.isVisible(), is(true));
    }

    @Step
    public void clickButtonClose() throws FindFailed {
        screen.click(commonPage.sikuliButtonClose);
    }

    @Step
    public void clickOnButtonPeerRegistrationOnline() throws FindFailed {
        screen.click(commonPage.sikuliButtonPeerRegistrationOnline);
    }

    @Step
    public void assertButtonSendHeartbeat() {
        commonPage.waitFor(commonPage.buttonSendHeartbeat);
        assertThat(commonPage.buttonSendHeartbeat.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonSendHeartbeat() throws FindFailed {
        screen.click(commonPage.sikuliButtonSendHearbeat);
    }

    @Step
    public void clickOnButtonOkHeartbeat() throws FindFailed {
        screen.click(commonPage.sikuliButtonOk);
    }

    @Step
    public void clickOnButtonGoToHUBWhite() throws FindFailed {
        screen.click(commonPage.sikuliButtonGoToHUBWhite);
    }

    @Step
    public void clickOnButtonAddRole() throws FindFailed {
        screen.click(roleManagementPage.sikuliButtonAddRole);
    }
}