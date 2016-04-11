package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class ContainersPage extends PageObject {

    //region WEB ELEMENTS: Fields

    //endregion

    //region WEB ELEMENTS: Buttons

    public String sikuliButtonCheck = "src/test/resources/imgs/buttons/buttonCheck.png";
    public String sikuliButtonStop = "src/test/resources/imgs/buttons/buttonStop.png";
    public String sikuliButtonStart = "src/test/resources/imgs/buttons/buttonStart.png";
    public String sikuliButtonRemove = "src/test/resources/imgs/buttons/buttonDestroyContainer.png";
    public String sikuliButtonDestroy = "src/test/resources/imgs/buttons/buttonDestroy.png";



    //endregion

    //region WEB ELEMENTS: Checkboxes

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    @FindBy(xpath = "*//span[contains(text(),\"3\")]")
    public WebElementFacade containersThree;

    @FindBy(xpath = "*//span[contains(text(),\"2\")]")
    public WebElementFacade containersTwo;

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

    @FindBy(xpath = "*//h1[contains(text(),\"Containers\")]")
    public WebElementFacade headerContainers;

    //endregion
}