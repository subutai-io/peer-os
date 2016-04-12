package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class UserManagementPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//input[@class=\"b-popup-body-input b-popup-body-input_full b-popup-body-input_margin-bttm ng-pristine ng-untouched ng-invalid ng-invalid-required ng-valid-pattern\"]")
    public WebElementFacade fieldUsername;

    @FindBy(xpath = "*//input[@class=\"b-popup-body-input b-popup-body-input_full b-popup-body-input_margin-bttm ng-pristine ng-untouched ng-invalid ng-invalid-required\"]")
    public WebElementFacade fieldFullName;

    @FindBy(xpath = "*//input[@type=\"email\"]")
    public WebElementFacade fieldEmail;

    @FindBy(id = "js-password")
    public WebElementFacade fieldPassword;

    @FindBy(xpath = "*//input[@pw-check=\"js-password\"]")
    public WebElementFacade fieldConfirmPassword;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_green b-btn_search-field-level\"]")
    public WebElementFacade buttonAddUser;

    public String sikuliButtonAddUser = "src/test/resources/imgs/buttons/buttonAddUser.png";
    public String sikuliButtonSaveUser = "src/test/resources/imgs/buttons/buttonSaveUser.png";
    public String sikuliButtonRemove = "src/test/resources/imgs/buttons/buttonRemove.png";
    public String sikuliButtonOk = "src/test/resources/imgs/buttons/buttonOk.png";
    public String sikuliButtonChooseRoles = "src/test/resources/imgs/buttons/buttonRemoveRoles.png";

    @FindBy(xpath = "*//tr[@class=\"even b-midletrusted-user\"]//a[@class=\"b-icon b-icon_remove\"]")
    public WebElementFacade buttonRemoveUser;

    @FindBy(xpath = "*//i[@class=\"fa fa-angle-double-down\"]")
    public WebElementFacade buttonChooseAll;

    @FindBy(xpath = "*//a[@ng-click=\"identityUserFormCtrl.unselectAll()\"]")
    public WebElementFacade buttonRejectAll;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    @FindBy(xpath = "*//td[contains(text(),\"test@test.com\")]")
    public WebElementFacade usersEmail;

    //endregion

    //region WEB ELEMENTS: Pickers

    @FindBy(xpath = "*//option[contains(text(),\"Ultimate Trust\")]")
    public WebElementFacade pickerUltimateTrust;

    @FindBy(xpath = "*//tr[@class=\"even b-midletrusted-user\"]//span[contains(text(),\"Internal-System\")]//i[@class=\"fa fa-times\"]")
    public WebElementFacade roleInternalSystem;

    //endregion

    //region WEB ELEMENTS: Selectors

    @FindBy(xpath = "*//select[@class=\"b-popup-body-input b-popup-body-input_full b-popup-body-input_margin-bttm ng-pristine ng-untouched ng-valid\"]")
    public WebElementFacade selectorTrustedLevel;

    @FindBy(xpath = "*//li[contains(text(),\"iManagement\")]")
    public WebElementFacade selectorIManagement;

    //endregion

    //region WEB ELEMENTS: Images

    //endregion

    //region WEB ELEMENTS: Icons

    //endregion

    //region WEB ELEMENTS: Headers

    @FindBy(xpath = "*//h1[contains(text(),\"User management\")]")
    public WebElementFacade headerUserManagement;

    public String sikuliButtonSetPublicKey = "src/test/resources/imgs/buttons/buttonSetPublicKey.png";

    //endregion
}