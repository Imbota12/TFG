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

import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;

public class GeneralAdmin extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController= new BBDDController();
    private boolean allowBackPress = false;
    private ImageButton administrarTiendas, administrarEmpleados, anadirEmpleado, anadirTienda, cerrarSesion, estadisticasProductos, logs, ingresos;
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
        cerrarSesion= findViewById(R.id.cerrar_sesion_admin);
        estadisticasProductos = findViewById(R.id.estadisticasProductos);
        logs = findViewById(R.id.logs);
        ingresos = findViewById(R.id.ingresos);


        cerrarSesion.setOnClickListener(v -> cerrarSesion());

        estadisticasProductos.setOnClickListener(v ->{
            bbddController.insertarLog("Acceso estadisticas productos", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, EstadisticasProductos.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });

        ingresos.setOnClickListener(v ->{
            bbddController.insertarLog("Acceso a ingresos", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, Balance.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });

        logs.setOnClickListener(v ->{
            bbddController.insertarLog("Acceso a logs", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, Logs.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });

        administrarTiendas.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso lista tiendas", LocalDateTime.now(), usuario.getDni());
            Intent i = new Intent(GeneralAdmin.this, ListaTiendas.class);
            i.putExtra("usuarioDNI", usuarioDNI);
            startActivity(i);
        });
        anadirEmpleado.setOnClickListener(v -> {
            if (bbddController.comprobarRegistrosTienda()) {
                bbddController.insertarLog("Acceso formulario añadir empleados", LocalDateTime.now(), usuario.getDni());
                Intent i = new Intent(GeneralAdmin.this, CrearUsuario.class);
                i.putExtra("usuarioDNI", usuarioDNI);
                startActivity(i);
            }else{
                Toast.makeText(GeneralAdmin.this, "Debes crear una tienda primero", Toast.LENGTH_SHORT).show();
            }
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
    private void cerrarSesion() {
        finish();
        bbddController.insertarLog("Cierre sesión", LocalDateTime.now(), usuario.getDni());
        Intent intent = new Intent(GeneralAdmin.this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "No puedes retroceder. Por favor, cierra sesión primero.", Toast.LENGTH_SHORT).show();
        }
    }
}