package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class PluginsPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//input[@placeholder=\"Enter cluster name\"]")
    public WebElementFacade fieldClusterName;

    @FindBy(xpath = "*//input[@ng-model=\"genericCtrl.newProfile\"]")
    public WebElementFacade fieldProfile;

    @FindBy(id = "user-domain")
    public WebElementFacade fieldDomainName;

    @FindBy(xpath = "*//input[@ng-model=\"genericCtrl.newOperation.operationName\"]")
    public WebElementFacade fieldOperationName;

    @FindBy(xpath = "*//textarea[@ng-model=\"genericCtrl.newOperation.commandName\"]")
    public WebElementFacade fieldOperation;

    @FindBy(xpath = "*//input[@ng-model=\"value.name\"]")
    public WebElementFacade fieldEnvironmentName;

    @FindBy(xpath = "*//input[@ng-model=\"value.domain\"]")
    public WebElementFacade fieldDomain;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_blue b-btn_margin-r\"]")
    public WebElementFacade buttonConsole;

    public String sikuliButtonCreate = "src/test/resources/imgs/buttons/buttonCreate.png";
    public String sikuliButtonConsole = "src/test/resources/imgs/buttons/buttonConsole.png";
    public String sikuliButtonInstall = "src/test/resources/imgs/buttons/buttonInstall.png";
    public String sikuliButtonInstallPlugin = "src/test/resources/imgs/buttons/buttonInstallPlugin.png";
    public String sikuliButtonQuickInstall = "src/test/resources/imgs/buttons/buttonQuickInstall.png";
    public String sikuliButtonConfigureOperations = "src/test/resources/imgs/buttons/buttonConfigureOperations.png";
    public String sikuliButtonAddOperation = "src/test/resources/imgs/buttons/buttonAddOperation.png";
    public String sikuliButtonExecute = "src/test/resources/imgs/buttons/buttonExecute.png";
    public String sikuliButtonUninstall = "src/test/resources/imgs/buttons/buttonUninstall.png";


    //endregion

    //region WEB ELEMENTS: Titles

    public String sikuliTitleManage = "src/test/resources/imgs/titles/titleManage.png";
    public String sikuliTitleCreate = "src/test/resources/imgs/titles/titleCreate.png";

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    @FindBy(xpath = "*//td[contains(text(),\"test profile\")]")
    public WebElementFacade titleOfProfileName;

    //endregion

    //region WEB ELEMENTS: Pickers

    //endregion

    //region WEB ELEMENTS: Selectors
    public String sikuliSelectorEnvironment = "src/test/resources/imgs/icons/selectorSelectEnvironment.png";

    @FindBy(xpath = "*//option[contains(text(), \"Test Environment Master\")]")
    public WebElementFacade selectorEnvironmentMaster;
    //endregion

    //region WEB ELEMENTS: Images

    @FindBy(xpath = "*//div[@class=\"b-plugins-list-item\"]")
    public WebElementFacade pluginItems;

    //endregion

    //region WEB ELEMENTS: Icons

    //endregion

    //region WEB ELEMENTS: Headers

    @FindBy(xpath = "*//h1[contains(text(),\"Plugins\")]")
    public WebElementFacade headerPlugins;

    //endregion

    //region WEB ELEMENTS: Shell output

    @FindBy(xpath = "*//pre[contains(text(),\"bin\")]")
    public WebElementFacade outputOfLsCommand;

    //endregion
}