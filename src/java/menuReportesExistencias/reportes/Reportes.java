/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package menuReportesExistencias.reportes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import menuReportesExistencias.dominio.TOExistencias;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 *
 * @author Torres
 */
public class Reportes {

    public static void generarReporteArrayList(ArrayList<TOExistencias> lista, String rutaArchivoCompilado, String nombreDelArchivo, String almacen) throws JRException, IOException {
//        String ruta = "C:\\Reportes\\ordenCompraDaap.pdf";
//        String ubicacionCompilado = "C:\\Reportes\\ordenCompra.jasper";
        JasperPrint jasperprint;
        JasperReport report;
        Map<String, Object> parametros = new HashMap<String, Object>();
        Date d = new Date();
        parametros.put("fecha", utilerias.Utilerias.darFormatoFecha(d));
        parametros.put("almacen", almacen);
        report = (JasperReport) JRLoader.loadObjectFromFile(rutaArchivoCompilado);
        jasperprint = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(lista));
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=rOrdenCompra" + nombreDelArchivo + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();
    }
}
