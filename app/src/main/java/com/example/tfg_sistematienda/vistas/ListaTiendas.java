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

import com.example.tfg_sistematienda.Adaptadores.AdaptadorTienda;
import com.example.tfg_sistematienda.BBDD.ConexionBBDD;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;
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
import java.util.Date;
import java.util.List;

public class ListaTiendas extends AppCompatActivity {

    private UsuarioModel usuario;
    private ConexionBBDD bbddController=new ConexionBBDD();
    private static final int MAX_CELL_LENGTH = 32767;
    private RecyclerView lista;
    private AdaptadorTienda adaptadorTienda;
    private static final int REQUEST_EXTERNAL_STORAGE = 98;
    private List<TiendaModel> listaTiendas;
    private ImageButton generarExcel, volverMenu;
    private boolean allowBackPress=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_tiendas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);
        generarExcel = findViewById(R.id.excelListaTiendas);
        volverMenu = findViewById(R.id.volvermenuAdmin);

        lista = findViewById(R.id.listaTienda);
        lista.setLayoutManager(new LinearLayoutManager(this));

        cargarTiendas();

        adaptadorTienda = new AdaptadorTienda(listaTiendas, this);
        lista.setAdapter(adaptadorTienda);

        volverMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaTiendas.this, GeneralAdmin.class);
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
        Sheet sheet = workbook.createSheet("Tiendas");

        // Crear encabezados
        String[] encabezados = {"CIF", "Nombre", "Direccion", "Telefono"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(encabezados[i]);
        }

        // Llenar datos de productos
        int rowNum = 1;
        for (TiendaModel tienda : listaTiendas) {
            Row row = sheet.createRow(rowNum++);
            if (tienda.getCif().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "CIF excede el límite de caracteres.");
            } else {
                row.createCell(0).setCellValue(tienda.getCif());
            }

            if (tienda.getNombre().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Nombre excede el límite de caracteres.");
            } else {
                row.createCell(1).setCellValue(tienda.getNombre());
            }

            if (tienda.getDireccion().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Direccion excede el límite de caracteres.");
            } else {
                row.createCell(2).setCellValue(tienda.getDireccion());
            }

            if (tienda.getTelefono().length() > MAX_CELL_LENGTH) {
                Log.e("ExcelError", "Telefono excede el límite de caracteres.");
            } else {
                row.createCell(3).setCellValue(tienda.getTelefono());
            }
        }

        requestStoragePermission();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // Definir el nombre del archivo con la fecha y hora actual
        String fileName = "tiendas_" + timeStamp + ".xlsx";

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

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarTiendas() {
        listaTiendas = bbddController.obtenerListaTiendas();
    }
}