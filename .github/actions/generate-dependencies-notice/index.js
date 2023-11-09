/*
Copyright (c) 2023 Robert Bosch Manufacturing Solutions GmbH
Copyright (c) 2023 Contributors to the Eclipse Foundation

See the NOTICE file(s) distributed with this work for additional
information regarding copyright ownership.

This program and the accompanying materials are made available under the
terms of the Apache License, Version 2.0 which is available at
https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

SPDX-License-Identifier: Apache-2.0
*/

const https = require('https')
const fs = require('fs')
const path = require('path')
const { exec } = require("child_process")
const { parse } = require('csv-parse/sync')
const core = require('@actions/core');


const basePath = core.getInput('base-path');
const relativeMavenDepsPath = core.getInput('maven-deps-path');
const relativeBaseImageLayersPath = core.getInput('base-image-layers-path');

var dashToolPath = `${basePath}/dash-tool.jar`
var legalDirectory = `${basePath}/legal`
var mavenDepPath = path.join(basePath, relativeMavenDepsPath)
var baseImageLayersPath = path.join(basePath, relativeBaseImageLayersPath)
var summaryOutputPath = `${legalDirectory}/dependencies.txt`
var baseImageLayerMappingPath = `${legalDirectory}/baseImageLayers.txt`
var licenseDirectory = `${legalDirectory}/licenses`

main()

async function main() {
    await asyncDashToolDownload("https://repo.eclipse.org/service/local/artifact/maven/redirect?r=dash-licenses&g=org.eclipse.dash&a=org.eclipse.dash.licenses&v=LATEST")

    createDirectories()

    var [baseLayerPackageList, baseLayerLicenses] = parseBaseImageLayers()
    
    writeBaseImageLayerMappingToFile(baseLayerPackageList)

    var dashToolResponse = await runDashTool()

    console.log(dashToolResponse)

    var depLicenses = parseDependencies()

    var spdxMapping = await fetchSPDXMapping()

    var mergedLicenseList = [...new Set(baseLayerLicenses.concat(depLicenses))]

    await downloadLicenses(mergedLicenseList, spdxMapping)
}

async function downloadLicenses(depLicenses, spdxMapping) {
    return Promise.all(depLicenses.map((license) => {
        var result = spdxMapping.licenses.find(mapping => mapping.licenseId === license)

        if(result != undefined) {
            return new Promise((resolve, reject) => {
                https.get(result.detailsUrl, (response) => {
                    let body = "";
        
                    response.on("data", (part) => {
                        body += part;
                    });
        
                    response.on("end", () => {
                        let licenseDetailsJson = JSON.parse(body);

                        fs.writeFileSync(`${licenseDirectory}/${licenseDetailsJson.licenseId}.txt`, licenseDetailsJson.licenseText, err => {
                            if(err) {
                                console.log(err)

                                reject()
                            }
                        })

                        console.log(`Successfully downloaded ${license}`)

                        resolve()
                    });
                })
            })
        } else {
            console.log(`WARNING: Unable to find ${license}`)
        }
    }))
}

function writeBaseImageLayerMappingToFile(data) {
    fs.writeFileSync(baseImageLayerMappingPath, JSON.stringify(data), (err) => {
        if(err) {
            console.log(err)
        }
    })
}

function parseBaseImageLayers() {
    var baseImageLayersFile = fs.readFileSync(baseImageLayersPath, (err, data) => {
        if(err) {
            console.log(error)

            return;
        }
    })

    var baseImageLayerJson = JSON.parse(baseImageLayersFile)

    var packages = []
    var licenses = []

    for(layer of baseImageLayerJson.images[0].image.layers) {
        for(package of layer.packages) {
            packages.push({
                name: package.name,
                version: package.version,
                proj_url: package.proj_url,
                license: package.pkg_license
            })

            licenses = licenses.concat(extractLicensesFromLicenseStatement(package.pkg_license))
        }
    }

    licenses = [...new Set(licenses)]

    licenses = licenses.filter(l => l !== "")

    return [packages, licenses]
}

function parseDependencies(licenses) {
    var dependenciesList = fs.readFileSync(summaryOutputPath, (err, data) => {
        if(err) {
            console.log(error)

            return;
        }
    })
    var records = parse(dependenciesList, {
        skip_empty_lines: true,
        delimiter: ',',
    })

    var licenses = []

    for(record of records) {
        licenses = licenses.concat(extractLicensesFromLicenseStatement(record[1]))
    }

    licenses = [...new Set(licenses)]

    licenses = licenses.filter(l => l !== "")

    return licenses
}

function extractLicensesFromLicenseStatement(licenseString) {
    var licenses = []

    license = licenseString.replace(/[ \(\)]/g, "")
    var splitLicense = license.split(/AND|OR|with|WITH/g)
    for(l of splitLicense) {
        licenses.push(l)
    }

    return licenses
}

async function fetchSPDXMapping() {
    return new Promise((resolve, reject) => {
        https.get("https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json", (response) => {
            let body = "";

            response.on("data", (part) => {
                body += part;
            });

            response.on("end", () => {
                try {
                    let spdxMappingJson = JSON.parse(body);
                    resolve(spdxMappingJson)
                } catch (error) {
                    console.error(error.message);
                };
            });
        })
    })
}

async function runDashTool() {
    return new Promise((resolve, reject) => {
        console.log(`Executing Dash Tool`)

        exec(`java -jar ${dashToolPath} ${mavenDepPath} -summary ${summaryOutputPath}`, (error, stdout, stderr) => {
            if (stderr) {
                resolve(stderr)
            }

            resolve(stdout)
        })
    })
}

async function asyncDashToolDownload(url) {
    return new Promise((resolve, reject) => {
        downloadDashTool(url, resolve, reject)
    })
}

async function downloadDashTool(url, resolve, reject) {
    https.get(url, (response) => {
        if (response.statusCode >= 400) {
            reject("Could not download Dash Tool")
        }

        if (response.statusCode > 300 && response.statusCode < 400 && !!response.headers.location) {
            downloadDashTool(response.headers.location, resolve, reject)
        } else {
            console.log(`Starting download of Dash Tool`)

            const filePath = fs.createWriteStream(dashToolPath);

            response.pipe(filePath)
            filePath.on('finish', () => {
                filePath.close()
                console.log(`Downloaded Dash Tool`)
                resolve(`Downloaded Dash Tool`)
            })
        }
    })
}

function createDirectories() {
    if(!fs.existsSync(legalDirectory)) {
        fs.mkdirSync(legalDirectory)
    }

    if(!fs.existsSync(licenseDirectory)) {
        fs.mkdirSync(licenseDirectory)
    }
}