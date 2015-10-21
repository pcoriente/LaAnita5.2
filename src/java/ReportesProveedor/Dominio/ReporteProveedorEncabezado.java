/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportesProveedor.Dominio;

import java.util.Date;

/**
 *
 * @author PJGT
 */
public class ReporteProveedorEncabezado {

    private int idEmpresa;
    private int codigoProductoInicial;
    private int codigoProductoFinal;
    private Date fechaInicial;
    private Date fechaFinal;
    private String empresa;

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getCodigoProductoInicial() {
        return codigoProductoInicial;
    }

    public void setCodigoProductoInicial(int codigoProductoInicial) {
        this.codigoProductoInicial = codigoProductoInicial;
    }

    public int getCodigoProductoFinal() {
        return codigoProductoFinal;
    }

    public void setCodigoProductoFinal(int codigoProductoFinal) {
        this.codigoProductoFinal = codigoProductoFinal;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
  
}
