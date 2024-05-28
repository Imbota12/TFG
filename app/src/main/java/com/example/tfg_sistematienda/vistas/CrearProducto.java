package com.example.tfg_sistematienda.vistas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class CrearProducto extends AppCompatActivity {

    // Constante para la solicitud de habilitación de Bluetooth
    private static final int REQUEST_ENABLE_BT = 17;
    // Constante para la solicitud de permiso de almacenamiento externo
    private static final int REQUEST_EXTERNAL_STORAGE = 189;
    // Constante para la solicitud de imagen desde la galería
    private static final int REQUEST_IMAGE_GALLERY = 16;
    // Constante para la solicitud de captura de imagen con la cámara
    private static final int REQUEST_IMAGE_CAMERA = 2;
    // Constante para la solicitud de permiso de uso de la cámara
    private static final int REQUEST_CAMERA_PERMISSION = 123;
    // Constante para la solicitud de permiso de conexión Bluetooth
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 22;
    // Constante para la solicitud de permiso de escritura en almacenamiento externo
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
    // Declaración de botones de la interfaz
    ImageButton tomarFoto, subirFoto, crearProducto, generarCodigoBarras, imprimirCodigoBarras, cancelarCrearProducto;
    // Declaración de vistas de imagen de la interfaz
    ImageView fotoProducto, imagenCodigoBarras;
    // Declaración de campos de texto editables
    EditText codigoBarras, nombre, descripcion, cantidadStock, precioUnidad;
    // Variable para almacenar el código de barras del producto
    private String codigoBarrasProducto;
    // Objeto Random para generar números aleatorios
    private Random random = new Random();
    // Controlador para interactuar con la base de datos
    private BBDDController bbddController = new BBDDController();
    // Adaptador Bluetooth para manejar conexiones Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    // Dispositivo Bluetooth seleccionado para la conexión
    private BluetoothDevice bluetoothDevice;
    // Socket Bluetooth para la conexión
    private BluetoothSocket bluetoothSocket;
    // Bitmap para almacenar la imagen del producto
    private Bitmap bitmap;

    // Bitmap para almacenar la imagen del código de barras
    private Bitmap codigoBarrasBitmap;

    // Array de bytes para almacenar la imagen en formato de bytes
    private byte[] imagenenByte = null;

    // Array de bytes para almacenar la imagen por defecto en formato de bytes
    private byte[] imagenDefectoByte;

    // Conexión Bluetooth para la impresora
    private BluetoothConnection connection;

    // Impresora EscPos para imprimir etiquetas
    private EscPosPrinter printer;

    // Modelo del usuario que está utilizando la aplicación
    private UsuarioModel usuario;
    private boolean allowBackPress=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilita el modo EdgeToEdge para una experiencia de pantalla completa
        EdgeToEdge.enable(this);

        // Establece el diseño de la actividad a 'activity_crear_producto'
        setContentView(R.layout.activity_crear_producto);

        // Ajusta los márgenes de la vista principal para que no queden detrás de las barras del sistema
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

        // Inicializa las vistas y asigna las variables correspondientes
        initializeViews();

        // Establece los valores por defecto para las vistas
        setDefaultValues();

        // Solicita los permisos necesarios para el almacenamiento
        requestStoragePermission();

        // Configura el botón para generar un código de barras único
        generarCodigoBarras.setOnClickListener(v -> {
            generarCodigoBarras.setEnabled(false);
            String nuevoCodigoBarras;

            // Obtiene la lista de todos los códigos de barras desde la base de datos
            List<String> listaCodigosBarras = obtenerListaCodigosBarrasDesdeBD();

            // Genera un código de barras único que no esté en la lista obtenida
            do {
                nuevoCodigoBarras = generarCodigoBarrasUnico();
            } while (listaCodigosBarras.contains(nuevoCodigoBarras));

            // Asigna y muestra el nuevo código de barras
            codigoBarrasProducto = nuevoCodigoBarras;
            codigoBarras.setText(codigoBarrasProducto);
            mostrarCodigoBarrasEnImagen(codigoBarrasProducto);
        });

        // Configura el botón para seleccionar una imagen desde la galería
        subirFoto.setOnClickListener(v -> seleccionarImagen());

        // Configura el botón para tomar una foto con la cámara
        tomarFoto.setOnClickListener(v -> tomarFotoDispositivo());

        // Configura el botón para cancelar la creación del producto y volver al menú principal
        cancelarCrearProducto.setOnClickListener(v -> confirmarCancelar());

        // Configura el botón para imprimir el código de barras
        imprimirCodigoBarras.setOnClickListener(v -> {
            try {
                // Verifica los permisos de conexión Bluetooth antes de intentar imprimir
                checkBluetoothConnectPermission();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Configura los filtros para los campos de texto editables
        setEditTextFilters();

        // Configura el botón para crear un producto con los datos ingresados
        crearProducto.setOnClickListener(v -> {
            // Verifica que los datos del producto sean válidos antes de insertar en la base de datos
            if (comprobarDatosProducto()) {
                insertarProductoBBDD();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón CANCELAR", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        tomarFoto = findViewById(R.id.hacer_foto);
        subirFoto = findViewById(R.id.elegir_foto);
        fotoProducto = findViewById(R.id.imagen_producto);
        generarCodigoBarras = findViewById(R.id.generar_cod_barras);
        imprimirCodigoBarras = findViewById(R.id.imprimir_cod_barras);
        imagenCodigoBarras = findViewById(R.id.imagen_cod_barras);
        nombre = findViewById(R.id.nombre_producto);
        descripcion = findViewById(R.id.descripcion_producto);
        cantidadStock = findViewById(R.id.unidades_producto);
        precioUnidad = findViewById(R.id.precio_producto);
        codigoBarras = findViewById(R.id.codigobarras_producto);
        crearProducto = findViewById(R.id.crear_producto);
        cancelarCrearProducto = findViewById(R.id.cancelar_crear_producto);
    }


    // Método para establecer los valores por defecto en las vistas
    private void setDefaultValues() {
        // Establece la imagen predeterminada del producto en la vista de la foto
        fotoProducto.setImageResource(R.mipmap.productosinimagen);

        // Establece la imagen predeterminada del código de barras en la vista de imagen del código de barras
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);

        // Establece el texto por defecto del campo de texto de código de barras y lo deshabilita
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);

        // Deshabilita el botón de imprimir código de barras por defecto
        imprimirCodigoBarras.setEnabled(false);

        // Convierte la imagen predeterminada en formato Bitmap
        Bitmap imagenDefecto = BitmapFactory.decodeResource(getResources(), R.mipmap.productosinimagen);

        // Convierte la imagen predeterminada en formato de bytes y la asigna a la variable imagenDefectoByte
        imagenDefectoByte = bitmapToByteArray(imagenDefecto);
    }


    // Método para configurar los filtros de entrada para los campos de texto editables
    private void setEditTextFilters() {
        // Configura el filtro de entrada para el campo de cantidad de stock
        cantidadStock.setFilters(new InputFilter[]{
                // Limita la longitud del texto a 5 caracteres
                new InputFilter.LengthFilter(5),
                // Filtra el texto de entrada para que solo contenga dígitos y tenga una longitud adecuada
                (source, start, end, dest, dstart, dend) -> {
                    StringBuilder builder = new StringBuilder(dest);
                    builder.replace(dstart, dend, source.subSequence(start, end).toString());
                    return (!builder.toString().matches("\\d{0,5}")) ? "" : null;
                }
        });

        // Configura el filtro de entrada para el campo de precio por unidad
        precioUnidad.setFilters(new InputFilter[]{
                new InputFilter() {
                    private boolean isDecimalInserted = false;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());
                        if ((source.equals(".") || source.equals(",")) && (isDecimalInserted || dstart == 0)) {
                            return "";
                        }
                        isDecimalInserted = builder.toString().contains(".") || builder.toString().contains(",");
                        return (!builder.toString().matches("^\\d+\\.?\\d{0,2}$") || builder.length() > 7) ? "" : null;
                    }
                }
        });
    }


    // Método para insertar el producto en la base de datos
    private void insertarProductoBBDD() {
        // Obtiene los datos del producto desde los campos de texto editables
        String nombreProducto = nombre.getText().toString();
        String descripcionProducto = descripcion.getText().toString();
        int cantidadProducto = Integer.parseInt(cantidadStock.getText().toString());
        double precioProducto = Double.parseDouble(precioUnidad.getText().toString());
        String codigoBarrasProducto = codigoBarras.getText().toString();
        String idTienda = usuario.getIdTienda();

        // Determina la imagen a utilizar (imagen actual o imagen por defecto) y la convierte en bytes
        byte[] imagen = (imagenenByte == null) ? imagenDefectoByte : imagenenByte;

        // Inserta el producto en la base de datos y registra un mensaje de registro
        boolean isInserted = bbddController.insertarProducto(codigoBarrasProducto, nombreProducto, descripcionProducto, cantidadProducto, precioProducto, 0, 0, imagen, idTienda);
        String logMessage = isInserted ? "Producto con codigo " + codigoBarrasProducto + " creado exitosamente" : "Error al insertar el nuevo producto " + codigoBarrasProducto + " en BBDD";
        bbddController.insertarLog(logMessage, LocalDateTime.now(), usuario.getDni());

        // Muestra un diálogo o una alerta según el resultado de la inserción en la base de datos
        if (isInserted) {
            mostrarDialogoCrearOtroProducto();
        } else {
            mostrarAlertaErrorBBDD();
        }
    }


    // Método para mostrar un diálogo de confirmación antes de cancelar la operación de creación del producto
    private void confirmarCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("¿ESTAS SEGURO?")
                .setMessage("¿ESTAS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?")
                .setPositiveButton("SI", (dialog, which) -> {
                    // Registra un mensaje de registro y finaliza la actividad actual
                    bbddController.insertarLog("Cancela generar el producto", LocalDateTime.now(), usuario.getDni());
                    finish();
                })
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .setCancelable(false) // Evita que el diálogo se cierre al tocar fuera de él
                .show(); // Muestra el diálogo en la interfaz
    }


    // Método para comprobar si los datos del producto son válidos
    private boolean comprobarDatosProducto() {
        // Inicializa la bandera para verificar la validez de los datos
        boolean datosValidos = true;

        // Valida cada campo del producto utilizando el método validarCampo()
        datosValidos &= validarCampo(nombre, "Campo vacío");
        datosValidos &= validarCampo(descripcion, "Campo vacío");
        datosValidos &= validarCampo(cantidadStock, "Campo vacío");
        datosValidos &= validarCampo(precioUnidad, "Campo vacío");

        // Verifica si se generó un código de barras antes de continuar
        if (codigoBarras.getText().toString().equals("SIN CODIGO DE BARRAS")) {
            // Si no se generó un código de barras, muestra un error y marca los datos como inválidos
            codigoBarras.setError("Debes generar un CÓDIGO DE BARRAS");
            datosValidos = false;
        }

        // Si hay errores, muestra un mensaje de advertencia al usuario
        if (!datosValidos) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        // Devuelve true si todos los datos son válidos, de lo contrario devuelve false
        return datosValidos;
    }


    // Método para validar un campo de texto editado
    private boolean validarCampo(EditText campo, String errorMensaje) {
        // Verifica si el campo está vacío
        if (campo.getText().toString().isEmpty()) {
            // Si está vacío, muestra un mensaje de error y devuelve false
            campo.setError(errorMensaje);
            return false;
        }
        // Si no está vacío, devuelve true
        return true;
    }


    // Método para limpiar los campos del formulario
    private void vaciarCampos() {
        // Limpia todos los campos del formulario y restablece los valores predeterminados
        nombre.setText("");
        descripcion.setText("");
        cantidadStock.setText("");
        precioUnidad.setText("");
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        generarCodigoBarras.setEnabled(true);
        imagenenByte = null;
        nombre.setError(null);
        descripcion.setError(null);
        cantidadStock.setError(null);
        precioUnidad.setError(null);
        codigoBarras.setError(null);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);
    }


    // Método para mostrar una alerta en caso de error en la inserción en la base de datos
    private void mostrarAlertaErrorBBDD() {
        // Crea y muestra un diálogo de alerta con un mensaje de error personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearProducto.this);
        builder.setTitle("Error en la inserción en BBDD")
                .setMessage("Hubo un error a la hora de insertar en BBDD. Compruebe los campos que sean ideales. Puede suceder que haya un error interno en la BBDD. Lo sentimos")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss()) // Botón para cerrar el diálogo
                .show();
    }


    // Método para mostrar un diálogo con opciones después de crear exitosamente un producto
    private void mostrarDialogoCrearOtroProducto() {
        // Crea y muestra un diálogo con opciones para crear otro producto o volver al menú principal
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Producto creado exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        // Opción para crear otro producto
        builder.setPositiveButton("Crear otro producto", (dialog, which) -> {
            // Limpia los campos del formulario para permitir al usuario crear otro producto
            vaciarCampos();
            // Registra un mensaje de registro indicando que se desea crear otro producto
            bbddController.insertarLog("Desea crear otro producto", LocalDateTime.now(), usuario.getDni());
        });
        // Opción para volver al menú principal
        builder.setNegativeButton("Volver al menú", (dialog, which) -> {
            // Registra un mensaje de registro indicando que se vuelve al menú principal
            bbddController.insertarLog("Vuelve al menú de reponedor", LocalDateTime.now(), usuario.getDni());
            // Finaliza la actividad actual para volver al menú principal
            finish();
        });
        builder.setCancelable(false); // Evita que el diálogo se cierre al tocar fuera de él
        builder.show(); // Muestra el diálogo en la interfaz
    }


    // Método para obtener una lista de códigos de barras desde la base de datos
    private List<String> obtenerListaCodigosBarrasDesdeBD() {
        // Obtiene la lista de códigos de barras utilizando el controlador de la base de datos
        return bbddController.obtenerListaCodigosBarras(); // Retorna la lista de códigos de barras
    }


    // Método para generar un código de barras único
    private String generarCodigoBarrasUnico() {
        Random random = new Random();
        StringBuilder numeroAleatorio = new StringBuilder();
        // Genera un código de barras de 13 dígitos de forma aleatoria
        for (int i = 0; i < 13; i++) {
            int digito = random.nextInt(10);
            numeroAleatorio.append(digito);
        }
        // Registra un mensaje de registro indicando que se ha generado un código de barras
        bbddController.insertarLog("Genera codigo barras " + numeroAleatorio + " ", LocalDateTime.now(), usuario.getDni());
        return numeroAleatorio.toString(); // Retorna el código de barras generado
    }


    // Método para solicitar permiso de almacenamiento externo
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicita permiso al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    // Método para seleccionar una imagen de la galería
    public void seleccionarImagen() {
        requestStoragePermission(); // Solicita permiso de almacenamiento antes de abrir la galería
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY); // Inicia la actividad de selección de imagen
    }


    // Método para tomar una foto con la cámara del dispositivo
    public void tomarFotoDispositivo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Si el permiso está concedido, iniciar la actividad de la cámara
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
        }
    }


    // Método llamado cuando se completa una actividad (por ejemplo, al seleccionar una imagen de la galería o tomar una foto)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                // Si la solicitud es para seleccionar una imagen de la galería y hay datos disponibles
                Uri uri = data.getData();
                try {
                    // Convierte la URI de la imagen en un objeto Bitmap
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    // Registra un mensaje de registro indicando que se seleccionó una foto de la galería
                    bbddController.insertarLog("Selección foto galería", LocalDateTime.now(), usuario.getDni());
                    // Muestra la imagen seleccionada en un ImageView
                    fotoProducto.setImageBitmap(bitmap);
                    // Convierte el Bitmap en un array de bytes
                    imagenenByte = bitmapToByteArray(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                // Si la solicitud es para tomar una foto con la cámara y hay datos disponibles
                bitmap = (Bitmap) data.getExtras().get("data");
                // Registra un mensaje de registro indicando que se tomó una foto con la cámara
                bbddController.insertarLog("Toma foto cámara", LocalDateTime.now(), usuario.getDni());
                // Muestra la foto tomada en un ImageView
                fotoProducto.setImageBitmap(bitmap);
                // Convierte el Bitmap en un array de bytes
                imagenenByte = bitmapToByteArray(bitmap);
            }
        }
    }


    // Método para convertir un objeto Bitmap en un array de bytes
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    // Método llamado cuando se solicitan permisos al usuario y se reciben las respuestas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de la cámara concedido, puedes abrir la cámara
                }
                break;
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de escritura en almacenamiento externo concedido, procede con el guardado de la imagen
                    guardarImagen(bitmap); // Aquí llama al método para guardar la imagen
                }
                break;
            case REQUEST_BLUETOOTH_CONNECT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de conexión Bluetooth concedido, puedes proceder con la funcionalidad Bluetooth
                    try {
                        conectarImpresora();
                    } catch (EscPosEncodingException | EscPosConnectionException |
                             EscPosParserException | EscPosBarcodeException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            // Agrega más casos si necesitas manejar más códigos de solicitud de permisos
        }
    }


    // Método para mostrar el código de barras en una imagen
    public void mostrarCodigoBarrasEnImagen(String codigoBarras) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            // Generar el código de barras
            BitMatrix bitMatrix = barcodeEncoder.encode(codigoBarras, BarcodeFormat.CODE_128, 800, 200);
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Crear un nuevo bitmap con el código de barras y el número debajo
            Bitmap combinedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() + 50, bitmap.getConfig());
            Canvas canvas = new Canvas(combinedBitmap);

            // Dibujar el código de barras en el lienzo
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Configurar el texto para el número debajo del código de barras
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);

            // Dibujar el número debajo del código de barras
            canvas.drawText(codigoBarras, (float) bitmap.getWidth() / 2, bitmap.getHeight() + 40, paint);

            // Establecer la imagen combinada en el ImageView
            imagenCodigoBarras.setImageBitmap(combinedBitmap);
            codigoBarrasBitmap = combinedBitmap;

            // Verificar el permiso de escritura en almacenamiento externo antes de guardar la imagen
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no ha sido concedido, solicitarlo
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // Si el permiso ha sido concedido, guardar la imagen
                guardarImagen(combinedBitmap);
            }

            // Habilitar el botón de impresión de código de barras
            imprimirCodigoBarras.setEnabled(true);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }


    // Método para guardar una imagen en el almacenamiento externo
    private void guardarImagen(Bitmap bitmap) {
        requestStoragePermission();

        // Crear un directorio para almacenar la imagen
        File directory = new File(getExternalFilesDir(null), "MiCarpeta");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Crear un nombre de archivo único para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "codigo_barras_" + timeStamp + ".png";
        File file = new File(directory, fileName);

        try {
            // Guardar la imagen en el archivo
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Método para conectar a la impresora Bluetooth
    private void conectarImpresora() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        // Obtener el adaptador Bluetooth predeterminado
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicitar al usuario que lo active
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Manejar el caso en que los permisos no estén concedidos
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
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
                builder.setItems(deviceNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Conectar al dispositivo seleccionado
                        BluetoothDevice selectedDevice = devices.get(which);
                        try {
                            connection = new BluetoothConnection(selectedDevice);
                            printer = new EscPosPrinter(connection, 200, 50f, 35);
                            // Imprimir
                            printer.printFormattedText("[C]<barcode type='128' height='10'>" + codigoBarrasProducto + "</barcode>\n");
                            bbddController.insertarLog("Imprime codigo de barras " + codigoBarrasProducto + " con impresora " + selectedDevice.getAddress().toString(), LocalDateTime.now(), usuario.getDni());
                            desconectarImpresora();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Manejar cualquier error de conexión o impresión aquí
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                // Manejar el caso en que no haya dispositivos emparejados
                return;
            }
        }
    }


    private void checkBluetoothConnectPermission() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MainActivity.PERMISSION_BLUETOOTH_ADMIN);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MainActivity.PERMISSION_BLUETOOTH_CONNECT);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MainActivity.PERMISSION_BLUETOOTH_SCAN);
        } else {
            conectarImpresora();
        }
    }


    private void desconectarImpresora() {
        if (connection != null) {
            connection.disconnect(); // Cierra la conexión Bluetooth
        }
    }

}