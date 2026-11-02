package com.zoreex.vpn;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private LinearLayout list;
    private TextView status;
    private Button refresh;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<VpnServer> servers = new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        loadServers();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(20));
        root.setBackgroundColor(0xFF101827);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("ZOREEX VPN");
        title.setTextSize(25);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(null, 1);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Free public VPN servers");
        subtitle.setTextColor(0xFFB8C4D8);
        root.addView(subtitle);

        refresh = new Button(this);
        refresh.setText("REFRESH SERVERS");
        root.addView(refresh);
        refresh.setOnClickListener(v -> loadServers());

        status = new TextView(this);
        status.setTextColor(0xFF71DDA2);
        status.setPadding(0, dp(12), 0, dp(12));
        root.addView(status);

        TextView warning = new TextView(this);
        warning.setText("Public servers may be slow, offline, or untrusted. This app downloads .ovpn profiles; OpenVPN for Android is required to connect.");
        warning.setTextColor(0xFFFFD18A);
        warning.setTextSize(12);
        root.addView(warning);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        root.addView(list);
        setContentView(scroll);
    }

    private void loadServers() {
        refresh.setEnabled(false);
        status.setText("Fetching public server list...");
        list.removeAllViews();
        executor.execute(() -> {
            ArrayList<VpnServer> found = new ArrayList<>();
            String error = null;
            try {
                HttpURLConnection c = (HttpURLConnection) new URL("https://www.vpngate.net/api/iphone/").openConnection();
                c.setConnectTimeout(15000);
                c.setReadTimeout(20000);
                c.setRequestProperty("User-Agent", "ZoreexVPN/1.0");
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                String line;
                boolean header = false;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("*") || line.trim().isEmpty()) continue;
                    if (!header) { header = true; continue; }
                    String[] p = parseCsv(line);
                    if (p.length < 15) continue;
                    String ip = p[1];
                    String ping = p.length > 3 ? p[3] : "?";
                    String country = p.length > 5 ? p[5] : "Unknown";
                    String code = p.length > 6 ? p[6] : "";
                    String config = p[p.length - 1];
                    if (config == null || config.length() < 20) continue;
                    found.add(new VpnServer(ip, ping, country, code, config));
                    if (found.size() >= 60) break;
                }
                br.close();
                c.disconnect();
                if (found.isEmpty()) error = "No usable servers found. Try again later.";
            } catch (Exception e) {
                error = "Could not load servers: " + e.getMessage();
            }
            final String err = error;
            runOnUiThread(() -> {
                refresh.setEnabled(true);
                servers.clear();
                servers.addAll(found);
                if (err != null) status.setText(err);
                else {
                    status.setText("Found " + servers.size() + " public servers.");
                    showServers();
                }
            });
        });
    }

    private void showServers() {
        list.removeAllViews();
        for (VpnServer s : servers) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundColor(0xFF1D2A3D);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
            cp.setMargins(0, dp(6), 0, dp(6));
            list.addView(card, cp);

            TextView name = new TextView(this);
            name.setText(s.country + " (" + s.countryCode + ")");
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(17);
            name.setTypeface(null, 1);
            card.addView(name);

            TextView detail = new TextView(this);
            detail.setText(s.ip + " | Ping: " + s.ping + " ms");
            detail.setTextColor(0xFFB8C4D8);
            card.addView(detail);

            Button open = new Button(this);
            open.setText("DOWNLOAD & OPEN PROFILE");
            card.addView(open);
            open.setOnClickListener(v -> downloadAndOpen(s));
        }
    }

    private void downloadAndOpen(VpnServer server) {
        status.setText("Preparing OpenVPN profile...");
        executor.execute(() -> {
            try {
                byte[] decoded = android.util.Base64.decode(server.configBase64, android.util.Base64.DEFAULT);
                File dir = new File(getExternalFilesDir(null), "profiles");
                if (!dir.exists()) dir.mkdirs();
                String code = server.countryCode.replaceAll("[^A-Za-z0-9]", "");
                if (code.isEmpty()) code = "server";
                File profile = new File(dir, code + "_" + server.ip.replace(".", "_") + ".ovpn");
                try (FileOutputStream out = new FileOutputStream(profile)) { out.write(decoded); }
                runOnUiThread(() -> {
                    status.setText("Profile ready. Choose OpenVPN to import.");
                    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", profile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/x-openvpn-profile");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Import profile with OpenVPN"));
                });
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Profile error: " + e.getMessage()));
            }
        });
    }

    private String[] parseCsv(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else quoted = !quoted;
            } else if (ch == ',' && !quoted) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private static class VpnServer {
        String ip, ping, country, countryCode, configBase64;
        VpnServer(String i, String p, String c, String cc, String b) {
            ip=i; ping=p; country=c; countryCode=cc; configBase64=b;
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
