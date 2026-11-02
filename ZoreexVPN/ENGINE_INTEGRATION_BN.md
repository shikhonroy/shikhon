# Zoreex VPN: OpenVPN 3 Android integration plan

## Goal

Build one installable Android APK. The user installs only Zoreex VPN. OpenVPN protocol handling runs inside the APK; the app must not launch or depend on another VPN client.

## Selected stack

- UI and Android lifecycle: Java + XML
- Android tunnel API: `android.net.VpnService`
- VPN protocol engine: OpenVPN 3 Core (C++20)
- Native bridge: JNI, with SWIG bindings where appropriate
- Server discovery: VPN Gate public relay API
- IDE target: CodeAssist, subject to its Android NDK/CMake support

## Why the existing prototype is not enough

The current prototype fetches VPN Gate CSV and decodes the Base64 `.ovpn` text. It then sends that profile to an external app. It does not contain an OpenVPN protocol engine, does not create an encrypted tunnel, and must not show a connected state.

## Required implementation stages

1. **Native engine build**
   - Pin an OpenVPN 3 Core revision.
   - Build shared native libraries for Android ABIs (at minimum arm64-v8a; add armeabi-v7a if supported).
   - Add the required crypto/dependency builds and CMake configuration.
   - Add JNI/SWIG bindings for profile evaluation, connect, disconnect, events and logs.
2. **Android TUN integration**
   - Implement a foreground `VpnService`.
   - Implement OpenVPN 3 `TunBuilderBase` callbacks using Android's `VpnService.Builder`.
   - Pass file descriptors and packet I/O correctly between the native engine and Android TUN interface.
   - Handle VPN permission consent, service restart, notification, network changes and disconnect cleanup.
3. **Profile pipeline**
   - Fetch public relay metadata.
   - Decode and validate the selected inline OpenVPN profile.
   - Reject unsupported directives and show useful errors rather than silently failing.
4. **App UI**
   - Server list, ping/status, connect/disconnect, connection timer, assigned IP, traffic counters and logs.
   - Never display Connected until the engine reports a successful connection and the tunnel is active.
5. **Testing and release**
   - Build APK on a supported Android NDK/CMake toolchain.
   - Test on real devices and multiple public relay servers.
   - Verify DNS routing, IPv4/IPv6 behavior, reconnects, battery use and leak protection.

## Public server warning

VPN Gate servers are volunteer-operated public relays. Availability, speed, privacy practices and reliability vary. They are not appropriate for banking, passwords or sensitive communications. The app should display this warning before the first connection.

## Licensing

OpenVPN 3 Core is offered under AGPL-3.0 or MPL-2.0. Select the applicable license path, include required notices, and comply with the chosen license when distributing source or APKs. Do not copy OpenVPN for Android (ics-openvpn) code into this project without accepting its GPLv2 obligations.

## Upstream technical references

- OpenVPN 3 Core describes itself as a C++ client library and points to `ClientAPI::OpenVPNClient` and `TunBuilderBase`: https://openvpn.github.io/openvpn3/
- Official source: https://github.com/OpenVPN/openvpn3
- License: https://openvpn.github.io/openvpn3/md_LICENSE.html
