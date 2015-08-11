package io.subutai.core.key2.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by caveman on 21.07.2015.
 */
public class Commands2 {


    private static String commandOutPut = null;
    private static Process p = null;
    private static ProcessBuilder processBuilder=null;
    private static String commandBase = "subutai keymanager";

    private static final Logger LOG = LoggerFactory.getLogger(Commands2.class.getName());

    /**
     *
     * @param name
     * @param email
     * @return output of the commnad executed...
     */
    //public static String GenerateKeyCommand (String name, String email, Host host) {
    public static String generateKeyCommand (String name, String email) {

        p = null;
<<<<<<< HEAD
        processBuilder = new ProcessBuilder( "subutai keymanager", "generate", name, email );
=======
        processBuilder = new ProcessBuilder( "subutai keymanager generate", name, email );
>>>>>>> 1fe8dc4b77e11a89bfdf766fedca412a4fedef6f
        // processBuilder.command("subutai keymanager generate " + name + " " + email);
        PrintOnLog(processBuilder, "output");
        try {
            p = processBuilder.start();
            commandOutPut = p.getOutputStream().toString();
            p.getOutputStream().flush();
            p.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            PrintOnLog(processBuilder, "error");
            commandOutPut = p.getErrorStream().toString();
            p.destroy();
        }
        return commandOutPut;
    }

    public static String generateCertificateCommand ( String keyID ) {
        processBuilder = new ProcessBuilder( commandBase, "generate_cert", keyID );
        PrintOnLog(processBuilder, "output");
        try {
            p = processBuilder.start();
            commandOutPut = p.getOutputStream().toString();
            p.getOutputStream().flush();
            p.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            PrintOnLog(processBuilder, "error");
            commandOutPut = p.getErrorStream().toString();
            p.destroy();
        }
        return commandOutPut;
    }

    public static String generateSubKey (String keyId) {
        processBuilder = new ProcessBuilder( commandBase, "generate_subkey", keyId );
        PrintOnLog(processBuilder, "output");
        try {
            p = processBuilder.start();
            commandOutPut = p.getOutputStream().toString();
            p.getOutputStream().flush();
            p.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            PrintOnLog(processBuilder, "error");
            commandOutPut = p.getErrorStream().toString();
            p.destroy();
        }
        return commandOutPut;
    }

    public static String getCertificate (String keyId) {
        processBuilder = new ProcessBuilder(commandBase, "generate_cert", keyId);
        commandOutPut = executeCommand(processBuilder);
        return commandOutPut;
    }

    public static String listAllKeys () {
        processBuilder = new ProcessBuilder( commandBase, "list" );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String listKeyWithID (String keyId) {
        processBuilder = new ProcessBuilder( commandBase, "list", keyId );

        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String readKeyWithId (String keyId) {
        processBuilder = new ProcessBuilder( commandBase, "export", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String readKeySshKeyWithId (String keyId) {
        processBuilder = new ProcessBuilder( commandBase, "export", "-ssh", keyId );
        return executeCommand( processBuilder );
    }

    public static String signFile ( String keyId, String filePath ) {
        processBuilder = new ProcessBuilder( commandBase, "sign", keyId, filePath );
        commandOutPut = executeCommand(processBuilder);
        return commandOutPut;
    }

    public static String signKeyWithKey (String signerKeyId, String signedKeyId) {
        processBuilder = new ProcessBuilder( commandBase, "sign_key", signerKeyId, signedKeyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

<<<<<<< HEAD
    public static String signKeyWithKey2 ( String signer, String signee ) {
        processBuilder = new ProcessBuilder( commandBase, "sign_key2", signer, signee  );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

=======
>>>>>>> 1fe8dc4b77e11a89bfdf766fedca412a4fedef6f
    public static String sendKeyToPublicServer ( String keyId ) {
        processBuilder = new ProcessBuilder( commandBase, "send", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String generateRevocationKey ( String keyId ) {
        processBuilder = new ProcessBuilder( commandBase, "generate_revkey", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String deleteKey ( String keyId ) {
        processBuilder = new ProcessBuilder( commandBase, "delete", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String deleteSubKey ( String keyId ) {
        processBuilder = new ProcessBuilder( commandBase, "del_subkey", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String revokeKey ( String keyId ) {
        processBuilder = new ProcessBuilder( commandBase, "revkey", keyId );
        commandOutPut = executeCommand( processBuilder );
        return commandOutPut;
    }

    public static String revokeSubKey ( String keyId ) {
        return executeCommand( new ProcessBuilder( commandBase, "rev_subkey", keyId ) );
    }

    private static String executeCommand(ProcessBuilder localProcessBuilder) {
        PrintOnLog(localProcessBuilder, "output");
        try {
            p = localProcessBuilder.start();
            commandOutPut = p.getOutputStream().toString();
            p.getOutputStream().flush();
            p.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            PrintOnLog(localProcessBuilder, "error");
            commandOutPut = p.getErrorStream().toString();
            p.destroy();
        }
        return commandOutPut;
    }




    private static void PrintOnLog ( ProcessBuilder processBuilder, String message) {
        LOG.info( "[ " +message+" ]" + "process builder string: => " + processBuilder.toString() );
    }

}
