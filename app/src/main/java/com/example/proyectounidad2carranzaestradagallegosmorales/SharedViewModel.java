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
        // Acumulamos los datos si es necesario
        String currentData = dataIn.getValue();
        if (currentData == null) {
            dataIn.setValue(input);  // Si no hay datos previos, establecemos los nuevos
        } else {
            dataIn.setValue(currentData + " " + input);  // Concatenamos los nuevos datos
        }

        // Procesamos los datos solo si se tienen los suficientes
        String data = dataIn.getValue();
        Log.d("SharedViewModel", "Datos recibidos: " + data);

        if (data != null && !data.isEmpty()) {
            // Extraemos los valores de humedad y temperatura de los datos recibidos
            String humedad = extractValue(data, "Humedad:");
            String temperatura = extractValue(data, "Temperatura:");

            Log.d("SharedViewModel", "Humedad extraída: " + humedad);
            Log.d("SharedViewModel", "Temperatura extraída: " + temperatura);

            // Si ambos valores se extraen correctamente, actualizamos el ViewModel
            if (humedad != null && !humedad.isEmpty() && temperatura != null && !temperatura.isEmpty()) {
                try {
                    // Usar Float.parseFloat para manejar valores decimales
                    float humedadValor = Float.parseFloat(humedad.replace("%", ""));  // Eliminamos "%" en humedad
                    float temperaturaValor = Float.parseFloat(temperatura);

                    // Solo actualizamos si los valores son válidos
                    setHum((int) humedadValor);
                    setTemp((int) temperaturaValor);

                    // Limpiar los datos procesados solo si todo es correcto
                    dataIn.setValue(null);  // Limpiar dataIn después de procesar los datos

                    Log.d("SharedViewModel", "Datos actualizados: Humedad = " + humedadValor + ", Temperatura = " + temperaturaValor);

                } catch (NumberFormatException e) {
                    Log.e("SharedViewModel", "Error al parsear los valores de humedad o temperatura", e);
                }
            } else {
                // Si los datos no están completos, mantenemos los datos acumulados y esperamos más datos
                Log.d("SharedViewModel", "Esperando más datos...");
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
    private String extractValue(String data, String label) {
        int startIndex = data.indexOf(label);
        if (startIndex == -1) {
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
        String value = data.substring(startIndex, endIndex).trim();

        // Verificar si el valor está vacío, en cuyo caso devolver null
        return value.isEmpty() ? null : value;
    }


}
