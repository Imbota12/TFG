package com.example.tfg_sistematienda.vistas;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductoDevuelto;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductoTicket;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosComprados;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosVenta;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;

import java.util.ArrayList;
import java.util.List;

public class RealizarDevolucion extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 37;
    private RecyclerView todosProductosTicket, productosDevueltos;
    private EditText codigoTicketBuscar, codigoProductoBuscar, totalDevolver;
    private Button escanearProducto, realizarDevolucion, cancelarDevolucion, escanearTicket;
    private String id_ticket, codigoEscaneado, productoEscaneado, aDevolver;
    private TextView ticket_devo;
    private AdaptadorProductoDevuelto adaptadorDevuelto;
    private List<ProductoModel> listaProductosTicket;
    private AdaptadorProductoTicket adaptadorProductoTicket;
    private List<ProductoModel> listaProductosDevueltos;
    private List<Producto_TicketModel> listaCantidades = new ArrayList<>();
    private BBDDController bbddController = new BBDDController();
    private TicketModel ticket;
    private double totalDevolucion;

    private BluetoothConnection connection;
    private EscPosPrinter printer;
    private BluetoothAdapter bluetoothAdapter;
    private double entregado, devuelto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_realizar_devolucion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        todosProductosTicket=findViewById(R.id.rv_productos_ticket);
        productosDevueltos=findViewById(R.id.rv_productos_a_devolver);

        ticket_devo=findViewById(R.id.tx_ticket_devo);
        ticket_devo.setText("");
        codigoTicketBuscar=findViewById(R.id.cod_barras_ticket);
        codigoProductoBuscar=findViewById(R.id.cod_produ_devo);
        totalDevolver=findViewById(R.id.total_a_devolver);
        totalDevolver.setText("0.00");
        totalDevolver.setEnabled(false);

        escanearProducto=findViewById(R.id.escanear_ticket);
        escanearTicket = findViewById(R.id.escanear_prod_devo);

        realizarDevolucion = findViewById(R.id.realizar_devolucion);
        cancelarDevolucion= findViewById(R.id.cancelar_devolucion);

        connection = null;
        printer = null;

        listaProductosDevueltos=new ArrayList<>();
        cargarProductos();

        //todosProductosTicket.



    }

    private void cargarProductos() {
        listaProductosTicket = bbddController.obtenerListaProductos();
    }
}