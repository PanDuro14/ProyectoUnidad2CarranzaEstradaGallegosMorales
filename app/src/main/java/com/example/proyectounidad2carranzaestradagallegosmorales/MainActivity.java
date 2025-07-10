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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.ActivityMainBinding;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.BorededDevices.BorededFragment;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.RecivedData.RecivedFragment;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.SendData.SendFragment;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private boolean pauseDueToBluetooth = false;
    private ConnectedThread MyConnectionBT = null;
    private static Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder dataStringIn = new StringBuilder();
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuración de la Toolbar y menú
        setupToolbar();

        // Configuración de botones
        setupButtons();

        // Inicialización de Bluetooth
        initializeBluetooth();

        // Configuración del ViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Fragmento inicial
        if (savedInstanceState == null) {
            showFragment(new BorededFragment());
        }

        // Handler para datos Bluetooth
        setupBluetoothHandler();

        // Observador para cambios en la dirección del dispositivo
        setupDeviceObserver();
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

    private void setupButtons() {
        // Botón Boreded Devices
        Button btnBoreded = binding.btnBorededDevices;
        btnBoreded.setOnClickListener(v -> showFragment(new BorededFragment()));

        // Botón Received Data
        Button btnReceived = binding.btnRecivedData;
        btnReceived.setOnClickListener(v -> showFragment(new RecivedFragment()));

        // Botón Send Data
        Button btnSend = binding.btnSendData;
        btnSend.setOnClickListener(v -> showFragment(new SendFragment()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_bonded_devices) {
            showFragment(new BorededFragment());
            return true;
        } else if (id == R.id.menu_received_data) {
            showFragment(new RecivedFragment());
            return true;
        } else if (id == R.id.menu_send_data) {
            showFragment(new SendFragment());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
        }

        registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    private void setupBluetoothHandler() {
        bluetoothIn = new Handler() {
            public void handleMessage(@NonNull android.os.Message msg) {
                if (msg.what == handlerState) {
                    dataStringIn.append((String) msg.obj);
                    int endOfLineIndex = dataStringIn.indexOf(" ");
                    if (endOfLineIndex > 0) {
                        String dataInPrint = dataStringIn.substring(0, endOfLineIndex + 1);
                        sharedViewModel.setDataIn(dataInPrint);
                        dataStringIn.delete(0, dataInPrint.length());
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void setupDeviceObserver() {
        sharedViewModel.getDeviceAddress().observe(this, address -> {
            if (address == null || address.equals("No hay dispositivo") || address.isEmpty()) {
                Toast.makeText(getBaseContext(), "No se ha seleccionado un dispositivo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                if (bluetoothDevice != null && bluetoothDevice.getAddress().equals(address)) {
                    Toast.makeText(getBaseContext(), "Ya estás conectado a este dispositivo", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    bluetoothSocket.close();
                    sharedViewModel.setIsConnected(false);
                    Toast.makeText(getBaseContext(), "Cambiando de dispositivo...", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Error al cerrar la conexión previa", Toast.LENGTH_SHORT).show();
                }
            }

            try {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                bluetoothSocket = createBluetoothSocket(bluetoothDevice);
                bluetoothSocket.connect();
                MyConnectionBT = new ConnectedThread(bluetoothSocket, bluetoothIn, handlerState);
                MyConnectionBT.start();
                sharedViewModel.setIsConnected(true);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Error al conectar el socket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @SuppressLint("MissingPermission")
    private void requestBluetoothEnable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (state == BluetoothAdapter.STATE_OFF) {
                pauseDueToBluetooth = true;
                onPause();
                requestBluetoothEnable();
            } else if (state == BluetoothAdapter.STATE_ON && pauseDueToBluetooth) {
                pauseDueToBluetooth = false;
                onResume();
            }
        }
    };

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(@NonNull BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void enviarDatosBT(String mensaje) {
        if (MyConnectionBT != null && bluetoothSocket != null && bluetoothSocket.isConnected()) {
            MyConnectionBT.write(mensaje);
            sharedViewModel.setDataOut(mensaje);
        } else {
            Toast.makeText(this, "Bluetooth no conectado", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeBluetoothConnection() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothSocket.close();
                sharedViewModel.setIsConnected(false);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al cerrar la conexión Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "Aplicación en pausa", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Aplicación activa", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBluetoothConnection();
        unregisterReceiver(btReceiver);
    }
}