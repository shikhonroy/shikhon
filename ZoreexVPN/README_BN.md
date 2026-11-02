# Zoreex VPN

Zoreex VPN is being developed as a standalone Android VPN client using Java/XML for the app UI and the OpenVPN 3 Core as the in-app VPN engine.

## Project status

**Not yet a working VPN client.** The current server-list prototype can retrieve VPN Gate public relay metadata and decode OpenVPN profiles, but it does not establish a VPN tunnel. The old external-app import flow is being replaced.

## Target architecture

- Android `VpnService`: owns the Android TUN interface and foreground connection lifecycle.
- OpenVPN 3 Core: compiled into the APK as native libraries (JNI/SWIG bridge); no separate VPN app should be required.
- Java UI: server selection, connect/disconnect, connection status, logs and settings.
- Public server provider: VPN Gate relay API, with clear warnings that volunteer relays can be unavailable, slow, monitored, or unsuitable for sensitive traffic.

## Important limitations

- A public server list is not a VPN service by itself. A compatible OpenVPN server, profile, certificates and working network path are required.
- Public volunteer relays are not guaranteed to be online or trustworthy. Do not use them for banking, private accounts, or sensitive information.
- A real release requires building and testing the native OpenVPN 3 engine for Android ABIs, integrating the TUN callbacks, and testing on physical Android devices. This repository has not yet passed that build or connection test.
- OpenVPN 3 Core is available under AGPL-3.0 or MPL-2.0; review the upstream license and satisfy its source/distribution obligations before publishing binaries.

## Development

The project currently retains the Java/XML starter under `app/`. See [ENGINE_INTEGRATION_BN.md](ENGINE_INTEGRATION_BN.md) for the implementation stages and why the native engine is required.

## Upstream references

- OpenVPN 3 Core: https://github.com/OpenVPN/openvpn3
- OpenVPN 3 Developer Guide: https://openvpn.github.io/openvpn3/
- VPN Gate public relay API: https://www.vpngate.net/api/iphone/
