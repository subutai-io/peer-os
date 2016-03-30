package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class ConsolePage extends PageObject {

    //region WEB ELEMENTS: Fields

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

    @FindBy(xpath = "*//select[@ng-model=\"consoleViewCtrl.selectedEnvironment\"]")
    public WebElementFacade selectorEnvironment;

    @FindBy(xpath = "*//div[@class=\"b-console-selects__item b-main-form__wrapper\"]")
    public WebElementFacade itemSelectorHost;

    @FindBy(xpath = "*//option[@ ng-repeat=\"host in consoleViewCtrl.hosts\"]")
    public WebElementFacade selectorHostsItemManagementHost;

    @FindBy(xpath = "html/body/ng-include/div[1]/div[2]/div[3]/div/div[2]/div[1]/div[1]/div[3]/select/option[3]")
    public WebElementFacade selectorHostsItemRecourceHost;


    //endregion

    //region WEB ELEMENTS: Images

    //endregion

    //region WEB ELEMENTS: Icons

    public String sikuliIconSelectorHost = "src/test/resources/imgs/icons/iconSelectorHost.png";

    //endregion

    //region WEB ELEMENTS: Headers

    //endregion

    //region WEB ELEMENTS: Shell output

    @FindBy(xpath = "*//pre[contains(text(),\"/\")]")
    public WebElementFacade outputOfPwdCommand;

    //endregion
}