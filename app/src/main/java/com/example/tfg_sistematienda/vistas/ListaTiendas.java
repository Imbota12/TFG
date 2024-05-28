package com.example.tfg_sistematienda.vistas;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorTienda;
import com.example.tfg_sistematienda.BBDD.ConexionBBDD;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.util.List;

public class ListaTiendas extends AppCompatActivity {

    private UsuarioModel usuario;
    private ConexionBBDD bbddController=new ConexionBBDD();

    private RecyclerView lista;
    private AdaptadorTienda adaptadorTienda;
    private List<TiendaModel> listaTiendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_tiendas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        lista = findViewById(R.id.listaTienda);
        lista.setLayoutManager(new LinearLayoutManager(this));

        cargarTiendas();

        adaptadorTienda = new AdaptadorTienda(listaTiendas, this);
        lista.setAdapter(adaptadorTienda);
    }

    private void cargarTiendas() {
        listaTiendas = bbddController.obtenerListaTiendas();
    }
}