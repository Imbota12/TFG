package com.example.tfg_sistematienda.vistas;

import static com.example.tfg_sistematienda.vistas.ListaInventario.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductoDevuelto;
import com.example.tfg_sistematienda.Adaptadores.AdaptadorProductoTicket;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RealizarDevolucion extends AppCompatActivity implements AdaptadorProductoTicket.OnProductoSeleccionadoListener, AdaptadorProductoDevuelto.OnProductRemovedListener, AdaptadorProductoDevuelto.OnQuantityChangedListener, AdaptadorProductoDevuelto.OnQuantityChangedListenerUp {

    private static final int REQUEST_ENABLE_BT = 37;
    public static final int PERMISSION_BLUETOOTH = 31;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 32;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 33;
    public static final int PERMISSION_BLUETOOTH_SCAN = 34;

    private RecyclerView todosProductosTicket, productosDevueltos;
    private EditText totalDevolver;
    private AutoCompleteTextView codigoTicketBuscar;
    private ImageButton realizarDevolucion, cancelarDevolucion, escanearTicket, buscarTicket;
    private String id_ticket, codigoEscaneado, productoEscaneado, aDevolver;
    private AdaptadorProductoDevuelto adaptadorDevuelto;
    private List<ProductoModel> listaProductosTicket;

    private double entregado, devuelto;
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
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 250;
    private UsuarioModel usuario;
    private boolean allowBackPress=false;

    // Define un ExecutorService para manejar las operaciones en un hilo separado
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


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

        // Obtiene el Intent que inició esta actividad
        Intent intent = getIntent();

        // Captura el DNI del usuario pasado desde la actividad anterior
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        // Recupera la información del usuario desde la base de datos utilizando el controlador de la base de datos
        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        codigosBarrasTickets= bbddController.obtenerListaCodigosTicket();
        todosProductosTicket=findViewById(R.id.rv_productos_ticket);
        productosDevueltos=findViewById(R.id.rv_productos_a_devolver);

        productosDevueltos.setLayoutManager(new LinearLayoutManager(RealizarDevolucion.this));
        adaptadorDevuelto = new AdaptadorProductoDevuelto(listaProductosDevueltos, this,  listaCantidades, this, this, this);
        productosDevueltos.setAdapter(adaptadorDevuelto);




        List<String> listaCodigosBarras = obtenerListaCodigosTicketDesdeBD();

        // Generar un código de barras único
        String nuevoIdBarras;
        do {
            nuevoIdBarras = generarIdUnico();
        } while (listaCodigosBarras.contains(nuevoIdBarras));

        // Mostrar el nuevo código de barras
        id_ticket = nuevoIdBarras;


        codigoTicketBuscar=findViewById(R.id.cod_barras_ticket);
        // Crear un ArrayAdapter vacío
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RealizarDevolucion.this, android.R.layout.simple_dropdown_item_1line);

// Asignar el adaptador al AutoCompleteTextView
        codigoTicketBuscar.setAdapter(adapter);

// Actualizar el adaptador con las sugerencias actuales
        actualizarSugerencias(obtenerTodasLasSugerencias(), adapter);




        totalDevolver=findViewById(R.id.total_a_devolver);
        totalDevolver.setText("0.00");
        totalDevolver.setEnabled(false);

        escanearTicket = findViewById(R.id.escanear_ticket);
        buscarTicket = findViewById(R.id.buscar_ticket);

        realizarDevolucion = findViewById(R.id.realizar_devolucion);
        cancelarDevolucion= findViewById(R.id.cancelar_devolucion);

        connection = null;
        printer = null;


        realizarDevolucion.setVisibility(View.INVISIBLE);

        cancelarDevolucion.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent i = new Intent(RealizarDevolucion.this, GeneralVendedor.class);
               i.putExtra("usuarioDNI", usuario.getDni());
               startActivity(i);
           }
        });
        realizarDevolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalDevolucion > 0) {
                    tramitarDevolucion();
                } else {
                   Toast.makeText(RealizarDevolucion.this, "Debe seleccionar algun producto para realizar una devolucion", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
                // Actualizar las sugerencias filtradas según la entrada actual
                filtrarSugerencias(input, adapter);

            }
        });

        buscarTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               codigoEscaneado = codigoTicketBuscar.getText().toString();
                try {
                    verificarCodigoEscaneado();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

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

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Pulse el botón CANCELAR DEVOLUCIÓN para volver atrás", Toast.LENGTH_SHORT).show();
        }
    }


    private void tramitarDevolucion(){

        entregado=0;
        devuelto=totalDevolucion;
        ticket = new TicketModel(id_ticket, totalDevolucion, true, false, entregado, devuelto, usuario.getIdTienda());
        if (bbddController.insertarTicket(ticket)) {
            for (ProductoModel producto : listaProductosTicket){
                if (producto.getCantidad() >0) {
                    Producto_TicketModel nuevoProductoTicket = new Producto_TicketModel(producto.getCodigoBarras(), id_ticket, producto.getCantidad());

                    if (bbddController.insertarProductoTicket(nuevoProductoTicket)) {

                    } else {
                        Log.e(TAG, "Error al insertar el producto en ticket_producto: " + nuevoProductoTicket.toString());
                    }
                }
            }
            for (ProductoModel productoDevuelto : listaProductosDevueltos){
                if(bbddController.incrementarVecesDevuelto(productoDevuelto.getCodigoBarras(), productoDevuelto.getCantidad())) {
                    Log.d(TAG, "Veces devueltas del producto actualizado: " + productoDevuelto.getCodigoBarras());
                }
                if (bbddController.modificarStockProducto(productoDevuelto.getCodigoBarras(), -productoDevuelto.getCantidad())) {
                    Log.d(TAG, "Stock del producto actualizado: " + productoDevuelto.getCodigoBarras());
                } else {
                    Log.e(TAG, "Error al actualizar el stock del producto: " + productoDevuelto.getCodigoBarras());
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



    private boolean conectarImpresora() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicita al usuario que lo active
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Manejar el caso en que los permisos no estén concedidos
                Toast.makeText(this, "Permiso Bluetooth no concedido", Toast.LENGTH_SHORT).show();
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
                            runOnUiThread(this::mostrarDialogoDevolucionExitosa);

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
                Toast.makeText(this, "No hay dispositivos Bluetooth emparejados", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    private String generarTextoTicket() {
        String textoTicket = "[C]\n";
        String nombreTienda = bbddController.obtenerNombreTienda(usuario.getIdTienda());
        LocalDateTime fecha = LocalDateTime.now();
        String fechaString = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear() + "   " + fecha.getHour() + ":" + fecha.getMinute() + ":" + fecha.getSecond();
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.imbott);
        String imagen = PrinterTextParserImg.bitmapToHexadecimalString(printer, logo);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        String devueltoFormateado = df.format(devuelto);
        String totalDevolver = df.format(totalDevolucion);

        textoTicket += "[C]<b><font size='tall'>TICKET DEVOLUCION</font></b>\n";
        textoTicket += "[C]<img>" + imagen + "</img>\n";
        textoTicket += "[C]\n";
        textoTicket += "[L]<b>FECHA: </b>" + fechaString + "\n";
        textoTicket += "[L]Por vendedor: " + usuario.getNombre() + "\n";
        textoTicket += "[L]En tienda: " + nombreTienda + "\n";
        textoTicket += "[L]Con CIF: <b>" + usuario.getIdTienda() + "</b>\n";
        textoTicket += "[L]*****************************\n";

        textoTicket += "[L]<b>Productos restantes:<b>\n";
        textoTicket += "[L]+++++++++++++++\n";
        for (ProductoModel producto : listaProductosTicket) {
            if (producto.getCantidad() > 0) {
                textoTicket += "[L]<b>" + producto.getNombre() + "</b>[L] " + producto.getPrecioUnidad() + "e/Uni[L] " + producto.getCantidad() + "\n";
                textoTicket += "[L] PRECIO TOTAL PRODUCTO: " + producto.getPrecioUnidad() * producto.getCantidad() + "e\n";
                textoTicket += "[L]  Cod Producto : " + producto.getCodigoBarras() + "\n";
                textoTicket += "[L]===========================\n";
            }
        }

        textoTicket += "[L]<b>Productos devueltos:<b>\n";
        textoTicket += "[L]--------------\n";
        for (ProductoModel producto : listaProductosDevueltos) {
            textoTicket += "[L]<b>" + producto.getNombre() + "</b>[L] " + producto.getPrecioUnidad() + "e/Uni[L] " + producto.getCantidad() + "\n";
            textoTicket += "[L] PRECIO TOTAL PRODUCTO: " + producto.getPrecioUnidad() * producto.getCantidad() + "e\n";
            textoTicket += "[L]  Cod Producto : " + producto.getCodigoBarras() + "\n";
            textoTicket += "[L]===========================\n";
        }

        textoTicket += "[L]<b> TOTAL A DEVOLVER:<b> " + totalDevolver + "\n";
        textoTicket += "[L] DEVUELTO:<b> " + devueltoFormateado + "\n";
        textoTicket += "[C]<barcode type='128' height='10'>" + id_ticket + "</barcode>\n";

        return textoTicket;
    }


    private void mostrarDialogoDevolucionExitosa() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Devolución Exitosa");
        builder.setMessage("La devolución se ha registrado correctamente.");

        // Agregar botones
        builder.setPositiveButton("Volver al Menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implementa lo que deseas hacer cuando se presiona el botón "Volver al Menú"
                // Por ejemplo, puedes iniciar la actividad del menú principal
                Intent intent = new Intent(RealizarDevolucion.this, GeneralVendedor.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
                finish(); // Esto evita que el usuario pueda volver atrás al menú principal desde esta actividad
            }
        });

        builder.setNegativeButton("Realizar Otra Devolución", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implementa lo que deseas hacer cuando se presiona el botón "Realizar Otra Venta"
                // Por ejemplo, puedes iniciar la actividad de realizar venta sin datos
                Intent intent = new Intent(RealizarDevolucion.this, RealizarDevolucion.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
            }
        });

        // Mostrar el diálogo
        builder.setCancelable(false);
        builder.show();
    }

    private void desconectarImpresora() {
        if (connection != null) {
            connection.disconnect(); // Cierra la conexión Bluetooth
        }
    }

    private List<String> obtenerTodasLasSugerencias() {
        return codigosBarrasTickets;
    }

    private String generarIdUnico() {
        Random random = new Random();
        StringBuilder numeroAleatorio = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int digito = random.nextInt(10);
            numeroAleatorio.append(digito);
        }
        numeroAleatorio.append("TD");
        return numeroAleatorio.toString();
    }


    private List<String> obtenerListaCodigosTicketDesdeBD() {
        List<String> codigos = bbddController.obtenerListaCodigosTicket();
        return codigos;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                switch (requestCode) {
                    case PERMISSION_BLUETOOTH:
                    case PERMISSION_BLUETOOTH_ADMIN:
                    case PERMISSION_BLUETOOTH_CONNECT:
                    case PERMISSION_BLUETOOTH_SCAN:
                        checkBluetoothConnectPermission();
                        break;
                }
            } catch (EscPosEncodingException | EscPosBarcodeException | EscPosParserException | EscPosConnectionException e) {
                e.printStackTrace();
            }
        } else {
            // Permiso denegado, manejar el caso aquí
            Toast.makeText(this, "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show();
        }
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
                try {
                    verificarCodigoEscaneado();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
    private void actualizarSugerencias(List<String> sugerencias, ArrayAdapter<String> adapter) {
        // Limpiar el adaptador y agregar las nuevas sugerencias
        adapter.clear();
        adapter.addAll(sugerencias);

        // Notificar al adaptador que los datos han cambiado
        adapter.notifyDataSetChanged();
    }


    private void filtrarSugerencias(String input, ArrayAdapter<String> adapter) {
        // Filtrar la lista de sugerencias basada en el texto de entrada utilizando Stream en Java
        List<String> sugerenciasFiltradas = codigosBarrasTickets.stream()
                .filter(sugerencia -> sugerencia.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());

        // Limpiar el adaptador y agregar las nuevas sugerencias filtradas
        adapter.clear();
        adapter.notifyDataSetChanged();
        adapter.addAll(sugerenciasFiltradas);

        // Notificar al adaptador que los datos han cambiado
        adapter.notifyDataSetChanged();
    }



    private void verificarCodigoEscaneado() throws ParseException {
        // Realizar una consulta a la base de datos para verificar la existencia del ticket correspondiente al código escaneado
        boolean ticketExistente = bbddController.verificarExistenciaTicket(String.valueOf(codigoEscaneado), usuario.getIdTienda());

        Date fecha_limite = bbddController.obtenerFechaLimiteDevolucion(String.valueOf(codigoEscaneado));
        Date fecha_actual = obtenerFechaActualFormato();

        if (ticketExistente) {
            if (!fecha_limite.before(fecha_actual)) {
                cargarProductos();
                realizarDevolucion.setVisibility(View.VISIBLE);
            }else{

                AlertDialog.Builder builder = new AlertDialog.Builder(RealizarDevolucion.this);
                builder.setTitle("Ticket vencido");
                builder.setMessage("Ya no se puede realizar la devolución al haber superado la fecha limite de devolucion : "+fecha_limite.toString());
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }

        } else {
            // Si el ticket no existe, mostrar un AlertDialog informando al usuario que el ticket no está registrado
            AlertDialog.Builder builder = new AlertDialog.Builder(RealizarDevolucion.this);
            builder.setTitle("Ticket no registrado");
            builder.setMessage("El ticket escaneado no se encuentra registrado en esta tienda.");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public Date obtenerFechaActualFormato() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Establecer zona horaria a UTC
        String fechaActualStr = sdf.format(new Date());
        try {
            return sdf.parse(fechaActualStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
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
        if (producto.getCantidad() == 0) {
            // Mostrar un AlertDialog de advertencia si el producto no está disponible
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alerta");
            builder.setMessage("El producto seleccionado ya no está disponible en este ticket.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Cerrar el diálogo o ejecutar alguna acción adicional si es necesario
                }
            });
            builder.show();
        } else {
            // Verificar si el producto ya está en la lista de productos devueltos
            boolean productoExistente = false;
            for (ProductoModel p : listaProductosDevueltos) {
                if (p.getCodigoBarras() == producto.getCodigoBarras()) {
                    // Crear una copia del producto devuelto
                    ProductoModel productoDevuelto = new ProductoModel(p);

                    // Incrementar la cantidad del producto devuelto
                    productoDevuelto.setCantidad(p.getCantidad() + 1);

                    // Buscar el producto en listaProductosTicket
                    for (ProductoModel pm : listaProductosTicket) {
                        if (pm.getCodigoBarras() == producto.getCodigoBarras()) {
                            // Crear una copia del producto en listaProductosTicket
                            ProductoModel productoTicket = new ProductoModel(pm);

                            // Reducir la cantidad del producto en listaProductosTicket
                            productoTicket.setCantidad(pm.getCantidad() - 1);

                            // Actualizar listaProductosTicket con la copia modificada
                            listaProductosTicket.set(listaProductosTicket.indexOf(pm), productoTicket);

                            // Notificar a los adaptadores de los cambios en las listas
                            adaptadorProductoTicket.notifyDataSetChanged();
                            adaptadorDevuelto.notifyDataSetChanged();
                            break; // Salir del bucle una vez que se encuentre el producto
                        }
                    }

                    // Agregar el producto devuelto a la lista de productos devueltos
                    Iterator<ProductoModel> iterator = listaProductosDevueltos.iterator();
                    while (iterator.hasNext()) {
                        ProductoModel pb = iterator.next();
                        if (pb.getCodigoBarras().equals(productoDevuelto.getCodigoBarras())) {
                            iterator.remove(); // Eliminar el producto que coincide con el ID
                        }
                    }
                    listaProductosDevueltos.add(productoDevuelto);
                    productoExistente=true;
                    break;


                }
            }


            if (!productoExistente) {
                // Buscar el producto en listaProductosTicket
                for (ProductoModel p : listaProductosTicket) {
                    if (p.getCodigoBarras() == producto.getCodigoBarras()) {
                        // Reducir la cantidad del producto en 1
                        p.setCantidad(p.getCantidad() - 1);
                        adaptadorProductoTicket.notifyDataSetChanged();
                        adaptadorDevuelto.notifyDataSetChanged();
                        break; // Salir del bucle una vez que se encuentre el producto
                    }
                }

                // Agregar el producto a la lista de productos devueltos
                ProductoModel nuevoProducto = new ProductoModel(producto);
                nuevoProducto.setCantidad(1);

// Agregar el nuevo producto a la lista de productos devueltos
                listaProductosDevueltos.add(nuevoProducto);

                adaptadorProductoTicket.notifyDataSetChanged();
                adaptadorDevuelto.notifyDataSetChanged();
            }

            // Notificar a los adaptadores de los cambios en las listas
            adaptadorProductoTicket.notifyDataSetChanged();
            adaptadorDevuelto.notifyDataSetChanged();

            // Recalcular el precio total y actualizar la vista correspondiente
            totalDevolver.setText(Double.toString(calcularPrecioTotal(listaProductosDevueltos)));
        }
    }


    private double calcularPrecioTotal(List<ProductoModel> listaProductosDevueltos) {
        totalDevolucion = 0.0;

        // Recorre la lista de productos devueltos
        for (ProductoModel producto : listaProductosDevueltos) {
            // Obtiene el precio por unidad del producto y la cantidad devuelta
            double precioPorUnidad = producto.getPrecioUnidad();
            int cantidadDevuelta = producto.getCantidad();

            // Calcula el subtotal para este producto y lo suma al total de la devolución
            totalDevolucion += precioPorUnidad * cantidadDevuelta;
        }

        // Formatear el totalDevolucion para mostrar solo dos decimales con punto decimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        String totalDevolucionFormateado = df.format(totalDevolucion);

        // Convertir el totalDevolucion formateado de nuevo a double
        return Double.parseDouble(totalDevolucionFormateado);
    }


    @Override
    public void onQuantityChangedListenerUp(Producto_TicketModel producto) {

    }

    @Override
    public void onQuantityChangedDown(ProductoModel producto) {

        if (producto.getCantidad() == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Eliminar este producto de la lista?")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Eliminar el producto de la lista
                            int indexToRemove = -1; // Inicializa el índice a -1, indicando que no se ha encontrado el producto
                            for (int i = 0; i < listaProductosDevueltos.size(); i++) {
                                ProductoModel pro = listaProductosDevueltos.get(i);
                                if (pro.getCodigoBarras().equals(producto.getCodigoBarras())) {
                                    indexToRemove = i; // Almacena el índice del producto que coincida
                                    break; // Salir del bucle una vez que se encuentra el producto
                                }
                            }

                            if (indexToRemove != -1) {
                                // Aumentar la cantidad del producto correspondiente en la listaTicket
                                for (ProductoModel pm : listaProductosTicket) {
                                    if (pm.getCodigoBarras().equals(producto.getCodigoBarras())) {
                                        pm.setCantidad(pm.getCantidad() + 1);
                                        adaptadorProductoTicket.notifyDataSetChanged();
                                        break; // Salir del bucle una vez que se encuentra el producto
                                    }
                                }

                                // Eliminar el producto de la lista de productos devueltos
                                listaProductosDevueltos.remove(indexToRemove);
                                adaptadorDevuelto.notifyItemRemoved(indexToRemove);
                            }
                            totalDevolver.setText(String.valueOf(calcularPrecioTotal(listaProductosDevueltos)));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // No hacer nada
                        }
                    });
            builder.create().show();
        } else {

            int indexToRemove = -1; // Inicializa el índice a -1, indicando que no se ha encontrado el producto
            for (int i = 0; i < listaProductosDevueltos.size(); i++) {
                ProductoModel pro = listaProductosDevueltos.get(i);
                if (pro.getCodigoBarras().equals(producto.getCodigoBarras())) {
                    indexToRemove = i; // Almacena el índice del producto que coincida
                    break; // Salir del bucle una vez que se encuentra el producto
                }
            }

            if (indexToRemove != -1) {
                // Disminuir la cantidad del producto en listaProductosDevueltos
                ProductoModel productoDevuelto = listaProductosDevueltos.get(indexToRemove);
                productoDevuelto.setCantidad(productoDevuelto.getCantidad() - 1);
                adaptadorDevuelto.notifyItemChanged(indexToRemove);

                // Aumentar la cantidad del producto correspondiente en la listaTicket
                for (ProductoModel pm : listaProductosTicket) {
                    if (pm.getCodigoBarras().equals(producto.getCodigoBarras())) {
                        pm.setCantidad(pm.getCantidad() + 1);
                        adaptadorProductoTicket.notifyDataSetChanged();
                        break; // Salir del bucle una vez que se encuentra el producto
                    }
                }
                totalDevolver.setText(String.valueOf(calcularPrecioTotal(listaProductosDevueltos)));
            }


        }


    }



    @Override
    public void onProductRemoved(int position) {
        ProductoModel productoRemovido = listaProductosDevueltos.get(position);

        // Incrementar la cantidad del producto removido en la lista de productos del ticket
        for (ProductoModel producto : listaProductosTicket) {
            if (producto.getCodigoBarras() == productoRemovido.getCodigoBarras()) {
                producto.setCantidad(producto.getCantidad() + 1); // Incrementar la cantidad
                break;
            }
        }

        // Eliminar el producto de la lista de productos devueltos
        listaProductosDevueltos.remove(position);

        // Notificar al adaptador de la lista de productos devueltos que los datos han cambiado
        adaptadorDevuelto.notifyDataSetChanged();

        // Actualizar el precio total
        totalDevolver.setText(Double.toString(calcularPrecioTotal(listaProductosDevueltos)));
    }


}