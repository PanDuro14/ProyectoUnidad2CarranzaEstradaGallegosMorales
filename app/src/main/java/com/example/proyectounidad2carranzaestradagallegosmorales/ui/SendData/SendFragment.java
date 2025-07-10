package com.example.proyectounidad2carranzaestradagallegosmorales.ui.SendData;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentSendBinding;

public class SendFragment extends Fragment {
    private FragmentSendBinding binding;
    private SharedViewModel sharedViewModel;

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

        sharedViewModel.getDataOut().observe(getViewLifecycleOwner(), data -> {
            String estado = " ";
            if (data == "1"){
                estado = "Rojo";
            } else if (data == "2"){
                estado = "Amarillo";
            } else if (data == "3") {
                estado = "Verde";
            } else {
                estado = "Apagado";
            }
            binding.tvLastStatus.setText("Ultimo estado: " + estado);
        });

        binding.btn1.setOnClickListener(v -> enviarMensaje("1"));
        binding.btn2.setOnClickListener(v -> enviarMensaje("2"));
        binding.btn3.setOnClickListener(v -> enviarMensaje("3"));
        binding.btn4.setOnClickListener(v -> enviarMensaje("Apagado"));

        return view;
    }

    private void enviarMensaje(String mensaje){
        mensaje = mensaje.trim();
        if(!mensaje.isEmpty()){
            ((MainActivity) requireActivity()).enviarDatosBT(mensaje);

        }
    }
}