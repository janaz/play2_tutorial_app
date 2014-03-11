package com.neutrino;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.*;

public class ShellScriptRun {


    int iExitValue;
    String sCommandString;
    String outputValue = null;
    public void runScript(String command) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        sCommandString = command;
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);//kills after 60 sec
        oDefaultExecutor.setWatchdog(watchdog);
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

        oDefaultExecutor.setStreamHandler(streamHandler);
        try {
            iExitValue = oDefaultExecutor.execute(oCmdLine);
            outputValue = outputStream.toString();
        } catch (ExecuteException e) {
            // TODO Auto-generated catch block
            System.err.println("Execution failed.");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("permission denied.");
            e.printStackTrace();
        }
        System.out.println(outputStream);
    }

    public static void main(String args[]) {
        ShellScriptRun testScript = new ShellScriptRun();
        testScript.runScript("ls -l");
    }

}
