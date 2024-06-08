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
    private BBDDController bbddController= new BBDDController();
    private ImageButton volverMenu;
    private RecyclerView masVendidos, masDevueltos;
    private AdaptadorMasVendidos adaptadorMasVendidos;
    private AdaptadorMasDevueltos adaptadorMasDevueltos;
    private List<ProductoModel> listaMasVendidos;
    private List<ProductoModel> listaMasDevueltos;
    private boolean allowBackPress=false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_productos);
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

        masDevueltos= findViewById(R.id.masdevueltos);
        masVendidos= findViewById(R.id.masvendidos);

        masDevueltos.setLayoutManager(new LinearLayoutManager(this));
        masVendidos.setLayoutManager(new LinearLayoutManager(this));

        new LoadDataAsyncTask().execute();

        volverMenu.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso a menu admin", LocalDateTime.now(), usuario.getDni());
            Intent intent1 = new Intent(EstadisticasProductos.this, GeneralAdmin.class);
            intent1.putExtra("usuarioDNI", usuarioDNI);
            startActivity(intent1);
        });
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EstadisticasProductos.this);
            progressDialog.setMessage("Cargando datos...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            listaMasDevueltos = bbddController.obtenerProductosMasDevueltos();
            listaMasVendidos = bbddController.obtenerProductosMasVendidos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            adaptadorMasDevueltos = new AdaptadorMasDevueltos(EstadisticasProductos.this, listaMasDevueltos);
            masDevueltos.setAdapter(adaptadorMasDevueltos);

            adaptadorMasVendidos = new AdaptadorMasVendidos(EstadisticasProductos.this, listaMasVendidos);
            masVendidos.setAdapter(adaptadorMasVendidos);
        }
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

}