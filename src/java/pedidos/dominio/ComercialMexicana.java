/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedidos.dominio;

import java.util.Date;

/**
 *
 * @author Usuario
 */
public class ComercialMexicana {

    private String ordenCompra;
    private Date fechaEmbarque;
    private Date fechaCancelacion;
    private int codigoTienda;
    private double cantidad;
    private Date fechaElaboracion;
    private String numeroProveedor;
    private String upc;
    private double pendientePorSurtir;
    private double camasPorPallet;
    private double cajasPorCamas;

    public String getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(String ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public Date getFechaEmbarque() {
        return fechaEmbarque;
    }

    public void setFechaEmbarque(Date fechaEmbarque) {
        this.fechaEmbarque = fechaEmbarque;
    }

    public Date getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(Date fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public int getCodigoTienda() {
        return codigoTienda;
    }

    public void setCodigoTienda(int codigoTienda) {
        this.codigoTienda = codigoTienda;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Date getFechaElaboracion() {
        return fechaElaboracion;
    }

    public void setFechaElaboracion(Date fechaElaboracion) {
        this.fechaElaboracion = fechaElaboracion;
    }

    public String getNumeroProveedor() {
        return numeroProveedor;
    }

    public void setNumeroProveedor(String numeroProveedor) {
        this.numeroProveedor = numeroProveedor;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public double getPendientePorSurtir() {
        return pendientePorSurtir;
    }

    public void setPendientePorSurtir(double pendientePorSurtir) {
        this.pendientePorSurtir = pendientePorSurtir;
    }

    public double getCamasPorPallet() {
        return camasPorPallet;
    }

    public void setCamasPorPallet(double camasPorPallet) {
        this.camasPorPallet = camasPorPallet;
    }

    public double getCajasPorCamas() {
        return cajasPorCamas;
    }

    public void setCajasPorCamas(double cajasPorCamas) {
        this.cajasPorCamas = cajasPorCamas;
    }

}
