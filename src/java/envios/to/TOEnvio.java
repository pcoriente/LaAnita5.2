package envios.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOEnvio {
    private int idEnvio;
    private int idCedis;
    private int idEmpresa;
    private int idAlmacen;
    private Date generado;
    private Date enviado;
    private double peso;
    private int status;
    private int prioridad;
    private int idChofer;
    private int idCamion;

    @Override
    public String toString() {
        return "--"+String.format("%08d", this.idEnvio)+"--";
        //return Integer.toString(this.idEnvio);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.idEnvio;
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

    public TOEnvio() {
        this.generado=new Date();
        this.enviado=new Date();
    }

    public TOEnvio(int idCedis, int idEmpresa, int idAlmacen) {
        this.idCedis = idCedis;
        this.idEmpresa = idEmpresa;
        this.idAlmacen = idAlmacen;
        this.generado=new Date();
        this.enviado=new Date();
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

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public Date getGenerado() {
        return generado;
    }

    public void setGenerado(Date generado) {
        this.generado = generado;
    }

    public Date getEnviado() {
        return enviado;
    }

    public void setEnviado(Date enviado) {
        this.enviado = enviado;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getIdChofer() {
        return idChofer;
    }

    public void setIdChofer(int idChofer) {
        this.idChofer = idChofer;
    }

    public int getIdCamion() {
        return idCamion;
    }

    public void setIdCamion(int idCamion) {
        this.idCamion = idCamion;
    }
}
