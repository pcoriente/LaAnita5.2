package envios.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOEnvio {
    private int idEnvio;
    private int idCedis;
    private int folioEnvio;
    private Date generado;
//    private Date fechaEstatus;
    private Date fechaEnvio;
    private Date fechaFletera;
//    private Date fechaAnita;
//    private Date fechaQuimicos;
//    private int diasInventario;
    private int prioridad;
    private int idUsuario;
//    private int propietario;
    private int estatus;

    @Override
    public String toString() {
        return "--"+String.format("%08d", this.folioEnvio)+"--";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.idEnvio;
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
        final TOEnvio other = (TOEnvio) obj;
        if (this.idEnvio != other.idEnvio) {
            return false;
        }
        return true;
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }

    public int getFolioEnvio() {
        return folioEnvio;
    }

    public void setFolioEnvio(int folioEnvio) {
        this.folioEnvio = folioEnvio;
    }

    public Date getGenerado() {
        return generado;
    }

    public void setGenerado(Date generado) {
        this.generado = generado;
    }

//    public Date getFechaEstatus() {
//        return fechaEstatus;
//    }
//
//    public void setFechaEstatus(Date fechaEstatus) {
//        this.fechaEstatus = fechaEstatus;
//    }
//
    public Date getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Date fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Date getFechaFletera() {
        return fechaFletera;
    }

    public void setFechaFletera(Date fechaFletera) {
        this.fechaFletera = fechaFletera;
    }

//    public Date getFechaAnita() {
//        return fechaAnita;
//    }
//
//    public void setFechaAnita(Date fechaAnita) {
//        this.fechaAnita = fechaAnita;
//    }
//
//    public Date getFechaQuimicos() {
//        return fechaQuimicos;
//    }
//
//    public void setFechaQuimicos(Date fechaQuimicos) {
//        this.fechaQuimicos = fechaQuimicos;
//    }
//
//    public int getDiasInventario() {
//        return diasInventario;
//    }
//
//    public void setDiasInventario(int diasInventario) {
//        this.diasInventario = diasInventario;
//    }
//
    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

//    public int getPropietario() {
//        return propietario;
//    }
//
//    public void setPropietario(int propietario) {
//        this.propietario = propietario;
//    }
//
    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }
}
