package od.classes;

import net.serenitybdd.core.pages.PageObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExecuteShellCommand extends PageObject{

    public String executeCommand(String command) {

        StringBuilder output = new StringBuilder();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(output.toString());
        return output.toString();
    }

    public void executeConsoleCommand(String command){
        evaluateJavascript("function setCommand(value) {\n" +
                "var appElement = document.getElementsByClassName('b-terminal')[0];" +
                "var $scope = angular.element(appElement).scope();" +
                "$scope.$apply(function() {" +
                "$scope.commandLine = value;" +
                "$scope.execute();" +
                "});" +
                "} " +
                "setCommand('"+ command +"')");
        waitABit(10000);
    }
}
