/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ReportesProveedor.DAO;

import ReportesProveedor.Dominio.ReporteProveedorDetalle;
import ReportesProveedor.Dominio.ReporteProveedorEncabezado;
import agentes.dao.DaoAgentes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import listaPrecioIdeal.DAO.DAOListaPrecio;
import listaPrecioIdeal.MbListaPrecioIdeal;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author PJGT
 */
public class DAOReportesProveedor {

    private DataSource ds = null;

    public DAOReportesProveedor() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            try {
                throw (ex);
            } catch (NamingException ex1) {
                Logger.getLogger(DaoAgentes.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public ArrayList<ReporteProveedorDetalle> dameInformacion(ReporteProveedorEncabezado encabezado) throws SQLException {
        ArrayList<ReporteProveedorDetalle> lst = new ArrayList<>();
        Connection cn = ds.getConnection();
        Statement st = cn.createStatement();
        ResultSet rs = null;
        String sql = "SELECT c.contribuyente as proveedor, d.importe as importe,  d.importe-d.unitario as descuentos , d.impuestos as impuestos, d.unitario+d.impuestos as total   FROM \n"
                + "(SELECT idReferencia, sum(costo * cantFacturada) as importe , sum (unitario*cantFacturada) as unitario,  sum(i.importe*cantFacturada)as impuestos \n"
                + "FROM movimientosDetalle md\n"
                + "inner join movimientos m \n"
                + "on m.idMovto = md.idMovto\n"
                + "inner join (select idEmpaque, idMovto, sum(importe)as importe FROM	 movimientosDetalleImpuestos GROUP BY idMovto, idEmpaque ) i\n"
                + "on  i.idMovto = md.idMovto and i.idEmpaque = md.idEmpaque\n"
                + "WHERE m.fecha BETWEEN '" + utilerias.Utilerias.darFormatoFecha(encabezado.getFechaInicial()) + "' and '" + utilerias.Utilerias.darFormatoFecha(encabezado.getFechaFinal()) + "' and m.idEmpresa = " + encabezado.getIdEmpresa() + " and m.idReferencia BETWEEN " + encabezado.getCodigoProductoInicial() + " and " + encabezado.getCodigoProductoFinal() + " and m.idTipo = 1\n"
                + "GROUP BY idReferencia) d\n"
                + "INNER JOIN proveedores p\n"
                + "on p.idProveedor = d.idReferencia\n"
                + "INNER JOIN contribuyentes c \n"
                + "on c.idContribuyente = p.idContribuyente";
        try {
            rs = st.executeQuery(sql);
            while (rs.next()) {
                ReporteProveedorDetalle detalle = new ReporteProveedorDetalle();
                detalle.setDescuento(rs.getDouble("descuentos"));
                detalle.setImporte(rs.getDouble("importe"));
                detalle.setIva(rs.getDouble("impuestos"));
                detalle.setNombre(rs.getString("proveedor"));
                detalle.setTotal(rs.getDouble("total"));
                lst.add(detalle);
            }
        } finally {
            st.close();
            try {
                rs.close();
            } catch (NullPointerException e) {
            }
            cn.close();
        }
        return lst;
    }

    
     public void generarReporte(ReporteProveedorEncabezado encabezado) throws JRException, SQLException {
         Connection cn = ds.getConnection();
        try {
            DAOListaPrecio dao = new DAOListaPrecio();
            dao.generarReporte();
//            String ruta = "C:\\Reportes\\listaPrecioIdeal.pdf";
            
//        -----RUTA DEL SERVIDOR-----
            String ruta = "C:\\Carlos Pat\\Reportes\\listaPrecioIdeal.pdf";
            String ubicacionCompilado = "C:\\Carlos Pat\\Reportes\\comprarPorProveedor.jasper";
//        ------------------------------
//            String ubicacionCompilado = "C:\\Reportes\\comprarPorProveedor.jasper";
            JasperPrint jasperprint;
            JasperReport report;
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("empresa", encabezado.getIdEmpresa());
            parametros.put("codigoProveedorInicial",encabezado.getCodigoProductoInicial());
            parametros.put("codigoProveedorFinal", encabezado.getCodigoProductoFinal());
            parametros.put("fechaInicial", encabezado.getFechaInicial());
            parametros.put("fechaFinal", encabezado.getFechaFinal());
            try {
                report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
                jasperprint = JasperFillManager.fillReport(report, parametros, cn);
                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.addHeader("Content-disposition", "attachment; filename=listaPrecio.pdf");
                ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
                try {
                    JasperExportManager.exportReportToPdfFile(jasperprint, ruta);
                } catch (Exception e) {
                    System.out.println(e);
                }
                FacesContext.getCurrentInstance().responseComplete();
            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}