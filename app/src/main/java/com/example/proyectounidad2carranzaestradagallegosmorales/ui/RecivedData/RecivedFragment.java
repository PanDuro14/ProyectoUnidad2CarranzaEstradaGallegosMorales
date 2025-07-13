package com.example.proyectounidad2carranzaestradagallegosmorales.ui.RecivedData;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.R;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentRecivedBinding;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.humedad.humedadFragment;
import com.example.proyectounidad2carranzaestradagallegosmorales.ui.temperatura.temperaturaFragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class RecivedFragment extends Fragment {
    private FragmentRecivedBinding binding;
    private SharedViewModel sharedViewModel;
    Integer currentFragment = 2;
    private boolean isMessageSent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecivedBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inicializar el ViewModel
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();

            // Si está conectado a un dispositivo
            if (Boolean.TRUE.equals(isConnected)) {
                // Enviar el fragmento al ESP32
                if(!isMessageSent){
                    enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                    isMessageSent = true;
                }

                int humedad = sharedViewModel.getHum().getValue();
                int temperatura = sharedViewModel.getTemp().getValue();

                String info = "Temperatura: " + temperatura + " C Humedad: " + humedad + "%";
                Log.d("Información", "onCreateView: " + info);

                binding.tvDataRx.setText("Temperatura: " + temperatura + " C Humedad: " + humedad + "%");

                // Ocultar el mensaje de "No hay dispositivo conectado"
                binding.tvNoDeviceConnected.setVisibility(View.GONE);

            } else {
                // Si no se reciben datos, mostrar el mensaje de espera
                binding.tvDataRx.setText("Esperando datos...");
                // Mostrar el mensaje de "No hay dispositivo conectado"
                binding.tvNoDeviceConnected.setVisibility(View.VISIBLE);
                isMessageSent = false;
            }
        });

        binding.btnTemp.setOnClickListener(v -> showFragment(new temperaturaFragment()));
        binding.humBtn.setOnClickListener(v -> showFragment(new humedadFragment()));

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void enviarMensaje(String mensaje){
        mensaje = mensaje.trim();
        if(!mensaje.isEmpty()){
            ((MainActivity) requireActivity()).enviarDatosBT(mensaje);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedViewModel.setCurrentFragment(0);
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        isMessageSent = false;
    }
    @Override
    public void onResume() {
        super.onResume();
        sharedViewModel.setCurrentFragment(currentFragment);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedViewModel.setCurrentFragment(0);
        enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
        isMessageSent = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedViewModel.setCurrentFragment(0);
    }
}
