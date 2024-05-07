package com.example.tfg_sistematienda.vistas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
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
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CrearProducto extends AppCompatActivity {

    Button tomarFoto, subirFoto, crearProducto, generarCodigoBarras, imprimirCodigoBarras, cancelarCrearProducto;
    ImageView fotoProducto, imagenCodigoBarras;
    EditText codigoBarras, nombre, descripcion, cantidadStock, precioUnidad;

    private String codigoBarrasProducto;
    private Random random = new Random();

    private BBDDController bbddController= new BBDDController();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 17;
    private static final int REQUEST_EXTERNAL_STORAGE = 189;

    private static final int REQUEST_IMAGE_GALLERY = 16;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 123;

    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 22;


    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;


    private Bitmap bitmap, codigoBarrasBitmap;

    private byte[] imagenenByte=null;

     private byte[] imagenDefectoByte;


    private BluetoothConnection connection;
    private EscPosPrinter printer;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_producto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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

        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        imprimirCodigoBarras.setEnabled(false);

        Bitmap imagenDefecto = BitmapFactory.decodeResource(getResources(), R.mipmap.productosinimagen);
        imagenDefectoByte = bitmapToByteArray(imagenDefecto);

        connection = null;
        printer = null;

        requestStoragePermission();


        generarCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarCodigoBarras.setEnabled(false);

                // Obtener la lista de todos los códigos de barras desde la base de datos
                List<String> listaCodigosBarras = obtenerListaCodigosBarrasDesdeBD();

                // Generar un código de barras único
                String nuevoCodigoBarras;
                do {
                    nuevoCodigoBarras = generarCodigoBarrasUnico();
                } while (listaCodigosBarras.contains(nuevoCodigoBarras));

                // Mostrar el nuevo código de barras
                codigoBarrasProducto = nuevoCodigoBarras;
                codigoBarras.setText(codigoBarrasProducto);
                mostrarCodigoBarrasEnImagen(codigoBarrasProducto);
            }
        });


        subirFoto.setOnClickListener(v -> seleccionarImagen());


        tomarFoto.setOnClickListener(v -> tomarFotoDispositivo());

        cancelarCrearProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarCancelar();
            }
        });



        imprimirCodigoBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    checkBluetoothConnectPermission();
                } catch (EscPosEncodingException | EscPosBarcodeException | EscPosParserException | EscPosConnectionException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        cantidadStock.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(5), // Limita la cantidad de caracteres a 5
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si el texto resultante contiene solo dígitos y tiene una longitud adecuada
                        if (!builder.toString().matches("\\d{0,5}")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });


        precioUnidad.setFilters(new InputFilter[] {
                new InputFilter() {
                    boolean isDecimalInserted = false;

                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si se insertó un punto o coma decimal
                        if (source.equals(".") || source.equals(",")) {
                            // Verificar si ya se ha insertado un punto o coma
                            if (isDecimalInserted || dstart == 0) {
                                return ""; // Evitar que se introduzca más de un punto o coma o que esté al principio
                            } else {
                                isDecimalInserted = true;
                            }
                        }

                        // Verificar si el texto resultante cumple con el formato numérico deseado
                        if (!builder.toString().matches("^\\d+\\.?\\d{0,2}$")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        // Verificar si el texto resultante tiene más de 7 dígitos en total
                        if (builder.length() > 7) {
                            return ""; // Si tiene más de 7 dígitos, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });



        crearProducto.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (comprobarDatosProducto()){
                  insertarProductoBBDD();
               }
           }
        });

    }


    private void insertarProductoBBDD(){
        String nombreProducto = nombre.getText().toString();
        String descripcionProducto = descripcion.getText().toString();
        int cantidadProducto = Integer.parseInt(cantidadStock.getText().toString());
        double precioProducto = Double.parseDouble(precioUnidad.getText().toString());
        String codigoBarrasProducto = codigoBarras.getText().toString();

        if (imagenenByte==null){
            if (bbddController.insertarProducto(codigoBarrasProducto, nombreProducto, descripcionProducto, cantidadProducto, precioProducto, 0, 0, imagenDefectoByte, "012541689P"  )){
                mostrarDialogoCrearOtroProducto();
            }else{
                mostrarAlertaErrorBBDD();
            }
        }else{
            if (bbddController.insertarProducto(codigoBarrasProducto, nombreProducto, descripcionProducto, cantidadProducto, precioProducto, 0, 0, imagenenByte, "012541689P" )){
                mostrarDialogoCrearOtroProducto();
            }else{
                mostrarAlertaErrorBBDD();
            }
        }



    }


    private void confirmarCancelar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿ESTAS SEGURO?");
        builder.setMessage("¿ESTAS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();
    }



    private boolean comprobarDatosProducto() {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombre.setError(null);
        descripcion.setError(null);
        cantidadStock.setError(null);
        precioUnidad.setError(null);
        codigoBarras.setError(null);

        // Verificar el nombre
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la descripción
        if (descripcion.getText().toString().isEmpty()) {
            descripcion.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la cantidad en stock
        if (cantidadStock.getText().toString().isEmpty()) {
            cantidadStock.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (precioUnidad.getText().toString().isEmpty()) {
            precioUnidad.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el código de barras
        if (codigoBarras.getText().toString().equals("SIN CODIGO DE BARRAS")) {
            codigoBarras.setError("Debes generar un CÓDIGO DE BARRAS");
            datosValidos = false;
        }

        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
    }


    private void vaciarCampos(){
        nombre.setText("");
        descripcion.setText("");
        cantidadStock.setText("");
        precioUnidad.setText("");
        codigoBarras.setText("SIN CODIGO DE BARRAS");
        codigoBarras.setEnabled(false);
        fotoProducto.setImageResource(R.mipmap.productosinimagen);
        generarCodigoBarras.setEnabled(true);
        imagenenByte=null;
        nombre.setError(null);
        descripcion.setError(null);
        cantidadStock.setError(null);
        precioUnidad.setError(null);
        codigoBarras.setError(null);
        imagenCodigoBarras.setImageResource(R.mipmap.codigobarrasvacio);

    }


    private void mostrarAlertaErrorBBDD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearProducto.this);
        builder.setTitle("Error en la inserccion en BBDD")
                .setMessage("Hubo un error a la hora de insertar en BBDD. Compruebe los campos que sean ideales. Puede suceder que haya un error interno en la BBDD. Lo sentimos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void mostrarDialogoCrearOtroProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Producto creado exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        builder.setPositiveButton("Crear otro producto", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes agregar el código para crear otro usuario
                // Por ejemplo, puedes limpiar los campos del formulario
                // y permitir al usuario ingresar los datos de otro usuario.
                vaciarCampos();
            }
        });
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();

    }


    private List<String> obtenerListaCodigosBarrasDesdeBD() {
        List <String> codigos = bbddController.obtenerListaCodigosBarras();
        return codigos;
    }



    private String generarCodigoBarrasUnico() {
        Random random = new Random();
        StringBuilder numeroAleatorio = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int digito = random.nextInt(10);
            numeroAleatorio.append(digito);
        }
        return numeroAleatorio.toString();
    }



    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }




    public void seleccionarImagen(){
        requestStoragePermission();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);


    }

    public void tomarFotoDispositivo(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    fotoProducto.setImageBitmap(bitmap);
                    imagenenByte= bitmapToByteArray(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                fotoProducto.setImageBitmap(bitmap);
                imagenenByte= bitmapToByteArray(bitmap);
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de la cámara concedido, puedes abrir la cámara
                } else {
                    // Permiso de la cámara denegado, muestra un mensaje o toma otra acción adecuada.
                }
                break;
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de escritura en almacenamiento externo concedido, procede con el guardado de la imagen
                    guardarImagen(bitmap); // Aquí llama al método para guardar la imagen
                } else {
                    // Permiso de escritura en almacenamiento externo denegado, muestra un mensaje o toma otra acción adecuada.
                }
                break;
            case REQUEST_BLUETOOTH_CONNECT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de conexión Bluetooth concedido, puedes proceder con la funcionalidad Bluetooth
                    try {
                        conectarImpresora();
                    } catch (EscPosEncodingException e) {
                        throw new RuntimeException(e);
                    } catch (EscPosBarcodeException e) {
                        throw new RuntimeException(e);
                    } catch (EscPosParserException e) {
                        throw new RuntimeException(e);
                    } catch (EscPosConnectionException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Permiso de conexión Bluetooth denegado, muestra un mensaje o toma otra acción adecuada.
                }
                break;
            // Agrega más casos si necesitas manejar más códigos de solicitud de permisos
        }
    }


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
            canvas.drawText(codigoBarras, bitmap.getWidth() / 2, bitmap.getHeight() + 40, paint);

            // Establecer la imagen combinada en el ImageView
            imagenCodigoBarras.setImageBitmap(combinedBitmap);
            codigoBarrasBitmap=combinedBitmap;

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

            guardarImagen(combinedBitmap);

            imprimirCodigoBarras.setEnabled(true);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }





    private void guardarImagen(Bitmap bitmap) {
        requestStoragePermission();

        File directory = new File(getExternalFilesDir(null), "MiCarpeta");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "codigo_barras_" + timeStamp + ".png";
        File file = new File(directory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }








    private void conectarImpresora() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // El Bluetooth no está activado, solicita al usuario que lo active
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
                            printer.printFormattedText("[C]<barcode type='128' height='10'>"+codigoBarrasProducto+"</barcode>\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Manejar cualquier error de conexión o impresión aquí
                        }
                    }
                });
                builder.show();
            } else {
                // Manejar el caso en que no haya dispositivos emparejados
                return;
            }
        }
    }


/*
        if (targetDevice != null) {
            try {
                // Obtener el socket Bluetooth
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                // Establecer la conexión Bluetooth
                bluetoothSocket.connect();

                // Obtener el OutputStream para enviar comandos ESC/POS
                OutputStream outputStream = bluetoothSocket.getOutputStream();

                // Enviar comandos ESC/POS a la impresora
                String textToPrint = "Hello, ESC/POS!";
                byte[] bytesToPrint = textToPrint.getBytes("UTF-8");
                outputStream.write(bytesToPrint);

                // Finalizar la conexión Bluetooth
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar el error de conexión
            }
        }
*/

    private void checkBluetoothConnectPermission() throws EscPosEncodingException, EscPosBarcodeException, EscPosParserException, EscPosConnectionException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MainActivity.PERMISSION_BLUETOOTH_ADMIN);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MainActivity.PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MainActivity.PERMISSION_BLUETOOTH_SCAN);
        } else {
            conectarImpresora();
        }
    }


    private void desconectarImpresora() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





/*
    private void imprimirCodigoBarras(Bitmap bitmap) {
        // Verificar si el socket Bluetooth está disponible y conectado
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            // La impresora Bluetooth no está disponible o no está conectada
            return;
        }

        try {
            // Convertir la imagen del código de barras a un formato imprimible (por ejemplo, ESC/POS)
            byte[] datosImprimibles = convertirImagenAFormatoImprimible(bitmap);

            // Enviar los datos a la impresora Bluetooth
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(datosImprimibles);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar errores de impresión
        }
    }
 */


    private byte[] convertirImagenAFormatoImprimible(Bitmap bitmap) {
        // Aquí deberías implementar la conversión de la imagen del código de barras a un formato imprimible
        // Puedes utilizar bibliotecas como EscPosPrinter para hacer esto
        // En este ejemplo, simplemente se convierte la imagen a un array de bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


/*

    private void imprimirCodigoBarras(Bitmap bitmap) {
        // Verificar si el socket Bluetooth está disponible y conectado
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            // La impresora Bluetooth no está disponible o no está conectada
            return;
        }

        try {
            // Convertir la imagen a un formato imprimible para la impresora térmica
            byte[] datosImprimibles = convertirImagenAFormatoTermico(bitmap);

            // Enviar los datos a la impresora Bluetooth
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(datosImprimibles);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar errores de impresión
        }
    }

    private byte[] convertirImagenAFormatoTermico(Bitmap bitmap) {
        // Escalar la imagen al tamaño de la etiqueta de 40x30 mm
        int nuevoAncho = 400; // 40 mm en píxeles a 10 píxeles/mm
        int nuevoAlto = 300; // 30 mm en píxeles a 10 píxeles/mm
        Bitmap imagenEscalada = Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);

        // Convertir la imagen a escala de grises
        Bitmap imagenEscalaGrises = convertirAGrises(imagenEscalada);

        // Convertir la imagen a datos de impresión en formato térmico
        byte[] datosImprimibles = convertirImagenATermico(imagenEscalaGrises);

        return datosImprimibles;
    }

    private Bitmap convertirAGrises(Bitmap bitmap) {
        // Convertir la imagen a escala de grises
        Bitmap imagenGrises = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imagenGrises);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // Configurar la matriz de color para convertir a escala de grises
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return imagenGrises;
    }

    private byte[] convertirImagenATermico(Bitmap bitmap) {
        // Aquí debes implementar la conversión de la imagen a datos de impresión en formato térmico
        // Esto depende de la especificación de comandos de tu impresora térmica
        // Deberás investigar la documentación de tu impresora para saber cómo enviar imágenes
        // Normalmente, las impresoras térmicas utilizan comandos específicos para imprimir imágenes
        // Consulta el manual de tu impresora para obtener más detalles sobre el formato de los datos de impresión
        return null;
    }



    private void imprimirImagen(File file) {
        if (bluetoothSocket != null) {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();

                // Iniciar la conexión de impresión
                outputStream.write(new byte[]{0x1B, 0x40});

                // Enviar la imagen como datos binarios
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();

                // Finalizar la conexión de impresión
                outputStream.write(new byte[]{0x1B, 0x64, 0x02});
            } catch (IOException e) {
                e.printStackTrace();
                // Manejar el error de impresión
            }
        }
    }
*/


}