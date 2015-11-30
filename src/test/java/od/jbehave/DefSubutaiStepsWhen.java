package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.When;

import java.io.FileNotFoundException;

public class DefSubutaiStepsWhen {

    @Steps
    SubutaiSteps subutaiSteps;

    @When("the user click on the menu item: Environment")
    public void click_on_link_environment(){
        subutaiSteps.clickOnMenuItemEnvironment();
    }

    @When("the user click on the menu item: Blueprint")
    public void click_on_link_blueprint(){
        subutaiSteps.clickOnMenuItemBlueprint();
    }

    @When("the user click on the menu item: Environments")
    public void click_on_link_environments(){
        subutaiSteps.clickOnMenuItemEnvironments();
    }

    @When("the user click on the menu item: Containers")
    public void click_on_link_Containers(){
        subutaiSteps.clickOnMenuItemContainers();
    }

    @When("the user click on the button: Create Blueprint")
    public void click_on_button_create_blueprint(){
        subutaiSteps.clickOnButtonCreateBlueprint();
    }

    @When("the user enter blueprint name: '$blueprintName'")
    public void enter_blueprint_name(String blueprintName){
        subutaiSteps.enterBlueprintName(blueprintName);
    }

    @When("the user enter node name: '$nodeName'")
    public void enter_node_name(String nodeName){
        subutaiSteps.enterNodeName(nodeName);
    }

    @When("the user select template: '$template'")
    public void select_template(String template){
        subutaiSteps.selectTemplate(template);
    }

    @When("the user enter number of containers: '$count'")
    public void enter_number_of_containers(String count){
        subutaiSteps.enterNumberOfContainers(count);
    }

    @When("the user enter SSH group ID: '$id'")
    public void enter_ssh_group_id(String id){
        subutaiSteps.enterSSHGroupID(id);
    }

    @When("the user enter host Group ID: '$id'")
    public void enter_host_group_id(String id){
        subutaiSteps.enterHostGroupID(id);
    }

    @When("the user select quota size: '$quotaSize'")
    public void select_quota_size(String quotaSize){
        subutaiSteps.selectQuotaSize(quotaSize);
    }

    @When("the user click on the button: Add to node list")
    public void click_button_add_to_node_list(){
        subutaiSteps.clickOnButtonAddToNodeList();
    }

    @When("the user click on the button: Create")
    public void click_button_create(){
        subutaiSteps.clickOnButtonCreate();
    }

    @When("the user click on the icon: Build")
    public void click_on_icon_build(){
        subutaiSteps.clickOnIconBuild();
    }

    @When("the user select peer: One")
    public void select_peer_one(){
        subutaiSteps.selectPeer(1);
    }

    @When("the user select peer: Two")
    public void select_peer_two(){
        subutaiSteps.selectPeer(2);
    }

    @When("the user select Strategie: '$strategie'")
    public void select_strategie(String strategie){
        subutaiSteps.selectStrategie(strategie);
    }

    @When("the user click on the button: Place")
    public void click_on_button_place(){
        subutaiSteps.clickOnButtonPlace();
    }

    @When("the user enter environment name: '$name'")
    public void enter_environment_name(String name){
        subutaiSteps.inputEnvironmentName(name);
    }

    @When("the user click on the link: Environment Build List")
    public void click_on_link_environment_build_list(){
        subutaiSteps.clickLinkBuildEnvironmentList();
    }

    @When("the user click on the button: Build")
    public void click_on_button_build(){
        subutaiSteps.clickOnButtonEnvironmentBuild();
    }

    @When("the user click on the button: OK")
    public void click_on_button_ok(){
        subutaiSteps.clickOnButtonOK();
    }

    @When("the user click on the icon: Grow")
    public void click_on_icon_grow(){
        subutaiSteps.clickOnIconGrow();
    }

    @When("the user select environment: Local Environment")
    public void select_environment(){
        subutaiSteps.selectEnvironment(1);
    }

    @When("the user click on the icon: Remove")
    public void click_on_icon_remove(){
        subutaiSteps.clickOnIconRemove();
    }

    @When("the user click on the button: Delete")
    public void click_on_button_delete(){
        subutaiSteps.clickOnButtonDeleteConfirm();
    }

    @When("the user click on the icon: Destroy")
    public void click_on_icon_destroy(){
        subutaiSteps.clickOnIconDestroy();
    }

    @When("the user click on the menu item: Peer Registration")
    public void click_menu_item_peer_registration() {
        subutaiSteps.waitABit(5000);
        subutaiSteps.clickOnMenuPeerRegistration();
    }

    @When("the user click on the link: Create Peer")
    public void click_on_link_create_peer(){
        subutaiSteps.clickOnLinkCreatePeer();
    }

    @When("the user enter peer ip: Second user")
    public void enter_peer_ip_second_user() throws FileNotFoundException {
        subutaiSteps.enterPeerIP();
    }

    @When("the user enter peer key phrase: '$phrase'")
    public void enter_peer_key_phrase(String phrase){
        subutaiSteps.enterPeerKeyPhrase(phrase);
    }

    @When("the user click on the button: Create for peer")
    public void click_on_button_create_for_peer(){
        subutaiSteps.clickOnButtonCreatePeer();
    }

    @When("the user click on the button: Approve")
    public void click_on_button_approve(){
        subutaiSteps.clickOnButtonApprove();
    }

    @When("the user enter approve key phrase: '$phrase'")
    public void enter_peer_approve_key_phrase(String phrase){
        subutaiSteps.enterPeerApproveKeyPhrase(phrase);
    }

    @When("the user click on the button popup: Approve")
    public void click_on_button_popup_approve(){
        subutaiSteps.clickOnButtonPopupApprove();
    }

    @When("the user click on the button: Unregister")
    public void click_on_button_unregister(){
        subutaiSteps.clickOnButtonUnregister();
    }

    @When("the user click on the button: Confirm Unregister")
    public void click_on_button_confirm_unregister(){
        subutaiSteps.clickOnButtonConfirmUnregister();
    }



    @When("the user click on the menu item: Console")
    public void click_menu_item(){
        subutaiSteps.clickLinkConsole();
    }

    @When("the user enter command: '$command'")
    public void enter_command(String command){
        subutaiSteps.enterCommand(command);
    }

    @When("the user select peer executeConsoleCommand: Two")
    public void select_peer_console(){
        subutaiSteps.selectPeerConsole(2);
    }

    @When("the user select any available resource host from select menu")
    public void select_resource_host_from_select_menu(){
        subutaiSteps.selectMenuResourceHost();
    }

    @When("the user select management host from select menu")
    public void select_menagement_host_from_select_menu(){
        subutaiSteps.selectMenuManagementHost();
    }

    @When("the user enter console command: '$command'")
    public void execute_console_command(String command){
        subutaiSteps.executeConsoleCommand(command);
    }

    @When("the user enter console command: subutai import hadoop")
    public void execute_console_command_subutai_import_hadoop(){
        subutaiSteps.executeConsoleCommand("subutai import hadoop");
        subutaiSteps.waitFunctionForSlowOperations(60000);
    }

    @When("the user enter console command: sudo lxc-info -Ssip -n for Container One")
    public void execute_console_command_container1(){
        subutaiSteps.executeConsoleCommand("sudo lxc-info -Ssip -n " + subutaiSteps.getContainerNameOne());
    }

    @When("the user enter console command: sudo lxc-info -Ssip -n for Container Two")
    public void execute_console_command_container2(){
        subutaiSteps.executeConsoleCommand("sudo lxc-info -Ssip -n " + subutaiSteps.getContainerNameTwo());
    }

    @When("the user enter console command: ps -ef and grep PID")
    public void execute_console_command_pid(){
        subutaiSteps.executeConsoleCommand("ps -ef | grep " + subutaiSteps.getPIDContainer());
    }

    //------------ 3023

    @When("the user click on console link")
    public void click_on_console_link(){
        subutaiSteps.clickOnLinkConsole();
    }

    @When("the user enter a command '$caommand' to Command field and press enter")
    public void enter_command_into_console(String command){
        subutaiSteps.seeRHCommandLine();
        subutaiSteps.clickOnConsole();
        subutaiSteps.inputCommandIntoConsole(command);
    }

    @When("the user click on link: environment on the console page")
    public void klick_on_environment_link_on_console_page(){
        subutaiSteps.pageConsoleClickOnEnvironmentLink();
    }

    @When("the user select '$environment' environment in select menu")
    public void select_environment_in_select_menu(String environment){
        subutaiSteps.selectEnvironmentInSelectMenu(environment);
    }

    @When("the user select container in select menu")
    public void user_select_container_in_select_menu(){
        subutaiSteps.selectContainerInSelectMenu();
    }

    @When("the user get resource host ID and go pgp key url")
    public void user_get_rh_id_and_go_to_pgp_url() throws FileNotFoundException {
        subutaiSteps.resourceHostPGPKey();
    }

    @When("the user get container ID and go pgp key url")
    public void user_get_container_id_and_go_to_pgp_url() throws FileNotFoundException {
        subutaiSteps.resourceHostPGPKey();
    }

    @When("the user click on the button: Environment")
    public void click_on_button_environment(){
        subutaiSteps.clickOnButtonEnvironmentOnConsole();
    }

    @When("the user select current environment")
    public void select_current_environment(){
        subutaiSteps.selectEnvironmentConsole();
    }

    @When("the user select first container")
    public void select_container(){
        subutaiSteps.selectContainer();
    }

    @When("the user click on the button: Peer")
    public void click_button_Peer(){
        subutaiSteps.clickButtonPeerConsole();
    }

    @When("the user select again one resource host")
    public void select_one_resource_host(){
        subutaiSteps.selectMenuResourceHostAgain();
    }


    @When("the user enter console command: ls -l /var/lib/apps/subutai/current/var/lib/lxc/ContainerName/rootfs")
    public void execute_console_command_container2_ls(){
        subutaiSteps.executeConsoleCommand("ls -l /var/lib/apps/subutai/current/var/lib/lxc/" + subutaiSteps.getContainerNameTwo() + "/rootfs/");
    }

    @When("the user click on the button: Stop")
    public void click_button_stop(){
        subutaiSteps.clickOnButtonStop();
        subutaiSteps.waitABit(5000);
        subutaiSteps.clickOnButtonStop();
    }

    @When("the user click on the button: Start")
    public void click_button_start(){
        subutaiSteps.clickOnButtonStart();
    }

    @When("the user select environment button on console")
    public void user_select_console_environment(){
        subutaiSteps.selectEnvironmentOnConsole();
    }

    @When("the user click on the menu item: User Identity")
    public void click_on_user_identity(){
        subutaiSteps.clickOnMenuItemUserIdentity();
    }

    @When("the user click on the menu item: Tokens")
    public void click_on_menu_items_tokens(){
        subutaiSteps.clickOnMenuItemsTokens();
    }

    @When("the user click on the button: Show Token")
    public void click_buton_show_token(){
        subutaiSteps.clickOnButtonShowToken();
    }

    @When("the user get Peer ID")
    public void user_get_peer_id(){
        subutaiSteps.getPeerID();
    }

    @When("the user get Environment ID")
    public void user_get_environment_data(){
        subutaiSteps.getEnvironmentData();
    }

    @When("the user get master IP")
    public void user_get_master_ip(){
        subutaiSteps.getContainerIp();
    }
}