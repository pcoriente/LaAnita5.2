/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ordenesDeCompra.Reporte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import ordenesDeCompra.dominio.TotalesOrdenCompra;

/**
 *
 * @author daap
 */
public class Reportes {
     public String generarReporteCorreo(ArrayList<OrdenCompraDetalle> orden, OrdenCompraEncabezado ordenEncabezado, TotalesOrdenCompra totalesOrdenesCompra) {
     //   PropertyConfigurator.configure("log4j.properties");
        String ubicacionCompilado = "C:\\Reportes\\ordenCompra.jasper";
        String ruta = "C:\\Reportes\\ordenCompra.pdf";
        JasperReport report;
        //JRExporter exporter = null;
        Map<String, Object> parametros = new HashMap<String, Object>();
//        aqui es donde paso los parametros del encabezado lo que dice numeroOrden es el nombre del campo del ireport
        parametros.put("numeroOrden", ordenEncabezado.getIdOrdenCompra());
        parametros.put("proveedor", ordenEncabezado.getProveedor());
        parametros.put("emision", ordenEncabezado.getFechaCreacion());
        parametros.put("entrega", ordenEncabezado.getFechaEntrega());
        parametros.put("empresa", ordenEncabezado.getNombreComercial());
        parametros.put("comercial", ordenEncabezado.getDesctoComercial());
        parametros.put("prontoPago", ordenEncabezado.getDesctoProntoPago());
//        ---------------------------------
        parametros.put("subtoF", totalesOrdenesCompra.getSubtoF());
        parametros.put("sumaDescuentosGeneralesF", totalesOrdenesCompra.getSumaDescuentosGeneralesF());
        parametros.put("sumaDescuentosProductosF", totalesOrdenesCompra.getSumaDescuentsoProductosF());
        parametros.put("sumaDescuentosTotalesF", totalesOrdenesCompra.getSumaDescuentosTotalesF());
        parametros.put("subTotalBrutoF", totalesOrdenesCompra.getSubTotalBrutoF());
        parametros.put("impF", totalesOrdenesCompra.getImpF());
        parametros.put("totalF", totalesOrdenesCompra.getTotalF());
        try {
            report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado); //DEPRECADO
            JasperPrint jasperprint = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(orden));
            JasperExportManager.exportReportToPdfFile(jasperprint, ruta);
        //    FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ruta;
    }
}
