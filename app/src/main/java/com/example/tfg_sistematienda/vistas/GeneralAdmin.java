package com.example.tfg_sistematienda.vistas;

import android.content.Intent;
import android.os.Bundle;
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

import java.time.LocalDateTime;

public class GeneralAdmin extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController= new BBDDController();

    private ImageButton administrarTiendas, administrarEmpleados, anadirEmpleado, anadirTienda;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        administrarTiendas = findViewById(R.id.adminTiendas);
        administrarEmpleados = findViewById(R.id.adminEmpleados);
        anadirEmpleado = findViewById(R.id.anadirEmpleado);
        anadirTienda = findViewById(R.id.anadirTienda);

        administrarTiendas.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso lista tiendas", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, ListaTiendas.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });
        anadirEmpleado.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso formulario añadir empleados", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, CrearUsuario.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });
        anadirTienda.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso formulario creacion tienda", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, CrearTienda.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });

        administrarEmpleados.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso lista empleados", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, ListaEmpleados.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });

    }
}