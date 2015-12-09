package od.jbehave;

import net.thucydides.core.annotations.Steps;
import od.steps.SubutaiSteps;
import org.jbehave.core.annotations.When;

import java.io.FileNotFoundException;

public class DefSubutaiStepsWhen {

    @Steps
    SubutaiSteps subutaiSteps;

    @When("the user clicks on the menu item: Monitoring")
    public void clicks_on_menu_item_monitoring(){
        subutaiSteps.clickOnMenuItemMonitoring();
    }

    @When("the user clicks on the menu item: Environment")
    public void clicks_on_link_environment(){
        subutaiSteps.clickOnMenuItemEnvironment();
    }

    @When("the user clicks on the menu item: Blueprint")
    public void clicks_on_link_blueprint(){
        subutaiSteps.clickOnMenuItemBlueprint();
    }

    @When("the user clicks on the menu item: Environments")
    public void clicks_on_link_environments(){
        subutaiSteps.clickOnMenuItemEnvironments();
    }

    @When("the user clicks on the menu item: Containers")
    public void clicks_on_link_Containers(){
        subutaiSteps.clickOnMenuItemContainers();
    }

    @When("the user clicks on the button: Create Blueprint")
    public void clicks_on_button_create_blueprint(){
        subutaiSteps.clickOnButtonCreateBlueprint();
    }

    @When("the user enters blueprint name: '$blueprintName'")
    public void enters_blueprint_name(String blueprintName){
        subutaiSteps.enterBlueprintName(blueprintName);
    }

    @When("the user enters node name: '$nodeName'")
    public void enters_node_name(String nodeName){
        subutaiSteps.enterNodeName(nodeName);
    }

    @When("the user selects template: '$template'")
    public void selects_template(String template){
        subutaiSteps.selectTemplate(template);
    }

    @When("the user enters number of containers: '$count'")
    public void enters_number_of_containers(String count){
        subutaiSteps.enterNumberOfContainers(count);
    }

    @When("the user enters SSH group ID: '$id'")
    public void enters_ssh_group_id(String id){
        subutaiSteps.enterSSHGroupID(id);
    }

    @When("the user enters host Group ID: '$id'")
    public void enters_host_group_id(String id){
        subutaiSteps.enterHostGroupID(id);
    }

    @When("the user selects quota size: '$quotaSize'")
    public void selects_quota_size(String quotaSize){
        subutaiSteps.selectQuotaSize(quotaSize);
    }

    @When("the user clicks on the button: Add to node list")
    public void clicks_button_add_to_node_list(){
        subutaiSteps.clickOnButtonAddToNodeList();
    }

    @When("the user clicks on the button: Create")
    public void clicks_button_create(){
        subutaiSteps.clickOnButtonCreate();
    }

    @When("the user clicks on the icon: Build")
    public void clicks_on_icon_build(){
        subutaiSteps.clickOnIconBuild();
    }

    @When("the user selects peer: One")
    public void selects_peer_one(){
        subutaiSteps.selectPeer(1);
    }

    @When("the user selects peer: Two")
    public void selects_peer_two(){
        subutaiSteps.selectPeer(2);
    }

    @When("the user selects Strategie: '$strategie'")
    public void selects_strategie(String strategie){
        subutaiSteps.selectStrategie(strategie);
    }

    @When("the user clicks on the button: Place")
    public void clicks_on_button_place(){
        subutaiSteps.clickOnButtonPlace();
    }

    @When("the user enters environment name: '$name'")
    public void enters_environment_name(String name){
        subutaiSteps.inputEnvironmentName(name);
    }

    @When("the user clicks on the link: Environment Build List")
    public void clicks_on_link_environment_build_list(){
        subutaiSteps.clickLinkBuildEnvironmentList();
    }

    @When("the user clicks on the button: Build")
    public void clicks_on_button_build(){
        subutaiSteps.clickOnButtonEnvironmentBuild();
    }

    @When("the user clicks on the button: OK")
    public void clicks_on_button_ok(){
        subutaiSteps.clickOnButtonOK();
    }

    @When("the user clicks on the icon: Grow")
    public void clicks_on_icon_grow(){
        subutaiSteps.clickOnIconGrow();
    }

    @When("the user selects environment: Local Environment")
    public void selects_environment(){
        subutaiSteps.selectEnvironment(1);
    }

    @When("the user clicks on the icon: Remove")
    public void clicks_on_icon_remove(){
        subutaiSteps.clickOnIconRemove();
    }

    @When("the user clicks on the button: Delete")
    public void clicks_on_button_delete(){
        subutaiSteps.clickOnButtonDeleteConfirm();
    }

    @When("the user clicks on the icon: Destroy")
    public void clicks_on_icon_destroy(){
        subutaiSteps.clickOnIconDestroy();
    }

    @When("the user clicks on the menu item: Peer Registration")
    public void clicks_menu_item_peer_registration() {
        subutaiSteps.waitABit(5000);
        subutaiSteps.clickOnMenuPeerRegistration();
    }

    @When("the user clicks on the link: Create Peer")
    public void clicks_on_link_create_peer(){
        subutaiSteps.clickOnLinkCreatePeer();
    }

    @When("the user enters peer ip: Second user")
    public void enters_peer_ip_second_user() throws FileNotFoundException {
        subutaiSteps.enterPeerIP();
    }

    @When("the user enters peer key phrase: '$phrase'")
    public void enters_peer_key_phrase(String phrase){
        subutaiSteps.enterPeerKeyPhrase(phrase);
    }

    @When("the user clicks on the button: Create for peer")
    public void clicks_on_button_create_for_peer(){
        subutaiSteps.clickOnButtonCreatePeer();
    }

    @When("the user clicks on the button: Approve")
    public void clicks_on_button_approve(){
        subutaiSteps.clickOnButtonApprove();
    }

    @When("the user enters approve key phrase: '$phrase'")
    public void enters_peer_approve_key_phrase(String phrase){
        subutaiSteps.enterPeerApproveKeyPhrase(phrase);
    }

    @When("the user clicks on the button popup: Approve")
    public void clicks_on_button_popup_approve(){
        subutaiSteps.clickOnButtonPopupApprove();
    }

    @When("the user clicks on the button: Unregister")
    public void clicks_on_button_unregister(){
        subutaiSteps.clickOnButtonUnregister();
    }

    @When("the user clicks on the button: Confirm Unregister")
    public void clicks_on_button_confirm_unregister(){
        subutaiSteps.clickOnButtonConfirmUnregister();
    }

    @When("the user clicks on the menu item: Console")
    public void clicks_menu_item(){
        subutaiSteps.clickLinkConsole();
    }

    @When("the user enters command: '$command'")
    public void enters_command(String command){
        subutaiSteps.enterCommand(command);
    }

    @When("the user selects peer executeConsoleCommand: Two")
    public void selects_peer_console(){
        subutaiSteps.selectPeerConsole(2);
    }

    @When("the user selects any available resource host from select menu")
    public void selects_resource_host_from_select_menu(){
        subutaiSteps.selectMenuResourceHost();
    }

    @When("the user selects management host from select menu")
    public void selects_menagement_host_from_select_menu(){
        subutaiSteps.selectMenuManagementHost();
    }

    @When("the user enters console command: '$command'")
    public void executes_console_command(String command){
        subutaiSteps.executeConsoleCommand(command);
    }

    @When("the user enters console command ping")
    public void pings_container(){
        subutaiSteps.executeConsoleCommand("ping " + subutaiSteps.containerIp + " -c 3");
    }

    @When("the user should waits")
    public void user_wait(){
        subutaiSteps.waitABit(20000);
    }

    @When("the user enters console command: subutai import hadoop")
    public void executes_console_command_subutai_import_hadoop(){
        subutaiSteps.executeConsoleCommand("subutai import hadoop");
        subutaiSteps.waitFunctionForSlowOperations(60000);
    }

    @When("the user enters console command: sudo lxc-info -Ssip -n for Container One")
    public void executes_console_command_container1(){
        subutaiSteps.executeConsoleCommand("sudo lxc-info -Ssip -n " + subutaiSteps.getContainerNameOne());
    }

    @When("the user enters console command: sudo lxc-info -Ssip -n for Container Two")
    public void executes_console_command_container2(){
        subutaiSteps.executeConsoleCommand("sudo lxc-info -Ssip -n " + subutaiSteps.getContainerNameTwo());
    }

    @When("the user enters console command: ps -ef and grep PID")
    public void executes_console_command_pid(){
        subutaiSteps.executeConsoleCommand("ps -ef | grep " + subutaiSteps.getPIDContainer());
    }

    //------------ 3023

    @When("the user clicks on console link")
    public void clicks_on_console_link(){
        subutaiSteps.clickOnLinkConsole();
    }

    @When("the user enters a command '$caommand' to Command field and press enter")
    public void enters_command_into_console(String command){
        subutaiSteps.seeRHCommandLine();
        subutaiSteps.clickOnConsole();
        subutaiSteps.inputCommandIntoConsole(command);
    }

    @When("the user clicks on link: environment on the console page")
    public void click_on_environment_link_on_console_page(){
        subutaiSteps.pageConsoleClickOnEnvironmentLink();
    }

    @When("the user selects '$environment' environment in select menu")
    public void selects_environment_in_select_menu(String environment){
        subutaiSteps.selectEnvironmentInSelectMenu(environment);
    }

    @When("the user selects container one in select menu")
    public void user_selects_container_one_in_select_menu(){
        subutaiSteps.selectContainerOneInSelectMenu();
    }

    @When("the user selects container two in select menu")
    public void user_selects_container_two_in_select_menu(){
        subutaiSteps.selectContainerTwoInSelectMenu();
    }

    @When("the user gets resource host ID and go pgp key url")
    public void user_gets_rh_id_and_go_to_pgp_url() throws FileNotFoundException {
        subutaiSteps.resourceHostPGPKey();
    }

    @When("the user gets container ID and go pgp key url")
    public void user_gets_container_id_and_go_to_pgp_url() throws FileNotFoundException {
        subutaiSteps.resourceHostPGPKey();
    }

    @When("the user clicks on the button: Environment")
    public void clicks_on_button_environment(){
        subutaiSteps.clickOnButtonEnvironmentOnConsole();
    }

    @When("the user selects current environment")
    public void selects_current_environment(){
        subutaiSteps.selectEnvironmentConsole();
    }

    @When("the user selects first container")
    public void selects_container(){
        subutaiSteps.selectContainer();
    }

    @When("the user clicks on the button: Peer")
    public void clicks_button_Peer(){
        subutaiSteps.clickButtonPeerConsole();
    }

    @When("the user selects again one resource host")
    public void selects_one_resource_host(){
        subutaiSteps.selectMenuResourceHostAgain();
    }


    @When("the user enters console command: ls -l /var/lib/apps/subutai/current/var/lib/lxc/ContainerName/rootfs")
    public void executes_console_command_container2_ls(){
        subutaiSteps.executeConsoleCommand("ls -l /var/lib/apps/subutai/current/var/lib/lxc/" + subutaiSteps.getContainerNameTwo() + "/rootfs/");
    }

    @When("the user runs bash script vagrant")
    public void runs_shell_script() throws FileNotFoundException {
        subutaiSteps.run_bash_script("src/test/resources/files/vagrant.sh");
    }

    @When("the user runs bash script aws")
    public void runs_shell_script_aws() throws FileNotFoundException {
        subutaiSteps.run_bash_script("src/test/resources/files/aws_run.sh");
    }

    @When("the user clicks on the button: Stop")
    public void clicks_button_stop(){
        subutaiSteps.clickOnButtonStop();
    }

    @When("the user clicks on the button: Start")
    public void clicks_button_start(){
        subutaiSteps.clickOnButtonStart();
    }

    @When("the user selects environment button on console")
    public void user_selects_console_environment(){
        subutaiSteps.selectEnvironmentOnConsole();
    }

    @When("the user clicks on the menu item: User Identity")
    public void clicks_on_user_identity(){
        subutaiSteps.clickOnMenuItemUserIdentity();
    }

    @When("the user clicks on the menu item: Tokens")
    public void clicks_on_menu_items_tokens(){
        subutaiSteps.clickOnMenuItemsTokens();
    }

    @When("the user clicks on the button: Show Token")
    public void clicks_buton_show_token(){
        subutaiSteps.clickOnButtonShowToken();
    }

    @When("the user gets Peer ID")
    public void user_gets_peer_id(){
        subutaiSteps.getPeerID();
    }

    @When("the user gets Environment ID")
    public void user_gets_environment_data(){
        subutaiSteps.getEnvironmentData();
    }

    @When("the user gets master IP")
    public void user_gets_master_ip(){
        subutaiSteps.getContainerIp();
    }

    //---------3117

    @When("the user clicks on the button: configure")
    public void clicks_on_button_configure(){
        subutaiSteps.clickOnButtonConfigure();
    }

    @When("the user inserts domain '$domain' in input field")
    public void inputs_domain(String domain){
        subutaiSteps.inputDomainInTheField(domain);
    }

    @When("the user selects domain strategy '$strategy'")
    public void selects_domain_strategy(String strategy){
        subutaiSteps.clickOnSelectMenuDomainStrtegy();
        subutaiSteps.selectDomainStrtegyRoundRobin(strategy);
        subutaiSteps.pressEnterOnDomainStrategy();
    }

    @When("the user adds PEM certificate from file")
    public void adds_PEM_certificate(){
        subutaiSteps.selectFileToUpload();
    }

    @When("the user presses on the button: save")
    public void presses_on_the_button_save(){
        subutaiSteps.domainClickOnTheButtonSave();
    }

    @When("the user clicks on the first container button: configure")
    public void clicks_on_the_first_container_button_configure(){
        subutaiSteps.clickOnFirstContainerButtonConfigure();
    }

    @When("the user clicks on the second container button: configure")
    public void clicks_on_the_second_container_button_configure(){
        subutaiSteps.clickOnSecondContainerButtonConfigure();
    }

    @When("the user clicks on the third container button: configure")
    public void clicks_on_the_third_container_button_configure(){
        subutaiSteps.clickOnThirdContainerButtonConfigure();
    }

    @When("the user clicks on ceckbox")
    public void clicks_on_checkbox(){
        subutaiSteps.clickOnContainerDomainCheckbox();
    }

    @When("the user clicks on the button: save")
    public void clicks_on_container_checkbow_button_save(){
        subutaiSteps.clickOnContainerDomainButtonSave();
    }

    @When("the user compares IP")
    public void compares_ip_container(){
        subutaiSteps.compareContainerIP();
    }

    @When("the user stops some container by pressing Stop button")
    public void stops_some_container(){
        subutaiSteps.clickOnContainerButtonStop();
    }

    @When("the user gets ip of stopped container")
    public void gets_ip_of_stopped_container(){
        subutaiSteps.getIPStoppedContainer();
    }

    @When("the user presses F5 several times")
    public void reloads_page(){
        subutaiSteps.reloadPage();
    }

    @When("the user gets ip of disabled container")
    public void gets_ip_of_disabled_container(){
        subutaiSteps.shouldGetIPDisabledContainer();
    }

    @When("the user clicks on the button: remove domain")
    public void clicks_on_button_remove_domain(){
        subutaiSteps.clickOnButtonRemoveDomain();
    }

    //-----3298
    @When("I do something")
    public void i_do_something(){
        subutaiSteps.clickOnSomeTubs();
    }

    @When("the user clicks on the menu item: User management")
    public void user_clicks_menu_item_user_management(){
        subutaiSteps.clickOnMenuItemUserManagement();
    }

    @When("the user clicks on the menu item: Roles management")
    public void user_clicks_menu_item_roles_management(){
        subutaiSteps.clickOnMenuItemRolesManagement();
    }

    @When("the user clicks on the menu item: Tracker")
    public void user_clicks_menu_item_tracker(){
        subutaiSteps.clickOnMenuItemTracker();
    }
}