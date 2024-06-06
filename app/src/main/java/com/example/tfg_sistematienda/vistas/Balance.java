package com.example.tfg_sistematienda.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.text.DecimalFormat;

public class Balance extends AppCompatActivity {

    private TextView total_devo, total_venta, total_ganado;
    private BBDDController bbddController= new BBDDController();
    private UsuarioModel usuario;
    private ImageButton volverMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_balance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        volverMenu = findViewById(R.id.volvermenuIngresos);

        volverMenu.setOnClickListener(v -> {
            Intent intent1 = new Intent(Balance.this, GeneralAdmin.class);
            intent1.putExtra("usuarioDNI", usuarioDNI);
            startActivity(intent1);
            finish();
        });
        total_devo = findViewById(R.id.total_devo);
        total_venta = findViewById(R.id.total_ventas);
        total_ganado = findViewById(R.id.total_gana);


        DecimalFormat df = new DecimalFormat("#.00");
        double devo = bbddController.obtenerSumaDevoluciones();
        double venta = bbddController.obtenerSumaVentas();
        double total = venta-devo;

        total_devo.setText(String.valueOf(df.format(devo)));
        total_venta.setText(String.valueOf(df.format(venta)));
        total_ganado.setText(String.valueOf(df.format(total)));


    }
}

