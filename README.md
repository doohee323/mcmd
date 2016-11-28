# Multiple command on ssh / telnet / shell

## Build
```
	1. define multiple commands in json file
		/mcmd-java/src/main/resources/mcmd.json
		
	2. build 
		mvn clean compile package
		
		target/mcmd-java-1.0.0-jar-with-dependencies.jar
		target/mcmd-java_1.0.0_all.deb
		
		// install debian
		sudo dpkg -i mcmd-java_1.0.0_all.deb
```

## Run
```
	1. run with jar 
	   java -jar mcmd-java-1.0.0-jar-with-dependencies.jar 
	   	-p "mcmd1/work1;mcmd1/work2" 		// required, run_path defined in json file
	   	-m "filename=data;filename2=data2" 	// optional, mapping variables for replacement
	   	-c "mcmd.json" 						// optional, configuration file, default: /mcmd-java/src/main/resources/mcmd.json
	   	-l "logback.xml"					// optional, logback file, default: /mcmd-java/src/main/resources/logback.xml
	   	
	   	-j {"kali_aws": {					// instead of using json file(mcmd.json), can define commands like this,
		    "hostInfo": {
		      "host": "54.183.204.226",
		      "port": 22,
		      "username": "ec2-user",
		      "keyfile": "/Users/dhong/.ssh/topzone_ca1.pem"
		    },
		    "work1": {
		      "type": "ssh",
		      "commands": [
		        "cd /opt/discover",
		        "sudo bash discover.sh",
		        "_WAIT 'Choice:'",
		        "1",
		        "_WAIT 'Company:'",
		        "Topzone",
		        "_WAIT 'Domain:'",
		        "topzone.biz",
		        "_WAIT 'Scan complete.'",
		        "_FINISH"
		      ]
		    }
		  }
		}
```
	   
```
	2. run after installing debian file (mcmd-java_1.0.0_all.deb)
	   /opt/mcmd-java/bin: executable files
	   /etc/mcmd-java: configuratino files
	   /var/log/mcmd-java: log files
	   
	   cd /opt/mcmd-java/bin/
	   
	   sudo java -jar mcmd.jar -l "/etc/mcmd-java/logback.xml" -c "/etc/mcmd-java/mcmd.json" -p "kali_aws/work3" -m "company=Topzone;domain=topzone.biz;mysql_host=localhost;user=root;password=1;schema=discover;run_date=20161118"
	   		
	   sudo java -jar mcmd.jar 
	   		-p "kali_aws/work1;kali_aws/work2;kali_aws/work3" 
	   		-m "company=Fortinet;domain=fortinet.com;mysql_host=localhost;user=root;password=1;schema=discover;run_date=20161118" 
	   		-c "/etc/mcmd-java/mcmd.json" 
	   		-l "/etc/mcmd-java/logback.xml"	   		
	   		
	   sudo java -jar mcmd.jar 
	   		-p "kali_aws/work1;kali_aws/work2;kali_aws/work3" 
	   		-m "company=Google;domain=google.com;mysql_host=localhost;user=root;password=1;schema=discover;run_date=20161118" 
	   		-c "/etc/mcmd-java/mcmd.json" 
	   		-l "/etc/mcmd-java/logback.xml"	   		
```

```
	3. Use in Java
		// configuration file
	    Mcmd mcmd = new Mcmd("mcmd.json");	
	    
	    // replacement string with other string in command 
	    Map<String, Object> var = new HashMap<String, Object>();
	    var.put("filename", "aaa");
	    			
	    // json path
	    mcmd.exec("mcmd1/work1", var); 
```

## Configuration file
	You can define commands which you want to run sequentially.

```
	ex) /mcmd-java/src/main/resources/mcmd.json
	
	{
	"mcmd1": {		// work group
		"hostInfo": {
			"host": "topzone.biz",
			"port": 22,
			"username": "ubuntu",
			"keyfile": "/home/ubuntu/.ssh/topzone_ca1.pem"
		},
		"work1": {	// work id
			"commands": [
				"cd /home/ubuntu",
				"rm -Rf {{filename}}.tar_bak",
				"_SLEEP 10",					// wait 10 seconds
				"tar cvf {{filename}}.tar *",
				"_WAIT ':~$'",					// wait until getting ":~$" in stdout
				"mv {{filename}}.tar {{filename}}.tar_bak",
				"_FINISH"	// send signal finish work
			]
		},
		"work2": {
			"type": "shell",
			"commands": [
				"scp -i {{keyfile}} {{username}}@{{host}}:/vagrant/{{filename}}.tar_bak ."
			]
		}
	}
	
	cf) Predefined variables
		_WAIT: wait for finishing previous command with expected string, ex) "_WAIT '/root/data$'" -> waiting for '/root/data$'
		_FINISH: finish a running work under a work group
		_SLEEP: n second waiting, ex) _SLEEP 10 -> 10 seconds waiting
		_REPLACE: replace a file contents like sed, ex) "_REPLACE \"aaa.sql\" \"bbb\" \"ccc\" "		-> replace all "bbb" string in aaa.sql with "ccc"
		_PREFIX: insert string in front of a file, ex) "_PREFIX \"aaa.sql\" \"use test;\" "  -> insert "use test;" in front of aaa.sql
}
```


