package envios.dominio;

import cedis.dominio.MiniCedis;
import java.util.Date;

/**
 *
 * @author jesc
 */
public class Envio {
    private int idEnvio;
    private MiniCedis cedis;
    private int folioEnvio;
    private Date generado;
//    private Date fechaEstatus;
    private Date fechaEnvio;
    private Date fechaFletera;
//    private Date fechaAnita;
//    private Date fechaQuimicos;
//    private int diasInventario;
    private int prioridad;
    private double peso;
    private double pesoDirectos;
    private int idUsuario;
//    private int propietario;
    private int estatus;
//    private ArrayList<Traspaso> traspasos;
    
    public Envio() {
        this.cedis = new MiniCedis();
        this.generado = new Date();
//        this.fechaEstatus = new Date();
        this.fechaEnvio = new Date();
        this.fechaFletera = new Date();
//        this.fechaAnita = new Date();
//        this.fechaQuimicos = new Date();
    }
    
    public Envio(MiniCedis cedis) {
        this.cedis=cedis;
        this.generado = new Date();
//        this.fechaEstatus = new Date();
        this.fechaEnvio = new Date();
        this.fechaFletera = new Date();
//        this.fechaAnita = new Date();
//        this.fechaQuimicos = new Date();
    }
    
    @Override
    public String toString() {
        return "--"+String.format("%08d", this.folioEnvio)+"--";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.idEnvio;
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
        final Envio other = (Envio) obj;
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

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
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

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getPesoDirectos() {
        return pesoDirectos;
    }

    public void setPesoDirectos(double pesoDirectos) {
        this.pesoDirectos = pesoDirectos;
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
