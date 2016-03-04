package traspasos;

import traspasos.dao.DAOTraspasos;
import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
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
import movimientos.dao.DAOMovimientos;
import movimientos.dao.DAOMovimientosAlmacen;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoProductoAlmacen;
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
import traspasos.dominio.Traspaso;
import traspasos.dominio.TraspasoProducto;
import traspasos.dominio.TraspasoProductoAlmacen;
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbTraspaso")
@SessionScoped
public class MbTraspaso implements Serializable {

    private boolean modoEdicion;
    private Traspaso traspaso;
    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private ArrayList<Traspaso> traspasos;
    private ArrayList<TraspasoProducto> detalle;
    private TraspasoProducto producto;
    private ArrayList<TraspasoProductoAlmacen> almacenDetalle, empaqueLotes;
    private TraspasoProductoAlmacen loteOrigen, loteDestino;
    private double cantTraspasar;
    private boolean solicitudes;
    private boolean pendientes;
    private Date fechaInicial;
    private boolean locked;
    DAOMovimientosAlmacen daoAlm;
    private DAOTraspasos dao;
    
    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;

    public MbTraspaso() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializa();
    }
    
    private TORechazoProductoAlmacen convertir(TOMovimientoProductoAlmacen to, int piezas) {
        TORechazoProductoAlmacen toProd = new TORechazoProductoAlmacen();
        toProd.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        toProd.setIdProducto(to.getIdProducto());
        toProd.setLote(to.getLote());
        toProd.setCantidad(to.getCantidad());
        toProd.setFechaCaducidad(to.getFechaCaducidad());
        toProd.setPiezas(piezas);
        return toProd;
    }

    private TraspasoProductoReporte convertirProductoReporte(TraspasoProducto prod) throws SQLException {
        boolean ya = false;
        TraspasoProductoReporte rep = new TraspasoProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setPiezas(prod.getProducto().getPiezas());
        rep.setUnitario(prod.getUnitario());
        for (TOMovimientoProductoAlmacen l : this.daoAlm.obtenerDetalleProducto(this.traspaso.getIdMovtoAlmacen(), prod.getProducto().getIdProducto())) {
            if (l.getCantidad() != 0) {
                if (ya) {
                    rep.getLotes().add(this.convertir(l, rep.getPiezas()));
                } else {
                    rep.setLote(l.getLote());
                    rep.setLoteCantidad(l.getCantidad());
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
            this.obtenDetalleTraspaso();
            this.daoAlm = new DAOMovimientosAlmacen();
            ArrayList<TraspasoProductoReporte> detalleReporte = new ArrayList<>();
            for (TraspasoProducto p : this.detalle) {
                if (p.getCantFacturada() != 0) {
                    detalleReporte.add(this.convertirProductoReporte(p));
                }
            }
            String sourceFileName = "C:\\Carlos Pat\\Reportes\\Traspaso.jasper";
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
            Map parameters = new HashMap();
            parameters.put("empresa", this.traspaso.getAlmacen().getEmpresa());

            parameters.put("cedis", this.traspaso.getAlmacen().getCedis());
            parameters.put("almacen", this.traspaso.getAlmacen().getAlmacen());

            parameters.put("concepto", "TRASPASO AL ALMACEN :");

            parameters.put("cedisOrigen", this.traspaso.getAlmacenDestino().getCedis());
            parameters.put("almacenOrigen", this.traspaso.getAlmacenDestino().getAlmacen());

            parameters.put("capturaFolio", this.traspaso.getFolio());
            parameters.put("capturaFecha", formatoFecha.format(this.traspaso.getFecha()));
            parameters.put("capturaHora", formatoHora.format(this.traspaso.getFecha()));

            parameters.put("idUsuario", this.traspaso.getIdUsuario());

//            try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Traspaso_" + this.traspaso.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
//            } catch (JRException e) {
//                Mensajes.mensajeError(e.getMessage());
//            } catch (IOException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//            }
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

    private TOTraspaso convertir(Traspaso traspaso) {
        return Traspasos.convertir(traspaso);
    }

    private double sumaPiezas() {
        double total = 0;
        for (TraspasoProducto prod : this.detalle) {
            total += prod.getCantFacturada();
        }
        return total;
    }

    public void grabar() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else if (this.sumaPiezas() != 0) {
                TOTraspaso to = this.convertir(this.traspaso);

                this.dao = new DAOTraspasos();
                this.dao.cerrar(to);
//                this.traspaso.setFolio(to.getFolio());
                this.traspaso.setFecha(to.getFecha());
                this.traspaso.setIdUsuario(to.getIdUsuario());
                this.traspaso.setPropietario(to.getPropietario());
                this.traspaso.setEstatus(to.getEstatus());
                this.setLocked(this.traspaso.getIdUsuario()==this.traspaso.getPropietario());
                Mensajes.mensajeSucces("El traspaso se grabo correctamente !!!");
            } else {
                Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

//    public TOTraspasoProducto convertir(TraspasoProducto prod) {
//        TOTraspasoProducto toProd = new TOTraspasoProducto();
//        toProd.setCantSolicitada(prod.getCantSolicitada());
//        toProd.setCantTraspasada(prod.getCantTraspasada());
//        movimientos.Movimientos.convertir(prod, toProd);
//        return toProd;
//    }

    public void gestionar() {
        if (this.producto.getCantFacturada() < 0) {
            this.producto.setCantFacturada(this.producto.getSeparados());
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
        } else if (this.producto.getCantFacturada() > this.producto.getCantSolicitada()-this.producto.getCantTraspasada()) {
            this.producto.setCantFacturada(this.producto.getSeparados());
            Mensajes.mensajeAlert("La cantidad traspasada no debe ser mayor que la solicitada !!!");
        } else if (this.producto.getCantFacturada() != this.producto.getSeparados()) {
            double cantFacturada = this.producto.getCantFacturada();
            this.producto.setCantFacturada(this.producto.getSeparados());
            try {
                DAOMovimientos daoMv = new DAOMovimientos();
                if (cantFacturada > this.producto.getSeparados()) {
                    double cantSolicitada = cantFacturada - this.producto.getSeparados();
                    double cantSeparada = daoMv.separar(this.traspaso.getIdMovto(), this.traspaso.getIdMovtoAlmacen(), this.traspaso.getAlmacen().getIdAlmacen(), this.producto.getProducto().getIdProducto(), cantSolicitada, false);
                    if (cantSeparada < cantSolicitada) {
                        cantFacturada = this.producto.getSeparados() + cantSeparada;
                        Mensajes.mensajeAlert("Solo se pudieron separar " + cantSeparada + " unidades !!!");
                    }
                } else {
                    double cantSolicitada = this.producto.getSeparados() - cantFacturada;
                    daoMv.liberar(this.traspaso.getIdMovto(), this.traspaso.getIdMovtoAlmacen(), this.traspaso.getAlmacen().getIdAlmacen(), this.producto.getProducto().getIdProducto(), cantSolicitada);
                }
                this.producto.setCantFacturada(cantFacturada);
                this.producto.setSeparados(cantFacturada);
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
            Mensajes.mensajeAlert("A ver que pasa !!!");
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOTraspasos();
            this.dao.cancelar(this.traspaso.getIdMovto(), this.traspaso.getIdMovtoAlmacen());
            this.traspaso.setPropietario(0);
            this.traspaso.setEstatus(6);
            this.setLocked(false);
            this.obtenTraspasos();
            this.modoEdicion = false;
            Mensajes.mensajeSucces("El traspaso se canceló con éxito !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void procesar() {
        TOTraspaso toMov = this.convertir(this.traspaso);
        try {
            this.dao = new DAOTraspasos();
            this.dao.procesar(toMov);
            this.traspaso.setIdMovto(toMov.getIdMovto());
            this.traspaso.setFecha(toMov.getFecha());
            this.traspaso.setIdUsuario(toMov.getIdUsuario());
            this.traspaso.setPropietario(toMov.getPropietario());
            this.traspaso.setEstatus(toMov.getEstatus());
            this.traspaso.setIdMovtoAlmacen(toMov.getIdMovtoAlmacen());
            this.obtenDetalleTraspaso();
            Mensajes.mensajeSucces("La solicitud fue aceptada !!!");
//            this.obtenTraspasos();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void rechazar() {
        try {
            this.dao = new DAOTraspasos();
            this.dao.rechazar(this.traspaso.getIdSolicitud());
            this.obtenSolicitudes();
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void salir() {
        if (this.traspaso.getIdMovto() != 0) {
            this.liberarTraspaso();
        } else {
            this.liberarSolicitud();
        }
        if(this.solicitudes) {
            this.obtenerSolicitudes();
        } else {
            this.obtenerTraspasos();
        }
        this.modoEdicion = false;
    }
    
    public void liberarSolicitud() {
        boolean ok = false;
        if (this.traspaso == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else {
            TOTraspaso toTraspaso = this.convertir(this.traspaso);
            try {
                this.dao = new DAOTraspasos();
                this.dao.liberarSolicitud(toTraspaso);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okTraspaso", ok);
    }
    
    public void liberarTraspaso() {
        boolean ok = false;
        if (this.traspaso == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else {
            TOTraspaso toTraspaso = this.convertir(this.traspaso);
            try {
                this.dao = new DAOTraspasos();
                this.dao.liberarTraspaso(toTraspaso);
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okTraspaso", ok);
    }

//    private TraspasoProducto convertir(TOTraspasoProducto toProd) throws SQLException {
//        TraspasoProducto prod = new TraspasoProducto();
//        prod.setCantSolicitada(toProd.getCantSolicitada());
//        prod.setCantTraspasada(toProd.getCantTraspasada());
//        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
//        movimientos.Movimientos.convertir(toProd, prod);
//        return prod;
//    }
//
    private void obtenDetalleTraspaso() {
        this.detalle = new ArrayList<>();
        TOTraspaso toTraspaso = this.convertir(this.traspaso);
        try {
            this.dao = new DAOTraspasos();
            for (TOTraspasoProducto to : this.dao.obtenerDetalleTraspaso(toTraspaso)) {
                this.detalle.add(Traspasos.convertir(to, this.mbBuscar.obtenerProducto(to.getIdProducto())));
            }
            this.traspaso.setIdUsuario(toTraspaso.getIdUsuario());
            this.traspaso.setPropietario(toTraspaso.getPropietario());
            this.traspaso.setEstatus(toTraspaso.getEstatus());
            this.setLocked(this.traspaso.getIdUsuario()==this.traspaso.getPropietario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private void obtenDetalleSolicitud() {
        this.detalle = new ArrayList<>();
        TOTraspaso toTraspaso = this.convertir(this.traspaso);
        try {
            this.dao = new DAOTraspasos();
            for (TOTraspasoProducto to : this.dao.obtenerDetalleSolicitud(toTraspaso)) {
                this.detalle.add(Traspasos.convertir(to, this.mbBuscar.obtenerProducto(to.getIdProducto())));
            }
            this.traspaso.setIdUsuario(toTraspaso.getIdUsuario());
            this.traspaso.setPropietario(toTraspaso.getPropietario());
            this.traspaso.setEstatus(toTraspaso.getEstatus());
            this.setLocked(this.traspaso.getIdUsuario()==this.traspaso.getPropietario());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void obtenerDetalle(SelectEvent event) {
        this.traspaso = (Traspaso) event.getObject();
        if (this.traspaso.getIdMovto() != 0) {
            this.obtenDetalleTraspaso();
        } else {
            this.obtenDetalleSolicitud();
        }
        this.producto = new TraspasoProducto();
        this.modoEdicion = true;
    }

    private void obtenTraspasos() throws NamingException, SQLException {
        this.traspasos = new ArrayList<>();
        this.dao = new DAOTraspasos();
        for (TOTraspaso to : this.dao.obtenerTraspasos(this.almacen.getIdAlmacen(), this.pendientes?0:5 , this.fechaInicial)) {
            traspasos.add(this.convertir(to));
        }
    }

    public void obtenerTraspasos() {
        try {
            this.obtenTraspasos();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Traspaso convertir(TOTraspaso toMov) {
        Traspaso mov = new Traspaso(new MovimientoTipo(35, "Traspaso"), this.almacen, this.mbAlmacenes.obtenerTOAlmacen(toMov.getIdReferencia()));
        Traspasos.convertir(toMov, mov);
        return mov;
    }

    private void obtenSolicitudes() throws NamingException, SQLException {
        this.traspasos = new ArrayList<>();
        this.dao = new DAOTraspasos();
        for (TOTraspaso to : this.dao.obtenerSolicitudes(this.almacen.getIdAlmacen())) {
            this.traspasos.add(this.convertir(to));
        }
    }

    public void obtenerSolicitudes() {
        try {
            if(this.isSolicitudes()) {
                this.obtenSolicitudes();
            } else {
                this.obtenTraspasos();
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializar();
        return "index.xhtml";
    }

    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        
        inicializaLocales();
    }

    private void inicializaLocales() {
        this.modoEdicion = false;
        this.solicitudes = true;
        this.pendientes = true;
        this.fechaInicial = new Date();
        this.traspasos = new ArrayList<>();
        this.setLocked(false);
    }

    public ArrayList<TraspasoProductoAlmacen> getAlmacenDetalle() {
        return almacenDetalle;
    }

    public void setAlmacenDetalle(ArrayList<TraspasoProductoAlmacen> almacenDetalle) {
        this.almacenDetalle = almacenDetalle;
    }

    public ArrayList<TraspasoProductoAlmacen> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<TraspasoProductoAlmacen> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
    }

    public TraspasoProductoAlmacen getLoteOrigen() {
        return loteOrigen;
    }

    public void setLoteOrigen(TraspasoProductoAlmacen loteOrigen) {
        this.loteOrigen = loteOrigen;
    }

    public TraspasoProductoAlmacen getLoteDestino() {
        return loteDestino;
    }

    public void setLoteDestino(TraspasoProductoAlmacen loteDestino) {
        this.loteDestino = loteDestino;
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<TraspasoProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<TraspasoProducto> detalle) {
        this.detalle = detalle;
    }

    public TraspasoProducto getProducto() {
        return producto;
    }

    public void setProducto(TraspasoProducto producto) {
        this.producto = producto;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(18);
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

    public MbMiniCedis getMbCedis() {
        return mbCedis;
    }

    public void setMbCedis(MbMiniCedis mbCedis) {
        this.mbCedis = mbCedis;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public boolean isSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(boolean solicitudes) {
        this.solicitudes = solicitudes;
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

    public Traspaso getTraspaso() {
        return traspaso;
    }

    public void setTraspaso(Traspaso traspaso) {
        this.traspaso = traspaso;
    }

    public ArrayList<Traspaso> getTraspasos() {
        return traspasos;
    }

    public void setTraspasos(ArrayList<Traspaso> traspasos) {
        this.traspasos = traspasos;
    }

//    public double getSumaLotes() {
//        return sumaLotes;
//    }
//
//    public void setSumaLotes(double sumaLotes) {
//        this.sumaLotes = sumaLotes;
//    }

    public TOAlmacenJS getAlmacen() {
        return almacen;
    }

    public void setAlmacen(TOAlmacenJS almacen) {
        this.almacen = almacen;
    }

    public ArrayList<SelectItem> getListaAlmacenes() {
        return listaAlmacenes;
    }

    public void setListaAlmacenes(ArrayList<SelectItem> listaAlmacenes) {
        this.listaAlmacenes = listaAlmacenes;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
