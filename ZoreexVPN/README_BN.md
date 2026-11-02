# Zoreex VPN (Android starter)

Java + XML Android project for listing VPN Gate public relay servers and downloading their OpenVPN profiles.

## Important
This is a starter app, not a standalone VPN engine. Install **OpenVPN for Android** (Arne Schwabe) to import the downloaded .ovpn profile and establish the tunnel. The app fetches live public server information from VPN Gate; public relays may be slow, offline, or untrusted. Do not use public relays for banking, passwords, or sensitive traffic.

## Open in CodeAssist
1. Download this repository as ZIP and extract it.
2. Open the `ZoreexVPN` folder in CodeAssist.
3. Sync Gradle.
4. Install OpenVPN for Android.
5. Run Zoreex VPN, refresh servers, choose one, then import its profile in OpenVPN.

Minimum SDK 23, target SDK 34, Java 8, Android Gradle Plugin 8.2.2.
