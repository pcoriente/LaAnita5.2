package ventas.dominio;

import impuestos.dominio.ImpuestosProducto;
import java.util.ArrayList;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class VentaProducto {
    private int idMovto;
    private Producto producto;
    private double cantOrdenada;
    private double cantOrdenadaSinCargo;
    private double cantFacturada;
    private double cantSinCargo;
    private double separados;
    private double precio;
    private double descuento;
    private double unitario;
    private double neto;
    private ArrayList<ImpuestosProducto> impuestos;
    
    public VentaProducto() {
        this.producto=new Producto();
    }
    
    public VentaProducto(Producto producto) {
        this.producto=producto;
    }
    
    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.producto != null ? this.producto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VentaProducto other = (VentaProducto) obj;
        if (this.producto != other.producto && (this.producto == null || !this.producto.equals(other.producto))) {
            return false;
        }
        return true;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getCantOrdenada() {
        return cantOrdenada;
    }

    public void setCantOrdenada(double cantOrdenada) {
        this.cantOrdenada = cantOrdenada;
    }

    public double getCantOrdenadaSinCargo() {
        return cantOrdenadaSinCargo;
    }

    public void setCantOrdenadaSinCargo(double cantOrdenadaSinCargo) {
        this.cantOrdenadaSinCargo = cantOrdenadaSinCargo;
    }

    public double getCantFacturada() {
        return cantFacturada;
    }

    public void setCantFacturada(double cantFacturada) {
        this.cantFacturada = cantFacturada;
    }

    public double getCantSinCargo() {
        return cantSinCargo;
    }

    public void setCantSinCargo(double cantSinCargo) {
        this.cantSinCargo = cantSinCargo;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getUnitario() {
        return unitario;
    }

    public void setUnitario(double unitario) {
        this.unitario = unitario;
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(double neto) {
        this.neto = neto;
    }

    public ArrayList<ImpuestosProducto> getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(ArrayList<ImpuestosProducto> impuestos) {
        this.impuestos = impuestos;
    }
}
