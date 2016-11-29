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
		"_HOSTINFO": {
			"_HOST": "topzone.biz",
			"_PORT": 22,
			"_USERNAME": "ubuntu",
			"_KEYFILE": "/Users/dhong/.ssh/topzone_ca1.pem"
		},
		"work1": {
			"_COMMANDS": [
				"cd /home/ubuntu",
				"rm -Rf {{filename}}.tar_bak",
				"_SLEEP 10",					// wait 10 seconds
				"tar cvf {{filename}}.tar *",
				"_WAIT ':~$'",					// wait until getting ":~$" in stdout
				"mv {{filename}}.tar {{filename}}.tar_bak",
				"_CLOSE"	// send signal finish work
			]
		},
		"work2": {
			"_TYPE": "shell",
			"_COMMANDS": [
				"scp -i {{_KEYFILE}} {{_USERNAME}}@{{_HOST}}:/vagrant/{{filename}}.tar_bak ."
			]
		}
	}
}
```

# Use in Golang
```
	
```
