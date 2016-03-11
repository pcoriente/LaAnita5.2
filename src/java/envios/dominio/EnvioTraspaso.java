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
    private double peso;
    private int diasInventario;
    private Date fechaProduccion;
    
    public EnvioTraspaso() {
        super();
        this.fechaProduccion = new Date();
    }
    
    public EnvioTraspaso(MovimientoTipo tipo, TOAlmacenJS almacen, TOAlmacenJS almacenDestino) {
        super(tipo, almacen, almacenDestino);
        this.fechaProduccion = new Date();
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

    public Date getFechaProduccion() {
        return fechaProduccion;
    }

    public void setFechaProduccion(Date fechaProduccion) {
        this.fechaProduccion = fechaProduccion;
    }
}
