package salidas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import entradas.dominio.MovimientoOficinaProductoReporte;
import java.io.IOException;
import movimientos.to.TOMovimiento;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import movimientos.dao.DAOLotes;
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.MovimientoTipo;
import movimientos.to.TOMovimientoProducto;
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
//    @ManagedProperty(value = "#{mbComprobantes}")
//    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private ArrayList<SelectItem> listaMovimientosTipos;
    private MovimientoTipo tipo;
    private SalidaOficinaProducto salidaProducto;
    private ArrayList<SalidaOficinaProducto> salidaDetalle;
    private Salida salida;
    private ArrayList<Salida> salidasPendientes;
    private DAOMovimientos dao;
    private DAOLotes daoLotes;

    public MbSalidasOficina() throws NamingException {
        this.mbAcciones = new MbAcciones();
//        this.mbComprobantes = new MbComprobantes();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }
    
    private MovimientoOficinaProductoReporte convertirProductoReporte(SalidaOficinaProducto prod) {
        MovimientoOficinaProductoReporte rep = new MovimientoOficinaProductoReporte();
        rep.setEmpaque(prod.getProducto().toString());
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setCantFacturada(prod.getCantFacturada());
        rep.setUnitario(prod.getUnitario());
        return rep;
    }
    
    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<MovimientoOficinaProductoReporte> detalleReporte = new ArrayList<>();
        for (SalidaOficinaProducto p : this.salidaDetalle) {
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
    
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        this.salidaProducto = this.salidaDetalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            oldValue=newValue;
        } else {
            newValue=oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    public void cancelar() {
        try {
            this.dao = new DAOMovimientos();
            this.dao.cancelarSalidaOficina(this.salida.getIdMovto());
            Mensajes.mensajeSucces("La cancelacion se realizo con exite !!!");
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    private void obtenerDetalle() throws NamingException, SQLException {
        this.salidaDetalle = new ArrayList<>();
        this.dao = new DAOMovimientos();
        for (TOMovimientoProducto to : this.dao.obtenerDetalle(this.salida.getIdMovto())) {
            this.salidaDetalle.add(this.convertirProductoOficina(to));
        }
    }

    public void cargaDetalleSalida(SelectEvent event) {
        this.salida = ((Salida) event.getObject());
        this.tipo = this.salida.getTipo();
        try {
            this.obtenerDetalle();
            this.salidaProducto = new SalidaOficinaProducto();
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private SalidaOficinaProducto convertirProductoOficina(TOMovimientoProducto to) throws SQLException {
        SalidaOficinaProducto p = new SalidaOficinaProducto();
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantFacturada(to.getCantFacturada());
        p.setSeparados(to.getCantFacturada());
        p.setUnitario(to.getUnitario());
        return p;
    }

    public void pendientes() {
        boolean ok = false;
        this.salidasPendientes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for (TOMovimiento to : this.dao.obtenerMovimientos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), this.tipo.getIdTipo(), 0)) {
                this.salidasPendientes.add(this.convertir(to));
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("ok", ok);
    }

    private Salida convertir(TOMovimiento to) throws SQLException {
        Salida s = new Salida();
        s.setIdMovto(to.getIdMovto());
        s.setAlmacen(this.mbAlmacenes.obtenerTOAlmacen(to.getIdAlmacen()));
        s.setTipo(this.dao.obtenerMovimientoTipo(to.getIdTipo()));
        s.setFecha(to.getFecha());
        s.setIdUsuario(to.getIdUsuario());
        s.setEstatus(to.getEstatus());
        return s;
    }

    public void grabar() {
        try {
            if(this.salidaDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (SalidaOficinaProducto e : this.salidaDetalle) {
                    total += e.getCantFacturada();
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOMovimiento to = this.convertirTO();
                    this.dao.grabarSalidaOficina(to);
                    this.salida.setIdUsuario(to.getIdUsuario());
                    this.salida.setFolio(to.getFolio());
                    this.salida.setEstatus(1);
                    this.obtenerDetalle();
                    Mensajes.mensajeSucces("La salida se realizo con exito !!!");
                } else {
                    Mensajes.mensajeAlert("No hay unidades en el movimiento !!!");
                }
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void gestionar() {
        double separados;
        try {
            this.daoLotes = new DAOLotes();
            double separar = this.salidaProducto.getCantFacturada() - this.salidaProducto.getSeparados();
            if (separar > 0) {
                separados = this.daoLotes.separarOficina(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), separar, false);
                if (separados < separar) {
                    Mensajes.mensajeAlert("No se pudieron obtener la cantidad solicitada");
                }
            } else {
                this.daoLotes.liberarOficina(this.salida.getIdMovto(), this.salida.getAlmacen().getIdAlmacen(), this.salidaProducto.getProducto().getIdProducto(), -separar);
                separados = separar;
            }
            this.salidaProducto.setSeparados(this.salidaProducto.getSeparados() + separados);
            this.salidaProducto.setCantFacturada(this.salidaProducto.getSeparados());
        } catch (SQLException ex) {
            this.salidaProducto.setCantFacturada(this.salidaProducto.getSeparados());
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            this.salidaProducto.setCantFacturada(this.salidaProducto.getSeparados());
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        SalidaOficinaProducto productoSeleccionado = new SalidaOficinaProducto(this.mbBuscar.getProducto());
        for (SalidaOficinaProducto p : this.salidaDetalle) {
            if (p.equals(productoSeleccionado)) {
                this.salidaProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            try {
                this.dao = new DAOMovimientos();
                this.dao.agregarProductoSalidaOficina(this.salida.getIdMovto(), this.convertirTOProducto(productoSeleccionado));
                this.salidaDetalle.add(productoSeleccionado);
                this.salidaProducto = productoSeleccionado;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimientoProducto convertirTOProducto(SalidaOficinaProducto p) {
        TOMovimientoProducto to = new TOMovimientoProducto();
        to.setIdProducto(p.getProducto().getIdProducto());
        to.setCantFacturada(p.getCantFacturada());
        return to;
    }

    public boolean comparaProducto(SalidaOficinaProducto p) {
        boolean disable = true;
        if (this.salidaProducto.getProducto().getIdProducto() == p.getProducto().getIdProducto()) {
            disable = false;
        }
        return disable;
    }

    public void salir() {
//        this.inicializar();
        this.modoEdicion = false;
    }

    public void capturar() {
        if (this.tipo.getIdTipo() == 0) {
            Mensajes.mensajeAlert("Se requiere seleccionar un concepto");
        } else {
            this.salida = new Salida();
            this.salida.setAlmacen(this.mbAlmacenes.getToAlmacen());
            this.salida.setTipo(this.tipo);
            try {
                this.dao = new DAOMovimientos();
                this.salida.setIdMovto(this.dao.agregarMovimientoOficina(this.convertirTO()));
                this.salidaDetalle = new ArrayList<>();
                this.salidaProducto = new SalidaOficinaProducto();
                this.modoEdicion = true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
    }

    private TOMovimiento convertirTO() {
        TOMovimiento to = new TOMovimiento();
        to.setIdMovto(this.salida.getIdMovto());
        to.setIdTipo(this.salida.getTipo().getIdTipo());
        to.setFolio(this.salida.getFolio());
        to.setIdCedis(this.salida.getAlmacen().getIdCedis());
        to.setIdEmpresa(this.salida.getAlmacen().getIdEmpresa());
        to.setIdAlmacen(this.salida.getAlmacen().getIdAlmacen());
        to.setFecha(this.salida.getFecha());
        to.setIdUsuario(this.salida.getIdUsuario());
        return to;
    }

    public String terminar() {
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    private void obtenerTipos() {
        try {
            this.listaMovimientosTipos = new ArrayList<>();
            this.tipo = new MovimientoTipo(0, "Seleccione");
            this.listaMovimientosTipos.add(new SelectItem(this.tipo, this.tipo.toString()));

            this.dao = new DAOMovimientos();
            for (MovimientoTipo t : this.dao.obtenerMovimientosTipos(false)) {
                this.listaMovimientosTipos.add(new SelectItem(t, t.toString()));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private void inicializa() {
        this.inicializar();
    }

    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
//        this.mbComprobantes.getMbAlmacenes().getMbCedis().obtenerDefaultCedis();
//        this.mbComprobantes.getMbAlmacenes().cargaAlmacenes();
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
        this.listaMovimientosTipos = null;
        this.salidaDetalle = new ArrayList<>();
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

//    public MbComprobantes getMbComprobantes() {
//        return mbComprobantes;
//    }
//
//    public void setMbComprobantes(MbComprobantes mbComprobantes) {
//        this.mbComprobantes = mbComprobantes;
//    }
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

    public SalidaOficinaProducto getSalidaProducto() {
        return salidaProducto;
    }

    public void setSalidaProducto(SalidaOficinaProducto salidaProducto) {
        this.salidaProducto = salidaProducto;
    }

    public ArrayList<SalidaOficinaProducto> getSalidaDetalle() {
        return salidaDetalle;
    }

    public void setSalidaDetalle(ArrayList<SalidaOficinaProducto> salidaDetalle) {
        this.salidaDetalle = salidaDetalle;
    }

    public ArrayList<Salida> getSalidasPendientes() {
        return salidasPendientes;
    }

    public void setSalidasPendientes(ArrayList<Salida> salidasPendientes) {
        this.salidasPendientes = salidasPendientes;
    }

    public Salida getSalida() {
        return salida;
    }

    public void setSalida(Salida salida) {
        this.salida = salida;
    }
}
