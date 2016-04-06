package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class PluginsPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//input[@placeholder=\"Enter cluster name\"]")
    public WebElementFacade fieldClusterName;

    //endregion

    //region WEB ELEMENTS: Buttons

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
}