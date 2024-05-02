package com.example.tfg_sistematienda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;
import com.example.tfg_sistematienda.vistas.CrearProducto;
import com.example.tfg_sistematienda.vistas.CrearTienda;
import com.example.tfg_sistematienda.vistas.CrearUsuario;
import com.example.tfg_sistematienda.vistas.GeneralAdmin;
import com.example.tfg_sistematienda.vistas.GeneralReponedor;
import com.example.tfg_sistematienda.vistas.GeneralVendedor;
import com.example.tfg_sistematienda.vistas.ListaInventario;

import org.mindrot.jbcrypt.BCrypt;

public class MainActivity extends AppCompatActivity {

    private Button iniciar, recuperar;

    private Button irCrearProducto, listaProductos;
    private BBDDController bbddController= new BBDDController();
    private EditText usuario, contrasena;

    public static final int PERMISSION_BLUETOOTH = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    public static final int PERMISSION_BLUETOOTH_SCAN = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciar = findViewById(R.id.iniciar_sesion);
        recuperar = findViewById(R.id.bt_recuperar);
        irCrearProducto = findViewById(R.id.crear_producto_ir);
        listaProductos = findViewById(R.id.bt_lista);

        usuario = findViewById(R.id.usuario);
        contrasena = findViewById(R.id.contrasena);

        irCrearProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CrearProducto.class);
                startActivity(i);
            }
        });

        listaProductos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ListaInventario.class);
                startActivity(i);
            }
        });
        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String contraseñaIntroducida = contrasena.getText().toString();
                String contraseñaBBDDHasheada = bbddController.obtenerContraseñaPorCorreo(usuario.getText().toString());

                if (contraseñaBBDDHasheada == null){
                    usuario.setError("El usuario no existe");
                    contrasena.setError("El usuario no existe");
                    return;
                }

                if (!BCrypt.checkpw(contraseñaIntroducida, contraseñaBBDDHasheada)){
                    contrasena.setError("La contraseña no es correcta");
                    return;
                }else{
                    UsuarioModel usuarioActual = bbddController.buscarUsuarioPorCorreo(usuario.getText().toString());
                    if (usuarioActual.isActivo()){
                        if (usuarioActual.isVendedor()){
                            Intent i = new Intent(MainActivity.this, GeneralVendedor.class);
                            i.putExtra("usuarioDNI", usuarioActual.getDni());
                            startActivity(i);
                        }else if (usuarioActual.isReponedor()){
                            Intent i = new Intent(MainActivity.this, GeneralReponedor.class);
                            i.putExtra("usuarioDNI", usuarioActual.getDni());
                            startActivity(i);
                        }else if (usuarioActual.isAdmin()){
                            Intent i = new Intent(MainActivity.this, GeneralAdmin.class);
                            i.putExtra("usuarioDNI", usuarioActual.getDni());
                            startActivity(i);
                        }

                    }else{
                        usuario.setError("El usuario no está activo");
                    }

                }

            }
        });

        recuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Recuperar Credenciales");

                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_recuperarcredenciales, null);
                builder.setView(dialogView);

                EditText nombre = dialogView.findViewById(R.id.recupera_nombre);
                EditText apellidos = dialogView.findViewById(R.id.recupera_apellido);
                EditText dni = dialogView.findViewById(R.id.recupera_dni);
                EditText telefono = dialogView.findViewById(R.id.recupera_telefono);


                Button buscar = dialogView.findViewById(R.id.bt_buscar_credenciales);

                TextView usuario = dialogView.findViewById(R.id.mostrar_usuario);

                TextView textoC = dialogView.findViewById(R.id.tv_contraR);

                TextView textoCr = dialogView.findViewById(R.id.tv_contraRR);


                EditText contra = dialogView.findViewById(R.id.passwd_reset);

                EditText contraVeri = dialogView.findViewById(R.id.passwd_reset_veri);

                Button cambiarContra = dialogView.findViewById(R.id.bt_restablecer_contra);

                Button resetContra = dialogView.findViewById(R.id.reset_contra);


                usuario.setVisibility(View.INVISIBLE);
                textoC.setVisibility(View.INVISIBLE);
                textoCr.setVisibility(View.INVISIBLE);
                contra.setVisibility(View.INVISIBLE);
                contraVeri.setVisibility(View.INVISIBLE);
                cambiarContra.setVisibility(View.INVISIBLE);
                resetContra.setVisibility(View.INVISIBLE);



                    buscar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        UsuarioModel usuarioEncontrado = bbddController.buscarUsuario(nombre.getText().toString(), apellidos.getText().toString(), dni.getText().toString(), telefono.getText().toString());

                        if ( usuarioEncontrado != null){
                            if (usuarioEncontrado.isActivo()){
                                usuario.setVisibility(View.VISIBLE);
                                usuario.setText(usuarioEncontrado.getCorreo());
                                resetContra.setVisibility(View.VISIBLE);



                            }else{
                                mostrarAlertaUsuarioNoActivo();
                            }

                        }else{
                            mostrarAlertaUsuarioNoEncontrado();
                        }
                    }
                });

                resetContra.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cambiarContra.setVisibility(View.VISIBLE);
                        contra.setVisibility(View.VISIBLE);
                        contraVeri.setVisibility(View.VISIBLE);
                        textoC.setVisibility(View.VISIBLE);
                        textoCr.setVisibility(View.VISIBLE);
                    }
                });



                    cambiarContra.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (contra.getText().toString().equals(contraVeri.getText().toString())){
                                UsuarioModel usuarioEncontrado = bbddController.buscarUsuario(nombre.getText().toString(), apellidos.getText().toString(), dni.getText().toString(), telefono.getText().toString());
                                usuarioEncontrado.setContraseña(BCrypt.hashpw(contra.getText().toString(), BCrypt.gensalt()));
                                if (bbddController.actualizarUsuario(usuarioEncontrado)){
                                    cambioContraOK();
                                }else{
                                    cambioContraNoOK();
                                }

                            }else{
                                contra.setError("Las contraseñas no coinciden");
                                contraVeri.setError("Las contraseñas no coinciden");
                            }
                        }
                    });

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });



    }



    private void mostrarAlertaUsuarioNoEncontrado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Usuario no encontrado")
                .setMessage("No se encontró ningún usuario con los datos proporcionados. Revise los datos introducidos ó póngase en contacto con el jefe llamando al 641938476 o escriba un correo a ioanbota2002@outlook.es  Gracias")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void mostrarAlertaUsuarioNoActivo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Usuario no activo")
                .setMessage("Es posible que actualmente no tenga acceso para acceder ya que no está usted activo. Póngase en contacto con el jefe llamando al 641938476 o escriba un correo a ioanbota2002@outlook.es  Gracias")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void cambioContraOK() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Contraseña actualizada")
                .setMessage("Su contraseña a sido actualizada con éxito. No la vuelva a olvidar!")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void cambioContraNoOK() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Error actualizacion de contraseña")
                .setMessage("Hubo un error a la hora de actualizar su contraseña. Póngase en contacto con el jefe llamando al 641938476 o escriba un correo a ioanbota2002@outlook.es  Gracias")

                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }




}