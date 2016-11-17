# Multiple command on ssh / telnet / shell

# Usage
```
	1. define multiple commands in json file
	2. build 
		-. install
			- glide
				cf. https://github.com/Masterminds/glide
			- ~/mcmd-go> glide up
	3. run 
	   
```

# Configuration file
You can define commands which you want to run sequentially.

```
	ex) /mcmd/mcmd-go/etc/mcmd.json
	
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

# Use in Golang
```
	
```
