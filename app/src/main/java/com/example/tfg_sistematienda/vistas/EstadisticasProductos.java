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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorMasDevueltos;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorMasVendidos;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;
import java.util.List;

public class EstadisticasProductos extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController = new BBDDController();
    private ImageButton volverMenu;
    private RecyclerView masVendidos, masDevueltos;
    private AdaptadorMasVendidos adaptadorMasVendidos;
    private AdaptadorMasDevueltos adaptadorMasDevueltos;
    private List<ProductoModel> listaMasVendidos;
    private List<ProductoModel> listaMasDevueltos;
    private boolean allowBackPress = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_productos);
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
        volverMenu = findViewById(R.id.volvermenuIngresos);
        masDevueltos = findViewById(R.id.masdevueltos);
        masVendidos = findViewById(R.id.masvendidos);
        // Configurar el diseño del RecyclerView
        masDevueltos.setLayoutManager(new LinearLayoutManager(this));
        masVendidos.setLayoutManager(new LinearLayoutManager(this));
        // Cargar los datos en segundo plano
        new LoadDataAsyncTask().execute();
        // Configurar el OnClickListener para el botón de volver al menú
        volverMenu.setOnClickListener(v -> {
            // Registrar el acceso al menú de administrador
            bbddController.insertarLog("Acceso a menu admin", LocalDateTime.now(), usuario.getDni());
            Intent intent1 = new Intent(EstadisticasProductos.this, GeneralAdmin.class);
            intent1.putExtra("usuarioDNI", usuarioDNI);
            startActivity(intent1);
        });
    }

    @Override
    public void onBackPressed() {
        // Controlar el comportamiento del botón de retroceso
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            // Mostrar un mensaje indicando cómo volver al menú principal
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

    // Clase interna para cargar los datos en segundo plano
    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar un ProgressDialog mientras se cargan los datos
            progressDialog = new ProgressDialog(EstadisticasProductos.this);
            progressDialog.setMessage("Cargando datos...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Obtener la lista de productos más devueltos y más vendidos desde la base de datos
            listaMasDevueltos = bbddController.obtenerProductosMasDevueltos();
            listaMasVendidos = bbddController.obtenerProductosMasVendidos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Ocultar el ProgressDialog al completarse la carga de datos
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // Configurar el adaptador para la lista de productos más devueltos
            adaptadorMasDevueltos = new AdaptadorMasDevueltos(EstadisticasProductos.this, listaMasDevueltos);
            masDevueltos.setAdapter(adaptadorMasDevueltos);
            // Configurar el adaptador para la lista de productos más vendidos
            adaptadorMasVendidos = new AdaptadorMasVendidos(EstadisticasProductos.this, listaMasVendidos);
            masVendidos.setAdapter(adaptadorMasVendidos);
        }
    }
}
