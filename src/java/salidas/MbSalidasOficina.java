package salidas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.MovimientoOficinaProductoReporte;
import java.io.IOException;
import movimientos.to.TOMovimientoOficina;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOMovimientosOficina;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOProductoOficina;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbSalidasOficina")
@SessionScoped
public class MbSalidasOficina implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private ProductoOficina producto;
    private ArrayList<ProductoOficina> detalle;
    private MovimientoOficina salida;
    private ArrayList<MovimientoOficina> pendientes;
    private DAOMovimientosOficina dao;

    public MbSalidasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    private MovimientoOficinaProductoReporte convertirProductoReporte(ProductoOficina prod) {
        MovimientoOficinaProductoReporte rProd = new MovimientoOficinaProductoReporte();
        rProd.setEmpaque(prod.getProducto().toString());
        rProd.setSku(prod.getProducto().getCod_pro());
        rProd.setCantFacturada(prod.getCantFacturada());
        rProd.setUnitario(prod.getUnitario());
        return rProd;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<MovimientoOficinaProductoReporte> detalleReporte = new ArrayList<>();
        for (ProductoOficina p : this.detalle) {
            if (p.getCantFacturada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\MovimientoOficina.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.salida.getAlmacen().getEmpresa());

        parameters.put("cedis", this.salida.getAlmacen().getCedis());
        parameters.put("almacen", this.salida.getAlmacen().getAlmacen());

        parameters.put("concepto", this.salida.getTipo().getTipo());
        parameters.put("conceptoTipo", "SALIDA OFICINA CONCEPTOS VARIOS");

        parameters.put("capturaFolio", this.salida.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.salida.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.salida.getFecha()));

        parameters.put("idUsuario", this.salida.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=SalidaOficina_" + this.salida.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientosOficina();
            this.dao.cancelarMovimiento(this.salida.getIdMovto(), false);
            Mensajes.mensajeSucces("La cancelacion se realizo con exite !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void grabar() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (movimientos.Movimientos.sumaPiezasOficina(this.detalle) == 0) {
                Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
            } else {
                TOMovimientoOficina toMov = this.convertir(this.salida);

                this.dao = new DAOMovimientosOficina();
                this.dao.grabarDetalle(toMov, false);
                this.salida.setFolio(toMov.getFolio());
                this.salida.setFecha(toMov.getFecha());
                this.salida.setIdUsuario(toMov.getIdUsuario());
                this.salida.setEstatus(5);

                this.obtenDetalle(this.salida.getIdMovto());
                Mensajes.mensajeSucces("La salida se realizo con exito !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionar() {
        if (this.producto.getCantFacturada() < 0) {
            this.producto.setCantFacturada(this.producto.getSeparados());
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else if (this.producto.getCantFacturada() != this.producto.getSeparados()) {
            TOProductoOficina toProd = new TOProductoOficina();
            movimientos.Movimientos.convertir(this.producto, toProd);
            this.producto.setCantFacturada(this.producto.getSeparados());
            try {
                this.dao = new DAOMovimientosOficina();
                if (toProd.getCantFacturada() > this.producto.getSeparados()) {
                    double cantSolicitada = toProd.getCantFacturada() - this.producto.getSeparados();
                    double cantSeparada = this.dao.separar(this.salida.getAlmacen().getIdAlmacen(), toProd, this.producto.getSeparados());
                    if (cantSeparada < cantSolicitada) {
                        Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                    }
                } else {
                    this.dao.liberar(this.salida.getAlmacen().getIdAlmacen(), toProd, this.producto.getSeparados());
                }
                this.producto.setCantFacturada(toProd.getCantFacturada());
                this.producto.setSeparados(toProd.getCantFacturada());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.producto = this.detalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        ProductoOficina prod = new ProductoOficina(this.mbBuscar.getProducto());
        for (ProductoOficina p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            prod.setIdMovto(this.salida.getIdMovto());
            try {
                TOProductoOficina toProd = new TOProductoOficina();
                movimientos.Movimientos.convertir(prod, toProd);

                this.dao = new DAOMovimientosOficina();
                this.dao.agregarProducto(toProd, 0);

                this.detalle.add(prod);
                this.producto = prod;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void salir() {
        this.modoEdicion = false;
    }

    private ProductoOficina convertir(TOProductoOficina toProd) throws SQLException {
        ProductoOficina prod = new ProductoOficina(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

    private void obtenDetalle(int idMovto) {
        this.detalle = new ArrayList<>();
        try {
            this.dao = new DAOMovimientosOficina();
            for (TOProductoOficina to : this.dao.obtenerDetalle(idMovto)) {
                this.detalle.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerDetalle(SelectEvent event) {
        this.salida = ((MovimientoOficina) event.getObject());
        this.obtenDetalle(this.salida.getIdMovto());
        this.producto = new ProductoOficina();
        this.modoEdicion = true;
    }

    private MovimientoOficina convertir(TOMovimientoOficina toMov) throws SQLException {
        MovimientoOficina mov = new MovimientoOficina(this.tipo, this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void pendientes() {
        boolean ok = false;
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.pendientes = new ArrayList<>();
            try {
                this.dao = new DAOMovimientosOficina();
                for (TOMovimientoOficina to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.tipo.getIdTipo(), 0, new Date())) {
                    this.pendientes.add(this.convertir(to));
                }
                ok = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private TOMovimientoOficina convertir(MovimientoOficina mov) {
        TOMovimientoOficina toMov = new TOMovimientoOficina();
        movimientos.Movimientos.convertir(mov, toMov);
        return toMov;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.salida = new MovimientoOficina(this.tipo, this.mbAlmacenes.getToAlmacen());
            try {
                this.dao = new DAOMovimientosOficina();
                this.salida.setIdMovto(this.dao.agregarMovimiento(this.convertir(this.salida), false));
                this.detalle = new ArrayList<>();
                this.producto = new ProductoOficina();
                this.modoEdicion = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void obtenerTipos() {
        try {
            this.listaMovimientosTipos = new ArrayList<>();
            this.tipo = new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

            this.dao = new DAOMovimientosOficina();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(false)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.detalle = new ArrayList<>();
    }

    private void inicializa() {
        this.inicializar();
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(26);
        }
        return acciones;
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

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<SelectItem> getListaMovimientosTipos() {
        if (this.listaMovimientosTipos == null) {
            this.obtenerTipos();
        }
        return listaMovimientosTipos;
    }

    public void setListaMovimientosTipos(ArrayList<SelectItem> listaMovimientosTipos) {
        this.listaMovimientosTipos = listaMovimientosTipos;
    }

    public MovimientoTipo getTipo() {
        return tipo;
    }

    public void setTipo(MovimientoTipo tipo) {
        this.tipo = tipo;
    }

    public ProductoOficina getProducto() {
        return producto;
    }

    public void setProducto(ProductoOficina producto) {
        this.producto = producto;
    }

    public ArrayList<ProductoOficina> getSalidaDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoOficina> detalle) {
        this.detalle = detalle;
    }

    public ArrayList<MovimientoOficina> getPendientes() {
        return pendientes;
    }

    public void setPendientes(ArrayList<MovimientoOficina> pendientes) {
        this.pendientes = pendientes;
    }

    public MovimientoOficina getSalida() {
        return salida;
    }

    public void setSalida(MovimientoOficina salida) {
        this.salida = salida;
    }
}
