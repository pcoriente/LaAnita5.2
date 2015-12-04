/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package menuReportesExistencias.dominio;

import java.io.Serializable;
import producto2.dominio.Producto;

/**
 *
 * @author Torres
 */
public class TOExistencias implements Serializable {

    private int sku;
    private int idEmpaque;
    private double existencia;
    private double transito;
    private double total;
    private double existenciaMinima;
    private double existenciaMaxima;
    private double porPedir;
    private Producto producto = new Producto();

    public int getSku() {
        return sku;
    }

    public void setSku(int sku) {
        this.sku = sku;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public double getExistencia() {
        return existencia;
    }

    public void setExistencia(double existencia) {
        this.existencia = existencia;
    }

    public double getTransito() {
        return transito;
    }

    public void setTransito(double transito) {
        this.transito = transito;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

   
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }


    public double getPorPedir() {
        return porPedir;
    }

    public void setPorPedir(double porPedir) {
        this.porPedir = porPedir;
    }

    public double getExistenciaMinima() {
        return existenciaMinima;
    }

    public void setExistenciaMinima(double existenciaMinima) {
        this.existenciaMinima = existenciaMinima;
    }

    public double getExistenciaMaxima() {
        return existenciaMaxima;
    }

    public void setExistenciaMaxima(double existenciaMaxima) {
        this.existenciaMaxima = existenciaMaxima;
    }

    
    
    
}
