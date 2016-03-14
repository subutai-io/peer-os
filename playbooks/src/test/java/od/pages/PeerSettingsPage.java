package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class PeerSettingsPage extends PageObject {

    //region WEB ELEMENTS: Fields

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green\"]")
    public WebElementFacade buttonSetPeerOwner;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green\"]")
    public WebElementFacade buttonSave;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    //endregion

    //region WEB ELEMENTS: sikuli Title

    public String sikuliTitlePolicy = "src/test/resources/imgs/titles/titlePolicy.png";
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

    @FindBy(xpath = "*//span[contains(text(),\"Peer Settings\")]")
    public WebElementFacade headerPeerSettings;

    //endregion
}