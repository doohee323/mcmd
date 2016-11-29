package com.tz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * <pre>
 * </pre>
 * 
 * @version 1.0
 */
public class TelnetUtil {

    /**
     */
    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class);

    public static String readUntil(BufferedReader buf, String pattern) {
        StringBuffer sb = new StringBuffer(1000);
        String output = null;
        try {
            while ((output = buf.readLine()) != null) {
                if (output.indexOf(pattern) > -1) {
                    output.trim();
                    sb.append(output + "\r\n");
                    return sb.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String cmd(JsonObject input) throws Exception {
        String strRslt = "";
        try {
            String authUseYn = input.get("authUseYn").getAsString();
            String userId = input.get("userId").getAsString();
            String password = input.get("password").getAsString();
            String _HOST = input.get("_HOST").getAsString();
            String _PORT = input.get("_PORT").getAsString();
            int nPort = 23;
            if (!_PORT.equals(""))
                nPort = Integer.parseInt(_PORT);
            String command = input.get("command").getAsString();

            TelnetClient tn = new TelnetClient();
            tn.connect(_HOST, nPort);

            BufferedReader reader = new BufferedReader(new InputStreamReader(tn.getInputStream()));
            Writer writer = new PrintWriter(new OutputStreamWriter(tn.getOutputStream()), true);

            if (!authUseYn.equals("N")) {
                readUntil(reader, "login:");
                writer.write(userId);
                readUntil(reader, "assword:");
                writer.write(password);
                readUntil(reader, ">");
            }
            writer.write(command);
            strRslt = readUntil(reader, ">");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return strRslt;
    }

    public static void main(String[] arg) {
        try {
            TelnetUtil util = new TelnetUtil();
            JsonObject input = new JsonObject();
            util.cmd(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
