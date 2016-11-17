package com.tz;

import java.util.HashMap;
import java.util.Map;

public class McmdTest {

    public static void main(String[] arg) throws Exception {
        Mcmd mcmd = new Mcmd("mcmd.json");
        Map<String, Object> var = new HashMap<String, Object>();
        var.put("filename", "aaa");
        String stdout = mcmd.exec("mcmd1/work1", var).toString();
        System.out.println(stdout);
    }

}
