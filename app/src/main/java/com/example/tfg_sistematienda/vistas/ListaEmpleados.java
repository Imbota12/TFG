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
import com.example.tfg_sistematienda.modelos.ProductoModel;
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

public class ListaEmpleados extends AppCompatActivity {
    private RecyclerView lista;
    private AdaptadorEmpleado adaptadorEmpleado;
    private List<UsuarioModel> listaEmpleados;
    private static final int MAX_CELL_LENGTH = 32767;
    private static final int REQUEST_EXTERNAL_STORAGE = 118;

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

        // Llenar datos de productos
        int rowNum = 1;
        for (UsuarioModel empleado : listaEmpleados) {
            Row row = sheet.createRow(rowNum++);
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

            if (empleado.isActivo() == true) {
                row.createCell(5).setCellValue("SI");
            }else{
                row.createCell(5).setCellValue("NO");
            }

            row.createCell(6).setCellValue(empleado.getIdTienda());

            if (empleado.isReponedor()){
                row.createCell(7).setCellValue("REPONEDOR");
            }else if (empleado.isVendedor()){
                row.createCell(7).setCellValue("VENDEDOR");
            }

        }

        requestStoragePermission();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Definir el nombre del archivo con la fecha y hora actual
        String fileName = "empleados_" + timeStamp + ".xlsx";

        File file = new File(directory, fileName);

        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        bbddController.insertarLog("Generar excel empleados", LocalDateTime.now(), usuario.getDni());
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

    private void cargarEmpleados() {
        listaEmpleados = bbddController.obtenerListaEmpleados();
    }
}