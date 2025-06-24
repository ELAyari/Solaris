package com.esprit.Services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NominatimService {
    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final String USER_AGENT = "SmartWasteApp/1.0 (contact@votredomaine.com)";


    public  double[] getCoordinates(String address) throws Exception {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&format=json&addressdetails=1&limit=1",
                NOMINATIM_SEARCH_URL, encodedAddress);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", USER_AGENT);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JSONArray results = new JSONArray(jsonResponse);

                if (results.length() == 0) {
                    throw new Exception("Adresse non trouvée: " + address);
                }

                JSONObject firstResult = results.getJSONObject(0);
                return new double[]{
                        firstResult.getDouble("lat"),
                        firstResult.getDouble("lon")
                };
            }
        }
    }

    /**
     * Convertit des coordonnées GPS en adresse (reverse géocodage)
     * @param lat Latitude
     * @param lon Longitude
     * @return Adresse formatée (ex: "Tour Eiffel, Champ de Mars, Paris")
     */
    public String getAddress(double lat, double lon) throws Exception {
        // Validation des coordonnées
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            throw new IllegalArgumentException("Coordonnées invalides");
        }

        // Formatage avec Locale.US pour garantir le point décimal
        String url = String.format(Locale.US,
                "%s?lat=%.6f&lon=%.6f&format=json",
                NOMINATIM_REVERSE_URL,
                lat,
                lon
        );

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", USER_AGENT);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JSONObject result = new JSONObject(jsonResponse);

                if (result.has("error")) {
                    throw new Exception("Erreur API: " + result.toString());
                }
                return result.optString("display_name", "Adresse inconnue");
            }
        }
    }
}