package com.example.tfg_sistematienda.vistas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageButton;
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
    private BBDDController bbddController = new BBDDController();
    private boolean allowBackPress = false;
    private ProgressDialog progressDialog;
    private ImageButton administrarTiendas, administrarEmpleados, anadirEmpleado, anadirTienda, cerrarSesion, estadisticasProductos, logs, ingresos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_admin);
        // Configuración del margen para EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el DNI del usuario enviado desde MainActivity
        Intent intent = getIntent();
        String usuarioDNI = intent.getStringExtra("usuarioDNI");
        // Obtener el usuario correspondiente al DNI
        usuario = bbddController.obtenerEmpleado(usuarioDNI);
        // Referenciar los elementos de la interfaz de usuario
        administrarTiendas = findViewById(R.id.adminTiendas);
        administrarEmpleados = findViewById(R.id.adminEmpleados);
        anadirEmpleado = findViewById(R.id.anadirEmpleado);
        anadirTienda = findViewById(R.id.anadirTienda);
        cerrarSesion = findViewById(R.id.cerrar_sesion_admin);
        estadisticasProductos = findViewById(R.id.estadisticasProductos);
        logs = findViewById(R.id.logs);
        ingresos = findViewById(R.id.ingresos);

        // Configurar el OnClickListener para el botón de cerrar sesión
        cerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Configurar el OnClickListener para el botón de estadísticas de productos
        estadisticasProductos.setOnClickListener(v -> new LoadNewActivityTask(usuario, EstadisticasProductos.class, "Acceso estadisticas productos").execute());
        // Configurar el OnClickListener para el botón de ingresos
        ingresos.setOnClickListener(v -> new LoadNewActivityTask(usuario, Balance.class, "Acceso a ingresos").execute());
        // Configurar el OnClickListener para el botón de logs
        logs.setOnClickListener(v -> new LoadNewActivityTask(usuario, Logs.class, "Acceso a logs").execute());
        // Configurar el OnClickListener para el botón de administrar tiendas
        administrarTiendas.setOnClickListener(v -> new LoadNewActivityTask(usuario, ListaTiendas.class, "Acceso lista tiendas").execute());

        // Configurar el OnClickListener para el botón de añadir empleado
        anadirEmpleado.setOnClickListener(v -> {
            // Verificar si hay registros de tiendas
            if (bbddController.comprobarRegistrosTienda()) {
                new LoadNewActivityTask(usuario, CrearUsuario.class, "Acceso formulario añadir empleados").execute();
            } else {
                Toast.makeText(GeneralAdmin.this, "Debes crear una tienda primero", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el OnClickListener para el botón de añadir tienda
        anadirTienda.setOnClickListener(v -> new LoadNewActivityTask(usuario, CrearTienda.class, "Acceso formulario creacion tienda").execute());
        // Configurar el OnClickListener para el botón de administrar empleados
        administrarEmpleados.setOnClickListener(v -> new LoadNewActivityTask(usuario, ListaEmpleados.class, "Acceso lista empleados").execute());
    }

    // Método para cerrar sesión
    private void cerrarSesion() {
        // Finalizar la actividad y volver a la actividad de inicio de sesión
        finish();
        // Registrar el cierre de sesión en los logs
        bbddController.insertarLog("Cierre sesión", LocalDateTime.now(), usuario.getDni());
        Intent intent = new Intent(GeneralAdmin.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Controlar el comportamiento del botón de retroceso
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            // Mostrar un mensaje indicando cómo cerrar sesión
            Toast.makeText(this, "No puedes retroceder. Por favor, cierra sesión primero.", Toast.LENGTH_SHORT).show();
        }
    }

    // Clase interna para cargar una nueva actividad en segundo plano
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
            // Mostrar un ProgressDialog mientras se carga la nueva actividad
            progressDialog = new ProgressDialog(GeneralAdmin.this);
            progressDialog.setMessage("Cargando...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Insertar un log indicando el acceso a la nueva actividad
            bbddController.insertarLog(logMessage, LocalDateTime.now(), usuario.getDni());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Ocultar el ProgressDialog al completarse la carga de la nueva actividad
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // Crear un intent para iniciar la nueva actividad
            Intent intent = new Intent(GeneralAdmin.this, nextActivity);
            intent.putExtra("usuarioDNI", usuario.getDni());
            startActivity(intent);
        }
    }
}
