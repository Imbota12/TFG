package com.example.tfg_sistematienda.Adaptadores;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;

public class ViewHolderEmpleados extends RecyclerView.ViewHolder {
    public TextView nombreEmpleado;
    public TextView apellidosEmpleado;
    public TextView dniEmpleado;
    public TextView telefonoEmpleado;
    public TextView correoEmpleado;
    public Switch tipoEmpleado;
    public CheckBox empleadoActivo;
    public ImageButton llamada, correo, expulsar, contrato;

    public ViewHolderEmpleados(@NonNull View itemView) {
        super(itemView);
        nombreEmpleado = itemView.findViewById(R.id.nombre_emple);
        apellidosEmpleado = itemView.findViewById(R.id.apellido_emple);
        dniEmpleado = itemView.findViewById(R.id.dni_emple);
        telefonoEmpleado = itemView.findViewById(R.id.telefono_empleado);
        correoEmpleado = itemView.findViewById(R.id.correo_empleado);
        tipoEmpleado = itemView.findViewById(R.id.tipo);
        empleadoActivo = itemView.findViewById(R.id.cb_activo);
        llamada = itemView.findViewById(R.id.llamada);
        correo = itemView.findViewById(R.id.correo);
        expulsar = itemView.findViewById(R.id.despido);
        contrato = itemView.findViewById(R.id.contrato);
    }
}
