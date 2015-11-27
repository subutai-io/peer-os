package od.steps;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.pages.ReaderFromFile;
import od.pages.SubutaiPage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SubutaiSteps extends ScenarioSteps {
    SubutaiPage subutaiPage;

    @Step
    public void inputLogin(String login){
        subutaiPage.inputLogin.type(login);
    }

    @Step
    public void inputPassword(String password){
        subutaiPage.inputPassword.type(password);
    }

    @Step
    public void clickOnButtonLogin(){
        subutaiPage.buttonLogin.click();
        waitABit(5000);
    }

    @Step
    public void open_mng_h1() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        subutaiPage.open();
    }

    @Step
    public void open_mng_h2() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        subutaiPage.open();
    }

    @Step
    public void clickOnMenuItemEnvironment(){
        subutaiPage.linkEnvironment.click();
    }

    @Step
    public void clickOnMenuItemBlueprint(){
        subutaiPage.linkBlueprint.click();
    }

    @Step
    public void clickOnMenuItemEnvironments(){
        subutaiPage.linkEnvironments.click();
    }

    @Step
    public void clickOnMenuItemContainers(){
        subutaiPage.linkContainers.click();
    }

    @Step
    public void seeEnvironmentMenuItemEnvironment(){
        assertThat(subutaiPage.linkEnvironment.isVisible(), is(true));
    }

    @Step
    public void seeEnvironmentMenuItemBlueprint(){
        assertThat(subutaiPage.linkBlueprint.isVisible(), is(true));
    }

    @Step
    public void seeEnvironmentMenuItemEnvironments(){
        assertThat(subutaiPage.linkEnvironments.isVisible(), is(true));
    }

    @Step
    public void seeEnvironmentMenuItemContainers(){
        assertThat(subutaiPage.linkContainers.isVisible(), is(true));
    }

    @Step
    public void seeButtonCreateEnvironment() {
        assertThat(subutaiPage.buttonCreateBlueprintSecond.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonCreateBlueprint() {
        subutaiPage.buttonCreateBlueprintSecond.click();
    }

    @Step
    public void seeFieldEnterBlueprintName() {
        assertThat(subutaiPage.inputBlueprintName.isVisible(), is(true));
    }

    @Step
    public void enterBlueprintName(String blueprintName){
        subutaiPage.inputBlueprintName.type(blueprintName);
    }

    @Step
    public void enterNodeName(String nodeName) {
        subutaiPage.inputBlueprintNodeName.type(nodeName);
    }

    @Step
    public void selectTemplate(String template) {
        subutaiPage.selectBlueprintTemplate.selectByValue(template);
    }

    @Step
    public void enterNumberOfContainers(String count) {
        subutaiPage.inputBlueprintNumberOfContainers.type(count);
    }

    @Step
    public void enterSSHGroupID(String id) {
        subutaiPage.inputBlueprintSSHGroupID.type(id);
    }

    @Step
    public void enterHostGroupID(String id) {
        subutaiPage.inputBlueprintHostGroupID.type(id);
    }

    @Step
    public void selectQuotaSize(String quotaSize) {
        subutaiPage.selectBlueprintQuotaSize.selectByValue(quotaSize);
    }

    @Step
    public void clickOnButtonAddToNodeList() {
        subutaiPage.buttonBlueprintAddToNodeList.click();
    }

    @Step
    public void seeNodeListItem(){
        assertThat(subutaiPage.nodeListItem.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonCreate(){
        subutaiPage.buttonCreateBlueprint.click();
    }

    @Step
    public void seeCreatedBlueprint() {
        assertThat(subutaiPage.createdBlueprint.isDisplayed(), is(true));
    }

    @Step
    public void clickOnIconBuild(){
        subutaiPage.iconBuild.click();
    }

    @Step
    public void seeBuildEnvironment() {
        assertThat(subutaiPage.linkEnvironmentBuildList.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonPlace(){
        subutaiPage.buttonPlace.click();
    }

    @Step
    public void selectPeer(int index){
        subutaiPage.selectPeer.selectByIndex(index);
    }

    @Step
    public void selectStrategie(String value){
        subutaiPage.selectStrategie.selectByValue(value);
    }

    @Step
    public void inputEnvironmentName(String name){
        subutaiPage.inputEnvironmentName.type(name);
    }

    @Step
    public void clickLinkBuildEnvironmentList(){
        subutaiPage.linkEnvironmentBuildList.click();
    }

    @Step
    public void seeIconTwoContainers(){
        assertThat(subutaiPage.iconTwoContainers.isVisible(), is(true));
    }

    @Step
    public void seePopup(){
        assertThat(subutaiPage.popup.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonEnvironmentBuild(){
        subutaiPage.buttonEnvironmentBuild.click();
    }

    @Step
    public void seeHeaderSuccess(){
        subutaiPage.waitHeaderSuccess();
        assertThat(subutaiPage.headerSuccess.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonOK(){
        subutaiPage.buttonOK.click();
    }

    @Step
    public void seeTextYourEnvironmentStartCreation(){
        assertThat(subutaiPage.textYourEnvironmentStartCreation.isVisible(), is(true));
    }

    @Step
    public void seeTextYourEnvironmentHasBeenCreated(){
        assertThat(subutaiPage.textYourEnvironmentHasBeenCreated.isVisible(), is(true));
    }

    @Step
    public void waitTextYourEnvironmentHasBeenCreated() {
        subutaiPage.waitTextYourEnvironmentHasBeenCreated();
    }

    @Step
    public void clickOnIconGrow(){
        subutaiPage.iconGrow.click();
    }

    @Step
    public void seeSelectorEnvironment() {
        assertThat(subutaiPage.selectEnvironment.isVisible(), is(true));
    }

    @Step
    public void selectEnvironment(int index){
        subutaiPage.selectEnvironment.selectByIndex(index);
    }

    @Step
    public void seeTextYourEnvironmentStartGrowing(){
        assertThat(subutaiPage.textYourEnvironmentStartGrowing.isVisible(), is(true));
    }

    @Step
    public void seeTextYouSuccessfullyGrowEnvironment(){
        assertThat(subutaiPage.textYouSuccessfullyGrowEnvironment.isVisible(), is(true));
    }

    @Step
    public void waitTextYouSuccessfullyGrowEnvironment() {
        subutaiPage.waitTextYouSuccessfullyGrowEnvironment();
    }

    @Step
    public void clickOnIconRemove(){
        subutaiPage.iconRemove.click();
    }

    @Step
    public void clickOnButtonDeleteConfirm(){
        subutaiPage.buttonDeleteConfirm.click();
    }

    @Step
    public void seePopupAreYouSure(){
        assertThat(subutaiPage.popupAreYouSure.isVisible(), is(true));
    }

    @Step
    public void seeHeaderBlueprintDeleted(){
        assertThat(subutaiPage.headerBlueprintDeleted.isVisible(), is(true));
    }

    @Step
    public void seeTextYourEnvironmentStartDeleting(){
        assertThat(subutaiPage.textYourEnvironmentStartDeleting.isVisible(), is(true));
    }

    @Step
    public void seeTextYourEnvironmentHasBeenDestroyed(){
        assertThat(subutaiPage.textYourEnvironmentHasBeenDestroyed.isVisible(), is(true));
    }

    @Step
    public void waitTextYourEnvironmentHasBeenDestroyed(){
        subutaiPage.waitTextYourEnvironmentHasBeenDestroyed();
    }

    @Step
    public void clickOnIconDestroy(){
        subutaiPage.iconDestroy.click();
    }

    @Step
    public void clickOnMenuPeerRegistration() {
        subutaiPage.linkPeerRegistration.click();
    }

    @Step
    public void waitButtonCreatePeer(){
        subutaiPage.waitButtonCreatePeer();
    }

    @Step
    public void seeButtonCreatePeer() {
        assertThat(subutaiPage.linkCreatePeer.isVisible(), is(true));
    }

    @Step
    public void clickOnLinkCreatePeer(){
        subutaiPage.linkCreatePeer.click();
    }

    @Step
    public void seeInputPeerIP(){
        assertThat(subutaiPage.inputPeerIP.isVisible(), is(true));
    }

    @Step
    public void seeInputPeerKeyPhrase(){
        assertThat(subutaiPage.inputPeerKeyPhrase.isVisible(), is(true));
    }

    @Step
    public void enterPeerIP() throws FileNotFoundException {
        subutaiPage.inputPeerIP.type(ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2"));
    }

    @Step
    public void enterPeerKeyPhrase(String phrase){
        subutaiPage.inputPeerKeyPhrase.type(phrase);
    }

    @Step
    public void clickOnButtonCreatePeer(){
        subutaiPage.buttonCreatePeer.click();
    }

    @Step
    public void seeCrossPeer2Ip() throws FileNotFoundException {
        subutaiPage.textPeer2Ip.containsText(String.format("%s", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
    }

    @Step
    public void seeCrossPeer1Ip() throws FileNotFoundException {
        subutaiPage.textPeer1Ip.containsText(String.format("%s", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
    }

    @Step
    public void seeButtonCancel(){
        assertThat(subutaiPage.buttonCancel.isVisible(), is(true));
    }

    @Step
    public void seeButtonApprove(){
        assertThat(subutaiPage.buttonApprove.isVisible(), is(true));
    }

    @Step
    public void seeButtonReject(){
        assertThat(subutaiPage.buttonReject.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonApprove(){
        subutaiPage.buttonApprove.click();
    }

    @Step
    public void enterPeerApproveKeyPhrase(String phrase){
        subutaiPage.inputPeerApproveKeyPhrase.type(phrase);
    }

    @Step
    public void seePeerApproveKeyPhrase(){
        assertThat(subutaiPage.inputPeerApproveKeyPhrase.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonPopupApprove(){
        subutaiPage.buttonPopupApprove.click();
    }

    @Step
    public void waitButtonUnregister(){
        subutaiPage.waitButtonUnregister();
    }

    @Step
    public void seeButtonUnregister(){
        assertThat(subutaiPage.buttonUnregister.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonUnregister(){
        subutaiPage.buttonUnregister.click();
    }

    @Step
    public void clickOnButtonConfirmUnregister(){
        subutaiPage.buttonConfirmUnregister.click();
    }

    @Step
    public void waitTextUnregistered(){
        subutaiPage.waitTextUnregistered();
    }

    @Step
    public void seeTexUnregistered() {
        assertThat(subutaiPage.textUnregistered.isVisible(), is(true));
    }

    @Step
    public void seeTextNoDataAvailableInTable(){
        assertThat(subutaiPage.textNoDataAvailableInTable.isVisible(), is(true));
    }

    @Step
    public void clickLinkConsole(){
        subutaiPage.linkConsole.click();
        waitABit(3000);
    }

    @Step
    public void enterCommand(String command){
        waitABit(5000);
        subutaiPage.inputCommandLine.click();
        subutaiPage.inputCommandLine.sendKeys(Keys.ENTER);
        waitABit(5000);
    }

    @Step
    public void selectPeerConsole(int index){
        subutaiPage.selectPeerConsole.selectByIndex(index);
        waitABit(3000);
    }

    @Step
    public void executeConsoleCommand(String command){
        subutaiPage.executeConsoleCommand(command);
    }

    @Step
    public void selectMenuResourceHost(){
        subutaiPage.selectMenuResourceHost.click();
        subutaiPage.selectMenuResourceHost.selectByIndex(1);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectMenuEnvironment(){
        subutaiPage.selectMenuResourceHost.click();
        subutaiPage.selectMenuResourceHost.selectByIndex(1);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectContainer(){
        subutaiPage.selectContainer.click();
        subutaiPage.selectContainer.selectByIndex(1);
        subutaiPage.selectContainer.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectMenuManagementHost(){
        subutaiPage.selectMenuResourceHost.click();
        subutaiPage.selectMenuResourceHost.selectByIndex(2);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
    }

    @Step
    public void verifyOutputConsoleCommand(String expectedPhrase){
        assertThat(subutaiPage.getPreData(expectedPhrase), is(true));
    }

    @Step
    public String getContainerNameOne(){
        String s =  subutaiPage.getOutputRow("master", 0, 38, 2);
        return s;
    }

    @Step
    public String getContainerNameTwo(){
        String s = subutaiPage.getOutputRow("master",0, 38, 3);
        return s;
    }

    @Step
    public String getPIDContainer(){
        String s = subutaiPage.getPIDContainer("PID:");
        return s;
    }

    @Step
    public void getAllData(){
        subutaiPage.getAllData("upstart-udev-bridge --daemon");
    }

    @Step
    public void clickOnButtonEnvironmentOnConsole(){
        subutaiPage.buttonEnvironmentOnConsole.click();
    }

    @Step
    public void clickButtonPeerConsole(){
        subutaiPage.buttonPeerOnConsole.click();
    }

    @Step
    public void selectMenuResourceHostAgain(){
        subutaiPage.selectMenuResourceHost.click();
        subutaiPage.selectMenuResourceHost.selectByIndex(0);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
        subutaiPage.selectMenuResourceHost.click();
        subutaiPage.selectMenuResourceHost.selectByIndex(1);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectEnvironmentConsole(){
        subutaiPage.selectEnvironmentConsole.click();
        subutaiPage.selectEnvironmentConsole.selectByIndex(1);
        subutaiPage.selectEnvironmentConsole.sendKeys(Keys.ENTER);
    }

    @Step
    public void waitHeaderBlueprintDeleted() {
        subutaiPage.waitHeaderBlueprintDeleted();
    }
}