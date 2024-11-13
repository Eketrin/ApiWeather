package com.example.weatherapp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
public class MainActivity extends AppCompatActivity {

//основные переменные
    private EditText user_field; //город от пользователя
    ImageView imageView;
    TextView description_weather;
//  String url_icon = "https://openweathermap.org/img/wn/10d@2x.png";
    TextView temperature; //цифры
    private static final String API_KEY = "855011fda3cc956b0bba12e4a27464bb";
    boolean is_far_now = false;
    String icon_id;
    TextView feels_like;
    TextView humidity; //влажность
    TextView degrees; //направление ветра
    TextView speed_wind;
    TextView sunrize; //восход
    TextView sunset; //закат
//чисто текст
    TextView gradus_far;
    TextView gradus;
    Switch switch_gps;
    Switch switch_farengate;
    private double latitude;
    private double longitude;
//    private FusedLocationProviderClient fusedLocationClient;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        temperature = findViewById(R.id.temperature);
        user_field = findViewById(R.id.user_field);
        gradus_far = findViewById(R.id.gradus_far);
        gradus = findViewById(R.id.gradus);
        description_weather = findViewById(R.id.description_weather);
        feels_like = findViewById(R.id.feels_like);
        humidity = findViewById(R.id.humidity);
        degrees = findViewById(R.id.degrees);
        speed_wind = findViewById(R.id.speed_wind);
        sunrize = findViewById(R.id.sunrize);
        sunset = findViewById(R.id.sunset);
        switch_gps = findViewById(R.id.switch_gps);
        switch_farengate = findViewById(R.id.switch_farengate);
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }
    // Метод для получения информации о погоде
    public void GetInfoOfWeather(View view) {
        String city = user_field.getText().toString();
        if (!city.isEmpty()) {
            getWeatherData(city);
        } else {
            Toast.makeText(MainActivity.this, "Введите город", Toast.LENGTH_SHORT).show();
        }
    }



    private void displayWeatherData(JSONObject response) {
        try {
            //String weatherInfo = response.getJSONArray("weather").getJSONObject(0).getString("description");

            DecimalFormat myFormat = new DecimalFormat("#.#");  // округляем

            double t = response.getJSONObject("main").getDouble("temp");
            double f_like = response.getJSONObject("main").getDouble("feels_like");
            if (switch_farengate.isChecked()) { //если switch активен, то переводим в F
                temperature.setText((myFormat.format(t * 9 / 5 + 32)) + "");
                feels_like.setText((myFormat.format(f_like * 9 / 5 + 32)) + "");

                gradus_far.setText("°F");
                gradus.setText("°F");
            } else {
                temperature.setText(t + "");
                feels_like.setText(f_like + "");
                gradus_far.setText("°C");
                gradus.setText("°C");
            }
//            temperature.setText(response.getJSONObject("main").getDouble("temp")+ "");

            icon_id = response.getJSONArray("weather").getJSONObject(0)
                    .getString("icon");
            loadImage("https://openweathermap.org/img/wn/" + icon_id.substring(0, icon_id.length() - 1) +"d@2x.png");
            description_weather.setText(response.getJSONArray("weather").getJSONObject(0).getString("description")+"");

            humidity.setText(response.getJSONObject("main").getDouble("humidity")+ "");
            degrees.setText(convertWindDirection(response.getJSONObject("wind").getDouble("deg"))+ "");
            speed_wind.setText(response.getJSONObject("wind").getDouble("speed")+ "");
            sunrize.setText(
                    convertUnixTimeToHourMinute(
                            response.getJSONObject("sys").getLong("sunrise")
                            ,response.getInt("timezone") ));
            sunset.setText(
                    convertUnixTimeToHourMinute(
                            response.getJSONObject("sys").getLong("sunset")
                            ,response.getInt("timezone") ));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
        }
    }
    private void getWeatherData(String city) {
        String units = "metric"; // в цельсиях //
        String url;
        if (switch_gps.isChecked()){ //если включен
//            getLocation_gps();
            url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                    "&lon=" + longitude + "&appid=" + API_KEY + "&units=" + units + "&lang=ru";

        }else {
            url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid="
                    + API_KEY + "&units=" + units + "&lang=ru";
        }


        // https://api.openweathermap.org/data/2.5/weather?q=moscow&appid=855011fda3cc956b0bba12e4a27464bb&units=metric&lang=ru
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    displayWeatherData(response);
//                    try {
//                        double latitude = response.getJSONObject("coord").getDouble("lat");
//                        double longitude = response.getJSONObject("coord").getDouble("lon");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                },
                error -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

/*
    private void getLocation_gps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Toast.makeText(MainActivity.this, "Mестоположение получено",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, "Не удалось получить местоположение",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
*/

    // конвертация из unix в норм формат
    public static String convertUnixTimeToHourMinute(long unixTime, int offsetInSeconds) {
        // Преобразуем Unix-время в миллисекунды
        long timeInMillis = (unixTime + offsetInSeconds) * 1000;

        // Создаем объект Date
        Date date = new Date(timeInMillis);

        // Форматируем только часы и минуты
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("UTC")); // Устанавливаем часовой пояс на UTC

        // Возвращаем отформатированное время
        return format.format(date);
    }
    // конвертация градусов в текстовое направление
    public static String convertWindDirection(double degrees) {
        String direction;

        if (degrees >= 0 && degrees < 22.5) {
            direction = "Север";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "Северо-восток";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "Восток";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "Юго-восток";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "Юг";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "Юго-запад";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "Запад";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "Северо-запад";
        } else {
            direction = "Север"; // Для углов 337.5 и выше
        }

        return direction;
    }
    // загрузка иконок
    private void loadImage(String url) {
        Picasso.get()
                .load(url)
                .into(imageView);
    }



    //переход на страницы
    public void startSevenDayPage(View view){
        Intent intent = new Intent(this, SevenDaysPage.class);
        startActivity(intent);
    }
    public void startTwelveHoursPage(View view){
        Intent intent = new Intent(this, TwelveHoursPage.class);
        startActivity(intent);
    }


/*
    // на кнопке
    public void GetInfoOfWeather(View view){
//        если запрос пустой выводим сообщение
        if (user_field.getText().toString().trim().isEmpty()){
            Toast.makeText(MainActivity.this, R.string.null_user_field, Toast.LENGTH_LONG).show();
        }
        else {
            String city = user_field.getText().toString();
            String key = "855011fda3cc956b0bba12e4a27464bb";
            String units_to_go = "metric"; // imperial
            // "api.openweathermap.org/data/3.0/onecall?lat=30.489772&lon=-99.771335&units=metric"
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid="
                    + key + "&units=" + units_to_go + "&lang=ru";
            new GetUrlData().execute(url);
        }
    }
    //для получения данных
    private class GetUrlData extends AsyncTask <String, String, String> {
        // во время выполнения основного метода
        protected void onPreExecute(){
            super.onPreExecute();
            description_weather.setText("Загрузка...");
        }

        // тут получаем инфу с инета
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // на основе этого объекта обращаемся по адресу
                URL url = new URL(strings[0]);
                //открываем само соединение Http
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //считываем данные из потока
                InputStream stream = connection.getInputStream();
                //записываем в норм формат
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                //считываем по 1 линии
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                temperature.setText(jsonObject.getJSONObject("main").getDouble("temp")+ "");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
*/

}