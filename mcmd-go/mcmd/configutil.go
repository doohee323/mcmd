/**
 */

package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	s "strings"
)

type Config struct {
	Node map[string]interface{} `json:"mcmd1"`
}

func fromJson(file string) (Config, error) {
	buffer, err := ioutil.ReadFile(file)
	if err != nil {
		return Config{}, err
	}

	if err != nil {
		fmt.Print("Error:", err)
	}

	var conf Config
	err = json.Unmarshal(buffer, &conf)
	if err != nil {
		fmt.Print("Error:", err)
	}
	//fmt.Println(conf)

	//	hostInfo := getMap(conf.Node["hostInfo"])
	//	fmt.Println(" [========>] %v", hostInfo)
	//
	//	work1 := getMap(conf.Node["work1"])
	//	fmt.Println(" [========>] %v", work1)
	//	fmt.Println(" [========>] %v", work1["commands"])

	return conf, nil
}

func getMap(inter interface{}) map[string]string {
	m2 := make(map[string]string)
	if rec, ok := inter.(map[string]interface{}); ok {
		for key, val := range rec {
			//			fmt.Println(" [========>] %s = %s", key, val)
			m2[key] = fmt.Sprint(val)
		}
	}
	return m2
}

func (conf *Config) getNode(path string) map[string]string {
	return getMap(conf.Node[path])
}

func (conf *Config) getArry(path string) []string {
	arry := make([]string, 0)
	m2 := make(map[string]string)
	if s.Contains(path, "/") == true {
		spl := s.Split(path, "/")
		for i := range spl {
			if i == 0 {
				m2 = getMap(conf.Node[spl[i]])
			} else {
				if spl[i] == "commands" {
					m2[spl[i]] = m2[spl[i]][1 : len(m2[spl[i]])-1]
					commands := s.Split(m2[spl[i]], ";")
					arry = commands
					for j := range arry {
						if s.Contains(arry[j], "_WAIT") == true || s.Contains(arry[j], "_FINSIH") == true {
							arry[j] = "echo " + arry[j]
						} else if s.Contains(arry[j], "_SLEEP2") == true {
							strTime := arry[j][len("_SLEEP2 "):len(arry[j])]
							strTime = s.Replace(strTime, " ", "", 1)
							arry[j] = "sleep " + strTime
						}
					}

				}
			}
		}
	}
	return arry
}
