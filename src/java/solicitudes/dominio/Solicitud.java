package solicitudes.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class Solicitud {
    private int idMovto;
    private TOAlmacenJS almacen;
    private int folio;
    private Date fecha;
    private int idUsuario;
    private TOAlmacenJS almacenOrigen;
    private int idUsuarioOrigen;
    private int propietario;
    private int estatus;
    
    public Solicitud() {
        this.almacen = new TOAlmacenJS();
        this.fecha = new Date();
        this.almacenOrigen = new TOAlmacenJS();
    }
    
    public Solicitud(TOAlmacenJS almacen, TOAlmacenJS almacenOrigen) {
        this.almacen = almacen;
        this.fecha = new Date();
        this.almacenOrigen = almacenOrigen;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
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

    public TOAlmacenJS getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(TOAlmacenJS almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public int getIdUsuarioOrigen() {
        return idUsuarioOrigen;
    }

    public void setIdUsuarioOrigen(int idUsuarioOrigen) {
        this.idUsuarioOrigen = idUsuarioOrigen;
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
