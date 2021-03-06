package movimientos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class MovimientoAlmacen {
    private int idMovtoAlmacen;
    private MovimientoTipo tipo;
    private TOAlmacenJS almacen;
    private int folio;
    private Date fecha;
    private int idUsuario;
    private int propietario;
    private int estatus;
    
    public MovimientoAlmacen() {
        this.tipo = new MovimientoTipo();
        this.almacen = new TOAlmacenJS();
        this.fecha = new Date();
    }
    
    public MovimientoAlmacen(MovimientoTipo tipo) {
        this.tipo = tipo;
        this.almacen = new TOAlmacenJS();
        this.fecha = new Date();
    }

    public MovimientoAlmacen(MovimientoTipo tipo, TOAlmacenJS almacen) {
        this.tipo = tipo;
        this.almacen = almacen;
        this.fecha = new Date();
    }
//    
//    public MovimientoAlmacen(int idTipo, TOAlmacenJS almacen) {
//        this.idTipo = idTipo;
//        this.almacen = almacen;
//        this.fecha = new Date();
//    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
