package com.example.tfg_sistematienda.Adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AdaptadorEmpleado extends RecyclerView.Adapter<ViewHolderEmpleados> {

    private List<UsuarioModel> listaEmpleados;
    private Context context;
    private UsuarioModel usuarioSeleccionado;
    private BBDDController bbddController = new BBDDController();

    private TextView tx_confir;
    private EditText dni, nombre, apellidos, telefono, correo, contrasena, confirmarContrasena;

    public AdaptadorEmpleado(Context context, List<UsuarioModel> listaEmpleados) {
        this.listaEmpleados = listaEmpleados;
        this.context = context;

    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }


    @NonNull
    @Override
    public ViewHolderEmpleados onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empleado, parent, false);
        return new ViewHolderEmpleados(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderEmpleados holder, @SuppressLint("RecyclerView") int position) {

        UsuarioModel empleado = listaEmpleados.get(position);
        holder.nombreEmpleado.setText(empleado.getNombre());
        holder.apellidosEmpleado.setText(empleado.getApellido());
        holder.dniEmpleado.setText(empleado.getDni());
        holder.telefonoEmpleado.setText(empleado.getTelefono());
        holder.correoEmpleado.setText(empleado.getCorreo());
        holder.empleadoActivo.setEnabled(false);
        holder.tipoEmpleado.setEnabled(false);

        if (empleado.isActivo()) {
            holder.empleadoActivo.setChecked(true);
            holder.expulsar.setVisibility(View.VISIBLE);
            holder.contrato.setVisibility(View.GONE);
        } else {
            holder.empleadoActivo.setChecked(false);
            holder.contrato.setVisibility(View.VISIBLE);
            holder.expulsar.setVisibility(View.GONE);
        }

        if (empleado.isVendedor()) {
            holder.tipoEmpleado.setChecked(false);
        } else if (empleado.isReponedor()) {
            holder.tipoEmpleado.setChecked(true);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuarioSeleccionado = empleado;
                abrirDialogoEditarEmpleado(usuarioSeleccionado);
            }
        });

        holder.contrato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirmar contrato")
                        .setMessage("¿Estás seguro de que quieres contratar a este empleado?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            // Actualizar el estado del empleado
                            empleado.setActivo(true);
                            // Actualizar en la base de datos
                            bbddController.actualizarEstadoEmpleado(empleado.getDni(), true);
                            // Notificar cambios al adaptador
                            notifyItemChanged(position);

                            // Enviar correo de despido
                            String asunto = "Contrato";
                            String mensaje = "Yo como responsable de la empresa IMBot S.L., "
                                    + "he decidido ofrecerle un contrato de trabajo en la tienda en la que había trabajado usted con anterioridad. "
                                    + "En caso de querer volver a trabajar con nosotros, confirme a este correo, o llamame directamente al 641938476 "
                                    + "Gracias por su consideración. Un saludo.";
                            enviarCorreo(empleado.getCorreo(), asunto, mensaje);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        });

        holder.llamada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = empleado.getTelefono();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                try {
                    v.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(), "No se puede realizar la llamada. No hay aplicaciones de llamadas disponibles.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Error al intentar realizar la llamada.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.correo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoEnviarCorreo(empleado);
            }
        });

        holder.expulsar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirmar despido")
                        .setMessage("¿Estás seguro de que quieres despedir a este empleado?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            // Actualizar el estado del empleado
                            empleado.setActivo(false);
                            // Actualizar en la base de datos
                            bbddController.actualizarEstadoEmpleado(empleado.getDni(), false);
                            // Notificar cambios al adaptador
                            notifyItemChanged(position);

                            // Enviar correo de despido
                            String asunto = "Despido";
                            String mensaje = "Yo como responsable de la empresa en la que esta actualmente, "
                                    + "por motivos definidos (baja producción, retrasos etc...) o por falta de trabajo, "
                                    + "hemos decidido despedirle por un tiempo. En caso de volver a necesitar de su servicio, "
                                    + "nos pondremos en contacto con usted. Gracias por su servicio. Un saludo.";
                            enviarCorreo(empleado.getCorreo(), asunto, mensaje);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        });

    }

    private void abrirDialogoEditarEmpleado(UsuarioModel empleadoAeditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Empleado");

        // Inflar el diseño del diálogo de edición de producto
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_editarempleado, null);
        builder.setView(dialogView);

        // Obtener referencias a las vistas del diálogo
        dni = dialogView.findViewById(R.id.edi_dni);
        nombre = dialogView.findViewById(R.id.edi_nombre);
        apellidos = dialogView.findViewById(R.id.edi_apellido);
        correo = dialogView.findViewById(R.id.edi_correo);
        telefono = dialogView.findViewById(R.id.edi_telefono);
        contrasena = dialogView.findViewById(R.id.edi_contrasena);
        tx_confir = dialogView.findViewById(R.id.tx_confi_contra);
        confirmarContrasena = dialogView.findViewById(R.id.edi_confirma_contra);


        dni.setEnabled(false);
        contrasena.setText("****");
        tx_confir.setVisibility(View.GONE);
        confirmarContrasena.setVisibility(View.GONE);

        dni.setText(empleadoAeditar.getDni());
        nombre.setText(empleadoAeditar.getNombre());
        apellidos.setText(empleadoAeditar.getApellido());
        correo.setText(empleadoAeditar.getCorreo());
        telefono.setText(empleadoAeditar.getTelefono());

        telefono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitas hacer nada aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No necesitas hacer nada aquí
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();

                // Limitar la longitud a 9 caracteres
                if (input.length() > 9) {
                    s.delete(9, input.length());
                    return;
                }

                // Validar el primer dígito
                if (!input.isEmpty()) {
                    char firstChar = input.charAt(0);
                    if (firstChar != '6' && firstChar != '7' && firstChar != '9') {
                        s.delete(0, 1);
                    }
                }

                // Eliminar caracteres no numéricos
                for (int i = s.length() - 1; i >= 0; i--) {
                    if (!Character.isDigit(s.charAt(i))) {
                        s.delete(i, i + 1);
                    }
                }
            }
        });


        contrasena.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tx_confir.setVisibility(View.VISIBLE);
                confirmarContrasena.setVisibility(View.VISIBLE);
            }
        });
        builder.setPositiveButton("Guardar Cambios", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = nombre.getText().toString();
                String nuevoApellido = apellidos.getText().toString();
                String nuevoCorreo = correo.getText().toString();
                String nuevoTelefono = telefono.getText().toString();
                String nuevaContrasena = "";
                if (contrasena.getText().toString().equals("****")) {
                    nuevaContrasena = contrasena.getText().toString();
                    if (comprobarDatosEmpleado(nuevaContrasena)) {
                        if (bbddController.modificarEmpleadoSinContra(dni.getText().toString(), nuevoNombre, nuevoApellido, nuevoCorreo, nuevoTelefono)) {
                            Toast.makeText(context, "Empleado modificado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error al modificar el empleado", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    nuevaContrasena = contrasena.getText().toString();
                    if (comprobarDatosEmpleado(nuevaContrasena)) {
                        String contraHash = hashPassword(nuevaContrasena);
                        // Actualizar los datos del producto en la BBDD
                        if (bbddController.modificarEmpleadoConContra(dni.getText().toString(), nuevoNombre, nuevoApellido, nuevoCorreo, nuevoTelefono, contraHash)) {
                            Toast.makeText(context, "Empleado modificado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error al modificar el empleado", Toast.LENGTH_SHORT).show();
                        }

                        String asunto = "Cambio de contraseña de su cuenta";
                        String mensaje = "Le informamos que sus credenciales de acceso han sido modificadas. Para acceder necesitará\n"
                                + "introducir el correo de acceso  " + nuevoCorreo + " \n"
                                + "y su nueva contraseña: " + nuevaContrasena + "\n";
                        enviarCorreo(nuevoCorreo, asunto, mensaje);

                        empleadoAeditar.setNombre(nuevoNombre);
                        empleadoAeditar.setApellido(nuevoApellido);
                        empleadoAeditar.setCorreo(nuevoCorreo);
                        //empleadoAeditar.setContraseña(nuevaContrasena);
                        empleadoAeditar.setTelefono(nuevoTelefono);

                        notifyDataSetChanged();
                    }
                }

            }
        });

        // Configurar el botón de cancelar del diálogo
        builder.setNegativeButton("Cancelar Cambios", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cerrar el diálogo
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        builder.show();
        builder.setCancelable(false);
    }

    private boolean comprobarDatosEmpleado(String nuevaContrasena) {
        boolean datosValidos = true;

        // Limpiar los errores previos
        nombre.setError(null);
        apellidos.setError(null);
        correo.setError(null);
        contrasena.setError(null);
        telefono.setError(null);


        // Verificar el nombre
        if (nombre.getText().toString().isEmpty()) {
            nombre.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar la descripción
        if (apellidos.getText().toString().isEmpty()) {
            apellidos.setError("Campo vacío");
            datosValidos = false;
        }

        // Verificar el precio por unidad
        if (correo.getText().toString().isEmpty()) {
            correo.setError("Campo vacío");
            datosValidos = false;
        }

        if (contrasena.getText().toString().isEmpty()) {
            contrasena.setError("Campo vacío");
            datosValidos = false;
        }

        if (telefono.getText().toString().isEmpty()) {
            telefono.setError("Campo vacío");
            datosValidos = false;
        }
        if (contrasena.getText().length() < 4) {
            contrasena.setError("Contraseña demasiado corta");
            datosValidos = false;
        }

        if (!contrasena.getText().toString().equals("****")) {
            if (!contrasena.getText().toString().matches("^[a-zA-Z0-9]*$")) {
                contrasena.setError("Contraseña no válida. Sólo numeros o letras");
                datosValidos = false;
            }

            if (!contrasena.getText().toString().equals(nuevaContrasena)) {
                contrasena.setError("Las contraseñas no coinciden");
                confirmarContrasena.setError("Las contraseñas no coinciden");
                datosValidos = false;
            }
        }

        // Si hay errores, mostrar un mensaje de advertencia
        if (!datosValidos) {
            Toast.makeText(context, "Por favor, corrige los errores", Toast.LENGTH_SHORT).show();
        }

        return datosValidos;
    }


    private void mostrarDialogoEnviarCorreo(UsuarioModel trabajador) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enviar Correo Electrónico");

        View view = LayoutInflater.from(context).inflate(R.layout.dialogo_enviar_correo, null);
        EditText asuntoEditText = view.findViewById(R.id.asuntoEditText);
        EditText mensajeEditText = view.findViewById(R.id.mensajeEditText);

        builder.setView(view);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String asunto = asuntoEditText.getText().toString().trim();
            String mensaje = mensajeEditText.getText().toString().trim();

            enviarCorreo(trabajador.getCorreo(), asunto, mensaje);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void enviarCorreo(String correo, String asunto, String mensaje) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822"); // Utilizar el tipo MIME correcto para correo
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar Correo"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "No hay aplicaciones de correo electrónico instaladas.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Error al enviar el correo electrónico.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return listaEmpleados.size();
    }
}
