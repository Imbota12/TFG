package com.example.tfg_sistematienda.vistas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorEmpleado;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Clase para mostrar la lista de empleados y generar un archivo Excel con la información.
 */
public class ListaEmpleados extends AppCompatActivity {
    private static final int MAX_CELL_LENGTH = 32767;
    private static final int REQUEST_EXTERNAL_STORAGE = 118;
    private RecyclerView lista;
    private AdaptadorEmpleado adaptadorEmpleado;
    private List<UsuarioModel> listaEmpleados;
    private boolean allowBackPress = false;
    private BBDDController bbddController = new BBDDController();
    private UsuarioModel usuario;
    private ImageButton generarExcel, volverMenu;

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
        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);

        lista = findViewById(R.id.rv_listaEmpleados);
        lista.setLayoutManager(new LinearLayoutManager(this));

        cargarEmpleados();

        adaptadorEmpleado = new AdaptadorEmpleado(this, listaEmpleados);
        lista.setAdapter(adaptadorEmpleado);

        generarExcel = findViewById(R.id.excelListaEmpleados);
        volverMenu = findViewById(R.id.volvermenuAdminn);

        volverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Insertar log de acceso al menú admin
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuario.getDni());
                Intent intent = new Intent(ListaEmpleados.this, GeneralAdmin.class);
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
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para generar un archivo Excel con la lista de empleados.
     *
     * @throws IOException Si hay un error de entrada o salida al manipular el archivo.
     */
    private void generarExcel() throws IOException {
        File directory = new File(this.getExternalFilesDir(null), "MiCarpeta");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Empleados");

        // Crear encabezados
        String[] encabezados = {"DNI", "Nombre", "Apellidos", "Telefono", "Correo", "Activo", "ID Tienda perteneciente", "Tipo trabajador"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(encabezados[i]);
        }

        // Llenar datos de empleados
        int rowNum = 1;
        for (UsuarioModel empleado : listaEmpleados) {
            Row row = sheet.createRow(rowNum++);
            // Insertar datos en las celdas
            if (empleado.getDni().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "DNI excede el límite de caracteres.");
            } else {
                row.createCell(0).setCellValue(empleado.getDni());
            }

            if (empleado.getNombre().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Nombre excede el límite de caracteres.");
            } else {
                row.createCell(1).setCellValue(empleado.getNombre());
            }

            if (empleado.getApellido().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Apellidos excede el límite de caracteres.");
            } else {
                row.createCell(2).setCellValue(empleado.getApellido());
            }

            if (empleado.getTelefono().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Telefono excede el límite de caracteres.");
            } else {
                row.createCell(3).setCellValue(empleado.getTelefono());
            }

            if (empleado.getCorreo().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Correo excede el límite de caracteres.");
            } else {
                row.createCell(4).setCellValue(empleado.getCorreo());
            }

            // Insertar "SI" o "NO" en función del estado de activo
            if (empleado.isActivo()) {
                row.createCell(5).setCellValue("SI");
            } else {
                row.createCell(5).setCellValue("NO");
            }

            row.createCell(6).setCellValue(empleado.getIdTienda());

            // Insertar el tipo de trabajador en función de su rol
            if (empleado.isReponedor()) {
                row.createCell(7).setCellValue("REPONEDOR");
            } else if (empleado.isVendedor()) {
                row.createCell(7).setCellValue("VENDEDOR");
            }
        }

        // Solicitar permiso de almacenamiento
        requestStoragePermission();

        // Obtener la fecha y hora actual para incluirla en el nombre del archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Definir el nombre del archivo con la fecha y hora actual
        String fileName = "empleados_" + timeStamp + ".xlsx";

        File file = new File(directory, fileName);

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        // Insertar log de generación de archivo Excel
        bbddController.insertarLog("Generar excel empleados", LocalDateTime.now(), usuario.getDni());
        // Informar al usuario que el archivo se ha generado
        Toast.makeText(this, "Archivo Excel generado en " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    /**
     * Método para solicitar permiso de almacenamiento si no está concedido.
     */
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está concedido, solicita permiso al usuario
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * Método para cargar la lista de empleados desde la base de datos.
     */
    private void cargarEmpleados() {
        listaEmpleados = bbddController.obtenerListaEmpleados();
    }
}
