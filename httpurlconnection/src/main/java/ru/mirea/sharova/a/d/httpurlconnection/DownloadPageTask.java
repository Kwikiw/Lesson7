package ru.mirea.sharova.a.d.httpurlconnection;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import ru.mirea.sharova.a.d.httpurlconnection.databinding.ActivityMainBinding;

public class DownloadPageTask extends AsyncTask<String, Void, String[]> {

    private final WeakReference<ActivityMainBinding> bindingRef;

    public DownloadPageTask(ActivityMainBinding binding) {
        this.bindingRef = new WeakReference<>(binding);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ActivityMainBinding binding = bindingRef.get();
        if (binding != null) {
            binding.textViewIP.setText("Загружаем...");
        }
    }

    @Override
    protected String[] doInBackground(String... urls) {
        try {
            // First request to get IP info
            String ipInfo = downloadPage(urls[0]);
            JSONObject responseJson = new JSONObject(ipInfo);

            String ip = responseJson.getString("ip");
            String city = responseJson.getString("city");
            String region = responseJson.getString("region");
            String loc = responseJson.getString("loc");
            String[] parts = loc.split(",");
            String latitude = parts[0];
            String longitude = parts[1];

            // Second request to get weather info
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current_weather=true";
            String weatherInfo = downloadPage(weatherUrl);
            JSONObject weatherJson = new JSONObject(weatherInfo);
            String temperature = weatherJson.getJSONObject("current_weather").getString("temperature");

            return new String[]{ip, city, region, temperature};
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        ActivityMainBinding binding = bindingRef.get();
        if (binding != null && result != null) {
            binding.textViewIP.setText("IP: " + result[0]);
            binding.textViewCity.setText("Город: " + result[1]);
            binding.textViewRegion.setText("Регион: " + result[2]);
            binding.textViewWeather.setText("Погода: " + result[3] + "°C");
        }
    }

    private String downloadPage(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read;
                while ((read = inputStream.read()) != -1) {
                    bos.write(read);
                }
                data = bos.toString();
            } else {
                data = connection.getResponseMessage() + ". Error Code: " + responseCode;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }
}
