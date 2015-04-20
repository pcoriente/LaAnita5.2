/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listaPrecioIdeal.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import listaPrecioIdeal.dominio.ListaPrecioIdeal;
import listaPrecioIdeal.to.TOPrecioListaIdeal;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author Usuario
 */
public class DAOListaPrecio {

    private DataSource ds = null;

    public DAOListaPrecio() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }

    }

    public void guardarLista(ArrayList<ListaPrecioIdeal> lstListaPrecioIdeal) throws SQLException {
        Connection cn = ds.getConnection();
        PreparedStatement ps = null;
        Statement st = cn.createStatement();
        try {
            st.executeUpdate("begin transaction");
            for (ListaPrecioIdeal lst : lstListaPrecioIdeal) {
                String sql = "INSERT INTO listaPrecios (idProducto, precioLista) VALUES('" + lst.getProducto().getIdProducto() + "','" + lst.getPrecioLista() + "')";
                ps = cn.prepareStatement(sql);
                ps.executeUpdate();
            }
            st.executeUpdate("commit transaction");
        } catch (SQLException ex) {
            st.executeUpdate("rollback transaction");
            throw ex;
        } finally {
            ps.close();
        }
    }

    public ArrayList<TOPrecioListaIdeal> dameValores() throws SQLException {
        ArrayList<TOPrecioListaIdeal> listaPrecioIdeal = new ArrayList<TOPrecioListaIdeal>();
        Connection cn = ds.getConnection();
        String sql = "SELECT  * FROM listaPrecios ";
        Statement st = cn.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                TOPrecioListaIdeal toListaPrecioIdeal = new TOPrecioListaIdeal();
                toListaPrecioIdeal.setPrecioLista(rs.getDouble("precioLista"));
                toListaPrecioIdeal.setIdProducto(rs.getInt("idEmpaque"));
                listaPrecioIdeal.add(toListaPrecioIdeal);
            }
        } finally {
            cn.close();
        }
        return listaPrecioIdeal;

    }

    public void actualizar(ListaPrecioIdeal lstListaPrecioIdeal) throws SQLException {
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        try {
            String sql = "UPDATE listaPrecios set precioLista = '" + lstListaPrecioIdeal.getPrecioLista() + "' WHERE idProducto = '" + lstListaPrecioIdeal.getProducto().getIdProducto() + "' ";
            st.executeUpdate(sql);
        } finally {
            cn.close();
        }
    }

    public void generarReporte() throws JRException, SQLException {
        Connection cn = ds.getConnection();
//        String ruta = "C:\\Reportes\\listaPrecioIdeal.pdf";
        String ubicacionCompilado = "C:\\Reportes\\listaPrecios.jasper";
        JasperPrint jasperprint;
        JasperReport report;
        Map<String, Object> parametros = new HashMap<String, Object>();
        parametros.put("idAgente", 1);
//        parametros.put("hola", "Mi primer Reporte");
        try {
            report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
            jasperprint = JasperFillManager.fillReport(report, parametros, cn);
            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=listaPrecio.pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
            try {
//                JasperExportManager.exportReportToPdfFile(jasperprint, ruta);
            } catch (Exception e) {
                System.out.println(e);
            }
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
