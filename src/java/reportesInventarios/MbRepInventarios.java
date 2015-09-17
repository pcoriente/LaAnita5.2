package reportesInventarios;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import producto2.MbProductosBuscar;
import reportesInventarios.dao.DAORepInventarios;
import reportesInventarios.dominio.ProductoKardex;
import reportesInventarios.to.TOProductoKardexDetalle;
import reportesInventarios.to.TOProductoKardex;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbRepInventarios")
@SessionScoped
public class MbRepInventarios implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private int idModulo;
    private Date fechaInicial;
    private Date fechaFinal;
    private ProductoKardex productoKardex;
    private DAORepInventarios dao;

    public MbRepInventarios() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializaLocales();
    }
    
    public void imprimirKardexAlmacenPdf() {
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.fechaInicial.after(this.fechaFinal)) {
            Mensajes.mensajeAlert("La fecha inicial no debe ser posterior a la fecha final !!!");
        } else if (this.productoKardex.getProducto() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un producto !!!");
        } else {
            ArrayList<TOProductoKardexDetalle> detalleReporte = new ArrayList<>();
            try {
                this.dao = new DAORepInventarios();
                detalleReporte = this.dao.obtenerDetalleKardexAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.productoKardex.getProducto().getIdProducto(), this.fechaInicial, this.fechaFinal);
                String sourceFileName = "C:\\Carlos Pat\\Reportes\\KardexAlmacen.jasper";
                JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
                Map parameters = new HashMap();
                parameters.put("empresa", this.mbAlmacenes.getToAlmacen().getEmpresa());

                parameters.put("cedis", this.mbAlmacenes.getToAlmacen().getCedis());
                parameters.put("almacen", this.mbAlmacenes.getToAlmacen().getAlmacen());
                
                parameters.put("fechaInicial", this.fechaInicial);
                parameters.put("fechaFinal", this.fechaFinal);

                parameters.put("sku", this.productoKardex.getProducto().getCod_pro());
                parameters.put("producto", this.productoKardex.getProducto().toString());

                parameters.put("existencia", this.productoKardex.getExiFinal());
                parameters.put("minimo", this.productoKardex.getMinimo());
                parameters.put("maximo", this.productoKardex.getMaximo());
                JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
                JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.setContentType("application/pdf");
                httpServletResponse.addHeader("Content-disposition", "attachment; filename=KardexAlmacen " + this.productoKardex.getProducto().getCod_pro() + ".pdf");
                ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
                FacesContext.getCurrentInstance().responseComplete();
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (JRException ex) {
                Logger.getLogger(MbRepInventarios.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MbRepInventarios.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void imprimirKardexOficinaPdf() {
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Debe seleccionar un almacen !!!");
        } else if (this.fechaInicial.after(this.fechaFinal)) {
            Mensajes.mensajeAlert("La fecha inicial no debe ser posterior a la fecha final !!!");
        } else if (this.productoKardex.getProducto() == null) {
            Mensajes.mensajeAlert("Debe seleccionar un producto !!!");
        } else {
            ArrayList<TOProductoKardexDetalle> detalleReporte = new ArrayList<>();
            try {
                this.dao = new DAORepInventarios();
                detalleReporte = this.dao.obtenerDetalleKardexOficina(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.productoKardex.getProducto().getIdProducto(), this.fechaInicial, this.fechaFinal);
                String sourceFileName = "C:\\Carlos Pat\\Reportes\\KardexOficina.jasper";
                JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
                Map parameters = new HashMap();
                parameters.put("empresa", this.mbAlmacenes.getToAlmacen().getEmpresa());

                parameters.put("cedis", this.mbAlmacenes.getToAlmacen().getCedis());
                parameters.put("almacen", this.mbAlmacenes.getToAlmacen().getAlmacen());
                
                parameters.put("fechaInicial", this.fechaInicial);
                parameters.put("fechaFinal", this.fechaFinal);

                parameters.put("sku", this.productoKardex.getProducto().getCod_pro());
                parameters.put("producto", this.productoKardex.getProducto().toString());

                parameters.put("existencia", this.productoKardex.getExiFinal());
                parameters.put("minimo", this.productoKardex.getMinimo());
                parameters.put("maximo", this.productoKardex.getMaximo());
                JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
                JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

                HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                httpServletResponse.setContentType("application/pdf");
                httpServletResponse.addHeader("Content-disposition", "attachment; filename=KardexOficina " + this.productoKardex.getProducto().getCod_pro() + ".pdf");
                ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
                FacesContext.getCurrentInstance().responseComplete();
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (JRException ex) {
                Logger.getLogger(MbRepInventarios.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MbRepInventarios.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ProductoKardex convertir(TOProductoKardex toProd) {
        ProductoKardex prod = new ProductoKardex();
        prod.setProducto(this.mbBuscar.getProducto());
        prod.setExiFinal(toProd.getExistencia());
        prod.setMinimo(toProd.getMinimo());
        prod.setMaximo(toProd.getMaximo());
        return prod;
    }

    private void actualizaProductoSeleccioado() {
        try {
            this.dao = new DAORepInventarios();
            this.productoKardex=this.convertir(this.dao.obtenerProductoKardexOficina(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.mbBuscar.getProducto().getIdProducto()));
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccioado();
        }
    }

    public void validarPeriodo() {
        if (this.fechaInicial.after(this.fechaFinal)) {
            Mensajes.mensajeAlert("La fecha inicial no puede ser posterior a la final !!!");
        }
    }

    public void salir() {
    }

    public String terminar() {
        this.acciones = null;
        return "index.xhtml";
    }

    private void inicializaLocales() {
        this.fechaInicial = new Date();
        this.fechaFinal = new Date();
    }

    public void inicializar() {
        this.mbBuscar.inicializar();
        this.inicializaLocales();
    }

    public ProductoKardex getProductoKardex() {
        return productoKardex;
    }

    public void setProductoKardex(ProductoKardex productoKardex) {
        this.productoKardex = productoKardex;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.idModulo = idModulo;
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
            this.inicializar();
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }
}
