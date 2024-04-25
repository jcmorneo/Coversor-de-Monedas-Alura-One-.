import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CurrencyConverter {
    private static final String API_KEY = "22f07f1e5b77f15fc7f004ec";
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private static List<Conversion> conversionHistory = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean continuar = true;
        while (continuar) {
            System.out.println("Ingrese el monto en números:");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Consumir el salto de línea

            System.out.println("Ingrese el tipo de moneda a convertir (ejemplo: USD, EUR, GBP, COP, BRL, MXN, ARS):");
            String baseCurrency = scanner.nextLine().toUpperCase();

            System.out.println("Ingrese la moneda a la que desea convertir (ejemplo: USD, EUR, GBP, COP, BRL, MXN, ARS):");
            String targetCurrency = scanner.nextLine().toUpperCase();

            String apiUrl = API_BASE_URL + baseCurrency;
            HttpClient httpClient = HttpClients.createDefault();
            HttpRequestBase httpRequest = new HttpRequestBase() {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };
            httpRequest.setURI(URI.create(apiUrl));

            try {
                HttpResponse response = httpClient.execute(httpRequest);
                if (response.getStatusLine().getStatusCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
                    Gson gson = new Gson();
                    JsonObject jsonResponse = gson.fromJson(reader, JsonObject.class);

                    if (jsonResponse.has("conversion_rates")) {
                        JsonObject conversionRates = jsonResponse.getAsJsonObject("conversion_rates");
                        if (conversionRates.has(targetCurrency)) {
                            double exchangeRate = conversionRates.get(targetCurrency).getAsDouble();
                            double convertedAmount = amount * exchangeRate;
                            DecimalFormat formatter = new DecimalFormat("#,###.00");
                            System.out.printf("%s %s equivale a %s %s\n", formatter.format(amount), baseCurrency, formatter.format(convertedAmount), targetCurrency);

                            // Agregar la conversión al historial
                            Conversion conversion = new Conversion(amount, baseCurrency, convertedAmount, targetCurrency, LocalDateTime.now());
                            conversionHistory.add(conversion);
                        } else {
                            System.out.println("No se encontró el tipo de cambio para la moneda de destino.");
                        }
                    } else {
                        System.out.println("No se pudo obtener el tipo de cambio.");
                    }
                } else {
                    System.out.println("Error al conectarse a la API: " + response.getStatusLine().getStatusCode());
                }
            } catch (IOException e) {
                System.out.println("Error al conectarse a la API: " + e.getMessage());
            }

            System.out.println("¿Desea hacer otra conversión? (s/n)");
            String respuesta = scanner.nextLine().toLowerCase();
            continuar = respuesta.equals("s");
        }

        mostrarHistorial();
        System.out.println("Gracias por Utlizar el Conversor de Monedas Alura One.");
    }

    private static void mostrarHistorial() {
        System.out.println("Historial de Conversiones:");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < conversionHistory.size(); i++) {
            Conversion conversion = conversionHistory.get(i);
            String dateTimeFormatted = conversion.getDateTime().format(formatter);
            System.out.printf("Conversión %d: %s %s a %s %s el %s\n",
                    i + 1,
                    conversion.getAmount(),
                    conversion.getBaseCurrency(),
                    conversion.getConvertedAmount(),
                    conversion.getTargetCurrency(),
                    dateTimeFormatted
            );
        }
    }

    private static class Conversion {
        private double amount;
        private String baseCurrency;
        private double convertedAmount;
        private String targetCurrency;
        private LocalDateTime dateTime;

        public Conversion(double amount, String baseCurrency, double convertedAmount, String targetCurrency, LocalDateTime dateTime) {
            this.amount = amount;
            this.baseCurrency = baseCurrency;
            this.convertedAmount = convertedAmount;
            this.targetCurrency = targetCurrency;
            this.dateTime = dateTime;
        }

        public double getAmount() {
            return amount;
        }

        public String getBaseCurrency() {
            return baseCurrency;
        }

        public double getConvertedAmount() {
            return convertedAmount;
        }

        public String getTargetCurrency() {
            return targetCurrency;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }
    }
}









