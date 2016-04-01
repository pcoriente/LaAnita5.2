package envios.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoTipo;
import traspasos.dominio.Traspaso;

/**
 *
 * @author jesc
 */
public class EnvioTraspaso extends Traspaso {
    private int idEnvio;
    private double peso;
    private int diasInventario;
    private int diasInventario2;
    private Date fechaProduccion;
    private boolean directo;
    
    public EnvioTraspaso() {
        super();
        this.fechaProduccion = new Date();
    }
    
    public EnvioTraspaso(MovimientoTipo tipo, TOAlmacenJS almacen, TOAlmacenJS almacenDestino) {
        super(tipo, almacen, almacenDestino);
        this.fechaProduccion = new Date();
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getDiasInventario() {
        return diasInventario;
    }

    public void setDiasInventario(int diasInventario) {
        this.diasInventario = diasInventario;
    }

    public int getDiasInventario2() {
        return diasInventario2;
    }

    public void setDiasInventario2(int diasInventario2) {
        this.diasInventario2 = diasInventario2;
    }

    public Date getFechaProduccion() {
        return fechaProduccion;
    }

    public void setFechaProduccion(Date fechaProduccion) {
        this.fechaProduccion = fechaProduccion;
    }

    public boolean isDirecto() {
        return directo;
    }

    public void setDirecto(boolean directo) {
        this.directo = directo;
    }
}
