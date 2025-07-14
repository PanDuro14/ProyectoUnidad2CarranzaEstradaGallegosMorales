package com.example.proyectounidad2carranzaestradagallegosmorales.ui.temperatura;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//Esta madre la usamos para crear la animacion del termometro
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.example.proyectounidad2carranzaestradagallegosmorales.MainActivity;
import com.example.proyectounidad2carranzaestradagallegosmorales.SharedViewModel;
import com.example.proyectounidad2carranzaestradagallegosmorales.databinding.FragmentTemperaturaBinding;

public class temperaturaFragment extends Fragment {
    private FragmentTemperaturaBinding binding;
    private SharedViewModel sharedViewModel;
    Integer currentFragment = 2;
    private boolean isMessageSent = false;

    // Rango de temperaturas (ajusta según tus necesidades)
    private static final int MIN_TEMP = -10;
    private static final int MAX_TEMP = 50;

    // Altura máxima del mercurio (debe ser menor que la altura del termómetro)
    private static final int MAX_MERCURY_HEIGHT = 280; // en dp

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTemperaturaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.setCurrentFragment(currentFragment);


        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            Boolean isConnected = sharedViewModel.getIsConnected().getValue();

            if(Boolean.TRUE.equals(isConnected)){
                if(!isMessageSent){
                    enviarMensaje(sharedViewModel.getCurrentFragment().getValue().toString());
                    isMessageSent = true;
                }

                int temperatura = sharedViewModel.getTemp().getValue();
                String info = "Temperatura " + temperatura + " °C";

                binding.tvTemp.setText(info);

                // Actualizar la barra de mercurio
                updateMercuryLevel(temperatura);
            } else {
                binding.tvTemp.setText("Esperando datos...");
                isMessageSent = false;
            }
        });

        return view;
    }



    private void updateMercuryLevel(int temperature) {
        // Calcular la altura proporcional a la temperatura
        float percentage = (float)(temperature - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
        percentage = Math.max(0, Math.min(1, percentage)); // Asegurar que está entre 0 y 1

        int targetHeight = (int)(percentage * MAX_MERCURY_HEIGHT);

        // Convertir dp a píxeles
        final float scale = getResources().getDisplayMetrics().density;
        int targetHeightPx = (int)(targetHeight * scale + 0.5f);

        // Obtener la altura actual
        int currentHeight = binding.mercuryBar.getHeight();

        // Crear animación
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                binding.mercuryBar.getLayoutParams().height =
                        currentHeight + (int)((targetHeightPx - currentHeight) * interpolatedTime);
                binding.mercuryBar.requestLayout();
            }
        };

        animation.setDuration(500); // Duración de la animación en milisegundos
        binding.mercuryBar.startAnimation(animation);
    }

    // Resto del código permanece igual...
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