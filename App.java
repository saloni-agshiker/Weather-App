package weather.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.Scanner;

public class App
{
    static String apiKey = "0a1f2cbf10752786110ea7cc95ab3fe1";
    static String city;
    static double latitude;
    static double longitude;
    static HttpClient client = HttpClient.newHttpClient();

    public static void getCity() {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter a city: ");
        city = input.nextLine();

        String url = "http://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=5" + "&appid=" + apiKey;       

        // Create the HttpRequest
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Send the request and handle the response
        try {
            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
            if (response1.statusCode() == 200) {
                // Print the response body
                //System.out.println(response1.body());

                JSONArray locationsArray = new JSONArray(response1.body());
                for (int i = 0; i < locationsArray.length(); ++i) {
                    JSONObject location = locationsArray.getJSONObject(i);
                    String name = location.getString("name");
                    String state = location.getString("state");
                    System.out.println((i+1) + ". " + name + ", " + state);
                }

                System.out.print("Enter the number of the city you are looking for: ");
                int num = input.nextInt();
                JSONObject correctLoc = locationsArray.getJSONObject(num - 1);

                latitude = correctLoc.getDouble("lat");
                longitude = correctLoc.getDouble("lon");
                //System.out.println("lat: " + latitude);

            } else {
                System.out.println("Error: " + response1.statusCode() + " - " + response1.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getWeather() {
        // use the lat and lon to call the other API and get weather details
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            if (response2.statusCode() == 200) {
                // Print the response body
                //System.out.println("Response: " + response2.body());

                JSONObject jsonObject = new JSONObject(response2.body());
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String description = weatherObject.getString("description");
                System.out.println("Description: " + description);

                JSONObject mainObject = jsonObject.getJSONObject("main");
                double temp = kelvinToF(mainObject.getDouble("temp"));
                double minTemp = kelvinToF(mainObject.getDouble("temp_min"));
                double maxTemp = kelvinToF(mainObject.getDouble("temp_max"));


                System.out.printf("Temp: %.0f F %n", temp);
                System.out.printf("Min Temp: %.0f F %n", minTemp);
                System.out.printf("Max Temp: %.0f F %n", maxTemp);

                JSONObject sysObject = jsonObject.getJSONObject("sys");
                long sunriseTime = sysObject.getLong("sunrise");
                long sunsetTime = sysObject.getLong("sunset");
                
                System.out.printf("Sunrise: %s AM (EST) %n", unixToLocal(sunriseTime, false));
                System.out.printf("Sunset: %s PM (EST) %n", unixToLocal(sunsetTime, true));

            } else {
                System.out.println("Error: " + response2.statusCode() + " - " + response2.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static double kelvinToF(double kelvin) {
        return (kelvin - 273.15) * 9/5 + 32;
    }

    public static String unixToLocal(long unixTime, boolean needParse) {
        Instant instant = Instant.ofEpochSecond(unixTime);
        ZonedDateTime utcTime = instant.atZone(ZoneId.of("UTC"));
        ZonedDateTime localTime = instant.atZone(ZoneId.of("America/New_York"));
        String formattedTime = localTime.format(DateTimeFormatter.ofPattern("H:mm:ss"));
        if (!needParse) return formattedTime;
        else {
            LocalTime time = LocalTime.parse(formattedTime);
            String standardTime = time.format(DateTimeFormatter.ofPattern("h:mm:ss"));
            return standardTime;
        }
    }

    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);
        getCity();
        getWeather();
               
    }
}