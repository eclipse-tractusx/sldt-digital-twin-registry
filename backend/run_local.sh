###############################################################
# Copyright (c) 2021-2022 T-Systems International GmbH
# Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
###############################################################

#
# Shell script to build and run a local registry for testing purposes.
#
# Prerequisites: 
#   Windows, (git)-bash shell, java 11 (java) and maven (mvn) in the $PATH.
#
# Synposis: 
#   ./build_run_local.sh (-build)? (clean)? (-suspend)? (-debug)? (-proxy)?
#
# Comments: 
#

DEBUG_PORT=8887
DEBUG_SUSPEND=n
DEBUG_OPTIONS=
DB_FILE="./target/db_semantics;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
CLEAN_DB=n
PROXY=

for var in "$@"
do
  if [ "$var" == "-debug" ]; then
    DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=${DEBUG_PORT},server=y,suspend=${DEBUG_SUSPEND}"
  else 
      if [ "$var" == "-build" ]; then
        mvn install -DskipTests -Dmaven.javadoc.skip=true
      else       
        if [ "$var" == "-suspend" ]; then
          DEBUG_SUSPEND=y
        else
          if [ "$var" == "-clean" ]; then
            CLEAN_DB=y
            mvn clean
          else
            if [ "$var" == "-proxy" ]; then
              PROXY="-Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -Dhttps.proxyHost=${HTTP_PROXY_HOST} -Dhttps.proxyPort=${HTTP_PROXY_PORT}"
              if [ "${HTTP_NONPROXY_HOSTS}" != "" ]; then
                PROXY="${PROXY} -Dhttp.nonProxyHosts=${HTTP_NONPROXY_HOSTS} -Dhttps.nonProxyHosts=${HTTP_NONPROXY_HOSTS}"
              fi
            fi
          fi
        fi
      fi
  fi
done

H2_URL=jdbc:h2:file:${DB_FILE}

if [ "$CLEAN_DB" == "y" ]; then
  rm -f ${DB_FILE}*
fi

CALL_ARGS="-classpath target/registry-1.3.0-SNAPSHOT.jar \
           -Dspring.profiles.active=local \
           -Dspring.datasource.url=$H2_URL\
           -Dserver.ssl.enabled=false $PROXY $DEBUG_OPTIONS\
           org.springframework.boot.loader.JarLauncher"

java ${CALL_ARGS}

    
