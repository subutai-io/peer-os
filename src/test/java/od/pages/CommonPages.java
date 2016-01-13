package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class CommonPages extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(xpath = "*//input[@type=\"search\"]")
    public WebElementFacade fieldSearch;

    //endregion

    //region WEB ELEMENTS: Buttons

    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Menu Links

    @FindBy(id = "subt-link__monitoring")
    public WebElementFacade linkMonitoring;

    @FindBy(id = "subt-link__environment")
    public WebElementFacade linkEnvironment;

    @FindBy(id = "subt-link__blueprint")
    public WebElementFacade linkBlueprint;

    @FindBy(id = "subt-link__environments")
    public WebElementFacade linkEnvironments;

    @FindBy(id = "subt-link__containers")
    public WebElementFacade linkContainers;

    @FindBy(id = "subt-link__console")
    public WebElementFacade linkConsole;

    @FindBy(id = "subt-link__user-identity")
    public WebElementFacade linkUserIdentity;

    @FindBy(id = "subt-link__user-management")
    public WebElementFacade linkUserManagement;

    @FindBy(id = "subt-link__role-management")
    public WebElementFacade linkRoleManagement;

    @FindBy(id = "subt-link__tokens")
    public WebElementFacade linkTokens;

    @FindBy(id = "subt-link__peer-registration")
    public WebElementFacade linkPeerRegistration;

    @FindBy(id = "subt-link__resource-node")
    public WebElementFacade linkResourceNode;

    @FindBy(id = "subt-link__treaker")
    public WebElementFacade linkTracker;

    @FindBy(id = "subt-link__plugisns")
    public WebElementFacade linkPlugins;

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