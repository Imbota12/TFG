package com.example.tfg_sistematienda.vistas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorEmpleado;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.util.List;

public class ListaEmpleados extends AppCompatActivity {
    private RecyclerView lista;
    private AdaptadorEmpleado adaptadorEmpleado;
    private List<UsuarioModel> listaEmpleados;
    private BBDDController bbddController = new BBDDController();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_empleados);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lista = findViewById(R.id.rv_listaEmpleados);
        lista.setLayoutManager(new LinearLayoutManager(this));

        cargarEmpleados();

        adaptadorEmpleado = new AdaptadorEmpleado(this, listaEmpleados);
        lista.setAdapter(adaptadorEmpleado);

    }
    private void cargarEmpleados() {
        listaEmpleados = bbddController.obtenerListaEmpleados();
    }
}