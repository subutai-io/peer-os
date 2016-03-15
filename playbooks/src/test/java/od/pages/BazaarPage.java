package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class BazaarPage extends PageObject {

    //region WEB ELEMENTS: Fields

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green g-margin-bottom ng-scope\"]")
    public WebElementFacade buttonUploadNewPlugin;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    public String sikuliMenuTitleInstalled = "src/test/resources/imgs/titles/titleInstalled.png";
    public String sikuliMenuTitleAdvanced = "src/test/resources/imgs/titles/titleAdvanced.png";

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