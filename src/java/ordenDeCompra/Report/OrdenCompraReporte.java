/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ordenDeCompra.Report;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import ordenesDeCompra.dominio.OrdenCompraDetalle;
import ordenesDeCompra.dominio.OrdenCompraEncabezado;
import ordenesDeCompra.dominio.TotalesOrdenCompra;
import utilerias.Numero_a_Letra;
import utilerias.Utilerias;
/**
 *
 * @author daap
 */
public class OrdenCompraReporte {

    public OrdenCompraReporte() {
    }
    
    public String generarReporte(ArrayList<OrdenCompraDetalle> orden, OrdenCompraEncabezado ordenEncabezado, TotalesOrdenCompra totalesOrdenesCompra, int control) throws JRException {
        String ruta = "C:\\Reportes\\rOrdenCompra" + ordenEncabezado.getIdCotizacion() + ".pdf";
        String ubicacionCompilado = "C:\\Reportes\\ordenCompra.jasper";
        JasperPrint jasperprint;
        JasperReport report;
        Map<String, Object> parametros = new HashMap<String, Object>();
// aqui es donde paso los parametros del encabezado lo que dice numeroOrden es el nombre del campo del ireport
        parametros.put("numeroOrden", ordenEncabezado.getIdOrdenCompra());
        parametros.put("proveedor", ordenEncabezado.getProveedor());
        parametros.put("emision", ordenEncabezado.getFechaCreacion());
        parametros.put("entrega", ordenEncabezado.getFechaEntrega());
        parametros.put("empresa", ordenEncabezado.getNombreComercial());
        parametros.put("comercial", ordenEncabezado.getDesctoComercial());
        parametros.put("prontoPago", ordenEncabezado.getDesctoProntoPago());
//        ------------------------------Totales---------------------------------
        parametros.put("subtoF", totalesOrdenesCompra.getSubtoF());
        parametros.put("sumaDescuentosGeneralesF", totalesOrdenesCompra.getSumaDescuentosGeneralesF());
        parametros.put("sumaDescuentosProductosF", totalesOrdenesCompra.getSumaDescuentsoProductosF());
        parametros.put("sumaDescuentosTotalesF", totalesOrdenesCompra.getSumaDescuentosTotalesF());
        parametros.put("subTotalBrutoF", totalesOrdenesCompra.getSubTotalBrutoF());
        parametros.put("impF", totalesOrdenesCompra.getImpF());
        parametros.put("totalF", totalesOrdenesCompra.getTotalF());
        Numero_a_Letra numeroALetra = new Numero_a_Letra();
        Utilerias utilerias = new Utilerias();
        String letras = utilerias.quitarSigno$(totalesOrdenesCompra.getTotalF());
        letras = numeroALetra.Convertir(letras.trim(), true);
        parametros.put("letras", letras);
        report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
        jasperprint = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(orden));
        try {
            if (control == 0) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.addHeader("Content-disposition", "attachment; filename=rOrdenCompra" + ordenEncabezado.getIdCotizacion() + ".pdf");
                ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
            } else {
                try {
                    JasperExportManager.exportReportToPdfFile(jasperprint, ruta);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception e) {
            System.out.println(e);
            
        }
          return ruta;
    }
}
