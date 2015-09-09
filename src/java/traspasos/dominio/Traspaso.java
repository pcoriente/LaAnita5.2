package traspasos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;

/**
 *
 * @author jesc
 */
public class Traspaso extends MovimientoOficina {
    private int idSolicitud;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int solicitudIdUsuario;
    private int solicitudProietario;
    private int solicitudEstatus;
    private TOAlmacenJS almacenDestino;
    
    public Traspaso() {
        super();
        this.solicitudFecha=new Date();
        this.almacenDestino=new TOAlmacenJS();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.idSolicitud;
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
        final Traspaso other = (Traspaso) obj;
        if (this.idSolicitud != other.idSolicitud) {
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

    public int getSolicitudIdUsuario() {
        return solicitudIdUsuario;
    }

    public void setSolicitudIdUsuario(int solicitudIdUsuario) {
        this.solicitudIdUsuario = solicitudIdUsuario;
    }

    public int getSolicitudProietario() {
        return solicitudProietario;
    }

    public void setSolicitudProietario(int solicitudProietario) {
        this.solicitudProietario = solicitudProietario;
    }

    public int getSolicitudEstatus() {
        return solicitudEstatus;
    }

    public void setSolicitudEstatus(int solicitudEstatus) {
        this.solicitudEstatus = solicitudEstatus;
    }

    public TOAlmacenJS getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(TOAlmacenJS almacenDestino) {
        this.almacenDestino = almacenDestino;
    }
}
