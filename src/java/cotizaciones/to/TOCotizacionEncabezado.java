/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cotizaciones.to;

import java.util.Date;

/**
 *
 * @author daap
 */
public class TOCotizacionEncabezado {
    
   private int idCotizacion;
   private int idRequisicion;
   private int idProveedor;
   private String folioProveedor;
   private Date fechaCotizacion;
   private Double descuentoProveedor;
   private String observaciones;

    public int getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(int idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getFolioProveedor() {
        return folioProveedor;
    }

    public void setFolioProveedor(String folioProveedor) {
        this.folioProveedor = folioProveedor;
    }

    public Date getFechaCotizacion() {
        return fechaCotizacion;
    }

    public void setFechaCotizacion(Date fechaCotizacion) {
        this.fechaCotizacion = fechaCotizacion;
    }

    public Double getDescuentoProveedor() {
        return descuentoProveedor;
    }

    public void setDescuentoProveedor(Double descuentoProveedor) {
        this.descuentoProveedor = descuentoProveedor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
   
    
}
