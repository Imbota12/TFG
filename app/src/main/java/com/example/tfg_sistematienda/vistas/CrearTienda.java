package com.example.tfg_sistematienda.vistas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;
import java.util.List;

public class CrearTienda extends AppCompatActivity {

    private EditText nombreTienda, cifTienda, direccionTienda, telefonoTienda;
    private ImageButton crearTienda, cancelarCreacionTienda;

    private BBDDController bbddController = new BBDDController();
    private boolean allowBackPress=false;
    private UsuarioModel usuario;

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

        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuario = bbddController.obtenerEmpleado(usuarioDNI);

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

        InputFilter cifFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verifica que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verifica el formato: 8 dígitos seguidos de una letra mayúscula
                if (newText.length() <= 8) {
                    // Permitir solo dígitos en las primeras 8 posiciones
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                } else if (newText.length() == 9) {
                    // Permitir solo una letra mayúscula en la última posición
                    char lastChar = source.charAt(source.length() - 1);
                    if (!Character.isUpperCase(lastChar)) {
                        return "";
                    }
                }

                return null; // Aceptar el carácter
            }
        };

        cifTienda.setFilters(new InputFilter[] {cifFilter});



    }


    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón VOLVER MENÚ", Toast.LENGTH_SHORT).show();
        }
    }


    private void creacionTienda() {

        if (comprobarDatosTienda()){
            if(bbddController.insertarTienda(cifTienda.getText().toString(), nombreTienda.getText().toString(),  direccionTienda.getText().toString(), telefonoTienda.getText().toString())){
                bbddController.insertarLog("Creación tienda", LocalDateTime.now(), usuario.getDni());
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
                bbddController.insertarLog("Quiere crear otra tienda", LocalDateTime.now(), usuario.getDni());

            }
        });
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuario.getDni());
                Intent intent = new Intent(CrearTienda.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
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
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuario.getDni());
                Intent intent = new Intent(CrearTienda.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuario.getDni());
                startActivity(intent);
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