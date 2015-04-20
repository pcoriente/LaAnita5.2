package movimientos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOLote {
    private int idAlmacen;
    private int idEmpaque;
    private String lote;
    private Date fechaCaducidad;
    private double cantidad;
    private double saldo;
    private double separados;
    private double existenciaFisica;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.idEmpaque;
        hash = 53 * hash + (this.lote != null ? this.lote.hashCode() : 0);
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
        final TOLote other = (TOLote) obj;
        if (this.idEmpaque != other.idEmpaque) {
            return false;
        }
        if ((this.lote == null) ? (other.lote != null) : !this.lote.equals(other.lote)) {
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

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getSeparados() {
        return separados;
    }

    public void setSeparados(double separados) {
        this.separados = separados;
    }

    public double getExistenciaFisica() {
        return existenciaFisica;
    }

    public void setExistenciaFisica(double existenciaFisica) {
        this.existenciaFisica = existenciaFisica;
    }
}
