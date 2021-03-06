package solicitudes.dominio;

import java.util.Objects;
import producto2.dominio.Producto;

/**
 *
 * @author jesc
 */
public class SolicitudProducto {
    private int idSolicitud;
    private Producto producto;
    private double cajasSolicitadas;
    private double cantSolicitada;
    private double separados;
    
    public SolicitudProducto() {
        this.producto = new Producto();
    }
    
    public SolicitudProducto(Producto producto) {
        this.producto = producto;
    }

    @Override
    public String toString() {
        return producto.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.producto);
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
        final SolicitudProducto other = (SolicitudProducto) obj;
        if (!Objects.equals(this.producto, other.producto)) {
            return false;
        }
        return true;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getCajasSolicitadas() {
        return cajasSolicitadas;
    }

    public void setCajasSolicitadas(double cajasSolicitadas) {
        this.cajasSolicitadas = cajasSolicitadas;
    }

    public double getCantSolicitada() {
        return cantSolicitada;
    }

    public void setCantSolicitada(double cantSolicitada) {
        this.cantSolicitada = cantSolicitada;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }
}
