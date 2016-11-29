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

    public int _MAXWAIT = 1000;
    public int _INTERVALBTW = 500;
    public int _INTERVALWAIT = 2000;

    public StringBuffer cmd(JsonObject _HOSTINFO, List<String> _COMMANDS) throws Exception {
        StringBuffer result = new StringBuffer();
        Channel channel = null;
        Session session = null;
        ByteArrayOutputStream out = null;
        PrintStream ps = null;
        PrintStream shellStream = null;
        try {
            String _USERNAME = _HOSTINFO.get("_USERNAME").getAsString();
            String _HOST = _HOSTINFO.get("_HOST").getAsString();
            String _KEYFILE = _HOSTINFO.get("_KEYFILE").getAsString();
            int _PORT = _HOSTINFO.get("_PORT").getAsInt();
            File privateKey = new File(_KEYFILE);

            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getAbsolutePath());
            session = jsch.getSession(_USERNAME, _HOST, _PORT);
            session.setDaemonThread(true);
            Properties _CONFIG = new Properties();
            _CONFIG.put("StrictHostKeyChecking", "no");
            session.setConfig(_CONFIG);
            session.connect();

            out = new ByteArrayOutputStream();
            ps = new PrintStream(out);

            channel = session.openChannel("shell");
            channel.setOutputStream(ps);
            // channel.setOutputStream(System.out);
            shellStream = new PrintStream(channel.getOutputStream());
            channel.connect();

            int curSize = 0;
            for (String command : _COMMANDS) {
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
                            Thread.sleep(Integer.parseInt(strTime) * _INTERVALBTW);
                        } else {
                            if(command.contains("_CLOSE")) {
                                command = "echo " + command;
                            }
                            shellStream.println(command);
                            shellStream.flush();
                            Thread.sleep(_INTERVALBTW);
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
            Thread.sleep(_INTERVALWAIT);
            result = new StringBuffer(out.toString());
            if (nCnt > _MAXWAIT) {
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

    public void cmd(JsonObject _HOSTINFO, String command) throws Exception {
        String _USERNAME = _HOSTINFO.get("_USERNAME").getAsString();
        String _HOST = _HOSTINFO.get("_HOST").getAsString();
        String _KEYFILE = _HOSTINFO.get("_KEYFILE").getAsString();
        int _PORT = _HOSTINFO.get("_PORT").getAsInt();
        File privateKey = new File(_KEYFILE);
        FileOutputStream fos = null;
        Channel channel = null;
        Session session = null;

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey.getAbsolutePath());
            session = jsch.getSession(_USERNAME, _HOST, _PORT);
            session.setDaemonThread(true);
            Properties _CONFIG = new Properties();
            _CONFIG.put("StrictHostKeyChecking", "no");
            session.setConfig(_CONFIG);
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
                    Thread.sleep(_INTERVALBTW);
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
        String _USERNAME = "ec2-user";
        String _HOST = "ec2-54-162-101-184.compute-1.amazonaws.com";
        String _KEYFILE = "/securedKeys/awskey1.pem";
        int _PORT = 22;

        JsonObject _HOSTINFO = new JsonObject();
        _HOSTINFO.addProperty("_USERNAME", _USERNAME);
        _HOSTINFO.addProperty("_HOST", _HOST);
        _HOSTINFO.addProperty("_KEYFILE", _KEYFILE);
        _HOSTINFO.addProperty("_PORT", _PORT);

        List<String> _COMMANDS = new ArrayList<String>();
        _COMMANDS.add("sudo su");
        _COMMANDS.add("ls -al");

        SshUtil util = new SshUtil();
        try {
            util.cmd(_HOSTINFO, _COMMANDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}