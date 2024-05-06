package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorProducto;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosComprados;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosVenta;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RealizaVenta extends AppCompatActivity implements AdaptadorProductosVenta.OnProductoSeleccionadoListener {

    private RecyclerView todosProductos, productosComprados;
    private EditText codigoBuscar, idTicket;
    private Button escanearProducto, realizarVenta, cancelarVenta;
    private TextView precioTotal;

    private String id_ticket;
    private AdaptadorProductosComprados adaptadorComprados;
    private List<ProductoModel> listaTodosProductos;

    private AdaptadorProductosVenta adaptadorTodos;
    private List<ProductoModel> listaProductosComprados;

    private List<Producto_TicketModel> listaCantidades= new ArrayList<>();

    private BBDDController bbddController = new BBDDController();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_realiza_venta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        todosProductos = findViewById(R.id.rv_todos);
        productosComprados = findViewById(R.id.rv_comprados);
        idTicket = findViewById(R.id.id_ticket);
        idTicket.setEnabled(false);

        listaProductosComprados = new ArrayList<>();

        cargarProductos();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        todosProductos.setLayoutManager(layoutManager);

        adaptadorTodos = new AdaptadorProductosVenta(this, listaTodosProductos, this);
        todosProductos.setAdapter(adaptadorTodos);

        productosComprados.setLayoutManager(new LinearLayoutManager(this));
        adaptadorComprados = new AdaptadorProductosComprados(this, listaProductosComprados);
        productosComprados.setAdapter(adaptadorComprados);

        List<String> listaCodigosBarras = obtenerListaIdDesdeBD();

        // Generar un código de barras único
        String nuevoIdBarras;
        do {
            nuevoIdBarras = generarIdUnico();
        } while (listaCodigosBarras.contains(nuevoIdBarras));

        // Mostrar el nuevo código de barras
        id_ticket = nuevoIdBarras;
        idTicket.setText(id_ticket);

    }

    private List<String> obtenerListaIdDesdeBD() {
        List<String> codigos = bbddController.obtenerListaIds();
        return codigos;
    }

    private String generarIdUnico() {
        Random random = new Random();
        StringBuilder numeroAleatorio = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int digito = random.nextInt(10);
            numeroAleatorio.append(digito);
        }
        numeroAleatorio.append("TC");
        return numeroAleatorio.toString();
    }

    private void cargarProductos() {
        listaTodosProductos = bbddController.obtenerListaProductos();
    }

    @Override
    public void onProductoSeleccionado(ProductoModel producto) {
        boolean productoExistente = false;
        Iterator<ProductoModel> iterator = listaProductosComprados.iterator();

        while (iterator.hasNext()) {
            ProductoModel p = iterator.next();

            // Si el producto seleccionado ya está en la lista de productos comprados
            if (p.getCodigoBarras() == producto.getCodigoBarras()) {
                // Verificar si la cantidad a mostrar es mayor que la cantidad en stock
                if ((p.getCantidad() + 1) > p.getCantidadStock()) {
                    // Mostrar un AlertDialog de advertencia
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Alerta");
                    builder.setMessage("No hay suficiente stock disponible para este producto.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cerrar el diálogo o ejecutar alguna acción adicional si es necesario
                        }
                    });
                    builder.show();
                    return; // Detener el proceso
                }
                // Aumenta la cantidad del producto existente
                p.setCantidad(p.getCantidad() + 1);
                productoExistente = true;

                // Actualizar la cantidad en listaCantidades
                for (Producto_TicketModel productoTicket : listaCantidades) {
                    if (productoTicket.getCodigoBarras_producto() == producto.getCodigoBarras()) {
                        productoTicket.setCantidad(productoTicket.getCantidad() + 1);
                        break;
                    }
                }
                break; // Termina el bucle ya que ya hemos encontrado el producto
            }
        }

// Si el producto seleccionado no existe en la lista de productos comprados, agrégalo
        if (!productoExistente) {
            listaProductosComprados.add(producto);
            listaCantidades.add(new Producto_TicketModel(producto.getCodigoBarras(), id_ticket, producto.getCantidad()));
        }


// Notificar al adaptador de productos comprados de los cambios
        adaptadorComprados.notifyDataSetChanged();
    }
}