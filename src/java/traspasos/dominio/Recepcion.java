package traspasos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;

/**
 *
 * @author jesc
 */
public class Recepcion extends MovimientoOficina {
    private int idSolicitud;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int idTraspaso;
    private int traspasoFolio;
    private Date traspasoFecha;
    private TOAlmacenJS almacenOrigen;
    
    public Recepcion() {
        super();
        solicitudFecha=new Date();
        traspasoFecha=new Date();
        this.almacenOrigen=new TOAlmacenJS();
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getSolicitudFolio() {
        return solicitudFolio;
    }

    public void setSolicitudFolio(int solicitudFolio) {
        this.solicitudFolio = solicitudFolio;
    }

    public Date getSolicitudFecha() {
        return solicitudFecha;
    }

    public void setSolicitudFecha(Date solicitudFecha) {
        this.solicitudFecha = solicitudFecha;
    }

    public int getIdTraspaso() {
        return idTraspaso;
    }

    public void setIdTraspaso(int idTraspaso) {
        this.idTraspaso = idTraspaso;
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
}
