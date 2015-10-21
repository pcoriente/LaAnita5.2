/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportesProveedor;

import Message.Mensajes;
import ReportesProveedor.DAO.DAOReportesProveedor;
import ReportesProveedor.Dominio.ReporteProveedorDetalle;
import ReportesProveedor.Dominio.ReporteProveedorEncabezado;
import empresas.MbMiniEmpresa;
import empresas.dominio.MiniEmpresa;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import utilerias.Habilita;

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
    private ArrayList<ReporteProveedorDetalle> lst = new ArrayList<>();
    private Habilita habilita = new Habilita();

    public MbReporteProveedor() {
        encabezadoBusqueda = new ReporteProveedorEncabezado();
        miniEmpresa = new MbMiniEmpresa();
    }

    public boolean validar() {
        DateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd");
        String feini = formatoFecha.format(encabezadoBusqueda.getFechaInicial());
        String fefin = formatoFecha.format(encabezadoBusqueda.getFechaFinal());
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
        } else if (fefin.compareTo(feini) < 0) {
            Mensajes.MensajeAlertP("La Fecha Final no Puede ser Menor a la Inicial");
        } else {
            ok = true;
        }
        return ok;
    }

    public void generarReporte() {
        try {
            String ubicacionCompilado = "C:\\Carlos Pat\\Reportes\\comprasPvdAcum.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(this.getLst());
            Map parametros = new HashMap();

            parametros.put("empresa", encabezadoBusqueda.getEmpresa());
            parametros.put("codigoProveedorInicial", encabezadoBusqueda.getCodigoProductoInicial());
            parametros.put("codigoProveedorFinal", encabezadoBusqueda.getCodigoProductoFinal());
            parametros.put("fechaInicial", encabezadoBusqueda.getFechaInicial());
            parametros.put("fechaFinal", encabezadoBusqueda.getFechaFinal());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
            JasperPrint jasperprint = JasperFillManager.fillReport(report, parametros, beanColDataSource);
            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=comprasPorProveedor.pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();

            Mensajes.mensajeSucces("Reporte Generado");
        } catch (JRException ex) {
            Mensajes.mensajeAlert(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(MbReporteProveedor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void inicializar(int estatus) {
        if (estatus == 1) {
            System.out.println(estatus+"   estado es por proveedor ");
            habilita.setDisabled(false);
            habilita.setDisabledProd(true);
        }
        if (estatus == 2) {
            System.out.println(estatus+"   estado es por producto ");
            habilita.setDisabled(true);
            habilita.setDisabledProd(false);
        }
        lst.removeAll(lst);
        System.out.println("Entro  a limpiar");
    }

    public String salir() {
//        lst = null;
        lst.removeAll(lst);
        return "index.xhtml";
    }

    public void buscar() {
        boolean ok = validar();
        if (ok) {
            DAOReportesProveedor dao = new DAOReportesProveedor();
            try {
                encabezadoBusqueda.setIdEmpresa(empresa.getIdEmpresa());
                encabezadoBusqueda.setEmpresa(empresa.getNombreComercial());
                lst = dao.dameInformacion(encabezadoBusqueda);
                if (lst.isEmpty()) {
                    Mensajes.mensajeAlert("No hay Registros Para Procesar");
                } else {
                    Mensajes.mensajeSucces("Registros Procesados");
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbReporteProveedor.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    public ArrayList<ReporteProveedorDetalle> getLst() {
        return lst;
    }

    public void setLst(ArrayList<ReporteProveedorDetalle> lst) {
        this.lst = lst;
    }

}
