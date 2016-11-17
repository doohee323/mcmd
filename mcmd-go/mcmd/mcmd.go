/**
 */

package main

import (
	iniflags "bitbucket.org/n0needt0/iniflags"
	"bitbucket.org/n0needt0/uppr"
	"flag"
	"fmt"
	"os"
	//	"strconv"
	//	s "strings"
	//	"time"
	//	"io/ioutil"
//	"bytes"
	"golang.org/x/crypto/ssh"
//	"io"
)

func main() {
	var log = uppr.GetLog()

	var (
		help = flag.Bool("help", false, "Show these options")
		//		host        = flag.String("host", "topzone.biz", "host IP or CNAME")
		//		port        = flag.Int("port", 22, "port")
		//		testtimeout = flag.Int("testtimeout", 540, "Timeout Sec to wait til cancel running test")
		logfile  = flag.String("logfile", "STDOUT", "Logfile destination. STDOUT | STDERR or file path")
		loglevel = flag.String("loglevel", "DEBUG", "Loglevel CRITICAL, ERROR, WARNING, NOTICE, INFO, DEBUG")
	)

	iniflags.Parse()

	//now fire up logging
	uppr.New(*logfile, *loglevel)

	if *help {
		flag.PrintDefaults()
		os.Exit(1)
	}

	conf, err := fromJson("/Users/dhong/Documents/workspace/go/src/mcmd-go/etc/mcmd.json")
	log.Error(err)
	log.Error(conf)

	hostInfo := conf.getNode("hostInfo")

	//	var sshConfig = &MakeConfig{
	//		Server: hostInfo["host"],
	//		Port:   hostInfo["port"],
	//		User:   hostInfo["username"],
	//		//	Password: "password",
	//		Key:         hostInfo["keyfile"],
	//		Testtimeout: *testtimeout,
	//	}
	//
	//	commands := conf.getArry("work1/commands")
	//	fmt.Println(commands)
	//
	//	for _, cmd := range commands {
	//		fmt.Println("==============cmd:" + cmd)
	//		if s.Contains(cmd, "_SLEEP") == true {
	//			strTime := cmd[len("_SLEEP "):len(cmd)]
	//			strTime = s.Replace(strTime, " ", "", 1)
	//			nTim, _ := strconv.Atoi(strTime)
	//			nDur := time.Second * 2
	//			time.Sleep(nDur)
	//		} else {
	//			channel, done, err := sshConfig.Shell(cmd)
	//			if err != nil {
	//				log.Fatal(err)
	//			}
	//			stillGoing := true
	//			output := ""
	//			for stillGoing {
	//				select {
	//				case <-done:
	//					stillGoing = false
	//				case line := <-channel:
	//					output += line
	//					log.Error(line)
	//				}
	//			}
	//		}
	//	}

	publicKey, err := PublicKeyFile(hostInfo["keyfile"])
	if err != nil {
		log.Error(err)
	}

	sshConfig := &ssh.ClientConfig{
		User: hostInfo["username"],
		Auth: []ssh.AuthMethod{
			publicKey,
		},
	}

	client := &SSHClient{
		Config: sshConfig,
		Host:   hostInfo["host"],
		Port:   22,
	}

	commands := conf.getArry("work1/commands")
	fmt.Println(commands)

	for _, cmd1 := range commands {
		fmt.Println("==============cmd:" + cmd1)

		cmd := &SSHCommand{
			Path:   cmd1,
			Env:    []string{"LC_DIR=/"},
			Stdin:  os.Stdin,
			Stdout: os.Stdout,
			Stderr: os.Stderr,
		}

		fmt.Printf("Running command: %s\n", cmd.Path)
		if err := client.RunCommand(cmd); err != nil {
			fmt.Fprintf(os.Stderr, "command run error: %s\n", err)
			os.Exit(1)
		}
	}

}
