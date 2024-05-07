package com.example.tfg_sistematienda.vistas;


import static com.example.tfg_sistematienda.vistas.ListaInventario.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import com.example.tfg_sistematienda.Adaptadores.AdaptadorProducto;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosComprados;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosVenta;
import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RealizaVenta extends AppCompatActivity implements AdaptadorProductosVenta.OnProductoSeleccionadoListener, AdaptadorProductosComprados.OnProductRemovedListener, AdaptadorProductosComprados.OnPriceUpdateListener, AdaptadorProductosComprados.OnQuantityChangedListener, AdaptadorProductosComprados.OnQuantityChangedListenerUp {

    private RecyclerView todosProductos, productosComprados;
    private EditText codigoBuscar, idTicket;
    private Button escanearProducto, realizarVenta, cancelarVenta;
    private TextView precioTotal;

    private String id_ticket, codigoEscaneado;
    private AdaptadorProductosComprados adaptadorComprados;
    private List<ProductoModel> listaTodosProductos;
    private AdaptadorProductosVenta adaptadorTodos;
    private List<ProductoModel> listaProductosComprados;
    private List<Producto_TicketModel> listaCantidades= new ArrayList<>();
    private BBDDController bbddController = new BBDDController();
    private TicketModel ticket;
    private double totalVenta = 0.0;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 150;

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
        precioTotal=findViewById(R.id.tv_total);
        precioTotal.setText("0");

        listaProductosComprados = new ArrayList<>();

        cargarProductos();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        todosProductos.setLayoutManager(layoutManager);

        adaptadorTodos = new AdaptadorProductosVenta(this, listaTodosProductos, this);
        todosProductos.setAdapter(adaptadorTodos);

        productosComprados.setLayoutManager(new LinearLayoutManager(this));
        adaptadorComprados = new AdaptadorProductosComprados(this, listaProductosComprados, listaCantidades, listaTodosProductos, this, this, this, this);
        productosComprados.setAdapter(adaptadorComprados);

        escanearProducto= findViewById(R.id.leer_producto_venta);
        codigoBuscar= findViewById(R.id.cod_barras_producto);
        cancelarVenta = findViewById(R.id.bt_cancelar_venta);
        realizarVenta = findViewById(R.id.bt_realizar_venta);

        List<String> listaCodigosBarras = obtenerListaCodigosTicketDesdeBD();

        // Generar un código de barras único
        String nuevoIdBarras;
        do {
            nuevoIdBarras = generarIdUnico();
        } while (listaCodigosBarras.contains(nuevoIdBarras));

        // Mostrar el nuevo código de barras
        id_ticket = nuevoIdBarras;
        idTicket.setText(id_ticket);


        codigoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filtrarProductosPorCodigo(s.toString());
            }
        });

        escanearProducto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                iniciarEscaner();
            }
        });

        cancelarVenta.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Limpiar la pila de actividades y comenzar la nueva actividad
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        realizarVenta.setOnClickListener(v -> {
            crearTicketconCodigo();
        });


    }

    private void crearTicketconCodigo(){
       ticket=new TicketModel(id_ticket, totalVenta, false, true, 0,0);

       ;
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
                codigoBuscar.setText("");
                codigoEscaneado = result.getContents();
                Log.d(TAG, "Código de barras escaneado: " + codigoEscaneado);

                // Buscar el producto correspondiente al código de barras escaneado
                ProductoModel productoEscaneado = buscarProductoPorCodigoBarras(codigoEscaneado);
                if (productoEscaneado != null) {
                    // Verificar si hay suficiente stock disponible
                    if (productoEscaneado.getCantidad() >= productoEscaneado.getCantidadStock()) {
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

                    // Buscar si el producto ya está en la lista de productos comprados
                    boolean productoExistente = false;
                    for (ProductoModel p : listaProductosComprados) {
                        if (p.getCodigoBarras().equals(productoEscaneado.getCodigoBarras())) {
                            // Aumentar la cantidad del producto existente
                            p.setCantidad(p.getCantidad() + 1);
                            adaptadorComprados.notifyDataSetChanged();
                            // Buscar el Producto_TicketModel correspondiente y aumentar su cantidad
                            for (Producto_TicketModel productoTicket : listaCantidades) {
                                if (productoTicket.getCodigoBarras_producto().equals(productoEscaneado.getCodigoBarras())) {
                                    productoTicket.setCantidad(productoTicket.getCantidad() + 1);
                                    adaptadorComprados.notifyDataSetChanged();
                                    break;
                                }
                            }
                            productoExistente = true;
                            break;
                        }
                    }

                    // Si el producto no está en la lista de productos comprados, agregarlo
                    if (!productoExistente) {
                        listaProductosComprados.add(productoEscaneado);
                        adaptadorComprados.notifyDataSetChanged();
                        listaCantidades.add(new Producto_TicketModel(productoEscaneado.getCodigoBarras(), id_ticket, 1)); // Iniciar cantidad en 1
                        adaptadorComprados.notifyDataSetChanged();
                    }

                    // Notificar al adaptador de productos comprados de los cambios
                    adaptadorComprados.notifyDataSetChanged();
                    precioTotal.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
                } else {
                    // Mostrar un mensaje de que el producto no fue encontrado
                    Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private ProductoModel buscarProductoPorCodigoBarras(String codigoBarras) {
        for (ProductoModel producto : listaTodosProductos) {
            if (producto.getCodigoBarras().equals(codigoBarras)) {
                return producto;
            }
        }
        return null; // Si no se encuentra ningún producto con el código de barras especificado
    }



    private void filtrarProductosPorCodigo(String codigo) {
        if (codigo.isEmpty()) {
            cargarProductos(); // Cargar todos los productos nuevamente
            adaptadorTodos.actualizarListaTodos(listaTodosProductos);
        } else {
            List<ProductoModel> productosFiltrados = new ArrayList<>();
            for (ProductoModel producto : listaTodosProductos) {
                if (producto.getCodigoBarras().startsWith(codigo)) {
                    productosFiltrados.add(producto);
                }
            }
            adaptadorTodos.actualizarListaTodos(productosFiltrados);
        }
    }



    private List<String> obtenerListaCodigosTicketDesdeBD() {
        List<String> codigos = bbddController.obtenerListaCodigosTicket();
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
        // Verificar si hay suficiente stock disponible
        if (producto.getCantidad() >= producto.getCantidadStock()) {
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

        // Buscar si el producto ya está en la lista de productos comprados
        boolean productoExistente = false;
        for (ProductoModel p : listaProductosComprados) {
            if (p.getCodigoBarras() == producto.getCodigoBarras()) {
                // Aumentar la cantidad del producto existente
                p.setCantidad(p.getCantidad() + 1);
                adaptadorComprados.notifyDataSetChanged();
                // Buscar el Producto_TicketModel correspondiente y aumentar su cantidad
                for (Producto_TicketModel productoTicket : listaCantidades) {
                    if (productoTicket.getCodigoBarras_producto() == producto.getCodigoBarras()) {
                        productoTicket.setCantidad(productoTicket.getCantidad() + 1);
                        adaptadorComprados.notifyDataSetChanged();
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
            listaProductosComprados.add(producto);
            adaptadorComprados.notifyDataSetChanged();
            listaCantidades.add(new Producto_TicketModel(producto.getCodigoBarras(), id_ticket, 1)); // Iniciar cantidad en 1
            adaptadorComprados.notifyDataSetChanged();

        }

        // Notificar al adaptador de productos comprados de los cambios
        adaptadorComprados.notifyDataSetChanged();
        precioTotal.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
    }




    private double calcularPrecioTotal(List<Producto_TicketModel> listaCantidades) {


        // Recorre la lista de productos
        for (Producto_TicketModel producto : listaCantidades) {
            // Obtiene el precio por unidad del producto y la cantidad

            double precioPorUnidad = bbddController.obtenerPrecioUnidadporCodigo(producto.getCodigoBarras_producto());
            int cantidad = producto.getCantidad();

            // Calcula el subtotal para este producto y lo suma al total de la venta
            totalVenta += precioPorUnidad * cantidad;
        }

        return totalVenta;
    }

    @Override
    public void onProductRemoved(int position) {
        ProductoModel productoRemovido = listaProductosComprados.get(position);

        // Decrementar el stock disponible del producto removido en la lista de todos los productos
        for (ProductoModel producto : listaTodosProductos) {
            if (producto.getCodigoBarras() == productoRemovido.getCodigoBarras()) {
                producto.setCantidadStock(bbddController.obtenerStockporCodigo(producto.getCodigoBarras())); // Aumentar el stock disponible
                break;
            }
        }

        // Eliminar el producto de la lista de compras
        listaProductosComprados.remove(position);

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
        adaptadorTodos.notifyDataSetChanged();

        // Actualizar el precio total
        precioTotal.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
    }

    @Override
    public void onPriceUpdated(double newPrice) {
       precioTotal.setText(String.valueOf(newPrice));
    }

    @Override
    public void onQuantityChangedDown(Producto_TicketModel producto) {
        // Actualizar el stock disponible en la lista de todos los productos
        for (ProductoModel productoModel : listaTodosProductos) {
            if (productoModel.getCodigoBarras() == producto.getCodigoBarras_producto()) {
                productoModel.setCantidad(productoModel.getCantidad() - 1 ); // Incrementar el stock disponible
                break;
            }
        }

        // Actualizar la cantidad en listaCantidades
        for (Producto_TicketModel productoTicket : listaCantidades) {
            if (productoTicket.getCodigoBarras_producto() == producto.getCodigoBarras_producto()) {
                productoTicket.setCantidad(producto.getCantidad());
                break;
            }
        }

        // Eliminar el producto de la lista de compras si la cantidad es cero
        for (Iterator<ProductoModel> iterator = listaProductosComprados.iterator(); iterator.hasNext();) {
            ProductoModel productoModel = iterator.next();
            if (productoModel.getCodigoBarras() == producto.getCodigoBarras_producto() && producto.getCantidad() == 0) {
                iterator.remove(); // Eliminar el producto de la lista de compras
                break;
            }
        }

        // Notificar al adaptador de la lista de todos los productos que los datos han cambiado
        adaptadorTodos.notifyDataSetChanged();

        // Actualizar el precio total
        precioTotal.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
    }



    @Override
    public void onQuantityChangedListenerUp(Producto_TicketModel producto) {
        for (ProductoModel productoModel : listaTodosProductos) {
            if (productoModel.getCodigoBarras() == producto.getCodigoBarras_producto()) {
                productoModel.setCantidad(productoModel.getCantidad() + 1 ); // Incrementar el stock disponible
                break;
            }

        }

        for (Producto_TicketModel productoTicket : listaCantidades) {
            if (productoTicket.getCodigoBarras_producto() == producto.getCodigoBarras_producto()) {
                productoTicket.setCantidad(producto.getCantidad());
                break;
            }
        }

        // Notificar al adaptador de la lista de todos los productos que los datos han cambiado
        adaptadorTodos.notifyDataSetChanged();

        // Actualizar el precio total
        precioTotal.setText(Double.toString(calcularPrecioTotal(listaCantidades)));
    }
}