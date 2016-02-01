package mvEntradas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import traspasos.dominio.TraspasoProductoReporte;
import java.io.IOException;
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
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.dominio.MovimientoOficina;
import movimientos.dominio.MovimientoTipo;
import movimientos.dominio.ProductoOficina;
import movimientos.to.TOMovimientoOficina;
import movimientos.to.TOMovimientoProductoAlmacen;
import movimientos.to.TOProductoOficina;
import mvEntradas.dao.DAOEntradasProduccion;
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
import rechazos.to.TORechazoProductoAlmacen;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbEntradasProduccion")
@SessionScoped
public class MbEntradasProduccion implements Serializable {

    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Accion> acciones;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private boolean modoEdicion;
    private MovimientoOficina entrada;
    private ArrayList<MovimientoOficina> entradas;
    private ProductoOficina producto;
    private ArrayList<ProductoOficina> detalle;
    private TOMovimientoProductoAlmacen lote;
    private ArrayList<TOMovimientoProductoAlmacen> empaqueLotes;
    private Object oldValue, newValue;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    private DAOEntradasProduccion dao;

    public MbEntradasProduccion() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();

        this.listaMovimientosTipos = new ArrayList<>();
        this.tipo = new MovimientoTipo(0, "Selecciona un concepto");
        this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));
        this.tipo = new MovimientoTipo(3, "Entrada de Producto");
        this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));
        this.tipo = new MovimientoTipo(18, "Entrada de Semiterminado");
        this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

        this.inicializa();
    }

    private TORechazoProductoAlmacen convertir(TOMovimientoProductoAlmacen to) {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        toProd.setIdProducto(to.getIdProducto());
        toProd.setLote(to.getLote());
        toProd.setCantidad(to.getCantidad());
        toProd.setFechaCaducidad(to.getFechaCaducidad());
        return toProd;
    }

    private TraspasoProductoReporte convertirProductoReporte(ProductoOficina prod) throws SQLException {
        boolean ya = false;
        TraspasoProductoReporte rep = new TraspasoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setPiezas(prod.getProducto().getPiezas());
        rep.setUnitario(prod.getUnitario());
        for (TOMovimientoProductoAlmacen l : this.dao.obtenerProductoDetalle(this.entrada.getIdMovtoAlmacen(), prod.getProducto().getIdProducto())) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(this.convertir(l));
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
                    rep.setPiezas(rep.getPiezas());
                    ya = true;
                }
            }
        }
        return rep;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        try {
            this.dao = new DAOEntradasProduccion();
            ArrayList<TraspasoProductoReporte> detalleReporte = new ArrayList<>();
            for (ProductoOficina p : this.detalle) {
                if (p.getCantFacturada() != 0) {
                    detalleReporte.add(this.convertirProductoReporte(p));
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\produccion.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.entrada.getAlmacen().getEmpresa());

            parameters.put("cedis", this.entrada.getAlmacen().getCedis());
            parameters.put("almacen", this.entrada.getAlmacen().getAlmacen());

            parameters.put("concepto", this.entrada.getTipo().getTipo());

            parameters.put("folio", this.entrada.getFolio());
            parameters.put("fecha", this.entrada.getFecha());

            parameters.put("idUsuario", this.entrada.getIdUsuario());

            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=EntradaProduccion_" + this.entrada.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
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
                this.dao = new DAOEntradasProduccion();
                for (TOProductoOficina toProd : this.dao.grabar(toMov)) {
                    this.detalle.add(this.convertir(toProd));
                }
                this.entrada.setFolio(toMov.getFolio());
                this.entrada.setFecha(toMov.getFecha());
                this.entrada.setEstatus(toMov.getEstatus());
                Mensajes.mensajeSucces("La entrada se grabó correctamente !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOEntradasProduccion();
            this.dao.cancelarMovimiento(this.entrada.getIdMovto(), this.entrada.getIdMovtoAlmacen());
            this.entradas.remove(this.entrada);
            this.entrada = null;
            this.modoEdicion = false;
            Mensajes.mensajeSucces("La cancelacion se realizo con exito !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void eliminarProducto() {
        try {
            this.dao = new DAOEntradasProduccion();
            this.dao.eliminarProducto(this.entrada.getIdMovto(), this.entrada.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
            this.detalle.remove(this.producto);
            this.producto = null;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionarLotes() {
        boolean okLotes = false;
        TOMovimientoProductoAlmacen toLote = new TOMovimientoProductoAlmacen();
        toLote.setIdMovtoAlmacen(this.lote.getIdMovtoAlmacen());
        toLote.setIdProducto(this.lote.getIdProducto());
        toLote.setLote(this.lote.getLote());
        toLote.setCantidad(this.lote.getCantidad());
        toLote.setSeparados(this.lote.getSeparados());

        this.lote.setCantidad(this.lote.getSeparados());
        if (toLote.getCantidad() < 0) {
            Mensajes.mensajeAlert("La cantidad no puede ser menor que cero");
        } else {
            TOProductoOficina toProd = new TOProductoOficina();
            movimientos.Movimientos.convertir(this.producto, toProd);
            try {
                this.dao = new DAOEntradasProduccion();
                this.dao.grabarProductoCantidad(toProd, toLote);
                this.lote.setCantidad(toLote.getCantidad());
                this.lote.setSeparados(this.lote.getCantidad());
                this.producto.setCantFacturada(this.producto.getCantFacturada() - toLote.getSeparados() + toLote.getCantidad());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", okLotes);
    }

    public void onCellEdit(CellEditEvent event) {
        this.oldValue = event.getOldValue();
        this.newValue = event.getNewValue();
        if (newValue == null) {
            oldValue = newValue;
            Mensajes.mensajeError("El valor a capturar no debe ser nulo !!!");
        } else {
            this.lote = this.empaqueLotes.get(event.getRowIndex());
        }
    }

    public void agregarLote() {
        boolean ok = false;
        ArrayList<String> turnos = new ArrayList<>();
        turnos.add("1");
        turnos.add("2");
        turnos.add("3");
        turnos.add("4");
        try {
            DAOMovimientosAlmacen daoAlm = new DAOMovimientosAlmacen();
            if (this.lote.getLote().length() < 5) {
                Mensajes.mensajeAlert("La longitud de un lote no puede ser menor a 5 !!!");
            } else if (turnos.indexOf(this.lote.getLote().substring(4, 5)) == -1) {
                Mensajes.mensajeAlert("Turno incorrecto. Debe ser (1, 2, 3, 4) !!!");
            } else if (!daoAlm.validaLote(this.lote)) {
                Mensajes.mensajeAlert("Lote no valido !!!");
            } else if (this.empaqueLotes.indexOf(this.lote) == -1) {
                daoAlm.agregarProducto(this.lote);
                this.empaqueLotes.add(this.lote);
                ok = true;
            } else {
                Mensajes.mensajeAlert("El lote ya se encuetra en el producto !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
    }

    public void nuevoLote() {
        this.lote = new TOMovimientoProductoAlmacen(this.entrada.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
    }

//    public void obtenerDetalleProducto(SelectEvent event) {
    public void obtenerDetalleProducto() {
        boolean ok = false;
//        this.producto = ((ProductoOficina) event.getObject());
        try {
            this.dao = new DAOEntradasProduccion();
            this.empaqueLotes = this.dao.obtenerProductoDetalle(this.entrada.getIdMovtoAlmacen(), this.producto.getProducto().getIdProducto());
            this.lote = null;
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLotes", ok);
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

                this.dao = new DAOEntradasProduccion();
                this.dao.agregarProducto(toProd);

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
            this.dao = new DAOEntradasProduccion();
            if (this.isLocked()) {
                this.dao.liberarEntrada(this.entrada.getIdMovto(), this.entrada.getIdMovtoAlmacen());
            }
            this.entradas = new ArrayList<>();
            for (TOMovimientoOficina to : this.dao.obtenerEntradas(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), this.pendientes ? 0 : 7, this.fechaInicial)) {
                this.entradas.add(this.convertir(to));
            }
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private ProductoOficina convertir(TOProductoOficina toProd) {
        ProductoOficina prod = new ProductoOficina(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.entrada = ((MovimientoOficina) event.getObject());
        TOMovimientoOficina toMov = this.convertir(this.entrada);
        try {
            this.detalle = new ArrayList<>();
            this.dao = new DAOEntradasProduccion();
            for (TOProductoOficina to : this.dao.obtenerDetalle(toMov)) {
                this.detalle.add(this.convertir(to));
            }
            this.entrada.setIdUsuario(toMov.getIdUsuario());
            this.entrada.setPropietario(toMov.getPropietario());
            this.entrada.setEstatus(toMov.getEstatus());
            this.setLocked(this.entrada.getIdUsuario() == this.entrada.getPropietario());
            this.producto = null;
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private MovimientoOficina convertir(TOMovimientoOficina toMov) {
        MovimientoOficina mov = new MovimientoOficina(this.tipo, this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(toMov, mov);
        return mov;
    }

    public void obtenerEntradas() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.entradas = new ArrayList<>();
            try {
                this.dao = new DAOEntradasProduccion();
                for (TOMovimientoOficina to : this.dao.obtenerEntradas(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.getTipo().getIdTipo(), this.pendientes ? 0 : 7, this.fechaInicial)) {
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

    public void crearEntrada() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un almacen !!!");
        } else {
            this.entrada = new MovimientoOficina(this.tipo, this.mbAlmacenes.getToAlmacen());
            TOMovimientoOficina toMov = this.convertir(this.entrada);
            try {
                this.dao = new DAOEntradasProduccion();
                this.dao.crearEntrada(toMov);
                this.entrada.setIdMovto(toMov.getIdMovto());
                this.entrada.setFecha(toMov.getFecha());
                this.entrada.setIdUsuario(toMov.getIdUsuario());
                this.entrada.setPropietario(toMov.getPropietario());
                this.entrada.setEstatus(toMov.getEstatus());
                this.entrada.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
                this.setLocked(this.entrada.getIdUsuario()==this.entrada.getPropietario());
                this.detalle = new ArrayList<>();
                this.producto = null;
                this.modoEdicion = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private void inicializaLocales() {
        this.tipo = new MovimientoTipo(0, "Selecciona un concepto");
        this.modoEdicion = false;
        this.producto = null;
        this.detalle = new ArrayList<>();
        this.pendientes = true;
        this.fechaInicial = new Date();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbBuscar.inicializar();

        this.inicializaLocales();
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

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(27);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public ArrayList<SelectItem> getListaMovimientosTipos() {
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

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public MovimientoOficina getEntrada() {
        return entrada;
    }

    public void setEntrada(MovimientoOficina entrada) {
        this.entrada = entrada;
    }

    public ArrayList<MovimientoOficina> getEntradas() {
        return entradas;
    }

    public void setEntradas(ArrayList<MovimientoOficina> entradas) {
        this.entradas = entradas;
    }

    public ProductoOficina getProducto() {
        return producto;
    }

    public void setProducto(ProductoOficina producto) {
        this.producto = producto;
    }

    public ArrayList<ProductoOficina> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<ProductoOficina> detalle) {
        this.detalle = detalle;
    }

    public TOMovimientoProductoAlmacen getLote() {
        return lote;
    }

    public void setLote(TOMovimientoProductoAlmacen lote) {
        this.lote = lote;
    }

    public ArrayList<TOMovimientoProductoAlmacen> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<TOMovimientoProductoAlmacen> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
