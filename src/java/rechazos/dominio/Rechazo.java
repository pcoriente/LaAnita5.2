package rechazos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class Rechazo extends MovimientoOficina {
    private int recepcionFolio;
    private Date recepcionFecha;
    private int traspasoFolio;
    private Date traspasoFecha;
    private TOAlmacenJS almacenOrigen;
    private int idRecepcion;
    
    public Rechazo() {
        super();
        this.almacenOrigen = new TOAlmacenJS();
        this.recepcionFecha = new Date();
        this.traspasoFecha = new Date();
    }
    
    public Rechazo(MovimientoTipo tipo, TOAlmacenJS almacen, TOAlmacenJS almacenOrigen) {
        super(tipo, almacen);
        this.almacenOrigen = almacenOrigen;
        this.recepcionFecha = new Date();
        this.traspasoFecha = new Date();
    }

    public int getRecepcionFolio() {
        return recepcionFolio;
    }

    public void setRecepcionFolio(int recepcionFolio) {
        this.recepcionFolio = recepcionFolio;
    }

    public Date getRecepcionFecha() {
        return recepcionFecha;
    }

    public void setRecepcionFecha(Date recepcionFecha) {
        this.recepcionFecha = recepcionFecha;
    }

    public int getTraspasoFolio() {
        return traspasoFolio;
    }

    public void setTraspasoFolio(int traspasoFolio) {
        this.traspasoFolio = traspasoFolio;
    }

    public Date getTraspasoFecha() {
        return traspasoFecha;
    }

    public void setTraspasoFecha(Date traspasoFecha) {
        this.traspasoFecha = traspasoFecha;
    }

    public TOAlmacenJS getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(TOAlmacenJS almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public int getIdRecepcion() {
        return idRecepcion;
    }

    public void setIdRecepcion(int idRecepcion) {
        this.idRecepcion = idRecepcion;
    }
}
