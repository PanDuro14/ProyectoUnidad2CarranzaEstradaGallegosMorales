package com.example.proyectounidad2carranzaestradagallegosmorales.ui.FotoResistenciaData;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentFotoResistenciaDataBinding;

public class FotoResistenciaDataFragment extends Fragment {
    private FragmentFotoResistenciaDataBinding binding;
    private SharedViewModel sharedViewModel;
    Integer currentFragment = 4;
    private boolean isMessageSent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFotoResistenciaDataBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();

            if(Boolean.TRUE.equals(isConnected)){
                if(!isMessageSent){
                    enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                    isMessageSent = true;
                }

                int ldrValue = sharedViewModel.getLdr().getValue();
                Log.d("LDR", "LDR valor obtenido " + ldrValue);
                binding.tvLdrValue.setText("LDR: " + ldrValue);
                if(ldrValue > 1){
                    view.setBackgroundColor(Color.YELLOW);
                    binding.tvLdrValue.setTextColor(Color.BLACK);
                    binding.tvLdrValue.setTextSize((ldrValue)*5);
                } else {
                    view.setBackgroundColor(Color.BLACK);
                    binding.tvLdrValue.setTextColor(Color.WHITE);
                    binding.tvLdrValue.setTextSize(24);
                }

            } else {
                binding.tvLdrValue.setText("Esperando datos...");
                isMessageSent = false;
            }
        });
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