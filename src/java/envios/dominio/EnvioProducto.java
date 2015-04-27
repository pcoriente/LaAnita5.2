package envios.dominio;

import producto2.dominio.Empaque;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class EnvioProducto {
    private int idEnvio;
    private int idMovto;
    private Producto producto;
    private double enviados;
    private double pendientes;
    private double peso;
    private double pesoTotal;

    public EnvioProducto() {}

    public EnvioProducto(Producto producto) {
        this.producto = producto;
    }

    @Override
    public String toString() {
        return this.producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.producto != null ? this.producto.hashCode() : 0);
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
        final EnvioProducto other = (EnvioProducto) obj;
        if (this.producto != other.producto && (this.producto == null || !this.producto.equals(other.producto))) {
            return false;
        }
        return true;
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
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

    public double getEnviados() {
        return enviados;
    }

    public void setEnviados(double enviados) {
        this.enviados = enviados;
    }

    public double getPendientes() {
        return pendientes;
    }

    public void setPendientes(double pendientes) {
        this.pendientes = pendientes;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getPesoTotal() {
        return pesoTotal;
    }

    public void setPesoTotal(double pesoTotal) {
        this.pesoTotal = pesoTotal;
    }
}
