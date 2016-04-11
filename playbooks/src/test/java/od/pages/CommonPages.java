package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class CommonPages extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//input[@type=\"search\"]")
    public WebElementFacade fieldSearch;

    @FindBy(xpath = "*//div[contains(text(), \"Login\")]/..//input")
    public WebElementFacade fieldLogin;

    @FindBy(xpath = "*//div[contains(text(), \"Password\")]/..//input")
    public WebElementFacade fieldPassword;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//button[@class=\"b-btn b-btn_green g-right\"]")
    public WebElementFacade buttonRegister;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green b-btn_search-field-level\"]")
    public WebElementFacade buttonGreen;

    @FindBy(xpath = "*//a[contains(text(), \"Go to HUB\")]")
    public WebElementFacade buttonGoToHUB;

    @FindBy(xpath = "*//button[contains(text(), \"Send Heartbeat\")]")
    public WebElementFacade buttonSendHeartbeat;

    //endregion
    //region SIKULI IMAGES: Buttons

    public String sikuliButtonLogin = "src/test/resources/imgs/buttons/buttonLogin.png";
    public String sikuliButtonGoToHUBGreen = "src/test/resources/imgs/buttons/buttonGoToHub.png";
    public String sikuliButtonSendHearbeat = "src/test/resources/imgs/buttons/buttonSendHeartbeat.png";

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Menu Links

    @FindBy(id = "subt-link__monitoring")
    public WebElementFacade linkMonitoring;

    @FindBy(id = "subt-link__environment")
    public WebElementFacade linkEnvironment;

    @FindBy(id = "subt-link__blueprint")
    public WebElementFacade linkBlueprint;

    @FindBy(id = "subt-link__environments")
    public WebElementFacade linkEnvironments;

    @FindBy(id = "subt-link__containers")
    public WebElementFacade linkContainers;

    @FindBy(id = "subt-link__console")
    public WebElementFacade linkConsole;

    @FindBy(id = "subt-link__user-identity")
    public WebElementFacade linkUserIdentity;

    @FindBy(id = "subt-link__user-management")
    public WebElementFacade linkUserManagement;

    @FindBy(id = "subt-link__role-management")
    public WebElementFacade linkRoleManagement;

    @FindBy(id = "subt-link__tokens")
    public WebElementFacade linkTokens;

    @FindBy(id = "subt-link__peer-registration")
    public WebElementFacade linkPeerRegistration;

    @FindBy(id = "subt-link__resource-node")
    public WebElementFacade linkResourceNode;

    @FindBy(id = "subt-link__treaker")
    public WebElementFacade linkTracker;

    @FindBy(id = "subt-link__plugisns")
    public WebElementFacade linkPlugins;

    @FindBy(id="subt-link__plugin_integrator")
    public WebElementFacade linkPluginIntegrator;

    @FindBy(id = "subt-link__about")
    public WebElementFacade linkAbout;

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//a[@class=\"b-form-label\"]")
    public WebElementFacade linkSignUp;

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//a[@class=\"show-more\"]")
    public WebElementFacade linkClear;

    @FindBy(xpath = "*//a[@class=\"b-header-userbar-name ng-binding\"]")
    public WebElementFacade linkAdmin;

    //endregion
    //region SIKULI IMAGES: Menu Links

    public String sikuliUpperMenuItemRegisterPeer = "src/test/resources/imgs/menuItems/upperMenuItemRegisterPeer.png";

    public String sikuliMenuItemMonitoring = "src/test/resources/imgs/menuItems/menuItemMonitoring.png";
    public String sikuliMenuItemEnvironment = "src/test/resources/imgs/menuItems/menuItemEnvironment.png";
    public String sikuliMenuItemEnvironments = "src/test/resources/imgs/menuItems/menuItemEnvironments.png";
    public String sikuliMenuItemContainers = "src/test/resources/imgs/menuItems/menuItemContainers.png";
    public String sikuliMenuItemKurjun = "src/test/resources/imgs/menuItems/menuItemKurjun.png";
    public String sikuliMenuItemConsole = "src/test/resources/imgs/menuItems/menuItemConsole.png";
    public String sikuliMenuItemUserIdentity = "src/test/resources/imgs/menuItems/menuItemUserIdentity.png";
    public String sikuliMenuItemUserManagement = "src/test/resources/imgs/menuItems/menuItemUserManagement.png";
    public String sikuliMenuItemRoleManagement = "src/test/resources/imgs/menuItems/menuItemRoleManagement.png";
    public String sikuliMenuItemAccountSettings = "src/test/resources/imgs/menuItems/menuItemAccountSettings.png";
    public String sikuliMenuItemTokens = "src/test/resources/imgs/menuItems/menuItemTokens.png";
    public String sikuliMenuItemPeerRegistration = "src/test/resources/imgs/menuItems/menuItemPeerRegistration.png";
    public String sikuliMenuItemResourceHosts = "src/test/resources/imgs/menuItems/menuItemResourceHosts.png";
    public String sikuliMenuItemTracker = "src/test/resources/imgs/menuItems/menuItemTracker.png";
    public String sikuliMenuItemBazaar = "src/test/resources/imgs/menuItems/menuItemBazaar.png";
    public String sikuliMenuItemSystemSettings = "src/test/resources/imgs/menuItems/menuItemSystemSettings.png";
    public String sikuliMenuItemPeerSettings = "src/test/resources/imgs/menuItems/menuItemPeerSettings.png";
    public String sikuliMenuItemKurjunSettings = "src/test/resources/imgs/menuItems/menuItemKurjunSettings.png";
    public String sikuliMenuItemNetworkSettings = "src/test/resources/imgs/menuItems/menuItemNetworkSettings.png";
    public String sikuliMenuItemAdvanced = "src/test/resources/imgs/menuItems/menuItemAdvanced.png";
    public String sikuliMenuItemAbout = "src/test/resources/imgs/menuItems/menuItemAbout.png";
    public String sikuliButtonRegister = "src/test/resources/imgs/buttons/buttonRegister.png";
    public String sikuliButtonClose = "src/test/resources/imgs/buttons/buttonClosePopupHUBMessage.png";
    public String sikuliButtonPeerRegistrationOnline = "src/test/resources/imgs/buttons/buttonRegisterPeerOnline.png";
    public String sikuliButtonOk = "src/test/resources/imgs/buttons/buttonOk.png";
    public String sikuliButtonGoToHUBWhite = "src/test/resources/imgs/buttons/buttonGoToHubWhite.png";
    public String sikuliButtonOpen = "src/test/resources/imgs/buttons/buttonOpen.png";
    public String sikuliButtonAdd = "src/test/resources/imgs/buttons/buttonAdd.png";

//    public String sikuliTest = returnAbsoluteFilePath.GetPath("src/test/resources/imgs/menuItems/menuItemEnvironment.png");

    //endregion

    //region WEB ELEMENTS: Tables

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//div[@class=\"body\"]")
    public WebElementFacade upperMenuLoginBody;

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//li[@class=\"ng-scope\"]")
    public WebElementFacade upperMenuNotificationsBody;

    //endregion

    //region WEB ELEMENTS: Pickers

    //endregion

    //region WEB ELEMENTS: Selectors

    //endregion

    //region WEB ELEMENTS: Images

    //endregion

    //region WEB ELEMENTS: Icons

    public String sikuliIconNotifications = "src/test/resources/imgs/icons/iconNotification.png";

    //endregion

    //region WEB ELEMENTS: Headers

    //endregion

    //region WEB ELEMENTS: Text

    @FindBy(xpath = "*//p[contains(text(), \"Your environment has been built successfully.\")]")
    public WebElementFacade textEnvironmentHasBeenBuiltSuccessfully;

    @FindBy(xpath = "*//p[contains(text(), \"Your environment has been destroyed.\")]")
    public WebElementFacade textEnvironmentHasBeenDestroyed;

    @FindBy(xpath = "*//div[@class=\"b-hub-status__dropdown b-hub-status__dropdown_open\"]//div[contains(text(),\"Register Peer\")]")
    public WebElementFacade titleUpperMenuRegisterPeer;

    @FindBy(xpath = "*//p[contains(text(), \"Heartbeat sent successfully.\")]")
    public WebElementFacade textHeartbeatSentSuccessfully;

    //endregion
}