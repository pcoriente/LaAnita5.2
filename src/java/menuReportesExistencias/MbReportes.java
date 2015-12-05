/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package menuReportesExistencias;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import formatos.MbFormatos;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import menuReportesExistencias.DAO.DAOReportesExistencias;
import menuReportesExistencias.dominio.TOExistencias;
import net.sf.jasperreports.engine.JRException;
import org.primefaces.context.RequestContext;
import producto2.MbProductosBuscar;

/**
 *
 * @author Torres
 */
@Named(value = "mbReportes")
@SessionScoped
public class MbReportes implements Serializable {

    /**
     * Creates a new instance of MbReportes
     */
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenesJS = null;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbProductosBuscar = null;

    ArrayList<TOExistencias> lst = new ArrayList<>();
//    private TOExistencias tOExistencias = new TOExistencias();

    public MbReportes() throws NamingException {
        mbAlmacenesJS = new MbAlmacenesJS();
        mbProductosBuscar = new MbProductosBuscar();
    }

    public void busquedaDeExistencia() {
//        RequestContext context = RequestContext.getCurrentInstance();
//        context.execute("PF('block').show();");

        lst.removeAll(lst);
        try {
            DAOReportesExistencias dao = new DAOReportesExistencias();
            lst = dao.dameExistencia(mbAlmacenesJS.getToAlmacen().getIdAlmacen());
            for (TOExistencias lst1 : lst) {
                lst1.setTotal(lst1.getExistencia() + lst1.getTransito());
                lst1.setPorPedir(lst1.getTotal() - lst1.getExistenciaMaxima());
                lst1.setProducto(mbProductosBuscar.obtenerProducto(lst1.getIdEmpaque()));
            }
            Mensajes.mensajeSucces("Información encontrada");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbReportes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbReportes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void generarReporte() {
        try {
            menuReportesExistencias.reportes.Reportes.generarReporteArrayList(lst, "C:\\Carlos Pat\\reporteExistencias.jasper", "ReporteExistencias");
            Mensajes.mensajeSucces("Reporte generado correctamente");
        } catch (JRException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbReportes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
            Logger.getLogger(MbReportes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String salir() {
        return "index.xhtml";
    }

    public MbAlmacenesJS getMbAlmacenesJS() {
        return mbAlmacenesJS;
    }

    public void setMbAlmacenesJS(MbAlmacenesJS mbAlmacenesJS) {
        this.mbAlmacenesJS = mbAlmacenesJS;
    }

    public ArrayList<TOExistencias> getLst() {
        return lst;
    }

    public void setLst(ArrayList<TOExistencias> lst) {
        this.lst = lst;
    }

    public MbProductosBuscar getMbProductosBuscar() {
        return mbProductosBuscar;
    }

    public void setMbProductosBuscar(MbProductosBuscar mbProductosBuscar) {
        this.mbProductosBuscar = mbProductosBuscar;
    }

}
