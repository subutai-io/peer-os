package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import sun.security.pkcs11.P11Util;

public class BazaarPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(id = "js-search")
    public WebElementFacade fieldSearch;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(xpath = "*//button[@class=\"b-btn b-btn_green g-margin-bottom ng-scope\"]")
    public WebElementFacade buttonUploadNewPlugin;

    @FindBy(xpath = "//a[@class=\"loading-button\"]")
    public WebElementFacade buttonLaunch;

    public String sikuliMenuButtonInstall = "src/test/resources/imgs/buttons/buttonInstallPlugin.png";
    public String sikuliButtonLaunch = "src/test/resources/imgs/buttons/buttonLaunch.png";

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

    //region WEB ELEMENTS: Wait For

    public void waitButtonLaunch(){
        waitFor(buttonLaunch);
    }


    //endregion
}