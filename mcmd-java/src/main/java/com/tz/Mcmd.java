package com.tz;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tz.utils.CmdUtil;
import com.tz.utils.ConfigUtil;
import com.tz.utils.SshUtil;
import com.tz.utils.TelnetUtil;

public class Mcmd {

    static final Logger log = LoggerFactory.getLogger(Mcmd.class);

    private static Gson gson;
    private JsonObject conf = null;
    public String _CurDir = null;

    public Mcmd(String configFile) throws Exception {
        gson = new Gson();
        final Reader reader = ConfigUtil.getFileReader(configFile);
        try {
            _CurDir = Paths.get("").toAbsolutePath().toString();
            conf = gson.fromJson(reader, JsonObject.class);
        } catch (final Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            Runtime.getRuntime().exit(1);
            throw new Exception(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                    throw new Exception(e.getMessage());
                }
            }
        }
    }

    public StringBuffer exec(String arg, JsonObject _CONFIG, Map<String, Object> var) throws Exception {
        try {
            List<String> _COMMANDS = new ArrayList<String>();

            String path[] = arg.split("/");
            String mcmd1 = conf.get(path[0]).toString();
            JsonObject mcmd1Obj = gson.fromJson(mcmd1, JsonObject.class);

            JsonObject _HOSTINFO = gson.fromJson(mcmd1Obj.get("_HOSTINFO").toString(), JsonObject.class);

            String work = mcmd1Obj.get(path[1]).toString();
            JsonObject workObj = gson.fromJson(work, JsonObject.class);

            String _TYPE = workObj.get("_TYPE").getAsString();
            String commandstr1 = workObj.get("_COMMANDS").toString();
            JsonArray mCommand = gson.fromJson(commandstr1, JsonArray.class);

            Map<String, Object> __HOSTINFO = new HashMap<String, Object>();
            for (Map.Entry<String, JsonElement> entry : _HOSTINFO.entrySet()) {
                String key = entry.getKey();
                JsonElement v = entry.getValue();
                __HOSTINFO.put(key, v.getAsString());
            }
            __HOSTINFO.put("_CURDIR", _CurDir);
            for (int j = 0; j < mCommand.size(); j++) {
                String cmd = mCommand.get(j).getAsString();
                cmd = replaceVariables(cmd, __HOSTINFO);
                cmd = replaceVariables(cmd, var);
                _COMMANDS.add(cmd);
            }

            if (_TYPE.equals("ssh")) {
                SshUtil util = new SshUtil();
                if (_CONFIG.has("_MAXWAIT")) {
                    util._MAXWAIT = _CONFIG.get("_MAXWAIT").getAsInt();
                }
                if (_CONFIG.has("_INTERVALBTW")) {
                    util._INTERVALBTW = _CONFIG.get("_INTERVALBTW").getAsInt();
                }
                if (_CONFIG.has("_INTERVALWAIT")) {
                    util._INTERVALWAIT = _CONFIG.get("_INTERVALWAIT").getAsInt();
                }
                return util.cmd(_HOSTINFO, _COMMANDS);
            } else if (_TYPE.equals("telnet")) {
                TelnetUtil util = new TelnetUtil();
                util.cmd(null);
            } else { // shell
                CmdUtil util = new CmdUtil();
                return util.cmd(_COMMANDS);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
        return null;
    }

    public StringBuffer exec(String path, Map<String, Object> var) throws Exception {
        String configStr = conf.get("_CONFIG").toString();
        JsonObject _CONFIG = gson.fromJson(configStr, JsonObject.class);
        return exec(path, _CONFIG, var);
    }

    public String replaceVariables(String orgStr, Map<String, Object> param) {
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                if (key.equals("_CURDIR")) {
                    orgStr = orgStr.replace(key, value.toString());
                } else {
                    orgStr = orgStr.replace("{{" + key + "}}", value.toString());
                }
            } else {
                orgStr = orgStr.replace("\"{{" + key + "}}\"", value.toString());
            }
        }
        return orgStr;
    }

    /*
     * Mcmd -c \
     * "mcmd.json\" -l \"logback.xml\" -p \"kali_aws/work1;kali_aws/work2\" -m \"filename=data;filename2=data2\"
     */
    public static void main(String[] args) throws Exception {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Mcmd mcmd2 = new Mcmd("mcmd.json");
        // Map<String, Object> var2 = new HashMap<String, Object>();
        // var2.put("company", "Fortinet");
        // var2.put("domain", "fortinet.com");
        //
        // var2.put("user", "root");
        // var2.put("password", "1");
        // var2.put("mysql_host", "localhost");
        //
        // var2.put("schema", "discover");
        // var2.put("run_date", "20161118");
        //// mcmd2.exec("kali_aws/work1", var2);
        // mcmd2.exec("kali_aws/work2", var2);
        // mcmd2.exec("kali_aws/work3", var2);
        // // // mcmd2.exec("mcmd0/work1", var2);

        String configFile = "mcmd.json";
        String logConfigFile = "logback.xml";
        String commandPaths = null;
        String jsonStr = null;
        String mappings = null;

        boolean bJchk = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].trim().equals("-c")) {
                configFile = args[i + 1];
                i++;
            } else if (args[i].trim().equals("-l")) {
                logConfigFile = args[i + 1];
                i++;
            } else if (args[i].trim().equals("-j")) {
                bJchk = true;
                break;
            } else if (args[i].trim().equals("-p")) {
                commandPaths = args[i + 1];
                i++;
            } else if (args[i].trim().equals("-m")) {
                mappings = args[i + 1];
                i++;
            }
        }
        if (bJchk) {
            for (int i = 0; i < args.length; i++) {
                jsonStr += args[i];
            }
            jsonStr = java.net.URLDecoder.decode(jsonStr, "UTF-8");
            jsonStr = jsonStr.substring(jsonStr.indexOf("-j{") + 2, jsonStr.length() - 1);
            jsonStr = jsonStr.replace("\t", "");
            jsonStr = jsonStr.replace("\n\\", "\\");
        }

        System.out.println("////////////////// logConfigFile: " + logConfigFile);
        ConfigUtil.setLogbackConfig(logConfigFile);

        System.out.println("////////////////// configFile: " + configFile);
        Mcmd mcmd = new Mcmd(configFile);

        List<String> commandArry = new ArrayList<String>();
        Map<String, Object> var = new HashMap<String, Object>();
        if (jsonStr == null) {
            if (commandPaths == null) {
                log.error(
                        "No command-path! ex) Mcmd -c \"mcmd.json\" -l \"logback.xml\" -p \"kali_aws/work1;kali_aws/work2\" -m \"filename=data;filename2=data2\"");
                throw new Exception("No command-path!");
            } else {
                String cmds[] = commandPaths.split(";");
                for (String cmd : cmds) {
                    commandArry.add(cmd);
                }
            }
            if (mappings != null) {
                String mps[] = mappings.split(";");
                for (String mp : mps) {
                    String mpa[] = mp.split("=");
                    var.put(mpa[0], mpa[1]);
                }
            }

            for (String commandPah : commandArry) {
                mcmd.exec(commandPah, var).toString();
                // log.debug(stdout);
            }
        } else {
            try {
                JsonObject _CONFIG = gson.fromJson(jsonStr, JsonObject.class);
                String parent = null;
                JsonElement body = null;
                String path = "";
                for (Entry<String, JsonElement> e : _CONFIG.entrySet()) {
                    if (parent == null) {
                        parent = e.getKey();
                        body = _CONFIG.get(parent);
                    }
                }
                JsonObject config2 = gson.fromJson(body.toString(), JsonObject.class);
                for (Entry<String, JsonElement> e : config2.entrySet()) {
                    if (!e.getKey().equals("_HOSTINFO")) {
                        path = parent + "/" + e.getKey();
                        mcmd.exec(path, _CONFIG, var);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                throw new Exception(e.getMessage());
            }
        }

        stopWatch.stop();
        System.out.println("///////////////: " + stopWatch.getTime());

        // String stdout = mcmd.exec("mcmd1/work1", var).toString();
        // log.debug(stdout);
        // String stdout2 = mcmd.exec("mcmd1/work2", var).toString();
        // log.debug(stdout2);
    }

}
