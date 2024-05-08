package com.example.tfg_sistematienda.vistas;

import static com.example.tfg_sistematienda.vistas.ListaInventario.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RealizarDevolucion extends AppCompatActivity implements AdaptadorProductoTicket.OnProductoSeleccionadoListener, AdaptadorProductoDevuelto.OnProductRemovedListener, AdaptadorProductoDevuelto.OnQuantityChangedListener, AdaptadorProductoDevuelto.OnQuantityChangedListenerUp {

    private static final int REQUEST_ENABLE_BT = 37;
    private RecyclerView todosProductosTicket, productosDevueltos;
    private EditText  codigoProductoBuscar, totalDevolver;
    private AutoCompleteTextView codigoTicketBuscar;
    private Button escanearProducto, realizarDevolucion, cancelarDevolucion, escanearTicket, buscarTicket;
    private String id_ticket, codigoEscaneado, productoEscaneado, aDevolver;
    private TextView ticket_devo;
    private AdaptadorProductoDevuelto adaptadorDevuelto;
    private List<ProductoModel> listaProductosTicket;
    private AdaptadorProductoTicket adaptadorProductoTicket;
    private List<ProductoModel> listaProductosDevueltos=new ArrayList<>();
    private List<Producto_TicketModel> listaCantidades = new ArrayList<>();
    private BBDDController bbddController = new BBDDController();
    private TicketModel ticket;
    private List<String> codigosBarrasTickets=new ArrayList<>();
    private double totalDevolucion;

    private BluetoothConnection connection;
    private EscPosPrinter printer;
    private BluetoothAdapter bluetoothAdapter;
    private double entregado, devuelto;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 250;


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


        codigosBarrasTickets= bbddController.obtenerListaCodigosTicket();
        todosProductosTicket=findViewById(R.id.rv_productos_ticket);
        productosDevueltos=findViewById(R.id.rv_productos_a_devolver);

        productosDevueltos.setLayoutManager(new LinearLayoutManager(RealizarDevolucion.this));
        adaptadorDevuelto = new AdaptadorProductoDevuelto(listaProductosDevueltos, this,  listaCantidades, this, this, this);
        productosDevueltos.setAdapter(adaptadorDevuelto);


        ticket_devo=findViewById(R.id.tx_ticket_devo);
        ticket_devo.setText("");

        codigoTicketBuscar=findViewById(R.id.cod_barras_ticket);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RealizarDevolucion.this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        codigoTicketBuscar.setAdapter(adapter);



        codigoProductoBuscar=findViewById(R.id.cod_produ_devo);
        totalDevolver=findViewById(R.id.total_a_devolver);
        totalDevolver.setText("0.00");
        totalDevolver.setEnabled(false);

        escanearProducto=findViewById(R.id.escanear_prod_devo);
        escanearTicket = findViewById(R.id.escanear_ticket);
        buscarTicket = findViewById(R.id.buscar_ticket);

        realizarDevolucion = findViewById(R.id.realizar_devolucion);
        cancelarDevolucion= findViewById(R.id.cancelar_devolucion);

        connection = null;
        printer = null;



        codigoTicketBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                // Llamar a un método para obtener las sugerencias de la base de datos y actualizar el ArrayAdapter
                actualizarSugerencias(input, adapter);

            }
        });

        buscarTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               codigoEscaneado = codigoTicketBuscar.getText().toString();
                verificarCodigoEscaneado();

            }
        });
        escanearTicket.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v){
               if (ContextCompat.checkSelfPermission(RealizarDevolucion.this, Manifest.permission.CAMERA)
                       != PackageManager.PERMISSION_GRANTED) {
                   ActivityCompat.requestPermissions(RealizarDevolucion.this,
                           new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
               } else {
                   iniciarEscaner();
               }
           }
        });


    }

    private void iniciarEscaner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Escanea un código de barras");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar el resultado del escaneo de código de barras
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "Escaneo cancelado");
            } else {

                codigoEscaneado = result.getContents();
                codigoTicketBuscar.setText(codigoEscaneado);
                Log.d(TAG, "Código de barras escaneado: " + codigoEscaneado);
                verificarCodigoEscaneado();

            }
        }
    }
                private void actualizarSugerencias(String input, ArrayAdapter<String> adapter) {
        // No necesitas obtener la lista de códigos de barras de tickets aquí
        // Solo necesitas filtrar la lista existente basada en el input

        // Filtrar la lista de códigos de barras de tickets para obtener sugerencias basadas en el input
        List<String> sugerencias = filtrarSugerencias(input, codigosBarrasTickets);

        // Limpiar el adaptador y agregar las nuevas sugerencias
        adapter.clear();
        adapter.addAll(sugerencias);

        // Notificar al adaptador que los datos han cambiado
        adapter.notifyDataSetChanged();
    }

    private List<String> filtrarSugerencias(String input, List<String> sugerencias) {
        // Si la entrada está vacía, devolver todas las sugerencias sin filtrar
        if (input.isEmpty()) {
            return sugerencias;
        } else {
            // Filtrar la lista de sugerencias basada en el texto de entrada utilizando Stream en Java
            return sugerencias.stream()
                    .filter(sugerencia -> sugerencia.toLowerCase().startsWith(input.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }



    private void verificarCodigoEscaneado() {
        // Realizar una consulta a la base de datos para verificar la existencia del ticket correspondiente al código escaneado
        boolean ticketExistente = bbddController.verificarExistenciaTicket(String.valueOf(codigoEscaneado));

        if (ticketExistente) {
            // Si el ticket existe, cargar los productos y configurar el RecyclerView
            cargarProductos();
        } else {
            // Si el ticket no existe, mostrar un AlertDialog informando al usuario que el ticket no está registrado
            AlertDialog.Builder builder = new AlertDialog.Builder(RealizarDevolucion.this);
            builder.setTitle("Ticket no registrado");
            builder.setMessage("El ticket escaneado no se encuentra registrado en la base de datos.");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }


    private void cargarProductos() {

        listaProductosTicket = bbddController.obtenerListaProductosparaDevo(String.valueOf(codigoEscaneado));

        // Configurar el LayoutManager y el adaptador para el RecyclerView después de cargar los productos
        GridLayoutManager layoutManager = new GridLayoutManager(RealizarDevolucion.this, 2);
        todosProductosTicket.setLayoutManager(layoutManager);
        adaptadorProductoTicket = new AdaptadorProductoTicket(RealizarDevolucion.this, listaProductosTicket, RealizarDevolucion.this);
        todosProductosTicket.setAdapter(adaptadorProductoTicket);
    }

    @Override
    public void onProductoSeleccionado(ProductoModel producto) {
        if ( producto.getCantidad() == 0 ) {
            // Mostrar un AlertDialog de advertencia
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alerta");
            builder.setMessage("El producto seleccionado ya no esta disponible en este ticket.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Cerrar el diálogo o ejecutar alguna acción adicional si es necesario
                }
            });
            builder.show();
            return; // Detener el proceso
        }else{
            boolean productoExistente = false;
            for (ProductoModel p : listaProductosDevueltos) {
                if (p.getCodigoBarras() == producto.getCodigoBarras()) {
                    // Aumentar la cantidad del producto existente
                    p.setCantidad(p.getCantidad() + 1);
                    adaptadorDevuelto.notifyDataSetChanged();
                    // Buscar el Producto_TicketModel correspondiente y aumentar su cantidad
                    for (Producto_TicketModel productoTicket : listaCantidades) {
                        if (productoTicket.getCodigoBarras_producto() == producto.getCodigoBarras()) {
                            productoTicket.setCantidad(productoTicket.getCantidad() + 1);
                            adaptadorDevuelto.notifyDataSetChanged();
//                        producto.setCantidad(producto.getCantidad() + 1);
//                        adaptadorComprados.notifyDataSetChanged();
                            break;
                        }
                    }
                    productoExistente = true;
                    break;
                }
            }

            // Si el producto no está en la lista de productos comprados, agregarlo
            if (!productoExistente) {
                listaProductosDevueltos.add(producto);
                adaptadorDevuelto.notifyDataSetChanged();
                listaCantidades.add(new Producto_TicketModel(producto.getCodigoBarras(), id_ticket, 1)); // Iniciar cantidad en 1
                adaptadorDevuelto.notifyDataSetChanged();

            }


            // Notificar al adaptador de productos comprados de los cambios
            adaptadorDevuelto.notifyDataSetChanged();
            totalDevolver.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
        }


    }

    private double calcularPrecioTotal(List<Producto_TicketModel> listaCantidades) {
        double totalVenta = 0.0;

        // Recorre la lista de productos
        for (Producto_TicketModel producto : listaCantidades) {
            // Obtiene el precio por unidad del producto y la cantidad
            double precioPorUnidad = bbddController.obtenerPrecioUnidadporCodigo(producto.getCodigoBarras_producto());
            int cantidad = producto.getCantidad();

            // Calcula el subtotal para este producto y lo suma al total de la venta
            totalVenta += precioPorUnidad * cantidad;
        }

        // Formatear el totalVenta para mostrar solo dos decimales con punto decimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        String totalVentaFormateado = df.format(totalVenta);

        // Convertir el totalVenta formateado de nuevo a double
        return Double.parseDouble(totalVentaFormateado);
    }

    @Override
    public void onQuantityChangedListenerUp(Producto_TicketModel producto) {

    }

    @Override
    public void onQuantityChangedDown(Producto_TicketModel producto) {

    }

    @Override
    public void onProductRemoved(int position) {
        ProductoModel productoRemovido = listaProductosDevueltos.get(position);

        // Decrementar el stock disponible del producto removido en la lista de todos los productos
        for (ProductoModel producto : listaProductosTicket) {
            if (producto.getCodigoBarras() == productoRemovido.getCodigoBarras()) {
                producto.setCantidad(producto.getCantidad()+1); // Aumentar el stock disponible
                break;
            }
        }

        // Eliminar el producto de la lista de compras
        listaProductosDevueltos.remove(position);

        // Eliminar el Producto_TicketModel correspondiente de listaCantidades
        Iterator<Producto_TicketModel> iterator = listaCantidades.iterator();
        while (iterator.hasNext()) {
            Producto_TicketModel productoTicket = iterator.next();
            if (productoTicket.getCodigoBarras_producto() == productoRemovido.getCodigoBarras()) {
                iterator.remove();
                break;
            }
        }

        // Notificar al adaptador de la lista de todos los productos que los datos han cambiado
        adaptadorProductoTicket.notifyDataSetChanged();

        // Actualizar el precio total
        totalDevolver.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
    }
}