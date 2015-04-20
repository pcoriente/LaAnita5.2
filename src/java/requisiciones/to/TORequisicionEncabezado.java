/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package requisiciones.to;

import java.util.Date;


public class TORequisicionEncabezado {
    
    private int idRequisicion;
    private int idEmpresa;
    private int idDepto;
    private int idSolicito;
    private int idAprobo;
    private String empleadoAprobo;
    private Date fechaRequisicion;
    private Date fechaAprobacion;
    private int status;
    private String observaciones;

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdDepto() {
        return idDepto;
    }

    public void setIdDepto(int idDepto) {
        this.idDepto = idDepto;
    }

    public int getIdSolicito() {
        return idSolicito;
    }

    public void setIdSolicito(int idSolicito) {
        this.idSolicito = idSolicito;
    }
    
    public int getIdAprobo() {
        return idAprobo;
    }

    public void setIdAprobo(int idAprobo) {
        this.idAprobo = idAprobo;
    }

    public Date getFechaRequisicion() {
        return fechaRequisicion;
    }

    public void setFechaRequisicion(Date fechaRequisicion) {
        this.fechaRequisicion = fechaRequisicion;
    }

    public Date getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(Date fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEmpleadoAprobo() {
        return empleadoAprobo;
    }

    public void setEmpleadoAprobo(String empleadoAprobo) {
        this.empleadoAprobo = empleadoAprobo;
    }
    
    
    

    
}
