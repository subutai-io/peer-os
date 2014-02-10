package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final Logger log = Logger.getLogger(ModuleComponent.class.getName());
    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;

        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public Layout getLayout() {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth(400, Sizeable.UNITS_PIXELS);
        layout.setHeight(400, Sizeable.UNITS_PIXELS);
        //layout.setStyle("border: 1px solid red;");
        //layout.setDebugId("id123");

        //layout.addComponent(UIUtil.getButton("Check Status", 120), "left: 30px; top: 50px;");

        Button button = new Button("Test");
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        //file:/opt/mgmt-server-karaf-0.0.1-SNAPSHOT/system/kiskis-server/monitor-module-0.0.1-SNAPSHOT.jar

        final URL urlToClass = this.getClass().getProtectionDomain().getCodeSource().getLocation();

        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        //ClazzL = new URLClassLoader(new URL[]{new File("/home/grant/plugins/MenuPlugin.jar").toURL()}, currentThreadClassLoader);
        final URLClassLoader cl = new URLClassLoader(new URL[]{ urlToClass }, currentThreadClassLoader);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                log.info("button click");

                //String script = "<script type=\"text/javascript\">console.log('hello from js');</script>";
//                String script = "var headID = document.getElementsByTagName(\"head\")[0]; console.log(headId);";

                String script = ""
                + " var s = document.createElement('script'); "
                        + " s.type = 'text/javascript'; "
                        + " var code = 'function hello(){console.log(123);}'; "
                        + " try { "
                        + " s.appendChild(document.createTextNode(code)); "
                        + " document.body.appendChild(s); hello();"   // <-
                        + " } catch (e) { "
                        + " s.text = code; "
                        + " document.body.appendChild(s); console.log(2);"
                        + " } ";

                //String script = "var a = 1; console.log(a);";

                //getWindow().executeJavaScript(script);

                //getWindow().executeJavaScript("console.log(1);");
                //getWindow().executeJavaScript("console.log( document.getElementById('subdiv') );");

                log.info("urlToClass: " + urlToClass);

                try {
                    //InputStream is = cl.getResourceAsStream("org/safehaus/kiskis/mgmt/server/ui/modules/monitor/Monitor.class");
                    InputStream is = cl.getResourceAsStream("js/text.js");
                    log.info("is: " + is);
                    log.info("size: " + is.available());

                    String script2 = streamToString(is);

                    //log.info("str: " + streamToString(is));
                    //log.info("script2: " + script2);

                    getWindow().executeJavaScript(script2);

                    is.close();

                } catch (Exception e) {
                    log.info("error: " + e);
                }

            }
        });

        layout.addComponent(button, "left: 30px; top: 50px;");

        AbsoluteLayout layout2 = new AbsoluteLayout();
        layout2.setWidth(100, Sizeable.UNITS_PIXELS);
        layout2.setHeight(200, Sizeable.UNITS_PIXELS);
        layout2.setDebugId("subdiv");

        layout.addComponent(layout2, "left: 100px; top: 10px;");

        return layout;
    }

    static String streamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    static String streamToString_(InputStream in) throws IOException {

        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb=new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();

        while(read != null) {
            sb.append(read);
            read = br.readLine();
        }

        return sb.toString();
    }

    @Override
    public void onCommand(Response response) {
        //CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return moduleName;
    }

}