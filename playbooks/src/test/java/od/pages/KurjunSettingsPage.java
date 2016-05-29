package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class KurjunSettingsPage extends PageObject {

    //region WEB ELEMENTS: Fields

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@ng-click=\"settingsKurjunCtrl.updateConfigUrls()\"]")
    public WebElementFacade buttonSave;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green\" and contains(text(),\"Add\")]")
    public WebElementFacade buttonAdd;

    @FindBy(xpath = "*//button[@ng-click=\"settingsKurjunCtrl.updateConfigQuotas()\"]")
    public WebElementFacade buttonSaveQuotas;

    @FindBy(id = "subt_link__create-peer")
    public WebElementFacade buttonAddUrl;
    //endregion

    //region WEB ELEMENTS: sikuli Title

    public String sikuliTitleQuotas = "src/test/resources/imgs/titles/titleQuotas.png";
    public String sikuliTitleUrlsList = "src/test/resources/imgs/titles/titleUrlsList.png";
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