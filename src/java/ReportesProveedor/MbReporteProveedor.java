/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportesProveedor;

import Message.Mensajes;
import ReportesProveedor.Dominio.ReporteProveedorEncabezado;
import empresas.MbEmpresas;
import empresas.MbMiniEmpresa;
import empresas.dominio.Empresa;
import empresas.dominio.MiniEmpresa;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author PJGT
 */
@Named(value = "mbReporteProveedor")
@SessionScoped
public class MbReporteProveedor implements Serializable {

    @ManagedProperty(value = "#{miniEmpresa}")
    private MbMiniEmpresa miniEmpresa;
    private ReporteProveedorEncabezado encabezadoBusqueda;
    private MiniEmpresa empresa = new MiniEmpresa();

    public MbReporteProveedor() {
        encabezadoBusqueda = new ReporteProveedorEncabezado();
        miniEmpresa = new MbMiniEmpresa();
    }

    public boolean validar() {
        boolean ok = false;
        if (empresa.getIdEmpresa() == 0) {
            Mensajes.mensajeAlert("Se requiere una empresa");
        } else if (encabezadoBusqueda.getCodigoProductoInicial() == 0) {
            Mensajes.mensajeAlert("Se requiere un codigo inicial");
        } else if (encabezadoBusqueda.getCodigoProductoFinal() == 0) {
            Mensajes.mensajeAlert("Se requiere un codigo final");
        } else if (encabezadoBusqueda.getFechaInicial().equals("")) {
            Mensajes.mensajeAlert("Se requiere una fecha inicial");
        } else if (encabezadoBusqueda.getFechaFinal().equals("")) {
            Mensajes.mensajeAlert("Se requiere una fecha final");
        } else {
            ok = true;
        }
        return ok;
    }

    public void buscar() {
        boolean ok = validar();
        if (ok) {

        }
    }
    
   
    

    public MbMiniEmpresa getMiniEmpresa() {
        return miniEmpresa;
    }

    public void setMiniEmpresa(MbMiniEmpresa miniEmpresa) {
        this.miniEmpresa = miniEmpresa;
    }

    public ReporteProveedorEncabezado getEncabezadoBusqueda() {
        return encabezadoBusqueda;
    }

    public void setEncabezadoBusqueda(ReporteProveedorEncabezado encabezadoBusqueda) {
        this.encabezadoBusqueda = encabezadoBusqueda;
    }

    public MiniEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(MiniEmpresa empresa) {
        this.empresa = empresa;
    }

   
    

}
