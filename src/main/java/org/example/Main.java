package org.example;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.json.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {

    public static void main(String[] args) {
        String API_KEY = "ddf66cfadafeec0c675d3e9b0490a33c";
        Scanner scanner = new Scanner(System.in);

        System.out.println("Название города:");
        String cityName = scanner.nextLine();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Weather Data");
        int rowNum = 0;

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Город");
        headerRow.createCell(1).setCellValue("Температура");
        headerRow.createCell(2).setCellValue("Описание");
        headerRow.createCell(3).setCellValue("Скорость ветра");

        try {
            String encodedCityName = URLEncoder.encode(cityName.trim(), "UTF-8");
            String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=" + API_KEY + "&units=metric&lang=ru";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String city = jsonResponse.getString("name");
            double temperature = jsonResponse.getJSONObject("main").getDouble("temp");
            String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
            double windSpeed = jsonResponse.getJSONObject("wind").getDouble("speed");

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(city);
            row.createCell(1).setCellValue(temperature);
            row.createCell(2).setCellValue(weatherDescription);
            row.createCell(3).setCellValue(windSpeed);

            try (FileWriter file = new FileWriter("weather_data.json", true)) {
                file.write(jsonResponse.toString(4) + "\n");
            }

            Set<Integer> selectedOptions = new HashSet<>();
            while (true) {
                System.out.println("\nЧто вы хотите вывести?");
                System.out.println("1 - Название города");
                System.out.println("2 - Температура");
                System.out.println("3 - Описание погоды");
                System.out.println("4 - Скорость ветра");
                System.out.println("5 - Вывести выбранные данные");
                System.out.println("0 - Выйти");
                System.out.print("Введите что вы хотите вывести: ");
                String choiceInput = scanner.nextLine();
                String[] choices = choiceInput.split(",");

                for (String choiceStr : choices) {
                        int choice = Integer.parseInt(choiceStr.trim());
                        if (choice == 0) {
                            System.out.println("Выход из программы.");

                            try (FileOutputStream outputStream = new FileOutputStream("WeatherData.xlsx")) {
                                workbook.write(outputStream);
                                workbook.close();
                                System.out.println("Данные успешно сохранены в файл WeatherData.xlsx");
                            } catch (Exception e) {
                                System.out.println("Ошибка при сохранении Excel файла: " + e.getMessage());
                            }

                            scanner.close();
                            return;
                        } else if (choice == 5) {
                            if (selectedOptions.contains(1)) {
                                System.out.println("Название города: " + city);
                            }
                            if (selectedOptions.contains(2)) {
                                System.out.println("Температура: " + temperature + "°C");
                            }
                            if (selectedOptions.contains(3)) {
                                System.out.println("Описание погоды: " + weatherDescription);
                            }
                            if (selectedOptions.contains(4)) {
                                System.out.println("Скорость ветра: " + windSpeed + " м/с");
                            }
                            selectedOptions.clear();
                        } else if (choice >= 1 && choice <= 4) {
                            selectedOptions.add(choice);
                            System.out.println("Пункт " + choice + " добавлен в выбор.");
                        } else {
                            System.out.println("Неверный выбор: " + choice);
                        }
                }
            }

        }
        catch (Exception e) {
            System.out.println("Ошибка при обработке города " + cityName + ": " + e.getMessage());
        }
    }
}