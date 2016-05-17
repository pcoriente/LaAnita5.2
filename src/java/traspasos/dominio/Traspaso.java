package traspasos.dominio;

import almacenes.to.TOAlmacenJS;
import java.util.Date;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;

/**
 *
 * @author jesc
 */
public class Traspaso extends MovimientoOficina {
    private int idEnvio;
    private int envioFolio;
    private int pedidoFolio;
    private boolean envio;
    private boolean directo;
    private int solicitudFolio;
    private Date solicitudFecha;
    private int solicitudIdUsuario;
    private int solicitudProietario;
    private int solicitudEstatus;
    private TOAlmacenJS almacenDestino;
    private int idSolicitud;
//    private double peso;
//    private int diasInventario;
    
    public Traspaso() {
        super();
        this.solicitudFecha=new Date();
        this.almacenDestino=new TOAlmacenJS();
    }
    
    public Traspaso(MovimientoTipo tipo, TOAlmacenJS almacen, TOAlmacenJS almacenDestino) {
        super(tipo, almacen);
        this.solicitudFecha = new Date();
        this.almacenDestino = almacenDestino;
    }
    
    public String getId() {
        return String.valueOf(this.idSolicitud)+String.valueOf(super.getIdMovto());
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getEnvioFolio() {
        return envioFolio;
    }

    public void setEnvioFolio(int envioFolio) {
        this.envioFolio = envioFolio;
    }

    public int getPedidoFolio() {
        return pedidoFolio;
    }

    public void setPedidoFolio(int pedidoFolio) {
        this.pedidoFolio = pedidoFolio;
    }

    public boolean isEnvio() {
        return envio;
    }

    public void setEnvio(boolean envio) {
        this.envio = envio;
    }

    public boolean isDirecto() {
        return directo;
    }

    public void setDirecto(boolean directo) {
        this.directo = directo;
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

//    public double getPeso() {
//        return peso;
//    }
//
//    public void setPeso(double peso) {
//        this.peso = peso;
//    }
//
//    public int getDiasInventario() {
//        return diasInventario;
//    }
//
//    public void setDiasInventario(int diasInventario) {
//        this.diasInventario = diasInventario;
//    }
}
