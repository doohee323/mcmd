package com.tz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Map;

/**
 * </pre>
 * 
 * @version 1.0
 */
public class CmdUtil {

    /**
     * <pre>
     * cmd(HashMap input)
     * </pre>
     * 
     * @param input
     * @throws Exception
     */
    public static void cmd(Map<String, String> input) throws Exception {
        String command = input.get("command").toString();
        System.out.println("shell command:" + command);

        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(command);
        } catch (Exception e) {
            System.out.println("shell return1 :" + e.getMessage());
            throw new Exception("shell command error 1!!!:" + command);
        }
    }

    /**
     * <pre>
     * </pre>
     * 
     * @param command
     *            : commmand
     * @return
     * @throws Exception
     */
    public static StringBuffer cmd(String command) throws Exception {
        System.out.println("shell command:" + command);

        StringBuffer strReturn = new StringBuffer();
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        try {
            ps = rt.exec(command);
            ps.waitFor();
        } catch (Exception e) {
            System.out.println("shell return1 :" + e.getMessage());
            throw new Exception("shell command error 1!!!:" + command);
        }

        if (ps.exitValue() == 0) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new SequenceInputStream(ps.getInputStream(), ps.getErrorStream())));
            try {
                String readLine = null;
                while ((readLine = br.readLine()) != null) {
                    strReturn.append(readLine).append("\n");
                }
            } catch (IOException e) {
                System.out.println("shell return2 :" + e.getMessage());
                throw new Exception("shell command error 2!!!:" + command);
            }
            System.out.println("shell return :" + strReturn);
        }
        return strReturn;
    }

}
