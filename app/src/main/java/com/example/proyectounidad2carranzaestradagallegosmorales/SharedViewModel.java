package com.example.proyectounidad2carranzaestradagallegosmorales;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    // Datos internos encapsulados con MutableLiveData
    private final MutableLiveData<String> deviceAddress = new MutableLiveData<>();
    private final MutableLiveData<Integer> temp = new MutableLiveData<>();
    private final MutableLiveData<Integer> hum = new MutableLiveData<>();
    private final MutableLiveData<String> dataIn = new MutableLiveData<>();
    private final MutableLiveData<String> dataOut = new MutableLiveData<>();

    // Constructor
    public SharedViewModel() {
        deviceAddress.setValue("No hay dispositivo");
        temp.setValue(0);
        hum.setValue(0);
        dataIn.setValue(null);
        dataOut.setValue("No hay datos transmitidos");
    }

    // GETTERS
    public LiveData<String> getDeviceAddress() {
        return deviceAddress;
    }

    public LiveData<Integer> getTemp() {
        return temp;
    }

    public LiveData<Integer> getHum() {
        return hum;
    }

    public LiveData<String> getDataIn() {
        return dataIn;
    }

    public LiveData<String> getDataOut() {
        return dataOut;
    }

    // SETTERS
    public void setDeviceAddress(String address) {
        deviceAddress.setValue(address);
    }

    public void setTemp(int temperature) {
        temp.setValue(temperature);
    }

    public void setHum(int humidity) {
        hum.setValue(humidity);
    }

    public void setDataIn(String input) {
        // Juntar los datos y asegurarse de que no haya saltos de línea o espacios innecesarios
        if (dataIn.getValue() == null) {
            dataIn.setValue(input);
        } else {
            String currentData = dataIn.getValue();
            dataIn.setValue(currentData + " " + input); // Concatenar los datos con un espacio
        }

        // Procesar los datos después de recibirlos completamente
        String data = dataIn.getValue();
        Log.d("SharedViewModel", "Datos recibidos: " + data); // Log para ver qué datos se están recibiendo

        if (data != null && !data.isEmpty()) {
            String humedad = extractValue(data, "Humedad:");
            String temperatura = extractValue(data, "Temperatura:");

            // Log para ver qué se está extrayendo
            Log.d("SharedViewModel", "Humedad extraída: " + humedad);
            Log.d("SharedViewModel", "Temperatura extraída: " + temperatura);

            // Si ambos valores se extraen correctamente, actualizar el ViewModel
            if (humedad != null && !humedad.isEmpty() && temperatura != null && !temperatura.isEmpty()) {
                try {
                    // Usar Float.parseFloat para manejar valores decimales
                    float humedadValor = Float.parseFloat(humedad.replace("%", "")); // Eliminar "%" en humedad
                    float temperaturaValor = Float.parseFloat(temperatura);

                    // Solo actualiza si los valores son válidos
                    setHum((int) humedadValor);
                    setTemp((int) temperaturaValor);

                    // Limpiar los datos procesados
                    dataIn.setValue(null);  // Limpiar dataIn después de procesar
                } catch (NumberFormatException e) {
                    Log.e("SharedViewModel", "Error al parsear los valores de humedad o temperatura", e);
                }
            }
        }
    }

    public void setDataOut(String output) {
        dataOut.setValue(output);
    }

    // comprobar la conexión con el dispositivo
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsConnected(){ return isConnected; }
    public void setIsConnected(boolean status) { isConnected.setValue(status);}


    // Método para extraer el valor de una etiqueta dada en el string
    private String extractValue(String data, String label){
        int startIndex = data.indexOf(label);
        if(startIndex == -1){
            return null;
        }

        // Mover el índice al final de la etiqueta
        startIndex += label.length();

        // Eliminar posibles espacios antes del valor
        while (startIndex < data.length() && Character.isWhitespace(data.charAt(startIndex))) {
            startIndex++;
        }

        // Buscar el siguiente espacio después del valor o tomar el final de la cadena
        int endIndex = data.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = data.length();
        }

        // Extraer el valor, asegurándose de eliminar espacios extras
        return data.substring(startIndex, endIndex).trim();
    }

}
