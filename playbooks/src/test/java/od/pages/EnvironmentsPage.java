package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

import java.awt.*;
import java.io.File;

public class EnvironmentsPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(id = "env-name")
    public WebElementFacade inputEnvironmentName;

    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(id = "subt-button__create-blueprint-first")
    public WebElementFacade buttonCreateEnvironment;

    @FindBy(xpath = "*//button[contains(text(), \"Apply\")]")
    public WebElementFacade buttonApply;

    @FindBy(xpath = "*//button[contains(text(), \"Build\")]")
    public WebElementFacade buttonBuild;

    @FindBy(xpath = "*//button[contains(text(), \"Delete\")]")
    public WebElementFacade buttonDelete;

    @FindBy(xpath = "*//button[contains(text(),\"Close\")]")
    public WebElementFacade buttonClose;

    public String sikuliButtonApply = "src/test/resources/imgs/buttons/buttonApply.png";
    public String sikuliButtonBuild = "src/test/resources/imgs/buttons/buttonBuild.png";
    public String sikuliButtonClosePopupBuild = "src/test/resources/imgs/buttons/buttonClosePopupBuild.png";
    public String sikuliButtonDelete = "src/test/resources/imgs/buttons/buttonDelete.png";
    public String sikuliButtonOk = "src/test/resources/imgs/buttons/buttonOk.png";



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

    //region SIKULI IMAGES: Icons

    public String sikuliIconDeleteEnvironment = "src/test/resources/imgs/icons/iconDeleteEnvironment.png";

    @FindBy(xpath = "*//span[contains(text(), \"mongo\")]")
    public WebElementFacade iconTemplateMongo;

    @FindBy(xpath = "//a[@class=\"b-icon b-icon_remove\"]")
    public WebElementFacade iconDeleteEnvironment;

    //endregion

    //region SIKULI IMAGES: Templates

    public String sikuliTemplateMongo = "src/test/resources/imgs/templates/templateMongo.png";

    public String sikuliTemplateMySite = "src/test/resources/imgs/templates/templateMySite.png";

    //endregion

    //region WEB ELEMENTS: Headers

    @FindBy(xpath = "*//h1[contains(text(),\"Environment Manager\")]")
    public WebElementFacade headerEnvironments;

    //endregion
}