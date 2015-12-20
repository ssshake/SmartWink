/**          
 *  Pairs a set of Pico remotes to a set of Caseta dimmers in hardware, bypassing SmartThings. Only Clear-Connect based dimmers may be paired this way.
 *
 *  This file is automatically generated from a template. Do not modify directly.
 *  Please refer to http://www.github.com/quantiletree/SmartWink for the source code.
 *
 *  Wink and the Wink Hub are trademarks of Wink, Inc. Lutron, Pico, Caseta, and Serena are trademarks of
 *  Lutron Electronics Co., Inc. SmartWink is an independent third-party application designed to bridge these systems,
 *  and is not affiliated with or sponsored by Wink, Lutron, or SmartThings.
 *
 *  Copyright 2015 Michael Barnathan (michael@barnathan.name)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

definition(
        // 
        name: "Hardlink Pico",
        namespace: "smartwink",
        author: "Michael Barnathan",
        description: "Pairs a set of Pico remotes to a set of Caseta dimmers in hardware, bypassing SmartThings. Only Clear-Connect based dimmers may be paired this way.",
        category: "SmartThings Labs",
        iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather13-icn@2x.png",
        iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather13-icn@2x.png"
)

preferences {
    // Page definition disables mode selection, since the hard pair is totally outside of SmartThings' mode control.
    page(name: "Device Selection", title: "Select devices to hard-pair", install: true, uninstall: true) {
        section("Instructions") {
            paragraph "Pico remotes support direct hardware pairing with other Clear Connect devices. This is the fastest and most reliable way to synchronize Pico remotes and Caseta dimmers. If non-Clear Connect dimmers are selected, they will not be paired."
            paragraph "Pairing may take up to 10 seconds per device added or removed, per Pico. Please be patient."
        }

        section("Devices to link:") {
            input "picos", "device.pico", title: "Pico Remotes:", multiple: true, required: true
            input "dimmers", "capability.switchLevel", title: "Caseta Dimmers:", multiple: true, required: true
        }

        section {
            label(name: "label", title: "Assign a name", required: false)
        }
    }
}

def installed() {}

def updated() {
    def newPicoIds = getIds(picos)
    def removedPicoIds = (state.lastPicos) ? (state.lastPicos - newPicoIds) : []
    state.lastPicos = newPicoIds
    if (removedPicoIds) {
        log.info "Pico remotes ${removedPicoIds} removed. Removing device pairings."
        pair(removedPicoIds, [])
    }

    log.info "Pairing list for ${picos} updated: ${dimmers}. Updating device pairings."
    pair(newPicoIds, getIds(dimmers))
}

def uninstalled() {
    if (picos) {
        log.info "Unpairing all dimmer ties for ${picos}"
        pair(getIds(picos), [])
    }
}

private getIds(devices) {
    return devices.collect { it.device.deviceNetworkId }
}

private pair(remoteIds, deviceIds) {
    if (!remoteIds) {
        // Uninstalled before the remote was selected.
        return
    }

    def idStr = deviceIds.join(",")
    def remoteStr = remoteIds.join(",")
    log.info "Setting remote pairing for [${remoteStr}] to [${idStr}]"

    // This is a hack, but the best way to get at the Wink Hub's address while batching in one request.
    // When not batched, the requests race for DB access and cause problems.
    picos[0].setPairList(idStr, remoteStr)
}
