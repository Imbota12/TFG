package com.example.tfg_sistematienda.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMasDevueltos extends RecyclerView.Adapter<ViewHolderMasDevueltos> {

    private List<ProductoModel> masDevueltos = new ArrayList<>();
    private Context context;
    private BBDDController bbddController = new BBDDController();

    public AdaptadorMasDevueltos(Context context, List<ProductoModel> masDevueltos) {
        this.masDevueltos = masDevueltos;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolderMasDevueltos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mas_devueltos, parent, false);
        return new ViewHolderMasDevueltos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderMasDevueltos holder, int position) {
        ProductoModel masDevuelto = masDevueltos.get(position);
        holder.nombreMasDevuelto.setText(masDevuelto.getNombre());
        holder.cantidadMasDevuelto.setText(String.valueOf(masDevuelto.getCantidad()));
        holder.codigoMasDevuelto.setText(String.valueOf(masDevuelto.getCodigoBarras()));
        holder.tiendaMasDevuelto.setText(bbddController.obtenerNombreTienda(masDevuelto.getIdTienda()));
    }

    @Override
    public int getItemCount() {
        return masDevueltos.size();
    }
}
