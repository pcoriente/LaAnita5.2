package mvEntradas;

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
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbEntradasOficina")
@SessionScoped
public class MbEntradasOficina implements Serializable {

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
    private ArrayList<ProductoOficina> detalle;
    private ProductoOficina producto;
    private ArrayList<MovimientoOficina> entradas;
    private MovimientoOficina entrada;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private DAOMovimientosOficina dao;

    public MbEntradasOficina() throws NamingException {
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
        parameters.put("empresa", this.entrada.getAlmacen().getEmpresa());

        parameters.put("cedis", this.entrada.getAlmacen().getCedis());
        parameters.put("almacen", this.entrada.getAlmacen().getAlmacen());

        parameters.put("concepto", this.entrada.getTipo().getTipo());
        parameters.put("conceptoTipo", "ENTRADA OFICINA CONCEPTOS VARIOS");

        parameters.put("capturaFolio", this.entrada.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.entrada.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.entrada.getFecha()));

        parameters.put("idUsuario", this.entrada.getIdUsuario());
        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=EntradaOficina_" + this.entrada.getFolio() + ".pdf");
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
            this.dao.cancelarMovimiento(this.entrada.getIdMovto(), true);
            this.obtenerEntradas();
            this.entrada = new MovimientoOficina();
            this.modoEdicion = false;
            Mensajes.mensajeError("La cancelacion de la entrada se realizo con exito !!!");
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
                TOMovimientoOficina toMov = this.convertir(this.entrada);

                this.detalle = new ArrayList<>();
                this.dao = new DAOMovimientosOficina();
                for (TOProductoOficina toProd : this.dao.grabarDetalle(toMov, true)) {
                    this.detalle.add(this.convertir(toProd));
                }
                this.entrada.setFolio(toMov.getFolio());
                this.entrada.setFecha(toMov.getFecha());
                this.entrada.setEstatus(toMov.getEstatus());
                Mensajes.mensajeSucces("La entrada se grabo correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionar() {
        try {
            TOProductoOficina toProd = new TOProductoOficina();
            movimientos.Movimientos.convertir(this.producto, toProd);

            this.dao = new DAOMovimientosOficina();
            this.dao.grabarProductoCantidad(toProd);

            this.producto.setSeparados(this.producto.getCantFacturada());
        } catch (SQLException ex) {
            this.producto.setCantFacturada(this.producto.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.producto.setCantFacturada(this.producto.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
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
            prod.setIdMovto(this.entrada.getIdMovto());
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
        try {
            this.dao = new DAOMovimientosOficina();
            if (this.isLocked()) {
                this.dao.liberarMovimiento(this.entrada.getIdMovto());
            }
            this.entradas = new ArrayList<>();
            for (TOMovimientoOficina to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), this.pendientes ? 0 : 7, this.fechaInicial)) {
                this.entradas.add(this.convertir(to));
            }
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private ProductoOficina convertir(TOProductoOficina toProd) throws SQLException {
        ProductoOficina prod = new ProductoOficina(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

//    private void obtenDetalle() throws NamingException, SQLException {
//        TOMovimientoOficina toMov = this.convertir(this.entrada);
//
//        this.detalle = new ArrayList<>();
//        this.dao = new DAOMovimientosOficina();
//        for (TOProductoOficina to : this.dao.obtenerDetalle(toMov)) {
//            this.detalle.add(this.convertir(to));
//        }
//        this.entrada.setIdUsuario(toMov.getIdUsuario());
//        this.entrada.setPropietario(toMov.getPropietario());
//        this.entrada.setEstatus(toMov.getEstatus());
//        this.setLocked(this.entrada.getIdUsuario() == this.entrada.getPropietario());
//    }
    public void obtenerDetalle(SelectEvent event) {
        this.entrada = ((MovimientoOficina) event.getObject());
        TOMovimientoOficina toMov = this.convertir(this.entrada);
        try {
            this.detalle = new ArrayList<>();
            this.dao = new DAOMovimientosOficina();
            for (TOProductoOficina to : this.dao.obtenerDetalle(toMov)) {
                this.detalle.add(this.convertir(to));
            }
            this.entrada.setIdUsuario(toMov.getIdUsuario());
            this.entrada.setPropietario(toMov.getPropietario());
            this.entrada.setEstatus(toMov.getEstatus());
            this.setLocked(this.entrada.getIdUsuario() == this.entrada.getPropietario());
            this.producto = new ProductoOficina();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private MovimientoOficina convertir(TOMovimientoOficina toMov) throws SQLException {
        MovimientoOficina mov = new MovimientoOficina(this.getTipo(), this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdAlmacen()));
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void obtenerEntradas() {
        if (this.tipo.getIdTipo() != 0 && this.mbAlmacenes.getToAlmacen().getIdAlmacen() != 0) {
            this.entradas = new ArrayList<>();
            try {
                this.dao = new DAOMovimientosOficina();
                for (TOMovimientoOficina to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), this.pendientes ? 0 : 7, this.fechaInicial)) {
                    this.entradas.add(this.convertir(to));
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimientoOficina convertir(MovimientoOficina mov) {
        TOMovimientoOficina toMov = new TOMovimientoOficina(mov.getTipo().getIdTipo());
        movimientos.Movimientos.convertir(mov, toMov);
        return toMov;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.entrada = new MovimientoOficina(this.tipo, this.mbAlmacenes.getToAlmacen());
            TOMovimientoOficina toMov = this.convertir(this.entrada);
            try {
                this.dao = new DAOMovimientosOficina();
                this.dao.agregarMovimiento(toMov, false);
                this.entrada.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
                this.entrada.setIdUsuario(toMov.getIdUsuario());
                this.entrada.setPropietario(toMov.getPropietario());
                this.setLocked(this.entrada.getIdUsuario() == this.entrada.getPropietario());
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
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(true)) {
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
        this.pendientes = true;
        this.fechaInicial = new Date();
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
            this.acciones = this.mbAcciones.obtenerAcciones(28);
        }
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

    public boolean isPendientes() {
        return pendientes;
    }

    public void setPendientes(boolean pendientes) {
        this.pendientes = pendientes;
    }

    public Date getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public ArrayList<MovimientoOficina> getEntradas() {
        return entradas;
    }

    public void setEntradas(ArrayList<MovimientoOficina> entradas) {
        this.entradas = entradas;
    }

    public ArrayList<ProductoOficina> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoOficina> detalle) {
        this.detalle = detalle;
    }

    public ProductoOficina getProducto() {
        return producto;
    }

    public void setProducto(ProductoOficina producto) {
        this.producto = producto;
    }

    public MovimientoOficina getEntrada() {
        return entrada;
    }

    public void setEntrada(MovimientoOficina entrada) {
        this.entrada = entrada;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
