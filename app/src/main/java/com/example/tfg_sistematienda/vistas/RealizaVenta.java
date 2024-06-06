package com.example.tfg_sistematienda.vistas;


import static com.example.tfg_sistematienda.vistas.ListaInventario.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosComprados;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductosVenta;
import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RealizaVenta extends AppCompatActivity implements AdaptadorProductosVenta.OnProductoSeleccionadoListener, AdaptadorProductosComprados.OnProductRemovedListener, AdaptadorProductosComprados.OnPriceUpdateListener, AdaptadorProductosComprados.OnQuantityChangedListener, AdaptadorProductosComprados.OnQuantityChangedListenerUp {

    private static final int REQUEST_ENABLE_BT = 27;
    private RecyclerView todosProductos, productosComprados;
    private EditText codigoBuscar;
    private ImageButton escanearProducto, realizarVenta, cancelarVenta;
    private TextView precioTotal;
    private String id_ticket, codigoEscaneado;
    private AdaptadorProductosComprados adaptadorComprados;
    private List<ProductoModel> listaTodosProductos;
    private AdaptadorProductosVenta adaptadorTodos;
    private List<ProductoModel> listaProductosComprados;
    private List<Producto_TicketModel> listaCantidades = new ArrayList<>();
    private BBDDController bbddController = new BBDDController();
    private TicketModel ticket;
    private double totalVenta;

    private double entregado, devuelto;
    private EditText aPagar, aRecoger, aDevolver;
    private ImageButton tarjeta, efectivo, realizarPago, comprobar;
    private TextView tx_Pagar, tx_Recoger, tx_Devolver, eu_Pagar, eu_Recoger, eu_Devolver;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 150;

    private BluetoothConnection connection;
    private EscPosPrinter printer;
    private BluetoothAdapter bluetoothAdapter;
    private UsuarioModel usuario;
    private boolean allowBackPress=false;
    // Define un ExecutorService para manejar las operaciones en un hilo separado
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        // Obtiene el Intent que inició esta actividad
        Intent intent = getIntent();

        // Captura el DNI del usuario pasado desde la actividad anterior
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        // Recupera la información del usuario desde la base de datos utilizando el controlador de la base de datos
        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        todosProductos = findViewById(R.id.rv_todos);
        productosComprados = findViewById(R.id.rv_comprados);

        precioTotal = findViewById(R.id.tv_total);
        precioTotal.setText("0");


        connection = null;
        printer = null;

        listaProductosComprados = new ArrayList<>();

        cargarProductos();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        todosProductos.setLayoutManager(layoutManager);

        adaptadorTodos = new AdaptadorProductosVenta(this, listaTodosProductos, this);
        todosProductos.setAdapter(adaptadorTodos);

        productosComprados.setLayoutManager(new LinearLayoutManager(this));
        adaptadorComprados = new AdaptadorProductosComprados(this, listaProductosComprados, listaCantidades, listaTodosProductos, this, this, this, this);
        productosComprados.setAdapter(adaptadorComprados);

        escanearProducto = findViewById(R.id.leer_producto_venta);
        codigoBuscar = findViewById(R.id.cod_barras_producto);
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



        codigoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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
            Intent i = new Intent(this, GeneralVendedor.class);
            i.putExtra("usuarioDNI", usuario.getDni());
            startActivity(i);
        });

        realizarVenta.setOnClickListener(v -> {
            totalVenta = calcularPrecioTotal(listaCantidades);
            if (totalVenta > 0) {
                abrirDialogoPago(totalVenta);
            } else {
                Toast.makeText(this, "No hay ningún producto seleccionado", Toast.LENGTH_SHORT).show();
            }
        });




    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Pulse el botón CANCELAR VENTA para volver atrás", Toast.LENGTH_SHORT).show();
        }
    }

    private void crearTicketconCodigo() {
        ticket = new TicketModel(id_ticket, totalVenta, false, true, entregado, devuelto, usuario.getIdTienda());
        if (bbddController.insertarTicket(ticket)) {
            for (Producto_TicketModel productoTicket : listaCantidades) {
                // Crear un nuevo objeto Producto_TicketModel con el código de ticket actual
                Producto_TicketModel nuevoProductoTicket = new Producto_TicketModel(productoTicket.getCodigoBarras_producto(), id_ticket, productoTicket.getCantidad());

                // Insertar el producto en la base de datos
                if (bbddController.insertarProductoTicket(nuevoProductoTicket)) {
                    if(bbddController.incrementarVecesComprado(nuevoProductoTicket.getCodigoBarras_producto(), nuevoProductoTicket.getCantidad())) {
                        Log.d(TAG, "Veces compradas del producto actualizado: " + nuevoProductoTicket.getCodigoBarras_producto());
                    }
                    // Modificar el stock del producto en la base de datos
                    if (bbddController.modificarStockProducto(nuevoProductoTicket.getCodigoBarras_producto(), nuevoProductoTicket.getCantidad())) {
                        Log.d(TAG, "Stock del producto actualizado: " + nuevoProductoTicket.getCodigoBarras_producto());
                    } else {
                        Log.e(TAG, "Error al actualizar el stock del producto: " + nuevoProductoTicket.getCodigoBarras_producto());
                    }
                } else {
                    Log.e(TAG, "Error al insertar el producto en ticket_producto: " + nuevoProductoTicket.toString());
                }
            }
            try {
                checkBluetoothConnectPermission();
            } catch (EscPosEncodingException | EscPosBarcodeException | EscPosParserException |
                     EscPosConnectionException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(this, "Error al crear el ticket", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetoothConnectPermission() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MainActivity.PERMISSION_BLUETOOTH_ADMIN);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MainActivity.PERMISSION_BLUETOOTH_CONNECT);
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MainActivity.PERMISSION_BLUETOOTH_SCAN);
                return;
            }
        }
        // Ya se tienen todos los permisos necesarios
        conectarImpresora();
    }

    private void desconectarImpresora() {
        if (connection != null) {
            connection.disconnect(); // Cierra la conexión Bluetooth
        }
    }


    private boolean conectarImpresora() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            runOnUiThread(() -> Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show());
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicita al usuario que lo active
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Manejar el caso en que los permisos no estén concedidos
                runOnUiThread(() -> Toast.makeText(this, "Permiso Bluetooth no concedido", Toast.LENGTH_SHORT).show());
                return false;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        if (connection == null || printer == null) {
            // Obtener la lista de dispositivos emparejados
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && !pairedDevices.isEmpty()) {
                // Crear una lista de nombres de dispositivos para el diálogo
                List<String> deviceNames = new ArrayList<>();
                final List<BluetoothDevice> devices = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    deviceNames.add(device.getName());
                    devices.add(device);
                }

                // Mostrar un cuadro de diálogo para que el usuario elija el dispositivo
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Selecciona un dispositivo Bluetooth");
                builder.setItems(deviceNames.toArray(new String[0]), (dialog, which) -> {
                    // Conectar al dispositivo seleccionado
                    BluetoothDevice selectedDevice = devices.get(which);
                    executorService.execute(() -> {
                        try {
                            connection = new BluetoothConnection(selectedDevice);
                            printer = new EscPosPrinter(connection, 200, 50f, 45);

                            String textoTicket = generarTextoTicket();

                            // Imprimir
                            printer.printFormattedText(textoTicket);

                            // Después de imprimir, muestra el diálogo de venta exitosa
                            runOnUiThread(this::mostrarDialogoVentaExitosa);
                            desconectarImpresora();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Mostrar un Toast indicando que la impresora no está operativa
                            runOnUiThread(() -> Toast.makeText(this, "La impresora no está operativa", Toast.LENGTH_SHORT).show());
                        }
                    });
                });
                builder.show();
                return true; // Impresora conectada con éxito
            } else {
                // Manejar el caso en que no haya dispositivos emparejados
                runOnUiThread(() -> Toast.makeText(this, "No hay dispositivos Bluetooth emparejados", Toast.LENGTH_SHORT).show());
                return false;
            }
        }
        return false; // La impresora ya está conectada
    }

    private String generarTextoTicket() {
        String textoTicket = "[C]\n";
        String nombreTienda = bbddController.obtenerNombreTienda(usuario.getIdTienda());
        LocalDateTime fecha = LocalDateTime.now();
        String fechaString = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear() + "   " + fecha.getHour() + ":" + fecha.getMinute() + ":" + fecha.getSecond();
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.logoticket);
        String imagen = PrinterTextParserImg.bitmapToHexadecimalString(printer, logo);

        textoTicket += "[L]<b><font size='big'>TICKET DE VENTA</font></b>\n";
        textoTicket += "[C]<img>" + imagen + "</img>\n";
        textoTicket += "[C]\n";
        textoTicket += "[L]<b>FECHA: </b>" + fechaString + "\n";
        textoTicket += "[L]Por vendedor: " + usuario.getNombre() + "\n";
        textoTicket += "[L]En tienda: " + nombreTienda + "\n";
        textoTicket += "[L]Con CIF: <b>" + usuario.getIdTienda() + "</b>\n";
        textoTicket += "[L]*****************************\n";

        for (ProductoModel producto : listaProductosComprados) {
            for (Producto_TicketModel prod : listaCantidades) {
                if (producto.getCodigoBarras().equals(prod.getCodigoBarras_producto())) {
                    textoTicket += "[L]<b>" + producto.getNombre() + "</b>[L] " + producto.getPrecioUnidad() + "e/Uni[L] " + prod.getCantidad() + "\n";
                    textoTicket += "[L] PRECIO TOTAL PRODUCTO: " + producto.getPrecioUnidad() * prod.getCantidad() + "e\n";
                    textoTicket += "[L]  Cod Producto : " + producto.getCodigoBarras() + "\n";
                    textoTicket += "[L]===========================\n";
                    break; // Romper el bucle interior cuando se encuentre la coincidencia
                }
            }
        }
        textoTicket += "[L]<b> TOTAL A PAGAR:<b> " + totalVenta + " euros\n";
        textoTicket += "[L]<b> ENTREGADO:<b> " + entregado + " euros\n";
        textoTicket += "[L] DEVUELTO:<b> " + devuelto + " euros\n";
        textoTicket += "[C]<barcode type='128' height='10'>" + id_ticket + "</barcode>\n";
        textoTicket += "[L]\n";
        textoTicket += "[L]Tiene 14 dias para devolver\n";
        textoTicket += "[L]\n";
        textoTicket += "[L]¡¡GRACIAS POR SU VISITA!!\n";

        return textoTicket;
    }

    private void mostrarDialogoVentaExitosa() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Venta Exitosa");
        builder.setMessage("La venta se ha registrado correctamente.");

        // Agregar botones
        builder.setPositiveButton("Volver al Menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implementa lo que deseas hacer cuando se presiona el botón "Volver al Menú"
                // Por ejemplo, puedes iniciar la actividad del menú principal
                Intent intent = new Intent(RealizaVenta.this, GeneralVendedor.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
                finish(); // Esto evita que el usuario pueda volver atrás al menú principal desde esta actividad
            }
        });

        builder.setNegativeButton("Realizar Otra Venta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implementa lo que deseas hacer cuando se presiona el botón "Realizar Otra Venta"
                // Por ejemplo, puedes iniciar la actividad de realizar venta sin datos
                Intent intent = new Intent(RealizaVenta.this, RealizaVenta.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
            }
        });

        // Mostrar el diálogo
        builder.setCancelable(false);
        builder.show();
    }



    private void abrirDialogoPago(double totalVenta){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("REALIZAR PAGO");

        // Inflar el diseño del diálogo de edición de producto
        View dialogView = LayoutInflater.from(this).inflate(R.layout.pago, null);
        builder.setView(dialogView);

        aPagar=dialogView.findViewById(R.id.a_pagar);
        aRecoger=dialogView.findViewById(R.id.entregado);
        aDevolver=dialogView.findViewById(R.id.a_devolver);

        tarjeta=dialogView.findViewById(R.id.pago_tarjeta);
        efectivo=dialogView.findViewById(R.id.pago_efectivo);

        realizarPago=dialogView.findViewById(R.id.tramitar_pago);
        realizarPago.setVisibility(View.INVISIBLE);

        tx_Pagar=dialogView.findViewById(R.id.tx_apagar);
        tx_Recoger=dialogView.findViewById(R.id.tx_entregado);
        tx_Devolver=dialogView.findViewById(R.id.tx_adevolver);

        eu_Pagar=dialogView.findViewById(R.id.eu_pagar);
        eu_Recoger=dialogView.findViewById(R.id.eu_entre);
        eu_Devolver=dialogView.findViewById(R.id.eu_devo);
        comprobar=dialogView.findViewById(R.id.comprobar);


        aPagar.setText(String.valueOf(totalVenta));
        aPagar.setEnabled(false);
        comprobar.setVisibility(View.INVISIBLE);


        eu_Devolver.setVisibility(View.INVISIBLE);
        eu_Recoger.setVisibility(View.INVISIBLE);
        tx_Devolver.setVisibility(View.INVISIBLE);
        tx_Recoger.setVisibility(View.INVISIBLE);
        aRecoger.setVisibility(View.INVISIBLE);
        aDevolver.setVisibility(View.INVISIBLE);

        tarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tx_Pagar.setVisibility(View.INVISIBLE);
                tx_Recoger.setVisibility(View.VISIBLE);
                tx_Devolver.setVisibility(View.VISIBLE);

                eu_Pagar.setVisibility(View.INVISIBLE);
                eu_Recoger.setVisibility(View.VISIBLE);
                eu_Devolver.setVisibility(View.VISIBLE);

                aRecoger.setEnabled(false);
                aDevolver.setEnabled(false);

                aPagar.setVisibility(View.INVISIBLE);
                aRecoger.setVisibility(View.VISIBLE);
                aDevolver.setVisibility(View.VISIBLE);
                realizarPago.setVisibility(View.VISIBLE);

                aRecoger.setText(String.valueOf(totalVenta));
                aDevolver.setText("0,00");

                entregado=Double.valueOf(aRecoger.getText().toString());
                devuelto=0.00;
            }
        });

        efectivo.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               tx_Recoger.setVisibility(View.VISIBLE);
               tx_Devolver.setVisibility(View.VISIBLE);
               eu_Recoger.setVisibility(View.VISIBLE);
               eu_Devolver.setVisibility(View.VISIBLE);
               aDevolver.setEnabled(false);
               aRecoger.setVisibility(View.VISIBLE);
               aDevolver.setVisibility(View.VISIBLE);


               aRecoger.addTextChangedListener(new TextWatcher() {
                   @Override
                   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                       // No necesitas implementar nada aquí
                   }

                   @Override
                   public void onTextChanged(CharSequence s, int start, int before, int count) {
                       // No necesitas implementar nada aquí
                   }

                   @Override
                   public void afterTextChanged(Editable s) {
                      comprobar.setVisibility(View.VISIBLE);
                   }
               });

               comprobar.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       entregado=Double.valueOf(aRecoger.getText().toString());
                       calcularCambio(totalVenta, entregado);

                   }
               });

           }
        });

        realizarPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearTicketconCodigo();
            }

        });

// Mostrar el diálogo
        builder.show();
        builder.setCancelable(false);

    }

    private void calcularCambio(double totalVenta, double entregado) {
        try {
            if (entregado < totalVenta) {
                showPaymentAlert();
                aDevolver.setText("");
            } else {
                // Calcular el cambio
                devuelto = entregado - totalVenta;
                // Formatear el valor de devuelto para mostrar solo dos decimales
                DecimalFormat df = new DecimalFormat("#.##");
                df.setMinimumFractionDigits(2); // Establecer el mínimo de dos decimales
                df.setMaximumFractionDigits(2);
                String devueltoFormateado = df.format(devuelto);
                // Establecer el texto formateado en el TextView
                aDevolver.setText(devueltoFormateado);
                realizarPago.setVisibility(View.VISIBLE);
                // Actualizar el valor de devuelto como double
                devuelto = Double.parseDouble(devueltoFormateado);
            }
        } catch (NumberFormatException e) {
            // Manejar la excepción si los campos no contienen números válidos
        }
    }


    private void showPaymentAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("La cantidad entregada es menor que la cantidad total. El cliente todavía debe pagar más.")
                .setPositiveButton("Aceptar", null)
                .show();
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


                ProductoModel productoEscaneado = null;
                for (ProductoModel producto : listaTodosProductos) {
                    if (producto.getCodigoBarras().equals(codigoEscaneado)) {
                        productoEscaneado = producto;
                        break; // Salir del bucle una vez que se haya encontrado el producto
                    }
                }
                if (productoEscaneado != null) {
                    // Verificar si hay suficiente stock disponible
                    if ( productoEscaneado.getCantidadStock() <= productoEscaneado.getCantidad() ) {
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
                            if (p.getCantidadStock() <= p.getCantidad()) {
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
                            } else {
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
                    }

                    // Si el producto no está en la lista de productos comprados, agregarlo
                    if (!productoExistente) {
                        productoEscaneado.setCantidad(productoEscaneado.getCantidad() + 1);
                        listaProductosComprados.add(productoEscaneado);
                        // Actualizar la cantidad en listaTodosProductos
                        for (ProductoModel producto : listaTodosProductos) {
                            if (producto.getCodigoBarras().equals(productoEscaneado.getCodigoBarras())) {
                                producto.setCantidad(productoEscaneado.getCantidad());
                                break;
                            }
                        }
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
        listaTodosProductos = bbddController.obtenerListaProductos(usuario.getIdTienda());
    }

    @Override

    public void onProductoSeleccionado(ProductoModel producto) {

        // Verificar si hay suficiente stock disponible
        if ( producto.getCantidadStock() <= producto.getCantidad() ) {
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
            producto.setCantidad(producto.getCantidad() + 1);
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
                productoModel.setCantidad(productoModel.getCantidad() - 1); // Incrementar el stock disponible
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
        for (Iterator<ProductoModel> iterator = listaProductosComprados.iterator(); iterator.hasNext(); ) {
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
                productoModel.setCantidad(productoModel.getCantidad() + 1); // Incrementar el stock disponible
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