package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {

    // Основные переменные
    private EditText user_field; // Город от пользователя
    ImageView imageView;
    TextView description_weather;
    TextView temperature; // Цифры
    private static final String API_KEY = "855011fda3cc956b0bba12e4a27464bb";
    TextView feels_like;
    TextView humidity; // Влажность
    TextView degrees; // Направление ветра
    TextView speed_wind;
    TextView sunrize; // Восход
    TextView sunset; // Закат
    TextView gradus_far;
    TextView gradus;

    Switch switch_farengate;
    private double latitude;
    private double longitude;

    private WeatherApi weatherApi;

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

        switch_farengate = findViewById(R.id.switch_farengate);

        // Инициализация Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);
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

    private void getWeatherData(String city) {
        String units = "metric";
        Call<WeatherResponse> call; // объект запроса
        // Инициализируем запрос к API погоды для получения данных по названию города
        call = weatherApi.getWeatherByCity(city, API_KEY, units, "ru");
//https://api.openweathermap.org/data/2.5/weather?q=moscow&appid=855011fda3cc956b0bba12e4a27464bb&units=metric&lang=ru
        // Асинхронно выполняем запрос к API
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                // Обрабатываем успешный ответ от API
                if (response.isSuccessful() && response.body() != null) {
                    // Если ответ успешный и тело ответа не null, передаем данные в метод для отображения
                    displayWeatherData(response.body());
                } else {
                    // Если произошла ошибка, показываем сообщение об ошибке
                    Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Обрабатываем ошибку, возникшую при выполнении запроса
                Log.e("Retrofit Error", t.getMessage()); // Логируем сообщение об ошибке
                // Показываем сообщение пользователю о том, что произошла ошибка сети
                Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Конвертация из Unix-времени в нормальный формат
    public static String convertUnixTimeToHourMinute(long unixTime, int offsetInSeconds) {
        long timeInMillis = (unixTime + offsetInSeconds) * 1000;
        Date date = new Date(timeInMillis);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    // Конвертация градусов в текстовое направление
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
        } else {
            direction = "Северо-запад";
        }
        return direction;
    }

    // Загрузка иконок
    private void loadImage(String url) {
        Picasso.get()
                .load(url)
                .into(imageView);
    }

    private void displayWeatherData(WeatherResponse response) {
        try {
            DecimalFormat myFormat = new DecimalFormat("#.#");  // Округляем

            double t = response.main.temp;
            double f_like = response.main.feels_like;
            if (switch_farengate.isChecked()) { // Если switch активен, то переводим в F
                temperature.setText(myFormat.format(t * 9 / 5 + 32) + "");
                feels_like.setText(myFormat.format(f_like * 9 / 5 + 32) + "");
                gradus_far.setText("°F");
                gradus.setText("°F");
            } else {
                temperature.setText(t + "");
                feels_like.setText(f_like + "");
                gradus_far.setText("°C");
                gradus.setText("°C");
            }

            String icon_id = response.weather.get(0).icon;
            loadImage("https://openweathermap.org/img/wn/" + icon_id.substring(0, icon_id.length() - 1) + "d@2x.png");
            description_weather.setText(response.weather.get(0).description);

            humidity.setText(response.main.humidity + " %");
            degrees.setText(convertWindDirection(response.wind.deg));
            speed_wind.setText(response.wind.speed + " м/с");
            sunrize.setText(convertUnixTimeToHourMinute(response.sys.sunrise, response.timezone));
            sunset.setText(convertUnixTimeToHourMinute(response.sys.sunset, response.timezone));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
        }
    }


    // Интерфейс API для Retrofit
interface WeatherApi {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units, //метрика
            @Query("lang") String lang //язык
    );

}

// Класс модели для ответа от API
class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public Sys sys;
    public int timezone;

    public class Main {
        public double temp;
        public double feels_like;
        public double humidity;
    }

    public class Weather {
        public String description;
        public String icon;
    }

    public class Wind {
        public double speed;
        public double deg;
    }

    public class Sys {
        public long sunrise;
        public long sunset;
    }
}
}
/*

HttpURLConnection — это встроенный класс Java, который предоставляет
функции для выполнения HTTP-запросов. Он позволяет отправлять запросы различных
типов (GET, POST и др.) и обрабатывать ответы от сервера. Это стандартный способ
работы с сетью в Java, который не требует установки дополнительных библиотек.

OkHttp — это мощная библиотека для работы с HTTP, разработанная компанией Square.
Она предлагает удобный интерфейс для выполнения HTTP-запросов и поддерживает асинхронные вызовы,
автоматическое кэширование, сжатие ответов, а также работу с WebSocket и HTTP/2. OkHttp является
более современным и эффективным решением по сравнению с HttpURLConnection.

Retrofit — это библиотека, также разработанная Square, которая построена на базе OkHttp.
Она предназначена для упрощения работы с RESTful API, используя аннотации для определения
интерфейсов и автоматически преобразуя JSON-ответы в объекты Java. Retrofit поддерживает как
асинхронные, так и синхронные запросы и легко интегрируется с библиотеками для реактивного
программирования, такими как RxJava.

Различия между HttpURLConnection, OkHttp и Retrofit заключаются в нескольких ключевых аспектах.
Во-первых, по сложности использования, HttpURLConnection является более сложным в применении,
так как требует ручной обработки потоков и ответов, что приводит к большему объему кода для выполнения
даже простых задач. В то время как OkHttp предлагает более удобный интерфейс, значительно упрощая
выполнение запросов, а Retrofit еще больше облегчает работу с API, позволяя использовать аннотации
и автоматически обрабатывая ответы, что сокращает объем кода до минимума.

Во-вторых, в отношении асинхронности, HttpURLConnection не поддерживает асинхронные запросы ,
 что может привести к блокировке основного потока приложения. Напротив, OkHttp
полностью поддерживает асинхронные вызовы, что позволяет избежать этой проблемы, а Retrofit
также поддерживает асинхронные запросы и может интегрироваться с библиотеками для реактивного
программирования, такими как RxJava, для создания более отзывчивых приложений.

Третьим важным аспектом является обработка данных. HttpURLConnection не предоставляет встроенных
средств для обработки форматов данных, таких как JSON, что требует дополнительной работы с парсингом.
OkHttp позволяет обрабатывать данные, но не предлагает автоматического преобразования JSON в объекты
Java, в то время как Retrofit автоматически преобразует JSON-ответы в объекты Java, что значительно
упрощает работу с данными.

Наконец, в поддержке современных технологий, HttpURLConnection не поддерживает такие современные
протоколы, как HTTP/2 и WebSocket. В отличие от него, OkHttp поддерживает эти технологии, что делает
его более подходящим для современных приложений, а Retrofit, будучи построенным на базе OkHttp, также
использует эти возможности, упрощая работу с API и обеспечивая поддержку современных стандартов.
Таким образом, выбор подхода зависит от специфики проекта и требований к функциональности.
 */
