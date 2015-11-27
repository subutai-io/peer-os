package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.pages.PageObject;

import java.util.Arrays;

public class SubutaiPage extends PageObject {

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

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment start deleting!\")]")
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

    @FindBy(ngModel = "selectedEnvironment")
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

    public void waitButtonUnregister(){
        waitFor(buttonUnregister);
    }

    public void waitTextUnregistered(){
        waitFor(textUnregistered);
    }

    public void waitHeaderBlueprintDeleted(){
        waitFor(headerBlueprintDeleted);
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

    public void getAllData(String phrase){
        String text = element("*//div[@class=\"terminal-results\"]//pre[@class=\"terminal-line\" and contains(text(), \"" + phrase + "\")]").getText();
        System.out.println(text);
    }

}