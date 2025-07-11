package com.example.proyectounidad2carranzaestradagallegosmorales;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.ActivityMainBinding;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.BorededDevices.BorededFragment;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.RecivedData.RecivedFragment;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.SendData.SendFragment;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    // Bluetooth Options
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    // Ciclo de vida del bluetooth
    private boolean pauseDueToBluetooth = false;

    // SharedViewModel
    private SharedViewModel sharedViewModel;

    // Manejar la conexión de dispositivos
    private ConnectedThread MyConnectionBT = null;
    private static Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder dataStringIn = new StringBuilder();

    // Manejar conexión con esp32
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicalizar el sharedVIewModel y el bluetooth
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Verificar el adaptador
        if(!bluetoothAdapter.isEnabled()){
            requestBluetoothEnable();
        }

        // Registrar el receiver
        registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


        // Vista y Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Instancia del fragmento
        if(savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new BorededFragment())
                    .commit();
        }
        // Setup Tool Bar
        setupToolbar();

        // Botones de navegación
        binding.btnBorededDevices.setOnClickListener(v -> showFragment(new BorededFragment()));
        binding.btnRecivedData.setOnClickListener(v -> showFragment(new RecivedFragment()));
        binding.btnSendData.setOnClickListener(v -> showFragment(new SendFragment()));

        // Hadler para manejar la lectura de datos bluetooth
        bluetoothIn = new Handler() {
            public void handleMessage(@NonNull android.os.Message msg) {
                if (msg.what == handlerState) {
                    // Añadimos los datos recibidos al StringBuilder
                    String receivedData = (String) msg.obj;
                    Log.d("Datos", "handleMessage: " + receivedData);

                    sharedViewModel.setDataIn(receivedData);

                    // Procesamos los datos directamente sin acumular demasiados datos
                    String[] parts = receivedData.trim().split(" ");
                    if (parts.length >= 4) {
                        // Extraemos humedad y temperatura de los datos procesados
                        String humedad = parts[1].trim();  // Eliminamos espacios o tabulaciones alrededor de la humedad
                        String temperatura = parts[3].trim();  // Eliminamos espacios o tabulaciones alrededor de la temperatura

                        //Log.d("Datos", "Humedad extraída: " + humedad);
                        //Log.d("Datos", "Temperatura extraída: " + temperatura);

                        // Solo actualizamos el ViewModel si los datos son válidos
                        if (humedad != null && !humedad.isEmpty() && temperatura != null && !temperatura.isEmpty()) {
                            try {
                                // Usar Float.parseFloat para manejar valores decimales
                                float humedadValor = Float.parseFloat(humedad.replace("%", "")); // Eliminamos "%" en humedad
                                float temperaturaValor = Float.parseFloat(temperatura.replace("C", "")); // Eliminamos "C" en temperatura

                                // Solo actualizamos si los valores son válidos
                                sharedViewModel.setHum((int) humedadValor);  // Si es necesario, puedes redondear
                                sharedViewModel.setTemp((int) temperaturaValor);  // Lo mismo para la temperatura


                            } catch (NumberFormatException e) {
                                Log.e("SharedViewModel", "Error al parsear los valores de humedad o temperatura", e);
                            }
                        }
                    }
                }
            }
        };





        sharedViewModel.getDeviceAddress().observe(this, new Observer<String>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onChanged(String address) {
                if(address == null || address.equals("No hay dispositivo") || address.isEmpty()){
                    Toast.makeText(getBaseContext(), "No se ha seleccionado un dispositivo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (bluetoothSocket != null && bluetoothSocket.isConnected()){
                    if (bluetoothDevice != null && bluetoothDevice.getAddress().equals(address)){
                        Toast.makeText(getBaseContext(), "Ya estás conectado a este dispositivo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        bluetoothSocket.close();
                        sharedViewModel.setIsConnected(false);
                        Toast.makeText(getBaseContext(), "Cambiando de dispositivo...", Toast.LENGTH_SHORT).show();
                    } catch (IOException e){
                        Toast.makeText(getBaseContext(), "Error al cerrar la conexión previa", Toast.LENGTH_SHORT).show();
                    }

                }

                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                try {
                    bluetoothSocket = createBluetoothSocket(bluetoothDevice);
                    bluetoothSocket.connect();
                    MyConnectionBT = new ConnectedThread(bluetoothSocket, bluetoothIn, handlerState);
                    MyConnectionBT.start();
                    sharedViewModel.setIsConnected(true);
                } catch (IOException e){
                    Toast.makeText(getBaseContext(), "Error al conectar el socket", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            // Color morado para el fondo
            toolbar.setBackgroundColor(getResources().getColor(R.color.purple_500));
        }
    }

    private void showFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void requestBluetoothEnable(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_OFF ){
                pauseDueToBluetooth = true;
                onPause();
                requestBluetoothEnable();
            } else if (state == BluetoothAdapter.STATE_ON && pauseDueToBluetooth){
                pauseDueToBluetooth = false;
                onResume();
            }
        }
    };

    private void closeBluetootConnection(){
        try {
            if(bluetoothSocket != null && bluetoothSocket.isConnected()){
                bluetoothSocket.close();
                sharedViewModel.setIsConnected(false);
                Toast.makeText(this, "Conexión con bluetooth cerrada", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e){
            Toast.makeText(this, "Error al cerrar la conexión con Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "Bluetooth Apagado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Bluetooth Reactivado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBluetootConnection();
        unregisterReceiver(btReceiver);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private BluetoothSocket createBluetoothSocket(@NonNull BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    // Enviar información al fragmento
    public void enviarDatosBT(String mensaje){
        if (MyConnectionBT != null && bluetoothSocket != null && bluetoothSocket.isConnected()){
            MyConnectionBT.write(mensaje);
            sharedViewModel.setDataOut(mensaje);
        } else {
            Toast.makeText(this, "Bluetooth no conectado ", Toast.LENGTH_SHORT).show();
        }
    }
}