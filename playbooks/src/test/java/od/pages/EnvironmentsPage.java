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

    @FindBy(id = "js-search")
    public WebElementFacade fieldSearch;

    @FindBy(ngModel = "domain.name")
    public WebElementFacade fieldDomainName;

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
    public String sikuliButtonAdvanced = "src/test/resources/imgs/buttons/buttonAdvanced.png";
    public String sikuliButtonEdit = "src/test/resources/imgs/buttons/buttonEdit.png";
    public String sikuliButtonNext = "src/test/resources/imgs/buttons/buttonNext.png";
    public String sikuliButtonAddUserTest = "src/test/resources/imgs/buttons/buttonAddUserTest.png";
    public String sikuliButtonConfigure = "src/test/resources/imgs/buttons/buttonConfigure.png";

    @FindBy(xpath = "*//div[@class=\"b-toggle b-toggle_cloud\"]")
    public WebElementFacade buttonModes;

    //endregion

    //region WEB ELEMENTS: Checkboxes

    @FindBy(xpath = "*//div[@class=\"b-toggle b-toggle_cloud\"]")
    public WebElementFacade checkboxEnviromentMode;

    //endregion

    //region WEB ELEMENTS: Links

    //endregion

    //region WEB ELEMENTS: Tables

    @FindBy(xpath = "*//span[contains(text(),\"Peer 1\")]")
    public WebElementFacade peer1;

    //endregion

    //region WEB ELEMENTS: Pickers
    public String sikuliPickerSmall = "src/test/resources/imgs/elements/pickerSmall.png";
    public String sikuliPickerMedium = "src/test/resources/imgs/elements/pickerMedium.png";


    //endregion

    //region WEB ELEMENTS: Selectors

    //endregion

    //region WEB ELEMENTS: Icons

    @FindBy(xpath = ".//*[@id='DataTables_Table_0']/tbody/tr/td[6]/a[2]")
    public WebElementFacade iconDeleteEnvironment;

    //endregion

    //region SIKULI IMAGES: Icons

    public String sikuliIconDeleteEnvironment = "src/test/resources/imgs/icons/iconDeleteEnvironment.png";
    public String sikuliIconShare = "src/test/resources/imgs/icons/iconShare.png";
    public String sikuliIconSettingsFirstCont = "src/test/resources/imgs/icons/iconSettingsCont1.png";
    public String sikuliIconSettingsSecondCont = "src/test/resources/imgs/icons/iconSettingsCont2.png";
    public String sikuliIconSettingsThirdContCass = "src/test/resources/imgs/icons/iconSettingsCont3Cass.png";

    //endregion

    //region SIKULI IMAGES: Titles

    public String sikuliTitleTemplates = "src/test/resources/imgs/titles/titleTemplates.png";

    //endregion

    //region Templates

    @FindBy(xpath = "*//span[contains(text(),\"mongo\")]")
    public WebElementFacade templateMongo;

    @FindBy(xpath = "*//span[@class=\"b-cloud-item-text ng-binding\" and contains(text(),\"webdemo\")]")
    public WebElementFacade templateWebdemo;

    @FindBy(xpath = "*//div[@data-template=\"cassandra\"]")
    public WebElementFacade templateCassandra;

    //endregion

    //region SIKULI IMAGES: Templates

    public String sikuliTemplateMongo = "src/test/resources/imgs/templates/templateMongo.png";
    public String sikuliTemplateMySite = "src/test/resources/imgs/templates/templateMySite.png";
    public String sikuliTemplateWebdemo = "src/test/resources/imgs/templates/templateWebdemo.png";
    public String sikuliTemplateCasandra = "src/test/resources/imgs/templates/templateCassandra.png";
    public String sikuliTemplateMaster = "src/test/resources/imgs/templates/templateMaster.png";
    public String sikuliTemplateApache = "src/test/resources/imgs/templates/templateApache.png";
    //endregion

    //region WEB ELEMENTS: Headers

    @FindBy(xpath = "*//h1[contains(text(),\"Environment Manager\")]")
    public WebElementFacade headerEnvironments;

    @FindBy(xpath = "*//li[contains(text(),\"Peers\")]")
    public WebElementFacade titlePeers;

    @FindBy(xpath = "*//span[@class=\"floating_element\"]")
    public WebElementFacade headerApache;

    //endregion

    //region Action: Wait for

    public void waitTemplateMongo(){
        waitFor(templateMongo);
    }

    public void waitForCloseButton(){
        waitFor(buttonClose);
    }

    public void waitTemplateCassandra(){
        waitFor(templateCassandra);
    }

    public void waitPeer1(){
        waitFor(peer1);
    }

    //endregion

    //region Action: Elements Sikuli

    public String sikuliPeer1 = "src/test/resources/imgs/elements/peerPeer1.png";
    public String sikuliPeer2 = "src/test/resources/imgs/elements/peerPeer2.png";
    public String sikuliResourceHost1 = "src/test/resources/imgs/elements/resourceHost1.png";
    public String sikuliPeerRH1 = "src/test/resources/imgs/elements/peerRH1.png";
    public String sikuliPeerRH2 = "src/test/resources/imgs/elements/peerRH2.png";
    public String sikuliCheckBoxDeleteInShareEnvi = "src/test/resources/imgs/elements/checkBoxDeleteInShareEnvi.png";
    public String sikuliCheckBoxAddDomain = "src/test/resources/imgs/elements/checkBoxAddDomain.png";
    //endregion
}