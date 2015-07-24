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
        String ruta = "C:\\Carlos Pat\\Reportes\\rOrdenCompra" + ordenEncabezado.getIdOrdenCompra() + ".pdf";
        String ubicacionCompilado = "";
        if (ordenEncabezado.getEstado() == 0) {
            ubicacionCompilado = "C:\\Carlos Pat\\Reportes\\ordenCompraCancelado.jasper";
        } else {
            ubicacionCompilado = "C:\\Carlos Pat\\Reportes\\ordenCompra.jasper";
        }
        JasperPrint jasperprint;
        JasperReport report;
        Map<String, Object> parametros = new HashMap<>();
// aqui es donde paso los parametros del encabezado lo que dice numeroOrden es el nombre del campo del ireport
//        parametros.put("numeroOrden", ordenEncabezado.getIdOrdenCompra());
//        parametros.put("proveedor", ordenEncabezado.getProveedor());
//        parametros.put("emision", ordenEncabezado.getFechaCreacion());
//        parametros.put("entrega", ordenEncabezado.getFechaEntrega());
//        parametros.put("empresa", ordenEncabezado.getEmpresa().getEmpresa());
//        parametros.put("comercial", ordenEncabezado.getDesctoComercial());
//        parametros.put("prontoPago", ordenEncabezado.getDesctoProntoPago());
        parametros.put("ordenEncabezado", ordenEncabezado);
        //PARAMETROS AÑADIDOS PARA REPORTE 3/JULIO/2015
        // parametros.put("direccionProveedor", ordenEncabezado.getProveedor().getDireccionFiscal().toString2().toUpperCase());
        //   parametros.put("direccionEntregaProveedor", ordenEncabezado.getProveedor().getDireccionEntrega().toString2().toUpperCase());
        // parametros.put("proveedorRFC", ordenEncabezado.getProveedor().getContribuyente().getRfc());
        //    parametros.put("limiteCredito",ordenEncabezado.getProveedor().getLimiteCredito());
        // parametros.put("codigoProveedor",ordenEncabezado.getProveedor().getCodigoProveedor());
        // parametros.put("proveedorMunicipio",ordenEncabezado.getProveedor().getDireccionFiscal().getMunicipio());
        // parametros.put("proveedorEstado",ordenEncabezado.getProveedor().getDireccionFiscal().getEstado());

//        ------------------------------Totales---------------------------------;
//        parametros.put("subtoF", totalesOrdenesCompra.getSubtoF());
//        parametros.put("sumaDescuentosGeneralesF", totalesOrdenesCompra.getSumaDescuentosGeneralesF());
//        parametros.put("sumaDescuentosProductosF", totalesOrdenesCompra.getSumaDescuentosProductosF());
//        parametros.put("sumaDescuentosTotalesF", totalesOrdenesCompra.getSumaDescuentosTotalesF());
//        parametros.put("subTotalBrutoF", totalesOrdenesCompra.getSubTotalBrutoF());
//        parametros.put("impF", totalesOrdenesCompra.getImpF());
//        parametros.put("totalF", totalesOrdenesCompra.getTotalF());
        parametros.put("totalesOrdenesCompra", totalesOrdenesCompra);
        Numero_a_Letra numeroALetra = new Numero_a_Letra();
        Utilerias utilerias = new Utilerias();
        String letras = utilerias.quitarSigno$(totalesOrdenesCompra.getTotalF());
        letras = utilerias.quitarComas(letras);
        letras = Double.toString(Double.parseDouble(letras));
        letras = numeroALetra.Convertir(letras.trim(), true);

        parametros.put("letras", letras);
        report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
        jasperprint = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(orden));
        try {
            if (control == 0) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.addHeader("Content-disposition", "attachment; filename=rOrdenCompra" + ordenEncabezado.getIdOrdenCompra() + ".pdf");
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
