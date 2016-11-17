/**
 */

package main

import (
	"bufio"
	"fmt"
	"golang.org/x/crypto/ssh"
	"golang.org/x/crypto/ssh/agent"
	"io"
	"io/ioutil"
	"net"
	"os"
	"path/filepath"
//	"strconv"
//	s "strings"
	"time"
)

type MakeConfig struct {
	User        string
	Server      string
	Key         string
	Port        string
	Password    string
	Testtimeout int
}

func PublicKeyFile(file string) (ssh.AuthMethod, error) {
	buffer, err := ioutil.ReadFile(file)
	if err != nil {
		return nil, err
	}

	key, err := ssh.ParsePrivateKey(buffer)
	if err != nil {
		return nil, err
	}
	return ssh.PublicKeys(key), nil
}

func (sshConf *MakeConfig) connect() (*ssh.Session, error) {
	auths := []ssh.AuthMethod{}

	if sshConf.Password != "" {
		auths = append(auths, ssh.Password(sshConf.Password))
	}

	if sshAgent, err := net.Dial("unix", os.Getenv("SSH_AUTH_SOCK")); err == nil {
		auths = append(auths, ssh.PublicKeysCallback(agent.NewClient(sshAgent).Signers))
		defer sshAgent.Close()
	}

	publicKey, err := PublicKeyFile(sshConf.Key)
	if err != nil {
		return nil, err
	}

	testtimeout := time.Duration(sshConf.Testtimeout) * time.Second
	config := &ssh.ClientConfig{
		User: sshConf.User,
		Auth: []ssh.AuthMethod{
			publicKey,
		},
		Timeout: testtimeout,
	}

	client, err := ssh.Dial("tcp", sshConf.Server+":"+sshConf.Port, config)
	if err != nil {
		return nil, err
	}

	session, err := client.NewSession()
	if err != nil {
		return nil, err
	}

	return session, nil
}

func (sshConf *MakeConfig) Shell(command string) (output chan string, done chan bool, err error) {
	session, err := sshConf.connect()
	if err != nil {
		return output, done, err
	}
	outReader, err := session.StdoutPipe()
	if err != nil {
		return output, done, err
	}
	errReader, err := session.StderrPipe()
	if err != nil {
		return output, done, err
	}
	outputReader := io.MultiReader(outReader, errReader)
	err = session.Start(command)
	scanner := bufio.NewScanner(outputReader)
	outputChan := make(chan string)
	done = make(chan bool)
	go func(scanner *bufio.Scanner, out chan string, done chan bool) {
		defer close(outputChan)
		defer close(done)
		for scanner.Scan() {
			outputChan <- scanner.Text()
		}
		done <- true
		session.Close()
	}(scanner, outputChan, done)
	return outputChan, done, err
}

func (sshConf *MakeConfig) Scp(sourceFile string) error {
	session, err := sshConf.connect()

	if err != nil {
		return err
	}
	defer session.Close()

	targetFile := filepath.Base(sourceFile)

	src, srcErr := os.Open(sourceFile)

	if srcErr != nil {
		return srcErr
	}

	srcStat, statErr := src.Stat()

	if statErr != nil {
		return statErr
	}

	go func() {
		w, _ := session.StdinPipe()
		//		fmt.Fprintln(w, "C0644", srcStat.Size(), targetFile)
		if srcStat.Size() > 0 {
			io.Copy(w, src)
			//			fmt.Fprint(w, "\x00")
			w.Close()
		} else {
			//			fmt.Fprint(w, "\x00")
			w.Close()
		}
	}()

	if err := session.Run(fmt.Sprintf("scp -t %s", targetFile)); err != nil {
		return err
	}

	return nil
}

