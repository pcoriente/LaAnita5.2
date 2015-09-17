package movimientos.to1;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author jesc
 */
public class Lote1 {
    private int idAlmacen;
    private int idProducto;
    private String lote;
    private double cantidad;
    private double separados;
    private double saldo;
    private Date fechaCaducidad;
    
    public Lote1() {
        this.lote="";
        this.fechaCaducidad=new Date();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.lote);
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
        final Lote1 other = (Lote1) obj;
        if (!Objects.equals(this.lote, other.lote)) {
            return false;
        }
        return true;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
