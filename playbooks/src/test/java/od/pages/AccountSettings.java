package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class AccountSettings extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//textarea[@class=\"b-form-input b-form-input_textarea bp-set-pub-key ng-pristine ng-untouched ng-valid\"]")
    public WebElementFacade fieldPGP;

    @FindBy(ngModel = "changePassCtrl.pass.oldPassword")
    public WebElementFacade fieldOldPassword;

    @FindBy(ngModel = "changePassCtrl.pass.newPassword")
    public WebElementFacade fieldNewPassword;

    @FindBy(ngModel = "changePassCtrl.pass.confirmPass")
    public WebElementFacade fieldConfirmPassword;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_blue e2e-plugin-btn\"]")
    public WebElementFacade buttonSetPublicKey;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green\"]")
    public WebElementFacade buttonSave;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    public String sikuliTitleChangePassword = "src/test/resources/imgs/titles/titleChangePassword.png";

    //endregion

    //region WEB ELEMENTS: Pickers

    //endregion

    //region WEB ELEMENTS: Selectors

    //endregion

    //region WEB ELEMENTS: Images

    //endregion

    //region WEB ELEMENTS: Icons

    //endregion

    //region WEB ELEMENTS: Headers

    //endregion
}