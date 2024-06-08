package com.example.tfg_sistematienda.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

    // Declaración de variables para los elementos de la interfaz y el controlador de base de datos
    private TextView total_devo, total_venta, total_ganado;
    private BBDDController bbddController = new BBDDController();
    private UsuarioModel usuario;
    private ImageButton volverMenu;
    private boolean allowBackPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar Edge to Edge para la Activity
        EdgeToEdge.enable(this);

        // Establecer el layout de la Activity
        setContentView(R.layout.activity_balance);

        // Ajustar el padding de la vista principal para tener en cuenta las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el Intent que inició esta Activity
        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        // Obtener los datos del usuario utilizando el controlador de base de datos
        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        // Encontrar la vista del botón 'volverMenu' y configurar su listener
        volverMenu = findViewById(R.id.volvermenuIngresos);
        volverMenu.setOnClickListener(v -> {
            // Crear un Intent para iniciar la Activity GeneralAdmin y pasar el usuarioDNI
            Intent intent1 = new Intent(Balance.this, GeneralAdmin.class);
            intent1.putExtra("usuarioDNI", usuarioDNI);
            startActivity(intent1);
            // Finalizar la Activity actual
            finish();
        });

        // Encontrar las vistas de los TextView para mostrar los totales
        total_devo = findViewById(R.id.total_devo);
        total_venta = findViewById(R.id.total_ventas);
        total_ganado = findViewById(R.id.total_gana);

        // Formatear los valores decimales
        DecimalFormat df = new DecimalFormat("#.00");

        // Obtener las sumas de devoluciones y ventas desde la base de datos
        double devo = bbddController.obtenerSumaDevoluciones();
        double venta = bbddController.obtenerSumaVentas();
        double total = venta - devo;

        // Mostrar los valores en los TextView correspondientes
        total_devo.setText(String.valueOf(df.format(devo)));
        total_venta.setText(String.valueOf(df.format(venta)));
        total_ganado.setText(String.valueOf(df.format(total)));
    }

    @Override
    public void onBackPressed() {
        // Controlar el comportamiento del botón de retroceso
        if (allowBackPress) {
            super.onBackPressed(); // Permitir el retroceso
        } else {
            // Mostrar un mensaje indicando al usuario que use el botón 'VOLVER MENÚ'
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }
}
