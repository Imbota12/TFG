package com.example.tfg_sistematienda.vistas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
    private ProgressDialog progressDialog;
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

        estadisticasProductos.setOnClickListener(v -> new LoadNewActivityTask(usuario, EstadisticasProductos.class, "Acceso estadisticas productos").execute());
        ingresos.setOnClickListener(v -> new LoadNewActivityTask(usuario, Balance.class, "Acceso a ingresos").execute());
        logs.setOnClickListener(v -> new LoadNewActivityTask(usuario, Logs.class, "Acceso a logs").execute());
        administrarTiendas.setOnClickListener(v -> new LoadNewActivityTask(usuario, ListaTiendas.class, "Acceso lista tiendas").execute());

        anadirEmpleado.setOnClickListener(v -> {
            if (bbddController.comprobarRegistrosTienda()) {
                new LoadNewActivityTask(usuario, CrearUsuario.class, "Acceso formulario añadir empleados").execute();
            } else {
                Toast.makeText(GeneralAdmin.this, "Debes crear una tienda primero", Toast.LENGTH_SHORT).show();
            }
        });

        anadirTienda.setOnClickListener(v -> new LoadNewActivityTask(usuario, CrearTienda.class, "Acceso formulario creacion tienda").execute());
        administrarEmpleados.setOnClickListener(v -> new LoadNewActivityTask(usuario, ListaEmpleados.class, "Acceso lista empleados").execute());
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

    private class LoadNewActivityTask extends AsyncTask<Void, Void, Void> {

        private UsuarioModel usuario;
        private Class<?> nextActivity;
        private String logMessage;

        public LoadNewActivityTask(UsuarioModel usuario, Class<?> nextActivity, String logMessage) {
            this.usuario = usuario;
            this.nextActivity = nextActivity;
            this.logMessage = logMessage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(GeneralAdmin.this);
            progressDialog.setMessage("Cargando...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            bbddController.insertarLog(logMessage, LocalDateTime.now(), usuario.getDni());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Intent intent = new Intent(GeneralAdmin.this, nextActivity);
            intent.putExtra("usuarioDNI", usuario.getDni());
            startActivity(intent);
        }
    }
}