package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class LoginPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(id = "subt-input__login")
    public WebElementFacade inputLogin;

    @FindBy(id = "subt-input__password")
    public WebElementFacade inputPassword;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(id = "subt-button__login")
    public WebElementFacade buttonLogin;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

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
