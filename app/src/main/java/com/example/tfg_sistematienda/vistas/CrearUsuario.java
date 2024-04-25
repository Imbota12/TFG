package com.example.tfg_sistematienda.vistas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.TiendaModel;

import java.util.ArrayList;
import java.util.List;

public class CrearUsuario extends AppCompatActivity {


    private BBDDController bbddController = new BBDDController();
    private EditText nombre, apellidos, usuario, contrasena, dni, telefono, veriContra;
    private Spinner tienda;
    private Button crear;
    private Switch vendedor, reponedor;
    private TextView error;
    private String nifTienda;
    private boolean isVendedor, isReponedor;

    @SuppressLint("MissingInflatedId")
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

        vendedor = findViewById(R.id.switch1);
        reponedor = findViewById(R.id.switch2);

        error = findViewById(R.id.tx_error);

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




        crear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comprobarCampos() == true){

                    vaciarCampos();
                };
                return true;
            }
        });



        nombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras
                String nombre = s.toString();
                if (!nombre.matches("[a-zA-Z]+")) {
                    // Si el texto contiene caracteres que no son letras, mostrar un mensaje de error
                    error.setText("El nombre solo puede contener letras");
                    crear.setEnabled(false);
                }else{
                    error.setText(" ");
                    crear.setEnabled(true);
                }
            }
        });

        apellidos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras
                String apellido = s.toString();
                if (!apellido.matches("[a-zA-Z]+")) {
                    // Si el texto contiene caracteres que no son letras, mostrar un mensaje de error
                    error.setText("El apellido solo puede contener letras");
                    crear.setEnabled(false);
                }else{
                    error.setText(" ");
                    crear.setEnabled(true);
                }
            }
        });

        dni.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo letras o números
                String dnia = s.toString();
                if (!dnia.matches("[a-zA-Z0-9]+")) {
                    // Si el texto contiene caracteres que no son letras ni números, mostrar un mensaje de error
                    error.setText("El DNI solo puede contener letras o números");
                    crear.setEnabled(false);
                } else {
                    // Si el texto contiene solo letras o números, eliminar el mensaje de error si estaba presente
                    error.setText(" ");
                    crear.setEnabled(true);
                }
            }
        });

        telefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene solo números y cumple con los criterios adicionales
                String telefono = s.toString();
                if (!telefono.matches("[6-9][0-9]{0,8}")) {
                    // Si el texto no cumple con los criterios, mostrar un mensaje de error
                    error.setText("El teléfono debe tener entre 6 y 9 dígitos y empezar por 6, 7 o 9");
                    crear.setEnabled(false);
                } else {
                    // Si el texto cumple con los criterios, eliminar el mensaje de error si estaba presente
                    error.setText(" ");
                    crear.setEnabled(true);
                }
            }
        });

        contrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes del cambio de texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción durante el cambio de texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si la contraseña cumple con los criterios
                String contrasena = s.toString();
                if (contrasena.isEmpty() || contrasena.length() < 4 || contrasena.length() > 16) {
                    // Si la contraseña está vacía o no cumple con los criterios, mostrar un mensaje de error
                    error.setText("La contraseña debe tener entre 4 y 16 caracteres");
                    crear.setEnabled(false);
                } else {
                    // Si la contraseña cumple con los criterios, eliminar el mensaje de error si estaba presente
                    error.setText(null);
                    crear.setEnabled(true);
                }
            }
        });


        rellenarSpinnerTiendas();

    }










    public void vaciarCampos(){
        nombre.setText(" ");
        apellidos.setText(" ");
        usuario.setText(" ");
        contrasena.setText(" ");
        dni.setText(" ");
        telefono.setText(" ");
        error.setText(null);
        veriContra.setText(" ");
        vendedor. setEnabled(false);
        reponedor.setEnabled(false);

    }

    public boolean comprobarCampos(){
        // TODO: 2021-05-27 Añadir validaciones de campos vacíos y validaciones de telefono, nombre, apellidos, contraseña, dni .
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            return false;
        }
        if (apellidos.getText().toString().isEmpty()) {
            apellidos.setError("Campo vacío");
            return false;
        }
        if (usuario.getText().toString().isEmpty()) {
            usuario.setError("Campo vacío");
            return false;
        }
        if (contrasena.getText().toString().isEmpty()) {
            contrasena.setError("Campo vacío");
            return false;
        }
        if (dni.getText().toString().isEmpty()) {
            dni.setError("Campo vacío");
            return false;
        }
        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            return false;
        }

        if (!vendedor.isChecked() && !reponedor.isChecked()){
            error.setText("Debe seleccionar un rol");
            return false;
        }

        if (!contrasena.equals(veriContra)){
            contrasena.setError("Las contraseñas deben coincidir");
            veriContra.setError("Las contraseñas deben coincidir");
            return false;
        }
        return true;
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
}