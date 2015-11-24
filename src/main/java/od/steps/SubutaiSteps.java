package od.steps;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.pages.ReaderFromFile;
import od.pages.SubutaiPage;

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
    }

    @Step
    public void open_mng_h1() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("http://%s:8181/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        subutaiPage.open();
    }

    @Step
    public void open_mng_h2() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("http://%s:8181/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
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
    public void seeHeader2Success(){
        subutaiPage.waitHeader2Success();
        assertThat(subutaiPage.header2Success.isVisible(), is(true));
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
}