#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2005-2015 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
# init script for WSO2 Cloud Domain Mapper


# Check the application status
#
check_status() {
  # Running ps with some arguments to check if the PID exists

  s="$(ps -ef | grep 'java -jar cloud-domain-mapper-1.0.0.jar'| grep -v "grep" | awk '{print$2}')"

  # If something was returned by the ps command, this function returns the PID
  if [ $s ] ; then
    return $s
  fi

  # In any another case, return 0
  return 0
}

# Starts the application
start() {

  # At first checks if the application is already started calling the check_status function
  check_status

  pid=$?

  if [ $pid -ne 0 ] ; then
    echo "WSO2 Cloud - Domain Mapper: Already started"
    exit 1
  fi

  # If the application isn't running, starts it
  echo -n "WSO2 Cloud - Domain Mapper: Started"
  java -jar cloud-domain-mapper-1.0.0.jar

}

# Stops the application
stop() {

  # Checks the application status
  check_status

  pid=$?

  if [ $pid -eq 0 ] ; then
    echo "WSO2 Cloud - Domain Mapper: Already stopped"
    exit 1
  fi

  # Kills the application process
  echo -n "WSO2 Cloud - Domain Mapper: "
  kill -9 $pid &
  echo "Stopped"
}

# Show the application status
status() {

  # The check_status function, again...
  check_status

  # If the PID was returned means the application is running
  if [ $? -ne 0 ] ; then
    echo "WSO2 Cloud - Domain Mapper: Running"
  else
    echo "WSO2 Cloud - Domain Mapper: Stopped"
  fi

}

#show application version
version() {
echo "WSO2 Cloud - Domain Mapper v1.0.0"
}

setup() {

  # At first checks if the application is already started calling the check_status function
  check_status

  pid=$?

  if [ $pid -ne 0 ] ; then
    stop
  fi

  # If the application isn't running, starts it
  echo -n "WSO2 Cloud - Domain Mapper: Fresh Start"
  java -jar cloud-domain-mapper-1.0.0.jar -Dsetup

}

# Agent logic, a simple case to call functions
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart|reload)
    stop
    start
    ;;
  version)
    version
    ;;
  setup)
    setup
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|reload|status|version|setup}"
    exit 1
esac

exit 0