package com.example.tfg_sistematienda.Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfg_sistematienda.R;
import com.example.tfg_sistematienda.controladores.BBDDController;
import com.example.tfg_sistematienda.modelos.LogModel;
import com.example.tfg_sistematienda.modelos.ProductoModel;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorMasVendidos extends RecyclerView.Adapter<ViewHolderMasVendidos> {

    private List<ProductoModel> masVendidos = new ArrayList<>();
    private Context context;
    private BBDDController bbddController = new BBDDController();


    public AdaptadorMasVendidos(Context context, List<ProductoModel> masVendidos) {
        this.masVendidos = masVendidos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderMasVendidos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mas_vendidos, parent, false);
        return new ViewHolderMasVendidos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderMasVendidos holder, int position) {
        ProductoModel producto = masVendidos.get(position);
        holder.nombreMasVendidos.setText(producto.getNombre());
        holder.codigoMasVendidos.setText(producto.getCodigoBarras());
        holder.cantidadMasVendidos.setText(String.valueOf(producto.getVecesComprado()));
        holder.tiendaMasVendidos.setText(bbddController.obtenerNombreTienda(producto.getIdTienda()));
    }

    @Override
    public int getItemCount() {
        return masVendidos.size();
    }
}
