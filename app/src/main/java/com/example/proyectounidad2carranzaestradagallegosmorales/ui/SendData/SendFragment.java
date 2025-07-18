package com.example.proyectounidad2carranzaestradagallegosmorales.ui.SendData;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentSendBinding;

public class SendFragment extends Fragment {
    private FragmentSendBinding binding;
    private SharedViewModel sharedViewModel;
    Integer currentFragment = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSendBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataOut().observe(getViewLifecycleOwner(), data -> {
            Log.d("SendFragment", "Data: " + data);
            String estado = " ";
            if (data.equals("3_1")){
                estado = "Secuencia 1";
            } else if (data.equals("3_2")){
                estado = "Secuencia 2";
            } else if (data.equals("3_3")) {
                estado = "Secuencia 3";
            } else {
                estado = "Apagado";
            }
            binding.tvLastStatus.setText("Ultimo estado: " + estado);
        });

        binding.btn1.setOnClickListener(v -> enviarMensaje(currentFragment + "_" + "1"));
        binding.btn2.setOnClickListener(v -> enviarMensaje(currentFragment + "_" + "2"));
        binding.btn3.setOnClickListener(v -> enviarMensaje(currentFragment + "_" + "3"));
        binding.btn4.setOnClickListener(v -> enviarMensaje(currentFragment + "_" + "Apagado"));

        return view;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedViewModel.setCurrentFragment(0);
    }
}