package com.tz.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshUtil {

    static final Logger log = LoggerFactory.getLogger(SshUtil.class);

    public int maxWait = 1000;
    public int intervalBtw = 500;
    public int intervalWait = 2000;

    public StringBuffer cmd(JsonObject hostInfo, List<String> commands) throws Exception {
        StringBuffer result = new StringBuffer();
        Channel channel = null;
        Session session = null;
        ByteArrayOutputStream out = null;
        PrintStream ps = null;
        PrintStream shellStream = null;
        try {
            String username = hostInfo.get("username").getAsString();
            String host = hostInfo.get("host").getAsString();
            String keyfile = hostInfo.get("keyfile").getAsString();
            int port = hostInfo.get("port").getAsInt();
            File privateKey = new File(keyfile);

            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getAbsolutePath());
            session = jsch.getSession(username, host, port);
            session.setDaemonThread(true);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            out = new ByteArrayOutputStream();
            ps = new PrintStream(out);

            channel = session.openChannel("shell");
            channel.setOutputStream(ps);
            // channel.setOutputStream(System.out);
            shellStream = new PrintStream(channel.getOutputStream());
            channel.connect();

            int curSize = 0;
            for (String command : commands) {
                if (!command.equals("")) {
                    result = new StringBuffer(out.toString());
                    if (command.indexOf("_WAIT ") > -1) {
                        String offset = command
                                .substring(command.indexOf("_WAIT ") + "_WAIT ".length(), command.length())
                                .replaceAll("'", "");
                        curSize = wait(out, result, offset, curSize);
                    } else {
                        if (command.startsWith("_SLEEP ")) {
                            String strTime = command.substring("_SLEEP ".length(), command.length()).trim();
                            strTime = strTime.replace(" ", "");
                            Thread.sleep(Integer.parseInt(strTime) * intervalBtw);
                        } else {
                            if(command.contains("_CLOSE")) {
                                command = "echo " + command;
                            }
                            shellStream.println(command);
                            shellStream.flush();
                            Thread.sleep(intervalBtw);
                        }
                        String showStr = result.substring(curSize, result.length()).trim();
                        if (!showStr.equals("")) {
                            log.debug(showStr);
                        }
                        curSize = result.length();
                    }
                }
            }
            curSize = wait(out, result, "_CLOSE", curSize);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR: Connecting via shell to " + e.getMessage());
            throw new Exception(e.getMessage());
        } finally {
            channel.disconnect();
            session.disconnect();
            try {
                out.close();
                ps.close();
                shellStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
                throw new Exception(e.getMessage());
            }
        }
        return new StringBuffer(result);
    }

    public int wait(ByteArrayOutputStream out, StringBuffer result, String offset, int curSize)
            throws InterruptedException {
        boolean bGo = true;
        int nCnt = 0;
        while (bGo) {
            Thread.sleep(intervalWait);
            result = new StringBuffer(out.toString());
            if (nCnt > maxWait) {
                bGo = false;
            }
            String showStr = result.substring(curSize, result.length()).trim();
            if (!showStr.equals("")) {
                log.debug(showStr);
            }
            if (showStr.indexOf(offset) > -1) {
                log.debug("// command finished with :" + offset);
                bGo = false;
            }
            curSize = result.length();
            nCnt++;
        }
        return curSize;
    }

    public void cmd(JsonObject hostInfo, String command) throws Exception {
        String username = hostInfo.get("username").getAsString();
        String host = hostInfo.get("host").getAsString();
        String keyfile = hostInfo.get("keyfile").getAsString();
        int port = hostInfo.get("port").getAsInt();
        File privateKey = new File(keyfile);
        FileOutputStream fos = null;
        Channel channel = null;
        Session session = null;

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getAbsolutePath());
            session = jsch.getSession(username, host, port);
            session.setDaemonThread(true);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("exec");
            // Channel channel=session.openChannel("shell"); exec
            fos = new FileOutputStream("/data3/logs/a.log", true);
            channel.setOutputStream(fos);

            ((ChannelExec) channel).setCommand(command);

            // X Forwarding
            // channel.setXForwarding(true);

            // channel.setInputStream(System.in);
            channel.setInputStream(null);

            // FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            // ((ChannelExec)channel).setErrStream(fos);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    log.debug(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0)
                        continue;
                    log.debug("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(intervalBtw);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    throw new Exception(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        } finally {
            try {
                channel.disconnect();
                session.disconnect();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        }
    }

    public static void main(String[] arg) {
        String username = "ec2-user";
        String host = "ec2-54-162-101-184.compute-1.amazonaws.com";
        String keyfile = "/securedKeys/awskey1.pem";
        int port = 22;

        JsonObject hostInfo = new JsonObject();
        hostInfo.addProperty("username", username);
        hostInfo.addProperty("host", host);
        hostInfo.addProperty("keyfile", keyfile);
        hostInfo.addProperty("port", port);

        List<String> commands = new ArrayList<String>();
        commands.add("sudo su");
        commands.add("ls -al");

        SshUtil util = new SshUtil();
        try {
            util.cmd(hostInfo, commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}