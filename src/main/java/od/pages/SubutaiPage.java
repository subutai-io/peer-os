package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.pages.PageObject;

import java.io.File;
import java.util.Arrays;

public class SubutaiPage extends PageObject {
    public String cip;
    public String webUi1;
    public String webUi2;
    public String webUi3;

    //----- GENERAL PLAYBOOKS

    @FindBy(id = "subt-input__login")
    public WebElementFacade inputLogin;

    @FindBy(id = "subt-input__password")
    public WebElementFacade inputPassword;

    @FindBy(id = "subt-button__login")
    public WebElementFacade buttonLogin;

    @FindBy(id = "subt-button__create-blueprint-second")
    public WebElementFacade buttonCreateBlueprintSecond;

    @FindBy(id = "subt-input__blueprint-name")
    public WebElementFacade inputBlueprintName;

    @FindBy(id = "subt-input__blueprint-node-name")
    public WebElementFacade inputBlueprintNodeName;

    @FindBy(id = "subt-select__blueprint-template")
    public WebElementFacade selectBlueprintTemplate;

    @FindBy(id = "subt-input__blueprint-number-of-containers")
    public WebElementFacade inputBlueprintNumberOfContainers;

    @FindBy(id = "subt-input__blueprint-ssh-group-id")
    public WebElementFacade inputBlueprintSSHGroupID;

    @FindBy(id = "subt-input__blueprint-host-group-id")
    public WebElementFacade inputBlueprintHostGroupID;

    @FindBy(id = "subt-select__blueprint-quota-size")
    public WebElementFacade selectBlueprintQuotaSize;

    @FindBy(id = "subt_button__blueprint-create")
    public WebElementFacade buttonCreateBlueprint;

    @FindBy(id = "subt_input__environment-name")
    public WebElementFacade inputEnvironmentName;

    @FindBy(xpath = "*//i[@class=\"b-icon b-icon_box-white b-icon_box-white_place\"]/..")
    public WebElementFacade buttonPlace;

    @FindBy(id = "subt-link__environment")
    public WebElementFacade linkEnvironment;

    @FindBy(id = "subt-link__blueprint")
    public WebElementFacade linkBlueprint;

    @FindBy(id = "subt-link__environments")
    public WebElementFacade linkEnvironments;

    @FindBy(id = "subt-link__containers")
    public WebElementFacade linkContainers;

    @FindBy(id = "subt_button__blueprint-add-to-node-list")
    public WebElementFacade buttonBlueprintAddToNodeList;

    @FindBy(className = "b-popup-box-node-list subt_button__blueprint-node-list-item ng-binding ng-scope")
    public WebElementFacade nodeListItem;

    @FindBy(xpath = "*//td[@class=\"b-main-table__controls\"]//a[@tooltip-title=\"Build\"]")
    public WebElementFacade iconBuild;

    @FindBy(xpath = "*//td[@class=\"ng-binding\"]")
    public WebElementFacade createdBlueprint;

    @FindBy(id = "subt_link__environment-build-list")
    public WebElementFacade linkEnvironmentBuildList;

    @FindBy(ngModel = "nodeGroup.peer")
    public WebElementFacade selectPeer;

    @FindBy(ngModel = "nodeGroup.createOption")
    public WebElementFacade selectStrategie;

    @FindBy(xpath = "*//span[@class=\"b-icon__inner-num ng-binding\" and contains(text(), \"2\")]")
    public WebElementFacade iconTwoContainers;

    @FindBy(xpath = "*//div[@class=\"b-popup\"]")
    public WebElementFacade popup;

    @FindBy(id = "subt_button__environment-build")
    public WebElementFacade buttonEnvironmentBuild;

    @FindBy(xpath = "*//h2[contains(text(), \"Success!\")]")
    public WebElementFacade headerSuccess;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//button[contains(text(), \"OK\")]")
    public WebElementFacade buttonOK;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment start creation.\")]")
    public WebElementFacade textYourEnvironmentStartCreation;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment has been created.\")]")
    public WebElementFacade textYourEnvironmentHasBeenCreated;

    @FindBy(xpath = "*//td[@class=\"b-main-table__controls\"]//a[@tooltip-title=\"Grow\"]")
    public WebElementFacade iconGrow;

    @FindBy(xpath = "*//td[@class=\"b-main-table__controls\"]//a[@class=\"b-icon b-icon_remove\"]")
    public WebElementFacade iconRemove;

    @FindBy(ngModel = "blueprintsBuildCtrl.environmentToGrow")
    public WebElementFacade selectEnvironment;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment start growing.\")]")
    public WebElementFacade textYourEnvironmentStartGrowing;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"You successfully grow environment.\")]")
    public WebElementFacade textYouSuccessfullyGrowEnvironment;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//h2[contains(text(), \"Are you sure?\")]")
    public WebElementFacade popupAreYouSure;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//button[@class=\"confirm\"]")
    public WebElementFacade buttonDeleteConfirm;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//h2[contains(text(), \"Deleted\")]")
    public WebElementFacade headerBlueprintDeleted;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment is being deleted!\")]")
    public WebElementFacade textYourEnvironmentStartDeleting;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment has been destroyed.\")]")
    public WebElementFacade textYourEnvironmentHasBeenDestroyed;

    @FindBy(xpath = "*//a[@class=\"b-icon b-icon_remove\"]")
    public WebElementFacade iconDestroy;

    @FindBy(id = "subt-link__peer-registration")
    public WebElementFacade linkPeerRegistration;

    @FindBy(id = "subt_link__create-peer")
    public WebElementFacade linkCreatePeer;

    @FindBy(id = "subt_input__peer-ip")
    public WebElementFacade inputPeerIP;

    @FindBy(id = "subt_input__peer-key-phrase")
    public WebElementFacade inputPeerKeyPhrase;

    @FindBy(id = "subt_button__create-peer")
    public WebElementFacade buttonCreatePeer;

    @FindBy(xpath = "*//td[@class=\"ng-scope\"]")
    public WebElementFacade textPeer1Ip;

    @FindBy(xpath = "*//td[@class=\"ng-scope\"]")
    public WebElementFacade textPeer2Ip;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_blue subt_button__peer-cancel\" and contains(text(), \"Cancel\")]")
    public WebElementFacade buttonCancel;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_green subt_button__peer-approve\" and contains(text(), \"Approve\")]")
    public WebElementFacade buttonApprove;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_red subt_button__peer-reject\" and contains(text(), \"Reject\")]")
    public WebElementFacade buttonReject;

    @FindBy(id = "subt_input__peer-approve-keyphrase")
    public WebElementFacade inputPeerApproveKeyPhrase;

    @FindBy(id = "subt_button__peer-approve-popup")
    public WebElementFacade buttonPopupApprove;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_red subt_button__peer-unregister\" and contains(text(), \"Unregister\")]")
    public WebElementFacade buttonUnregister;

    @FindBy(xpath = "*//button[@class=\"confirm\" and contains(text(), \"Unregister\")]")
    public WebElementFacade buttonConfirmUnregister;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//h2[contains(text(), \"Unregistered!\")]")
    public WebElementFacade textUnregistered;

    @FindBy(xpath = "*//td[@class=\"dataTables_empty\" and contains(text(), \"No data available in table\")]")
    public WebElementFacade textNoDataAvailableInTable;

    @FindBy(ngModel = "*//span[@class=\"terminal-cursor terminal-cursor-hidden\"]")
    public WebElementFacade inputCommandLine;

    @FindBy(id = "subt-link__console")
    public WebElementFacade linkConsole;

    @FindBy(xpath = "*//select[@class=\"b-popup-body-input ng-pristine ng-valid ng-touched\"]")
    public WebElementFacade selectPeerConsole;

    @FindBy(ngModel = "selectedPeer")
    public WebElementFacade selectMenuResourceHost;

    @FindBy(xpath = "*//li[contains(text(), \"Environment\")]")
    public WebElementFacade buttonEnvironmentOnConsole;

    @FindBy(xpath = "*//li[contains(text(), \"Peer\")]")
    public WebElementFacade buttonPeerOnConsole;

    @FindBy(ngModel = "selectedContainer")
    public WebElementFacade selectContainer;

    @FindBy(ngModel = "consoleViewCtrl.selectedEnvironment")
    public WebElementFacade selectEnvironmentConsole;


    //---------------------------------------------------------------------

    public void waitHeaderSuccess(){
        waitFor(headerSuccess);
    }

    public void waitTextYourEnvironmentHasBeenCreated() {
        waitFor(textYourEnvironmentHasBeenCreated);
    }

    public void waitTextYouSuccessfullyGrowEnvironment() {
        waitFor(textYouSuccessfullyGrowEnvironment);
    }

    public void waitTextYourEnvironmentHasBeenDestroyed(){
        waitFor(textYourEnvironmentHasBeenDestroyed);
    }

    public void waitButtonCreatePeer(){
        waitFor(linkCreatePeer);
    }

    public void waitButtonStartContainer(){
        waitFor(containerButtonStart);
    }

    public void waitButtonStopContainer(){
        waitFor(containerButtonStop);
    }

    public void waitButtonUnregister(){
        waitFor(buttonUnregister);
    }

    public void waitTextUnregistered(){
        waitFor(textUnregistered);
    }

    public void waitHeaderBlueprintDeleted(){
        waitFor(headerBlueprintDeleted);
    }

    public void waitDomainPopUpMenu(){
        waitFor(inputDomain);
    }

    public void executeConsoleCommand(String command){
        evaluateJavascript("function setCommand(value) {\n" +
                "var appElement = document.getElementsByClassName('b-terminal')[0];" +
                "var $scope = angular.element(appElement).scope();" +
                "$scope.$apply(function() {" +
                "$scope.commandLine = value;" +
                "$scope.execute();" +
                "});" +
                "} " +
                "setCommand('"+ command +"')");
        waitABit(10000);
    }

    public boolean getPreData(String expectedPhrase){
        try {
            return element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \""+ expectedPhrase +"\")]").isVisible();
        }
        catch (Exception e){
            return false;
        }
    }

    public String getOutputRow(String expectedPhrase, int rStart, int rEnd, int numberRow){
      String text = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \""+ expectedPhrase +"\")]").getText().trim();
        String sb[] = text.split("\n");
        for(int i=0; i < sb.length; i++)
            if (sb[i].contains(expectedPhrase)) {
                text += sb[i] + "\n";
            }
        sb = text.split("\n");
        return sb[numberRow].substring(rStart, rEnd);
    }

    public String getPIDContainer(String phrase){
        String text [] = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText().split("\n");
        System.out.println(text[1].substring(text[1].length() - 6, text[1].length()).trim());
        return text[1].substring(text[1].length() - 6, text[1].length()).trim();
    }

    public String getWebUiContainer1(String phrase){
        String text [] = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText().split("\n");
        webUi1 = text[0].substring(2, 4).trim();
        System.out.println(webUi1);
        return  webUi1;
    }

    public String getWebUiContainer2(String phrase){
        String text [] = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText().split("\n");
        webUi2 = text[1].substring(0, 2).trim();
        System.out.println(webUi2);
        return  webUi2;
    }

    public String getWebUiContainer3(String phrase){
        String text [] = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText().split("\n");
        webUi3 = text[0].substring(2, 5).trim();
        System.out.println(webUi3);
        return  webUi3;
    }

    public void getAllData(String phrase){
        String text = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText();
        System.out.println(text);
    }

    //------3023

    @FindBy(id = "subt-link__console")
    public WebElementFacade subutaiLinkConsole;

    @FindBy(xpath = "*//section[@class=\"terminal modern-terminal\"]")
    public WebElementFacade subuaiConsole;

    @FindBy(ngModel = "commandLine")
    public WebElementFacade inputConsole;

    @FindBy(xpath = "*//option[contains(text(),\"rh\")]")
    public WebElementFacade selectMenuResourceHostsOptionRH;

    @FindBy(className = "terminal-input ng-binding")
    public WebElementFacade consoleInput;

    @FindBy(xpath = "*//pre[contains(text(),\"test\")]")
    public WebElementFacade outputOfTestCommand;

    @FindBy(xpath = "*//pre[contains(text(),\"3 received\")]")
    public WebElementFacade outputOfTestCommandThreeReceived;

    @FindBy(xpath = "*//h1[contains(text(),\"Blueprint\")]")
    public WebElementFacade subutaiBlueprintHeader;

    @FindBy(xpath = "*//span[contains(text(),\":>\")]")
    public WebElementFacade endOfConsoleLine;

    @FindBy(xpath = "*//li[contains(text(),\"Environment\")]")
    public WebElementFacade pageConsoleLinkEnvironment;

    @FindBy(ngModel = "consoleViewCtrl.selectedEnvironment")
    public WebElementFacade environmentSelectMenu;

    @FindBy(ngModel = "selectedContainer")
    public WebElementFacade selectMenuContainer;

    @FindBy(xpath = "*//span[@class=\"terminal-prompt ng-binding\"]")
    public WebElementFacade terminalPromptBinding;

    @FindBy(xpath = "xhtml:html/xhtml:body/xhtml:pre[contains(text(),\"-----BEGIN PGP PUBLIC KEY BLOCK-----\")]")
    public WebElementFacade pgpHeader;

    @FindBy(xpath = "*//pre[contains(text(),\"/\")]")
    public WebElementFacade outputOfPwdCommand;

    @FindBy(xpath = "*//pre[contains(text(),\"3 received\")]")
    public WebElementFacade outputOfPingCommand;

    @FindBy(xpath = "*//pre[contains(text(),\"0 received\")]")
    public WebElementFacade outputOfWrongPingCommand;

    @FindBy(id = "subt-link__user-identity")
    public WebElementFacade linkUserIdentity;

    @FindBy(id = "subt-link__tokens")
    public WebElementFacade linkTokens;

    @FindBy(xpath = "*//tr[2]/td[8]/a")
    public WebElementFacade buttonShowToken;

    @FindBy(xpath = "*//div[contains(text(),\"Token\")]")
    public WebElementFacade popupMenuTokenTextHeader;

    @FindBy(xpath = "*//div[@class=\"b-logs-view b-logs-view_token ng-binding\"]")
    public WebElementFacade token;

    @FindBy(xpath = "xhtml:html/xhtml:body/xhtml:pre")
    public WebElementFacade peerID;

    @FindBy(xpath = "*//*[contains(text(),\"BEGIN PGP PUBLIC KEY BLOCK\")]")
    public WebElementFacade remotePGPKey;

    @FindBy(xpath = "xhtml:html/xhtml:body/xhtml:pre")
    public WebElementFacade environmentData;

    @FindBy(xpath = "*//*[contains(text(),\"BEGIN PGP PUBLIC KEY BLOCK\")]")
    public WebElementFacade environmentPGPKey;

    @FindBy(xpath = "*//tr[@class=\"ng-scope odd\"]//td[@class=\"subt_text__container-ip sorting_1\"]//span[@class=\"ng-binding ng-isolate-scope\"]")
    public WebElementFacade containerIp;

    //--------3117

    @FindBy(xpath = "*//span[@class=\"b-icon__inner-num ng-binding\" and contains(text(), \"3\")]")
    public WebElementFacade iconThreeContainers;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_grey\"]")
    public WebElementFacade buttonConfigure;

    @FindBy(ngModel = "domain.name")
    public WebElementFacade inputDomain;

    public String getDomain(){
        String domain;
        domain = evaluateJavascript("return document.getElementsByTagName('input')[1].value").toString();
        String sb[] = domain.split("\n");
        domain = sb[0];
        return domain;
    }

    @FindBy(ngModel = "domain.strategy")
    public WebElementFacade selectMenuDomainStrategy;

    @FindBy(xpath= "*//input[@id=\"js-uploadBtn\"]/..")
    public WebElementFacade inputUploadSSL;

    public void uploadFromFile(){
        File file = new File("src/test/resources/files/lighttpd.pem");
        String filePath = file.getAbsolutePath();
        evaluateJavascript("document.getElementById('js-uploadFile').value = '" + filePath + "'");
    }

    @FindBy(id = "subt_button__environment-add-domain")
    public WebElementFacade domainButtonSave;

    @FindBy(xpath = "*//b[@class=\"ng-binding\"]")
    public WebElementFacade domainBindingText;

    @FindBy(xpath = "*//option[contains(text(),\"ROUND_ROBIN\")]")
    public WebElementFacade domainOptonRoundRobi;

    @FindBy(xpath = ".//*[@id='DataTables_Table_1']/tbody/tr[1]/td[5]/button")
    public WebElementFacade firstContainerButtonConfigure;

    @FindBy(xpath = ".//*[@id='DataTables_Table_1']/tbody/tr[2]/td[5]/button")
    public WebElementFacade secondContainerButtonConfigure;

    @FindBy(xpath = ".//*[@id='DataTables_Table_1']/tbody/tr[3]/td[5]/button")
    public WebElementFacade thirdContainerButtonConfigure;

    @FindBy(id = "subt_checkbox__container-domain-add")
    public WebElementFacade containerDomainCheckbox;

    @FindBy(id = "subt_button__peer-approve-popup")
    public WebElementFacade checkboxSaveButton;

    @FindBy(xpath = "*//body")
    public WebElementFacade domainContainerIP;


    public boolean compareIP() {
        String gip;
        gip = evaluateJavascript("return document.body.innerHTML").toString();

        getDriver().navigate().refresh();
        getDriver().navigate().refresh();

        String sGip;
        sGip = evaluateJavascript("return document.body.innerHTML").toString();

        boolean result = gip.equals(sGip);
        return result;
    }

    @FindBy(xpath = "*//tr[@class=\"ng-scope odd\"]//button[@class=\"b-btn b-btn_red subt_button__container-stop\"]")
    public WebElementFacade containerButtonStop;

    @FindBy(xpath = ".//*[@class='row-border hover subt_table-containers-table ng-isolate-scope no-footer dataTable']/tbody/tr[1]/td[3]/span")
    public WebElementFacade ipStoppedContainer;

    public String getIPContainerStopped(){
        cip = ("Container IP:" + ipStoppedContainer.getText());

        return cip;
    }

    @FindBy(xpath = "*//tr[@class=\"ng-scope odd\"]//button[@class=\"b-btn b-btn_green subt_button__container-start\"]")
    public WebElementFacade containerButtonStart;

    public void pageReload(){
        getDriver().navigate().refresh();
        getDriver().navigate().refresh();
    }

    public boolean checkForOutOfIP() {
        String checkForOutput;
        checkForOutput = evaluateJavascript("return document.body.innerHTML").toString();
        for (int i=0; i < 5; i++)
        {
            if (!cip.equals(checkForOutput)){
                getDriver().navigate().refresh();
                getDriver().navigate().refresh();
            }
            else return false;
        }
        return true;
    }

    @FindBy(xpath = ".//*[@class='row-border hover subt_table-containers-table ng-isolate-scope no-footer dataTable']/tbody/tr[2]/td[3]/span")
    public WebElementFacade ipDesabledContainer;

    public String getIPContainerDisabled(){
        cip = ("Container IP:" + ipDesabledContainer.getText());

        return cip;
    }

    @FindBy(id = "subt_button__environment-remove-domain")
    public WebElementFacade enviromentButtonRemoveDomain;

    @FindBy(xpath = "*//h1[contains(text(),\"404\")]")
    public WebElementFacade desabledBindToEnvironment;

    @FindBy(xpath = ".//*[@id='errorTitleText']")
    public WebElementFacade unabledToConnect;

    //-----------------3298

    @FindBy(id="displayKeysButton")
    public WebElementFacade dyspleyKeysButton;

    @FindBy(id = "setupProviderButton")
    public WebElementFacade stepButton;

    @FindBy(id = "mhDecrypt")
    public WebElementFacade dekryptButton;

    public void pgpStart(){
        open(new String[]{"resource://jid1-aqqsmbyb0a8adg-at-jetpack/mailvelope/data/common/ui/options.html#displayKeys"});
    }

    @FindBy(xpath = "*//input[@ng-model=\"loginCtrl.name\"]")
    public WebElementFacade ngLogin;

    @FindBy(xpath = "*//input[@ng-model=\"loginCtrl.pass\"]")
    public WebElementFacade ngPassword;

    @FindBy(xpath = "*//div[@class=\"b-empty-list\"]//a[@ng-click=\"bvc.createBlueprintFrom()\"]")
    public WebElementFacade ngCreateBlueprint;

    @FindBy(xpath = "*//a[contains(text(),\"Add\")]")
    public WebElementFacade ngAdd;

    @FindBy(xpath = "*//a[contains(text(),\"Remove\")]")
    public WebElementFacade ngRemove;

    @FindBy(xpath = "*//select[@ng-model=\"containerViewCtrl.environmentId\"]")
    public WebElementFacade ngEnvironmentSelector;

    @FindBy(xpath = "*//select[@ng-model=\"containerViewCtrl.containersTypeId\"]")
    public WebElementFacade ngContainersSelector;

    @FindBy(xpath = "*//li[@ng-click=\"consoleViewCtrl.setCurrentType('peer')\"]")
    public WebElementFacade ngPeer;

    @FindBy(xpath = "*//select[@ng-change=\"consoleViewCtrl.setConsole(selectedPeer)\"]")
    public WebElementFacade ngSelectedPeer;

    @FindBy(id = "subt-link__user-management")
    public WebElementFacade linkUserManagement;

    @FindBy(xpath = "*//div[@class=\"b-workspace__header\"]//a[@ng-click=\"identityUserCtrl.userForm()\"]")
    public WebElementFacade ngAddUser;

    @FindBy(id = "subt-link__role-management")
    public WebElementFacade ngRolesManagment;

    @FindBy(xpath = "*//a[@ng-click=\"identityRoleCtrl.roleForm()\"]")
    public WebElementFacade ngAddRole;

    @FindBy(xpath = "*//button[@ng-click=\"tokensCtrl.addToken()\"]")
    public WebElementFacade ngAddToken;

    @FindBy(xpath = "*//input[@ng-model=\"tokensCtrl.newToken.token\"]")
    public WebElementFacade ngTokenName;

    @FindBy(xpath = "*//a[@ng-click=\"peerRegistrationCtrl.peerFrom()\"]")
    public WebElementFacade ngCreatePeer;

    @FindBy(id = "subt-link__treaker")
    public WebElementFacade linkTracker;

    @FindBy(xpath = "*//select[@ng-model=\"trackerCtrl.selectedModule\"]")
    public WebElementFacade ngSourceSelector;
}