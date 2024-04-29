package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;

import java.util.List;

public class CrearTienda extends AppCompatActivity {

    private EditText nombreTienda, cifTienda, direccionTienda, telefonoTienda;
    private Button crearTienda, cancelarCreacionTienda;

    private BBDDController bbddController = new BBDDController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_tienda);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombreTienda = findViewById(R.id.et_nombre_tienda);
        cifTienda = findViewById(R.id.et_cif_tienda);
        direccionTienda = findViewById(R.id.et_direccion_tienda);
        telefonoTienda = findViewById(R.id.et_numero_tienda);

        crearTienda = findViewById(R.id.crear_tienda);
        cancelarCreacionTienda = findViewById(R.id.cancelar_crear_tienda);

        crearTienda.setOnClickListener(v -> creacionTienda());
        cancelarCreacionTienda.setOnClickListener(v -> cancelarCreacionTienda());



        telefonoTienda.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(9), // Limita la cantidad de caracteres a 9
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si el texto resultante contiene solo dígitos y tiene una longitud adecuada
                        if (!builder.toString().matches("9\\d{0,8}")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });

        cifTienda.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(9), // Limita la cantidad de caracteres a 9
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source.subSequence(start, end).toString());

                        // Verificar si el texto resultante tiene el formato adecuado (1 letra seguida de 8 números)
                        if (!builder.toString().matches("[A-Z]\\d{0,8}")) {
                            return ""; // Si no cumple con el formato, eliminar la entrada
                        }

                        return null; // Aceptar este cambio de texto
                    }
                }
        });



    }




    private void creacionTienda() {

        if (comprobarDatosTienda()){
            if(bbddController.insertarTienda(cifTienda.getText().toString(), nombreTienda.getText().toString(),  direccionTienda.getText().toString(), telefonoTienda.getText().toString())){
                mostrarDialogoCrearOtraTienda();
            }else{
                mostrarAlertaErrorBBDD();
            }
        }

    }

    private void mostrarAlertaErrorBBDD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearTienda.this);
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

    private void mostrarDialogoCrearOtraTienda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tienda creada exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        builder.setPositiveButton("Crear otra tienda", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

        private void vaciarCampos(){
            nombreTienda.setText("");
            cifTienda.setText("");
            direccionTienda.setText("");
            telefonoTienda.setText("");
            nombreTienda.setError(null);
            cifTienda.setError(null);
            direccionTienda.setError(null);
            telefonoTienda.setError(null);
        }

    private void cancelarCreacionTienda() {
        confirmarCancelar();
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



    private boolean comprobarDatosTienda() {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombreTienda.setError(null);
        cifTienda.setError(null);
        telefonoTienda.setError(null);
        direccionTienda.setError(null);

        // Verificar el nombre
        if (nombreTienda.getText().toString().isEmpty()) {
            nombreTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la descripción
        if (cifTienda.getText().toString().isEmpty()) {
            cifTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la cantidad en stock
        if (direccionTienda.getText().toString().isEmpty()) {
            direccionTienda.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (telefonoTienda.getText().toString().isEmpty()) {
            telefonoTienda.setError("Campo vacío");
            datosValidos = false;
        }

        List<String> todosCIF= bbddController.obtenerListaCIF();
        if (todosCIF.contains(cifTienda.getText().toString())){
            cifTienda.setError("El CIF ya está en uso");
            datosValidos = false;
        }

        if (telefonoTienda.getText().toString().length()!= 9){
            telefonoTienda.setError("El teléfono debe tener 9 dígitos");
            datosValidos = false;
        }

        if (cifTienda.getText().toString().length()!= 9){
            cifTienda.setError("El CIF debe tener 9 dígitos");
            datosValidos = false;
        }

        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
    }
}