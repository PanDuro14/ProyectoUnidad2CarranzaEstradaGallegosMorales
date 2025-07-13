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
    private final MutableLiveData<Integer> currentFragment = new MutableLiveData<>();
    private final MutableLiveData<Integer> Ldr = new MutableLiveData<>();
    private final MutableLiveData<Integer> Pot = new MutableLiveData<>();

    // Constructor
    public SharedViewModel() {
        deviceAddress.setValue("No hay dispositivo");
        temp.setValue(0);
        hum.setValue(0);
        dataIn.setValue(null);
        dataOut.setValue("No hay datos transmitidos");
        currentFragment.setValue(0);
        Ldr.setValue(0);
        Pot.setValue(0);
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

    public LiveData<Integer> getCurrentFragment() { return currentFragment;  }

    public LiveData<Integer> getLdr() { return Ldr; }

    public LiveData<Integer> getPot() { return Pot; }

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

    public void setLdr(int ldr){
        Ldr.setValue(ldr);
    }

    public void setPot(int pot){
        Pot.setValue(pot);
    }

    public void setDataIn(String input) {
        // Si dataIn es null, le asignamos el valor actual
        if (dataIn.getValue() == null || dataIn.getValue().isEmpty()) {
            dataIn.setValue(input);  // Establecer los datos cuando está vacío
        } else {
            // Si hay datos previos, simplemente reemplazamos con los nuevos
            dataIn.setValue(input);  // Reemplazar el valor, sin concatenar
        }

        // Ahora procesamos los datos solo si están completos
        String data = dataIn.getValue();
        Log.d("SharedViewModel", "Datos recibidos: " + data);

        // Verificamos si los datos contienen los valores necesarios (Humedad y Temperatura, Lumines o PotValue)
        if (data != null && !data.isEmpty()) {
            if (data.contains("Humedad:") && data.contains("Temperatura:")) {
                // Extraemos humedad y temperatura
                String humedad = extractValue(data, "Humedad:");
                String temperatura = extractValue(data, "Temperatura:");

                if (humedad != null && temperatura != null) {
                    try {
                        // Procesamos los datos y los actualizamos
                        float humedadValor = Float.parseFloat(humedad.replace("%", ""));
                        setHum((int) humedadValor);

                        float temperaturaValor = Float.parseFloat(temperatura.replace("C", ""));
                        setTemp((int) temperaturaValor);

                        // Limpiamos los datos procesados
                        dataIn.setValue(null);  // Limpiamos la cadena acumulada después de procesar los datos

                    } catch (NumberFormatException e) {
                        Log.e("SharedViewModel", "Error al procesar la humedad o la temperatura", e);
                    }
                }
            }

            if (data.contains("Lumenes:")) {
                // Extraemos el valor de LDR
                String lumens = extractValue(data, "Lumenes:");
                Log.d("SharedViewModelLumens", "Datos antes de guardar" + lumens);
                if (lumens != null) {
                    try {
                        float ldrValue = Float.parseFloat(lumens);
                        Log.d("SharedViewModelLumens", "Datos antes de guardar" + ldrValue);

                        setLdr((int) ldrValue);

                        // Limpiamos los datos procesados
                        dataIn.setValue(null);

                    } catch (NumberFormatException e) {
                        Log.e("SharedViewModel", "Error al procesar el valor del LDR", e);
                    }
                }
            }

            if (data.contains("PotValue:")) {
                // Extraemos el valor del potenciómetro
                String potValue = extractValue(data, "PotValue:");
                if (potValue != null) {
                    try {
                        int pot = Integer.parseInt(potValue);
                        setPot(pot);  // Actualizamos el valor en el ViewModel

                        // Limpiamos los datos procesados
                        dataIn.setValue(null);

                    } catch (NumberFormatException e) {
                        Log.e("SharedViewModel", "Error al procesar el potenciómetro", e);
                    }
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

    public void setCurrentFragment(int currentFragmentValue){
        this.currentFragment.setValue(currentFragmentValue);
    }

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
