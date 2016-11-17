package com.tz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * </pre>
 * 
 * @version 1.0
 */
public class CmdUtil {

    static final Logger log = LoggerFactory.getLogger(CmdUtil.class);

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
        log.debug("shell command:" + command);

        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(command);
        } catch (Exception e) {
            log.error("shell return1 :" + e.getMessage());
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
        log.debug("shell command:" + command);

        StringBuffer strReturn = new StringBuffer();
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        try {
            ps = rt.exec(command);
            ps.waitFor();
        } catch (Exception e) {
            log.error("shell return1 :" + e.getMessage());
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
                log.error("shell return2 :" + e.getMessage());
                throw new Exception("shell command error 2!!!:" + command);
            }
            log.debug("shell return :" + strReturn);
        }
        return strReturn;
    }

}
