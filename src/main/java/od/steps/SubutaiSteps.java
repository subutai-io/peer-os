package od.steps;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import od.pages.ExecuteShellCommand;
import od.pages.ReaderFromFile;
import od.pages.SubutaiPage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SubutaiSteps extends ScenarioSteps {
    SubutaiPage subutaiPage;
    private Object token;
    private Object peerID;
    private Object environmentData;
    public Object containerIp;
    int sumFirst;
    int sumCommon;

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
    public void open_mng_h_arm() {
        subutaiPage.setDefaultBaseUrl("https://172.16.194.234:8443/");
        subutaiPage.open();
    }

    @Step
    public void open_aws_mng_h1() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/awsmh1_IP")));
        subutaiPage.open();
    }

    @Step
    public void open_aws_mng_h2() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/awsmh2_IP")));
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
        waitABit(5000);
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
        waitABit(5000);
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
        waitABit(5000);
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
        waitABit(2000);
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
    public void waitButtonStartContainer(){
        subutaiPage.waitButtonStartContainer();
    }

    @Step
    public void waitButtonStopContainer(){
        subutaiPage.waitButtonStopContainer();
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
        subutaiPage.selectMenuResourceHost.selectByIndex(2);
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
        subutaiPage.selectMenuResourceHost.selectByIndex(1);
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

    //--------------3023

    @Step
    public void clickOnLinkConsole(){
        subutaiPage.subutaiLinkConsole.click();
    }

    @Step
    public void seeConsole(){
        assertThat(subutaiPage.subuaiConsole.isVisible(), is(true));
    }

    @Step
    public void seeSelectMenuResourceHosts(){
        assertThat(subutaiPage.selectMenuResourceHost.isVisible(), is(true));
    }

    @Step
    public void clickOnSelectMenuResourceHosts(){
        subutaiPage.selectMenuResourceHost.click();
    }

    @Step
    public void clickOnOptionOfSelectMenuResourceHosts(){
        assertThat(subutaiPage.selectMenuResourceHostsOptionRH.isVisible(), is(true));
        subutaiPage.selectMenuResourceHost.selectByIndex(1);
        subutaiPage.selectMenuResourceHost.sendKeys(Keys.ENTER);
    }

    @Step
    public void inputCommandIntoConsole(String command){
        waitABit(5000);
        subutaiPage.executeConsoleCommand(command);
    }

    @Step
    public void seeOutputOfTestCommandThreeReceived(){
        waitABit(5000);
        assertThat(subutaiPage.outputOfTestCommandThreeReceived.isVisible(), is(true));
    }

    @Step
    public void seeHeaderBlueprint(){
        assertThat(subutaiPage.subutaiBlueprintHeader.isVisible(), is(true));
    }

    @Step
    public void clickOnConsole(){
        subutaiPage.endOfConsoleLine.click();
    }

    @Step
    public void seeRHCommandLine(){
        assertThat(subutaiPage.endOfConsoleLine.isVisible(), is(true));
    }

    @Step
    public void pageConsoleClickOnEnvironmentLink(){
        subutaiPage.pageConsoleLinkEnvironment.click();
    }

    @Step
    public void selectEnvironmentInSelectMenu(String environment){
        assertThat(subutaiPage.environmentSelectMenu.isVisible(), is(true));
        subutaiPage.environmentSelectMenu.click();
        subutaiPage.environmentSelectMenu.selectByVisibleText(environment);
        subutaiPage.environmentSelectMenu.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectContainerOneInSelectMenu(){
        assertThat(subutaiPage.selectMenuContainer.isVisible(), is(true));
        subutaiPage.selectMenuContainer.selectByIndex(1);
        subutaiPage.selectMenuContainer.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectContainerTwoInSelectMenu(){
        assertThat(subutaiPage.selectMenuContainer.isVisible(), is(true));
        subutaiPage.selectMenuContainer.selectByIndex(2);
        subutaiPage.selectMenuContainer.sendKeys(Keys.ENTER);
    }

    @Step
    public void resourceHostPGPKey() throws FileNotFoundException {
        waitABit(5000);
        String s = subutaiPage.terminalPromptBinding.getText();
        int rStart = 0;
        int rEnd = s.length() - 4 ;
        String result = s.substring(rStart, rEnd);


        subutaiPage.setDefaultBaseUrl("https://" + ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1") + ":8443/rest/v1/security/keyman/getpublickeyring?hostId=" + result );
        subutaiPage.open();
        waitABit(10000);
    }

    @Step
    public void seePGP(){
        assertThat(subutaiPage.pgpHeader.isVisible(), is(true));
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
        subutaiPage.selectMenuResourceHost.selectByIndex(2);
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

    @Step
    public void waitFunctionForSlowOperations(int time){
        waitABit(time);
    }

    @Step
    public void run_bash_script(String file) throws FileNotFoundException {
        ExecuteShellCommand executeShellCommand = new ExecuteShellCommand();
        System.out.println(executeShellCommand.executeCommand(file));
    }

    @Step
    public void clickOnButtonStop() {
        subutaiPage.containerButtonStop.click();
    }

    @Step
    public void seeButtonStart() {
        assertThat(subutaiPage.containerButtonStart.isVisible(), is(true));
    }

    @Step
    public void seeButtonStop() {
        assertThat(subutaiPage.containerButtonStop.isVisible(), is(true));
    }

    @Step
    public void seeOutputOfPwdCommand() {
        assertThat(subutaiPage.outputOfPwdCommand.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonStart() {
        subutaiPage.containerButtonStart.click();
    }

    @Step
    public void selectEnvironmentOnConsole() {
        subutaiPage.buttonEnvironmentOnConsole.click();
    }

    @Step
    public void clickOnMenuItemUserIdentity() {
        subutaiPage.linkUserIdentity.click();
    }

    @Step
    public void clickOnMenuItemsTokens() {
        subutaiPage.linkTokens.click();
    }

    @Step
    public void clickOnButtonShowToken() {
        subutaiPage.buttonShowToken.click();
    }

    @Step
    public void seeTextToken() {
        assertThat(subutaiPage.popupMenuTokenTextHeader.isVisible(), is(true));
    }

    @Step
    public Object getToken() {
        token = subutaiPage.token.getText();
        return token;
    }

    @Step
    public void observeLocalPeerID() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/security/keyman/getpublickeyfingerprint?sptoken=" + token, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        subutaiPage.open();
    }

    @Step
    public void observeRemotePeerID() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/security/keyman/getpublickeyfingerprint?sptoken=" + token, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        subutaiPage.open();
    }


    @Step
    public Object getPeerID() {
        peerID = subutaiPage.peerID.getText();
        return peerID;
    }

    @Step
    public void ownPGPKey() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/security/keyman/getpublickeyring?hostid=" + peerID, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h1")));
        subutaiPage.open();
    }

    @Step
    public void observeOwnPGPKey() {
        assertThat(subutaiPage.remotePGPKey.isVisible(), is(true));
    }

    @Step
    public void remotePGPKey() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/security/keyman/getpublickeyring?hostid=" + peerID, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        subutaiPage.open();
    }

    @Step
    public void observeEnvironmentData() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/environments?sptoken=" + token, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        subutaiPage.open();
    }

    @Step
    public Object getEnvironmentData() {
        environmentData = subutaiPage.environmentData.getText().substring(8, 44);
        return environmentData;
    }

    @Step
    public void environmentPGPKey() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8443/rest/v1/security/keyman/getpublickeyring?hostid=" + peerID +"-"+environmentData, ReaderFromFile.readDataFromFile("src/test/resources/parameters/mng_h2")));
        subutaiPage.open();
    }

    @Step
    public void observeEnvironmentPGPKey() {
        assertThat(subutaiPage.environmentPGPKey.isVisible(),is(true));
    }

    @Step
    public Object getContainerIp() {
        containerIp =  subutaiPage.containerIp.getText();
        return containerIp;
    }

    @Step
    public void executeConsoleCommandPingContainer(String command){
        subutaiPage.executeConsoleCommand(command);
    }

    @Step
    public void seeOutputOfPingCommand() {
        assertThat(subutaiPage.outputOfPingCommand.isVisible(), is(true));
    }

    public void seeOutputOfWrongPingCommand() {
        assertThat(subutaiPage.outputOfWrongPingCommand.isVisible(), is(true));
    }

    @Step
    public void open_vagrant_mgh() throws FileNotFoundException {
        subutaiPage.setDefaultBaseUrl(String.format("https://%s:8888/", ReaderFromFile.readDataFromFile("src/test/resources/parameters/vagrantMH_IP")));
        subutaiPage.open();
    }

    //-------3117

    @Step
    public void seeIconThreeContainers(){
        assertThat(subutaiPage.iconThreeContainers.isVisible(), is(true));
    }

    @Step
    public void clickOnButtonConfigure(){
        subutaiPage.buttonConfigure.click();
    }

    @Step
    public void seeEmptyInputDomain(){
        subutaiPage.waitDomainPopUpMenu();
        assertThat(subutaiPage.getDomain().isEmpty(), is(true));
    }

    @Step
    public void inputDomainInTheField(String domain){
        subutaiPage.inputDomain.type(domain);
    }

    @Step
    public void clickOnSelectMenuDomainStrtegy(){
        subutaiPage.selectMenuDomainStrategy.click();
    }

    @Step
    public void selectDomainStrtegyRoundRobin(String strategy){
        subutaiPage.selectMenuDomainStrategy.selectByVisibleText("ROUND_ROBIN");
    }

    @Step
    public void pressEnterOnDomainStrategy(){
        subutaiPage.selectMenuDomainStrategy.sendKeys(Keys.ENTER);
    }

    @Step
    public void selectFileToUpload(){
        subutaiPage.uploadFromFile();
    }

    @Step
    public void domainClickOnTheButtonSave(){
        subutaiPage.domainButtonSave.click();
    }

    @Step
    public void seeDomainBindingText(String text){
        waitABit(2000);
        subutaiPage.domainBindingText.containsText(text);
    }

    @Step
    public void roundRobinSelected(){
        assertThat(subutaiPage.domainOptonRoundRobi.isSelected(), is(true));
    }

    @Step
    public void clickOnFirstContainerButtonConfigure(){
        waitABit(5000);
        subutaiPage.firstContainerButtonConfigure.click();
    }

    @Step
    public void  clickOnSecondContainerButtonConfigure(){
        waitABit(5000);
        subutaiPage.secondContainerButtonConfigure.click();
    }

    @Step
    public void clickOnThirdContainerButtonConfigure(){
        waitABit(5000);
        subutaiPage.thirdContainerButtonConfigure.click();
    }

    @Step
    public void clickOnContainerDomainCheckbox(){
        subutaiPage.containerDomainCheckbox.click();
    }

    @Step
    public void clickOnContainerDomainButtonSave(){
        subutaiPage.checkboxSaveButton.click();
    }

    @Step
    public void seeCheckboxCheced(){
        waitABit(10000);
        assertThat(subutaiPage.containerDomainCheckbox.isSelected(), is(true));
    }

    @Step
    public void open_local_subutai_page() {
        subutaiPage.setDefaultBaseUrl(String.format("http://subut.ai/"));
        subutaiPage.open();
        waitABit(10000);
    }

    @Step
    public void seeDomainContainerIP(){
        assertThat(subutaiPage.domainContainerIP.isVisible(), is(true));
    }

    @Step
    public void compareContainerIP(){
        assertThat(subutaiPage.compareIP(), is(false));
    }

    @Step
    public void clickOnContainerButtonStop(){
        subutaiPage.containerButtonStop.click();
        waitABit(3000);
    }

    @Step
    public void getIPStoppedContainer(){
        subutaiPage.getIPContainerStopped();
    }

    @Step
    public void seeContainerButtonStart(){
        waitABit(30000);
        assertThat(subutaiPage.containerButtonStart.isVisible(), is(true));
    }

    @Step
    public void reloadPage(){
        subutaiPage.pageReload();
    }

    @Step
    public void checkForOutOfIP(){
        assertThat(subutaiPage.checkForOutOfIP(), is(true));
    }

    @Step
    public void shouldGetIPDisabledContainer(){
        subutaiPage.getIPContainerDisabled();
    }

    @Step
    public void seeCheckboxUncheck(){
        waitABit(5000);
        assertThat(subutaiPage.containerDomainCheckbox.isSelected(), is(false));
    }

    @Step
    public void clickOnButtonRemoveDomain(){
        subutaiPage.enviromentButtonRemoveDomain.click();
    }

    @Step
    public void notSeeDomainName(){
        waitABit(5000);
        assertThat(subutaiPage.domainBindingText.isVisible(), is(false));
    }

    @Step
    public void pageNotFoundMessage(){
        assertThat(subutaiPage.desabledBindToEnvironment.isVisible() || subutaiPage.unabledToConnect.isVisible(), is(true))  ;
    }


    @Step
    public void clickOnSomeTubs(){
        subutaiPage.dyspleyKeysButton.click();
        waitABit(6000);
        subutaiPage.stepButton.click();
        waitABit(6000);
        subutaiPage.dekryptButton.click();
        waitABit(6000);
    }

    @Step
    public void seeOutputOfTestCommand(){
        assertThat(subutaiPage.outputOfTestCommand.isVisible(), is(true));
    }

//    @Step
//    public void run_bash_script(String file) throws FileNotFoundException {
//        ExecuteShellCommand executeShellCommand = new ExecuteShellCommand();
//        executeShellCommand.executeCommand(file);
//        subutaiPage.pgpStart();
//    }

    @Step
    public void observeLogin() {
        assertThat(subutaiPage.ngLogin.isVisible(), is(true));
    }

    @Step
    public void observePassword() {
        assertThat(subutaiPage.ngPassword.isVisible(), is(true));
    }

    @Step
    public void observeNgCreateBlueprint() {
        assertThat(subutaiPage.ngCreateBlueprint.isVisible(), is(true));
    }

    @Step
    public void observeNgAdd() {
        assertThat(subutaiPage.ngAdd.isVisible(), is(true));
    }

    @Step
    public void observeNgRevove() {
        assertThat(subutaiPage.ngRemove.isVisible(), is(true));
    }

    @Step
    public void observeNgEnvironmentSelector() {
        assertThat(subutaiPage.ngEnvironmentSelector.isVisible(), is(true));
    }

    @Step
    public void observeNgContainersSelector() {
        assertThat(subutaiPage.ngContainersSelector.isVisible(), is(true));
    }

    @Step
    public void observeNgPeer() {
        assertThat(subutaiPage.ngPeer.isVisible(), is(true));
    }

    @Step
    public void observeNgSelectPeer() {
        assertThat(subutaiPage.ngSelectedPeer.isVisible(), is(true));
    }

    @Step
    public void clickOnMenuItemUserManagement() {
        subutaiPage.linkUserManagement.click();
    }

    @Step
    public void observeNgAddUser() {
        assertThat(subutaiPage.ngAddUser.isVisible(), is(true));
    }

    @Step
    public void observeNgAddRole() {
        assertThat(subutaiPage.ngAddRole.isVisible(), is(true));
    }

    @Step
    public void clickOnMenuItemRolesManagement() {
        subutaiPage.ngRolesManagment.click();
    }

    @Step
    public void observeNgAddToken() {
        assertThat(subutaiPage.ngAddToken.isVisible(), is(true));
    }

    @Step
    public void observeNgTokenName() {
        assertThat(subutaiPage.ngTokenName.isVisible(), is(true));
    }

    @Step
    public void observeNgCreatePeer() {
        assertThat(subutaiPage.ngCreatePeer.isVisible(), is(true));
    }

    @Step
    public void clickOnMenuItemTracker() {
        subutaiPage.linkTracker.click();
    }

    @Step
    public void observeNgSourceSelector() {
        assertThat(subutaiPage.ngSourceSelector.isVisible(), is(true));
    }

    @Step
    public String getWebUiWeght1() {
        String webUi1 = subutaiPage.getWebUiContainer1("K");
        return webUi1;
    }

    @Step
    public String getWebUiWeght2() {
        String webUi2 = subutaiPage.getWebUiContainer2("impl");
        return webUi2;
    }

    @Step
    public String getWebUiWeight3() {
        String webUi3 = subutaiPage.getWebUiContainer3("M");
        return webUi3;
    }

    @Step
    public int getWeightOfWeight1Weight2() {
        int sum1 = Integer.parseInt(subutaiPage.webUi1);
        int sum2 = Integer.parseInt(subutaiPage.webUi2);
        sumFirst = sum1 + sum2;
        System.out.println(sumFirst);
        return sumFirst;
    }

    @Step
    public int getWeightOfWeight1Weight2Weight3() {
        float sum3 = Float.parseFloat(subutaiPage.webUi3) * 1000;
        sumCommon = (int) (sum3 + sumFirst);
        System.out.println(sumCommon);
        return sumCommon;
    }

    @Step
    public void observeWebUiLessThan10Mb() {
        if ((sumCommon) < 10000) {
            is(true);
        }
    }

    @Step
    public void waitSleep(int i) {
        waitABit(i);
    }
}