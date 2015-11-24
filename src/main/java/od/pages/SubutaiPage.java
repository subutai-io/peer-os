package od.pages;

import ch.lambdaj.function.convert.Converter;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static ch.lambdaj.Lambda.convert;

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

    @FindBy(xpath = "*//td[@class=\"b-main-table__controls\"]//a[@class=\"b-icon b-icon_build\"]")
    public WebElementFacade iconBuild;

    @FindBy(xpath = "*//td[@class=\"ng-binding\"]")
    public WebElementFacade createdBlueprint;

    @FindBy(id = "subt_link__environment-build-list")
    public WebElementFacade linkEnvironmentBuildList;

    @FindBy(ngModel = "nodeGroup.peer")
    public WebElementFacade selectPeer;

    @FindBy(ngModel = "nodeGroup.strategyId")
    public WebElementFacade selectStrategie;

    @FindBy(xpath = "*//span[@class=\"b-icon__inner-num ng-binding\" and contains(text(), \"2\")]")
    public WebElementFacade iconTwoContainers;

    @FindBy(xpath = "*//div[@class=\"b-popup\"]")
    public WebElementFacade popup;

    @FindBy(id = "subt_button__environment-build")
    public WebElementFacade buttonEnvironmentBuild;

    @FindBy(xpath = "*//h2[contains(text(), \"Success!\")]")
    public WebElementFacade header2Success;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//button[contains(text(), \"OK\")]")
    public WebElementFacade buttonOK;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment start creation.\")]")
    public WebElementFacade textYourEnvironmentStartCreation;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment has been created.\")]")
    public WebElementFacade textYourEnvironmentHasBeenCreated;

    @FindBy(xpath = "*//td[@class=\"b-main-table__controls\"]//a[@class=\"b-icon b-icon_grow\"]")
    public WebElementFacade iconGrow;

    @FindBy(ngModel = "blueprintsBuildCtrl.environmentToGrow")
    public WebElementFacade selectEnvironment;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"Your environment start growing.\")]")
    public WebElementFacade textYourEnvironmentStartGrowing;

    @FindBy(xpath = "*//div[@class=\"sweet-alert showSweetAlert visible\"]//p[contains(text(), \"You successfully grow environment.\")]")
    public WebElementFacade textYouSuccessfullyGrowEnvironment;

    //---------------------------------------------------------------------

    public void waitHeader2Success(){
        waitFor(header2Success);
    }

    public void waitTextYourEnvironmentHasBeenCreated() {
        waitFor(textYourEnvironmentHasBeenCreated);
    }

    public void waitTextYouSuccessfullyGrowEnvironment() {
        waitFor(textYouSuccessfullyGrowEnvironment);
    }
}