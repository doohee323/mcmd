# Multiple command on ssh / telnet / shell

# Usage
```
	1. define multiple commands in json file
	2. build 
		mvn clean compile package
	3. run 
	   java -jar mcmd-java-1.0.0-SNAPSHOT-jar-with-dependencies.jar -p "mcmd1/work1;mcmd1/work2" ( -m "filename=data;filename2=data2" -c "mcmd.json" -l "logback.xml" ) 
```

# Configuration file
You can define commands which you want to run sequentially.

```
	ex) /mcmd-java/src/main/resources/mcmd.json
	
	{
	"mcmd1": {
		"hostInfo": {
			"host": "topzone.biz",
			"port": 22,
			"username": "ubuntu",
			"keyfile": "/Users/dhong/.ssh/topzone_ca1.pem"
		},
		"work1": {
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
}
```

# Use in Java
```
	// configuration file
    Mcmd mcmd = new Mcmd("mcmd.json");	
    
    // replacement string with other string in command 
    Map<String, Object> var = new HashMap<String, Object>();
    var.put("filename", "aaa");
    			
    // json path
    String stdout = mcmd.exec("mcmd1/work1", var).toString(); 
```
