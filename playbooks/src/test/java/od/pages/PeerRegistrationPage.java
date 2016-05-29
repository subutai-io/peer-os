package od.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class PeerRegistrationPage extends PageObject {

    //region WEB ELEMENTS: Fields

    @FindBy(id="subt_input__peer-ip")
    public WebElementFacade fieldPeerIp;

    @FindBy(id="subt_input__peer-key-phrase")
    public WebElementFacade fieldPeerKeyPhrase;

    @FindBy(id="subt_input__peer-approve-keyphrase")
    public WebElementFacade fieldPeerApprove;
    //endregion

    //region WEB ELEMENTS: Buttons

    @FindBy(id = "subt_link__create-peer")
    public WebElementFacade buttonCreatePeer;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_blue subt_button__peer-cancel\"]")
    public WebElementFacade buttonCancelPeerRequest;

    @FindBy(xpath = "*//a[@class=\"b-btn b-btn_red subt_button__peer-unregister\"]")
    public WebElementFacade buttonUnregister;

    public String sikuliButtonCreatePeer = "src/test/resources/imgs/buttons/buttonCreatePeer.png";
    public String sikuliButtonCreate = "src/test/resources/imgs/buttons/buttonCreate.png";
    public String sikuliButtonApprove = "src/test/resources/imgs/buttons/buttonApprove.png";
    public String sikuliButtonApprovePopUp = "src/test/resources/imgs/buttons/buttonApprovePopUp.png";
    public String sikuliButtonUnregister = "src/test/resources/imgs/buttons/buttonUnregister.png";
    public String sikuliButtonUnregisterPopup = "src/test/resources/imgs/buttons/buttonUnregisterPopup.png";

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

    @FindBy(xpath = "*//h1[contains(text(),\"Peer registration\")]")
    public WebElementFacade headerPeerRegistration;

    //endregion

    //region WEB ELEMENTS: wait for

    public void waitButtonUnregister(){
        waitFor(buttonUnregister);
    }

    //endregion
}