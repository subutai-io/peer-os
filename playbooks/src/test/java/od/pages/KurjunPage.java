package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class KurjunPage extends PageObject {

    //region WEB ELEMENTS: Fields

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green b-btn_search-field-level\"]")
    public WebElementFacade buttonUploadFile;

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_blue b-btn_search-field-level\"]")
    public WebElementFacade buttonRefresh;

    //endregion

    //region SIKULI IMAGES: Titles

    public String sikuliTitleAPT = "src/test/resources/imgs/titles/titleAPT.png";
    public String sikuliTitleRawFiles = "src/test/resources/imgs/titles/titleRawFiles.png";

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