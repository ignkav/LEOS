#!/usr/bin/env bash
#
# Copyright 2021 European Commission
#
# Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
#
#     https://joinup.ec.europa.eu/software/page/eupl
#
# Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and limitations under the Licence.
#

cd ./modules/annotate

if ! grep -q "proxy" ./client/.npmrc ; then
    echo "WARNING: PROXY SETTING NOT FOUND. If you are behind a proxy, you need to set proxy in ./client/.npmrc"
fi

if grep -q "bamboo_NPM_TOKEN" ./client/.npmrc ; then
    echo "ERROR: you need to remove/update NPM_TOKEN in ./client/.npmrc!!!"
    exit 1
fi

echo "---------------------Annotate Server-----------------------------------------------"


echo "---------------------Annotate Server COMPILING...----------------------------------"
mvn clean install -Denv=local -Dmaven.test.skip=true
echo "---------------------Annotate Server COMPILED.-------------------------------------"

echo "---------------------Annotate Server STARTING...-----------------------------------"
cd ./server
mvn  -Dspring-boot.run.folders=../config/target/generated-config/ spring-boot:run
echo "---------------------Annotate Server Killed----------------------------------------"
