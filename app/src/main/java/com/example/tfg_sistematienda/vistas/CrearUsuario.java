package com.example.tfg_sistematienda.vistas;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.MainActivity;
import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;



public class CrearUsuario extends AppCompatActivity {


    private BBDDController bbddController = new BBDDController();
    private EditText nombre, apellidos, usuario, contrasena, dni, telefono, veriContra;
    private Spinner tienda;
    private ImageButton crear, cancelar;
    private Switch vendedor, reponedor;
    private String nifTienda;
    private boolean isVendedor, isReponedor;
    private boolean allowBackPress=false;

    private UsuarioModel usuarioo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        nombre = findViewById(R.id.et_nombre_usuario);
        apellidos = findViewById(R.id.et_apellidos_usuario);
        usuario = findViewById(R.id.et_correo);
        contrasena = findViewById(R.id.ep_contra);
        dni = findViewById(R.id.et_dni);
        telefono = findViewById(R.id.ep_telefono_usuario);
        veriContra = findViewById(R.id.ep_contra_veri);

        tienda = findViewById(R.id.SelectTienda);

        crear = findViewById(R.id.bt_crear_usuario);

        cancelar = findViewById(R.id.anular);

        vendedor = findViewById(R.id.switch1);
        reponedor = findViewById(R.id.switch2);


        Intent intent = getIntent();
        // Capturar el putExtra enviado desde MainActivity
        String usuarioDNI = intent.getStringExtra("usuarioDNI");

        usuarioo = bbddController.obtenerEmpleado(usuarioDNI);

        vendedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switchVendedor, desactivar el switchReponedor
                if (isChecked) {
                    reponedor.setChecked(false);
                    isVendedor=true;
                    isReponedor=false;
                }
            }
        });

        reponedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Si se activa el switchReponedor, desactivar el switchVendedor
                if (isChecked) {
                    vendedor.setChecked(false);
                    isVendedor=false;
                    isReponedor=true;
                }
            }
        });



        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               confirmarCancelar();
            }
        });

        crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comprobarCampos() == true){
                    // Obtener los valores de los EditText y quitar los espacios en blanco
                    String nombreUsuario = nombre.getText().toString().trim();
                    String apellidosUsuario = apellidos.getText().toString().trim();
                    String dniUsuario = dni.getText().toString().trim();
                    String telefonoUsuario = telefono.getText().toString().trim();
                    String correoUsuario = usuario.getText().toString().trim();
                    String contraseñaUsuario = contrasena.getText().toString().trim();

                    if (bbddController.insertarUsuario(dniUsuario, nombreUsuario, apellidosUsuario, telefonoUsuario,
                            correoUsuario, hashPassword(contraseñaUsuario), nifTienda, false, isVendedor, isReponedor)) {
                        bbddController.insertarLog("Crea nuevo empleado", LocalDateTime.now(), usuarioo.getDni());
                        mostrarDialogoCrearOtroUsuario();
                        vaciarCampos();
                    } else {
                        mostrarAlertaErrorBBDD();
                    }

                }
            }
        });



        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isWhitespace(source.charAt(i))) {
                        return ""; // Rechazar el carácter
                    }
                }
                return null; // Aceptar el carácter
            }
        };

// Establece el InputFilter en el EditText
        nombre.setFilters(new InputFilter[] { filter });
        apellidos.setFilters(new InputFilter[] { filter });


        InputFilter dniFilter = new InputFilter() {
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

// Establece el InputFilter en el EditText de DNI
        dni.setFilters(new InputFilter[] { dniFilter });

        // Define el InputFilter para el teléfono
        InputFilter phoneFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String currentText = dest.toString();
                String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

                // Verifica que la longitud no exceda 9 caracteres
                if (newText.length() > 9) {
                    return "";
                }

                // Verifica el formato: empieza con 6, 7, 8 o 9 y consiste solo de dígitos
                if (newText.length() == 1 && !newText.matches("[6-9]")) {
                    return "";
                }
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }

                return null; // Aceptar el carácter
            }
        };

// Establece el InputFilter en el EditText de teléfono
        telefono.setFilters(new InputFilter[] { phoneFilter });

        // Define el InputFilter para caracteres válidos en un correo electrónico
        InputFilter emailFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetterOrDigit(c) && c != '@' && c != '.' && c != '-' && c != '_') {
                        return "";
                    }
                }
                return null;
            }
        };

// Establece el InputFilter en el EditText de usuario
        usuario.setFilters(new InputFilter[] { emailFilter });


        rellenarSpinnerTiendas();

    }





    private void mostrarDialogoCrearOtroUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usuario creado exitosamente");
        builder.setMessage("¿Qué desea hacer a continuación?");
        builder.setPositiveButton("Crear otro usuario", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Aquí puedes agregar el código para crear otro usuario
                // Por ejemplo, puedes limpiar los campos del formulario
                // y permitir al usuario ingresar los datos de otro usuario.
                vaciarCampos();
                bbddController.insertarLog("Acceso formulario creacion empleado", LocalDateTime.now(), usuarioo.getDni());
            }
        });
        builder.setNegativeButton("Volver al menú", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuarioo.getDni());
                Intent intent = new Intent( CrearUsuario.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuarioo.getDni());
                startActivity(intent);
            }
        });
        builder.setCancelable(false); // Evitar que el diálogo se cierre al tocar fuera de él
        builder.show();

    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Para volver pulse el botón ANULAR CONTRATO", Toast.LENGTH_SHORT).show();
        }
    }



    public void vaciarCampos(){
        nombre.setText("");
        apellidos.setText("");
        usuario.setText("");
        contrasena.setText("");
        dni.setText("");
        telefono.setText("");
        veriContra.setText("");
        vendedor.setChecked(false);
        reponedor.setChecked(false);
        vendedor.setError(null);
        reponedor.setError(null);
        usuario.setError(null);
        contrasena.setError(null);
        veriContra.setError(null);
        dni.setError(null);
        nombre.setError(null);
        apellidos.setError(null);
        telefono.setError(null);


    }

    public boolean comprobarCampos(){

        boolean todoOk=true;

        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            todoOk = false;
        }
        if (apellidos.getText().toString().isEmpty()) {
            apellidos.setError("Campo vacío");
            todoOk = false;
        }
        if (usuario.getText().toString().isEmpty()) {
            usuario.setError("Campo vacío");
            todoOk = false;
        }
        if (contrasena.getText().toString().isEmpty()) {
            contrasena.setError("Campo vacío");
            todoOk = false;
        }
        if (dni.getText().toString().isEmpty()) {
            dni.setError("Campo vacío");
            todoOk = false;
        }
        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            todoOk = false;
        }

        if (telefono.getText().length() < 9){
            telefono.setError("El teléfono debe tener al menos 9 dígitos");
            todoOk = false;
        }

        if (contrasena.getText().toString().isEmpty()){
            contrasena.setError("Campo vacío");
            todoOk = false;
        }

        if (contrasena.getText().length()<4){
            contrasena.setError("La contraseña tiene que tener minimo 4 digitos");
            todoOk=false;
        }

        if (!vendedor.isChecked() && !reponedor.isChecked()){
            vendedor.setError("Debe seleccionar un rol");
            reponedor.setError("Debe seleccionar un rol");
            todoOk = false;
        }

        if (!contrasena.getText().toString().equals(veriContra.getText().toString())){
            contrasena.setError("Las contraseñas deben coincidir");
            veriContra.setError("Las contraseñas deben coincidir");
            todoOk = false;
        }

        List<String> todosCorreos= bbddController.obtenerListaCorreos();
        if (todosCorreos.contains(usuario.getText().toString())){
            usuario.setError("El correo ya existe");
            todoOk = false;
        }

        if (!todoOk) {
            Toast.makeText(this, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return todoOk;
    }




    private void confirmarCancelar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿ESTAS SEGURO?");
        builder.setMessage("¿ESTAS SEGURO QUE QUIERE CANCELAR LA OPERACIÓN Y VOLVER AL MENÚ PRINCIPAL?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bbddController.insertarLog("Acceso menu admin", LocalDateTime.now(), usuarioo.getDni());
                Intent intent = new Intent(CrearUsuario.this, GeneralAdmin.class);
                intent.putExtra("usuarioDNI", usuarioo.getDni());
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


    public void rellenarSpinnerTiendas(){
        List<TiendaModel> tiendas = bbddController.obtenerListaTiendas();

        // Obtener nombres de las tiendas para mostrar en el Spinner
        ArrayList<String> nombresTiendas = new ArrayList<>();
        for (TiendaModel tienda : tiendas) {
            nombresTiendas.add(tienda.getNombre());
        }

        // Crear un adaptador para el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresTiendas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Configurar el adaptador en el Spinner
        tienda.setAdapter(adapter);

        // Manejar la selección del usuario en el Spinner
        tienda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener la tienda seleccionada por el usuario
                TiendaModel tiendaSeleccionada = tiendas.get(position);
                String nombreTienda = tiendaSeleccionada.getNombre();
                nifTienda = tiendaSeleccionada.getCif();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Manejar el caso en el que no se haya seleccionado ninguna tienda
            }
        });

    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private void mostrarAlertaErrorBBDD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearUsuario.this);
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

}