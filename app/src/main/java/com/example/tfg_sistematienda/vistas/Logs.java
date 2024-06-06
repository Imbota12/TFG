package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.Adaptadores.AdaptadorLog;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.LogModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Logs extends AppCompatActivity {

    private UsuarioModel usuario;
    private BBDDController bbddController= new BBDDController();
    private ImageButton volverMenu;
    private RecyclerView todosLogs;
    private AdaptadorLog adaptadorLogs;
    private List<LogModel> listaLogs;
    private boolean allowBackPress=false;
    private Spinner dnis, tiposFiltros;
    private ImageButton seleccionarFecha, filtrarDni, mostrarTodosLogs, filtrarFecha, filtrarDniyFecha, vaciarLogs;
    private TextView fecha;
    private String dniSeleccionado;
    private List<String> dni= new ArrayList<>();
    private int AnoSeleccionado, DiaSeleccionado, MesSeleccionado;
    private LocalDate fechaSeleccionada=null;
    private ArrayList<String> tipoFiltro = new ArrayList<>();
    private String filtroSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);
        volverMenu = findViewById(R.id.volvermenuLogs);

        todosLogs = findViewById(R.id.rv_logs);
        todosLogs.setLayoutManager(new LinearLayoutManager(this));

        dnis = findViewById(R.id.sp_dnis);
        seleccionarFecha = findViewById(R.id.selectFecha);
        fecha = findViewById(R.id.fecha_seleccionada);
        filtrarDni = findViewById(R.id.filtro_log_dni);
        mostrarTodosLogs = findViewById(R.id.mostrar_todos);
        filtrarFecha = findViewById(R.id.filtro_log_fecha);
        tiposFiltros = findViewById(R.id.sp_tipofiltro);
        filtrarDniyFecha = findViewById(R.id.filtro_log_fechaydni);
        vaciarLogs = findViewById(R.id.vaciarLogs);

        cargarSpinnerDnis();

        cargarLogs();

        tipoFiltro.add("igual");
        tipoFiltro.add("menor que");
        tipoFiltro.add("mayor que");

        cargarSpinnerTipoFiltro();
        fecha.setVisibility(View.INVISIBLE);


        vaciarLogs.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("¿ESTAS SEGURO?");
            builder.setMessage("¿ESTAS SEGURO QUE QUIERE BORRAR LOS LOGS (¡¡¡ESTA OPERACIÓN NO SE PODRÁ REVERTIR¡¡¡)?");
            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (bbddController.vaciarLogs()) {
                        bbddController.insertarLog("Vacia logs", LocalDateTime.now(), usuario.getDni());
                        Toast.makeText(Logs.this, "Logs borrados correctamente", Toast.LENGTH_SHORT).show();
                        cargarLogs();
                    } else {
                        Toast.makeText(Logs.this, "Error al vaciar logs", Toast.LENGTH_SHORT).show();
                    }
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

        });


        tiposFiltros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (position == 0) {
                   filtroSeleccionado = tipoFiltro.get(0);
               }else if (position == 1) {
                   filtroSeleccionado = tipoFiltro.get(1);
               }else if (position == 2) {
                   filtroSeleccionado = tipoFiltro.get(2);
               }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Manejar el caso en el que no se haya seleccionado ninguna tienda
            }
        });

        filtrarFecha.setOnClickListener(v -> {
            cargarLogsFiltradosFecha();
        });

        filtrarDniyFecha.setOnClickListener(v -> {
            cargarLogsFiltradosDniyFecha();
        });


        volverMenu.setOnClickListener(v -> {
            bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuario.getDni());
            Intent intent1 = new Intent(Logs.this, GeneralAdmin.class);
            intent1.putExtra("usuarioDNI", usuarioDNI);
            startActivity(intent1);
        });

        filtrarDni.setOnClickListener(v -> {
            cargarLogsFiltradosDni();
        });

        mostrarTodosLogs.setOnClickListener(v -> {
            cargarLogs();
        });
        seleccionarFecha.setOnClickListener(v -> {
            mostarDialogoFecha();
        });
    }

    private void cargarLogsFiltradosDniyFecha() {
        if (dniSeleccionado.equals("")) {
            Toast.makeText(this, "Debes seleccionar un dni y fecha arriba", Toast.LENGTH_SHORT).show();
        }else {
            if (filtroSeleccionado.equals("igual")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaIgualconDNI(fechaSeleccionada, dniSeleccionado);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            } else if (filtroSeleccionado.equals("mayor que")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaMayorconDNI(fechaSeleccionada, dniSeleccionado);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            } else if (filtroSeleccionado.equals("menor que")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaMenorconDNI(fechaSeleccionada, dniSeleccionado);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            }
        }
    }

    private void cargarSpinnerTipoFiltro() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipoFiltro);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tiposFiltros.setAdapter(adapter);
        tiposFiltros.setSelection(0);
    }

    private void cargarLogsFiltradosFecha() {
        if (fechaSeleccionada!=null) {
            if (filtroSeleccionado.equals("igual")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaIgual(fechaSeleccionada);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            } else if (filtroSeleccionado.equals("mayor que")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaMayor(fechaSeleccionada);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            } else if (filtroSeleccionado.equals("menor que")) {
                listaLogs = bbddController.obtenerListaLogsPorFechaMenor(fechaSeleccionada);
                adaptadorLogs = new AdaptadorLog(this, listaLogs);
                todosLogs.setAdapter(adaptadorLogs);
            }
        }else{
            Toast.makeText(this, "Debes seleccionar una fecha", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostarDialogoFecha() {
        // Obtén la fecha actual para inicializar el DatePickerDialog con ella
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Al seleccionar una fecha, se guarda en las variables
                        AnoSeleccionado = year;
                        MesSeleccionado = month;
                        DiaSeleccionado = dayOfMonth;
                        // Aquí puedes guardar la fecha en una variable o usarla según sea necesario
                        fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth); // month+1 porque LocalDate usa 1-12 para los meses

                        fecha.setText(fechaSeleccionada.toString());
                        fecha.setVisibility(View.VISIBLE);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void cargarLogsFiltradosDni() {
        if (dniSeleccionado.equals("")) {
            Toast.makeText(this, "Debes seleccionar un dni", Toast.LENGTH_SHORT).show();
        }else {
            listaLogs = bbddController.obtenerListaLogsPorDni(dniSeleccionado);
            adaptadorLogs = new AdaptadorLog(this, listaLogs);
            todosLogs.setAdapter(adaptadorLogs);
        }
    }

    private void cargarSpinnerDnis() {

        dni = bbddController.obtenerDnis();
        dni.add(0, "---SELECCIONA UN DNI A FILTRAR---");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dni);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dnis.setAdapter(adapter);
        dnis.setSelection(0);

        dnis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position>0 && position<dni.size()) {
                    dniSeleccionado = dni.get(position);
                }else{
                    dniSeleccionado="";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Manejar el caso en el que no se haya seleccionado ninguna tienda
            }
        });



    }

    private void cargarLogs() {
        listaLogs = bbddController.obtenerListaLogs();
        adaptadorLogs = new AdaptadorLog(this, listaLogs);
        todosLogs.setAdapter(adaptadorLogs);
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }

}