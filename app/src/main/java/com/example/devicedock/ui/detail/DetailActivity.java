package com.example.devicedock.ui.detail;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.devicedock.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    public static final String EXTRA_LOCAL_IP = "local_ip";
    public static final String EXTRA_DEVICE_NAME = "device_name";
    private TextView deviceNameTv, statusMessageTv;
    private TextView publicIpTv, cityTv, regionTv, countryTv, orgTv, carrierTv;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String IPIFY_URL = "https://api.ipify.org?format=json";
    private static final String IPINFO_BASE_URL = "https://ipinfo.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        String localIp = getIntent().getStringExtra(EXTRA_LOCAL_IP);
        String deviceName = getIntent().getStringExtra(EXTRA_DEVICE_NAME);

        initViews();
        deviceNameTv.setText("Details for: " + deviceName + " (" + localIp + ")");
        fetchGeographicalData();
    }

    private void initViews() {
        deviceNameTv = findViewById(R.id.text_device_name);
        statusMessageTv = findViewById(R.id.text_status_message);
        publicIpTv = findViewById(R.id.text_public_ip);
        cityTv = findViewById(R.id.text_city);
        regionTv = findViewById(R.id.text_region);
        countryTv = findViewById(R.id.text_country);
        orgTv = findViewById(R.id.text_org);
        carrierTv = findViewById(R.id.text_carrier);
    }

    private void fetchGeographicalData() {
        executor.execute(() -> {
            String publicIp = getPublicIp();

            if (publicIp == null) {
                runOnUiThread(() -> {
                    statusMessageTv.setText("Failed to retrieve public IP.");
                    Toast.makeText(this, "Network error fetching public IP.", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            String geoDataJson = getGeographicalInfo(publicIp);

            if (geoDataJson != null) {
                parseAndDisplayData(publicIp, geoDataJson);
            } else {
                runOnUiThread(() -> {
                    statusMessageTv.setText("Failed to retrieve geographical data.");
                });
            }
        });
    }
    private String getPublicIp() {
        String jsonResponse = performHttpRequest(IPIFY_URL);
        if (jsonResponse != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                return jsonObject.getString("ip");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getGeographicalInfo(String publicIp) {
        String url = IPINFO_BASE_URL + publicIp + "/geo";
        return performHttpRequest(url);
    }
    private String performHttpRequest(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } else {
                Log.e(TAG, "HTTP Error: " + responseCode + " for " + urlString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { /* ignore */ }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
    private void parseAndDisplayData(String publicIp, String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            String city = json.optString("city", "N/A");
            String region = json.optString("region", "N/A");
            String country = json.optString("country", "N/A");
            String org = json.optString("org", "N/A");

            String carrier = json.optString("carrier", json.optString("org", "N/A"));

            runOnUiThread(() -> {
                statusMessageTv.setText("Geographical data fetched successfully.");
                publicIpTv.setText(getString(R.string.label_public_ip) + publicIp);
                cityTv.setText(getString(R.string.label_city) + city);
                regionTv.setText(getString(R.string.label_region) + region);
                countryTv.setText(getString(R.string.label_country) + country);
                orgTv.setText(getString(R.string.label_organization) + org);
                carrierTv.setText(getString(R.string.label_carrier) + carrier);
            });

        } catch (JSONException e) {
            runOnUiThread(() -> {
                statusMessageTv.setText("Error parsing server response.");
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}