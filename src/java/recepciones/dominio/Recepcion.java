package recepciones.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class Recepcion extends MovimientoOficina {
//    private int idSolicitud;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int traspasoFolio;
    private Date traspasoFecha;
    private TOAlmacenJS almacenOrigen;
    private int idTraspaso;
    
    public Recepcion() {
        super();
        this.almacenOrigen=new TOAlmacenJS();
        this.solicitudFecha=new Date();
        this.traspasoFecha=new Date();
    }
    
    public Recepcion(MovimientoTipo tipo, TOAlmacenJS almacen, TOAlmacenJS almacenOrigen) {
        super(tipo, almacen);
        this.almacenOrigen=almacenOrigen;
        this.solicitudFecha=new Date();
        this.traspasoFecha=new Date();
    }

//    public int getIdSolicitud() {
//        return idSolicitud;
//    }
//
//    public void setIdSolicitud(int idSolicitud) {
//        this.idSolicitud = idSolicitud;
//    }

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
