package com.tz.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.nio.file.Paths;
import java.util.List;
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

    public int maxWait = 40;
    public int intervalBtw = 500;
    public int intervalWait = 2000;
    public String _CurDir = null;

    public CmdUtil() {
        _CurDir = Paths.get("").toAbsolutePath().toString();
    }

    /**
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

    /**
     */
    public StringBuffer cmd(List<String> commands) throws Exception {
        StringBuffer result = new StringBuffer();
        for (String command : commands) {
            if (!command.equals("")) {
                if (command.startsWith("_SLEEP ")) {
                    String strTime = command.substring("_SLEEP ".length(), command.length()).trim();
                    strTime = strTime.replace(" ", "");
                    Thread.sleep(Integer.parseInt(strTime) * intervalBtw);
                } else if (command.startsWith("_REPLACE ")) {
                    command = command.replace("\"\\\"", "\"`");
                    String strArry[] = command.split("\"");
                    String fileNm = strArry[1].replaceAll("^\\s+", "");
                    String strSearch = strArry[3];
                    String strReplace = strArry[5];
                    if (strSearch.equals("`")) {
                        strSearch = "\"";
                    }
                    replaceAll(fileNm, strSearch, strReplace);
                } else if (command.startsWith("_PREFIX ")) {
                    String strArry[] = command.split("\"");
                    String fileNm = strArry[1].replaceAll("^\\s+", "");
                    String strSearch = strArry[3];
                    prefix(fileNm, strSearch);
                } else {
                    Runtime rt = Runtime.getRuntime();
                    Process pc = null;
                    try {
                        log.debug(command);
                        pc = rt.exec(command);
                        int processComplete = pc.waitFor();
                        if (processComplete == 1) {
                            String[] cmdline = { "sh", "-c", command };
                            pc = rt.exec(cmdline);
                            processComplete = pc.waitFor();
                            if (processComplete == 1) {
                                log.error("shell return error! :");
                                throw new Exception("shell command error 1!!!:" + command);
                            }
                        }
                    } catch (Exception e) {
                        log.error("shell return1 :" + e.getMessage());
                    }
                    StringBuffer strReturn = new StringBuffer();
                    if (pc.exitValue() == 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                new SequenceInputStream(pc.getInputStream(), pc.getErrorStream())));
                        try {
                            String readLine = null;
                            while ((readLine = br.readLine()) != null) {
                                strReturn.append(readLine).append("\n");
                            }
                        } catch (IOException e) {
                            log.error("shell return2 :" + e.getMessage());
                            throw new Exception("shell command error 2!!!:" + command);
                        }
                        log.debug("" + strReturn);
                    }
                    result.append(strReturn);
                    Thread.sleep(intervalBtw);
                }
            }
        }
        return result;
    }

    public static void replaceAll(String fileNm, String strSearch, String strReplace) throws Exception {
        String filePath = getAbsolutePath(fileNm);
        StringBuffer sb = getFromFile(filePath);
        StringBuffer strBuf = replace(sb.toString(), strSearch, strReplace);
        write(filePath, strBuf);
    }

    public static void prefix(String fileNm, String strAppend) throws Exception {
        String filePath = getAbsolutePath(fileNm);
        StringBuffer sb = getFromFile(filePath);
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(strAppend);
        strBuf.append(sb);
        write(filePath, strBuf);
    }

    public static String getAbsolutePath(String fileNm) throws Exception {
        try {
            if (new File(fileNm).exists()) {
                return new File(fileNm).getAbsolutePath();
            } else {
                fileNm = ConfigUtil.class.getClassLoader().getResource(fileNm).getPath();
                return new File(fileNm).getAbsolutePath();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            log.error("error: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    public static StringBuffer replace(String strTarget, String strSearch, String strReplace) throws Exception {
        StringBuffer strBuf = new StringBuffer();
        try {
            String strCheck = new String(strTarget);
            while (strCheck.length() != 0) {
                int begin = strCheck.indexOf(strSearch);
                if (begin == -1) {
                    strBuf.append(strCheck);
                    break;
                } else {
                    int end = begin + strSearch.length();
                    strBuf.append(strCheck.substring(0, begin));
                    strBuf.append(strReplace);
                    strCheck = strCheck.substring(end);
                }
            }
        } catch (Exception e) {
            throw new Exception("[StringUtil][replace]" + e.getMessage(), e);
        }
        return strBuf;
    }

    /**
     */
    public static StringBuffer getFromFile(String fileName) throws IOException {
        return getFromFile(fileName, null);
    }

    /**
     */
    public static StringBuffer getFromFile(String fileName, String strChar) throws IOException {
        if (strChar == null || strChar.equals(""))
            strChar = null;

        StringBuffer sb = new StringBuffer(1000);
        InputStreamReader is = null;
        BufferedReader in = null;
        String lineSep = System.getProperty("line.separator");

        try {
            File f = new File(fileName);
            if (f.exists()) {
                if (strChar != null)
                    is = new InputStreamReader(new FileInputStream(f), strChar);
                else
                    is = new InputStreamReader(new FileInputStream(f));
                in = new BufferedReader(is);
                String str = "";

                int readed = 0;
                while ((str = in.readLine()) != null) {
                    if (strChar != null)
                        readed += (str.getBytes(strChar).length);
                    else
                        readed += (str.getBytes().length);
                    sb.append(str + lineSep);
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            if (is != null)
                is.close();
            if (in != null)
                in.close();
        }
        return sb;
    }

    /**
     */
    public static void write(String aFileNm, StringBuffer sb) throws IOException {
        write(aFileNm, sb, "utf-8", false);
    }

    /**
     */
    public static void write(String aFileNm, StringBuffer sb, String aCharSet, boolean bChk) throws IOException {
        OutputStreamWriter os = null;
        FileOutputStream fos = null;
        PrintWriter outp = null;
        try {
            String path = "";
            if (aFileNm.indexOf("/") > -1)
                path = aFileNm.substring(0, aFileNm.lastIndexOf("/"));
            if (!new File(path).exists())
                new File(path).mkdirs();
            // bChk : true -> append
            fos = new FileOutputStream(aFileNm, bChk);
            if (aCharSet == null) {
                os = new OutputStreamWriter(fos);
            } else {
                os = new OutputStreamWriter(fos, aCharSet);
            }
            outp = new PrintWriter(os, true);
            outp.println(sb);
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            if (os != null)
                os.close();
            if (fos != null)
                fos.close();
            if (outp != null)
                outp.close();
        }
    }
}
