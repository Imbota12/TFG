package com.example.tfg_sistematienda.vistas;



import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorProducto;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.example.tfg_sistematienda.R;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListaInventario extends AppCompatActivity {


    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 18;

    static final String TAG = "ScanBarcodeActivity";
    private static final int MAX_CELL_LENGTH = 32767;
    private boolean allowBackPress = false;
    private ImageButton botonEscaneo;
    private String codigoEscaneado;

    private EditText codigoBuscar;

    private RecyclerView recyclerView;
    private AdaptadorProducto adaptadorProducto;
    private List<ProductoModel> listaProductos;
    private ImageButton generarExcel, bajoStock, volverMenu;
    // Modelo del usuario que está utilizando la aplicación
    private UsuarioModel usuario;


    private BBDDController bbddController= new BBDDController();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_inventario);
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

        recyclerView = findViewById(R.id.lista_productos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarProductos();

        adaptadorProducto = new AdaptadorProducto(this, listaProductos);
        adaptadorProducto = new AdaptadorProducto(this, listaProductos);
        recyclerView.setAdapter(adaptadorProducto);

        codigoBuscar = findViewById(R.id.et_codigo_buscar);
        botonEscaneo = findViewById(R.id.escanear_producto);
        generarExcel = findViewById(R.id.genera_Excel);
        bajoStock = findViewById(R.id.bajo_stock);
        volverMenu = findViewById(R.id.volvermenu);

        volverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaInventario.this, GeneralReponedor.class);
                intent.putExtra("usuarioDNI", usuarioDNI);
                startActivity(intent);
            }
        });

        generarExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      try {
                        generarExcel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });


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

        botonEscaneo.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                iniciarEscaner();
            }
        });

        bajoStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarNotificacionStockBajo();
            }
        });

        // Solicitar permisos al inicio si no están concedidos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarNotificacionStockBajo() {
        List<ProductoModel> productosBajoStock = new ArrayList<>();
        for (ProductoModel producto : listaProductos) {
            if (producto.getCantidadStock() < 5) {
                productosBajoStock.add(producto);
            }
        }

        if (!productosBajoStock.isEmpty()) {
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Los siguientes productos están próximos a quedarse sin stock:\n\n");
            for (ProductoModel producto : productosBajoStock) {
                mensaje.append("Código de Barras: ").append(producto.getCodigoBarras()).append("\n");
                mensaje.append("Nombre: ").append(producto.getNombre()).append("\n");
                mensaje.append("Descripción: ").append(producto.getDescripcion()).append("\n");
                mensaje.append("Cantidad Stock: ").append(producto.getCantidadStock()).append("\n\n");
            }

            enviarCorreo("ioanbota2002@outlook.es", "Notificación de Stock Bajo", mensaje.toString());
        } else {
            Toast.makeText(this, "No hay productos con stock bajo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarCorreo(String destinatario, String asunto, String mensaje) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{destinatario});
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarProductos() {
        listaProductos = bbddController.obtenerListaProductos(usuario.getIdTienda());
    }


    private void filtrarProductosPorCodigo(String codigo) {
        if (codigo.isEmpty()) {
            cargarProductos(); // Cargar todos los productos nuevamente
            adaptadorProducto.actualizarLista(listaProductos);
        } else {
            List<ProductoModel> productosFiltrados = new ArrayList<>();
            for (ProductoModel producto : listaProductos) {
                if (producto.getCodigoBarras().startsWith(codigo)) {
                    productosFiltrados.add(producto);
                }
            }
            adaptadorProducto.actualizarLista(productosFiltrados);
        }
    }

    private void generarExcel() throws IOException {
        File directory = new File(this.getExternalFilesDir(null), "MiCarpeta");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        // Crear encabezados
        String[] encabezados = {"Código Barras", "Nombre", "Descripción", "Cantidad Stock", "Precio Unidad", "Veces Comprado", "Veces Devuelto", "ID Tienda"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(encabezados[i]);
        }

        // Llenar datos de productos
        int rowNum = 1;
        for (ProductoModel producto : listaProductos) {
            Row row = sheet.createRow(rowNum++);
            if (producto.getCodigoBarras().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Código de barras excede el límite de caracteres.");
            } else {
                row.createCell(0).setCellValue(producto.getCodigoBarras());
            }

            if (producto.getNombre().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Nombre excede el límite de caracteres.");
            } else {
                row.createCell(1).setCellValue(producto.getNombre());
            }

            if (producto.getDescripcion().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Descripción excede el límite de caracteres.");
            } else {
                row.createCell(2).setCellValue(producto.getDescripcion());
            }
            row.createCell(3).setCellValue(producto.getCantidadStock());
            row.createCell(4).setCellValue(producto.getPrecioUnidad());
            row.createCell(5).setCellValue(producto.getVecesComprado());
            row.createCell(6).setCellValue(producto.getVecesDevuelto());
            row.createCell(7).setCellValue(producto.getIdTienda());
        }

        requestStoragePermission();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Definir el nombre del archivo con la fecha y hora actual
        String fileName = "productos_" + timeStamp + ".xlsx";

        File file = new File(directory, fileName);

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        // Informar al usuario que el archivo se ha generado
        Toast.makeText(this, "Archivo Excel generado en " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }


    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicita permiso al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
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

        // Manejar el resultado de la captura de foto o selección de galería
        adaptadorProducto.onActivityResult(requestCode, resultCode, data);

        // Manejar el resultado del escaneo de código de barras
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "Escaneo cancelado");
            } else {
                codigoBuscar.setText("");
                codigoEscaneado = result.getContents();
                Log.d(TAG, "Código de barras escaneado: " + codigoEscaneado);
                codigoBuscar.setText(codigoEscaneado);
                filtrarProductosPorCodigo(codigoEscaneado);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iniciarEscaner();
                } else {
                    Log.d(TAG, "Permiso de cámara denegado");
                }
                break;

            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de escritura concedido
                    Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }



}