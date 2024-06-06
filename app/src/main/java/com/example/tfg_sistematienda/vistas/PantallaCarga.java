package com.example.tfg_sistematienda.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;

public class PantallaCarga extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 1300; // Duración en milisegundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_carga);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView = findViewById(R.id.imageView4);

        // Aquí estableces la imagen que tienes en tu carpeta res/drawable
        imageView.setImageResource(R.mipmap.paginaentrada);

        // Usar un Handler para retrasar el inicio de la próxima actividad
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Este método se ejecutará después de SPLASH_TIME_OUT
                Intent intent = new Intent(PantallaCarga.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}