package com.example.proyectounidad2carranzaestradagallegosmorales.ui.RecivedData;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentRecivedBinding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class RecivedFragment extends Fragment {
    private FragmentRecivedBinding binding;
    private SharedViewModel sharedViewModel;

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

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();
            if(Boolean.TRUE.equals(isConnected) && data != null && !data.isEmpty()){
                int humedad = sharedViewModel.getHum().getValue();
                int temperatura = sharedViewModel.getTemp().getValue();

                String info = "Temperatura: " + temperatura  +" C Humedad: " + humedad + "%";
                Log.d("Información", "onCreateView: " + info);

                binding.tvDataRx.setText("Temperatura: " + temperatura  +" C Humedad: " + humedad + "%");

            } else {
                binding.tvDataRx.setText("Esperando datos...");
            }
        });
        return view;
    }
}