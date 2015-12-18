package solicitudes;

import solicitudes.dao.DAOSolicitudes;
import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import cedis.dominio.MiniCedis;
import entradas.dominio.MovimientoOficinaProductoReporte;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import traspasos.dominio.TraspasoProducto;
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
import solicitudes.dominio.Solicitud;
import solicitudes.dominio.SolicitudProducto;
import solicitudes.to.TOSolicitud;
import solicitudes.to.TOSolicitudProducto;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbSolicitud")
@SessionScoped
public class MbSolicitud implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniCedis}")
    private MbMiniCedis mbCedis;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private String pendientes;
    private boolean modoEdicion;
    private MiniCedis cedis;
    private TOAlmacenJS almacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private Solicitud solicitud;
    private ArrayList<Solicitud> solicitudes;
    private ArrayList<SolicitudProducto> detalle;
    private SolicitudProducto producto;
    private TraspasoProducto resSolicitudProducto;
    private DAOSolicitudes dao;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private boolean locked;

    public MbSolicitud() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
        this.inicializa();
    }

    public void eliminar() {
        TOSolicitud to = this.convertir(this.solicitud);
        try {
            this.dao = new DAOSolicitudes();
            this.dao.eliminar(to);
            this.solicitudes.remove(this.solicitud);
            this.modoEdicion = false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private SolicitudProducto convertir(TOSolicitudProducto toProd) throws SQLException {
        SolicitudProducto prod = new SolicitudProducto();
        prod.setIdSolicitud(toProd.getIdSolicitud());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        prod.setCantSolicitada(toProd.getCantSolicitada());
        return prod;
    }

    public void obtenerDetalle(SelectEvent event) {
        this.solicitud = (Solicitud) event.getObject();
        TOSolicitud toMov = this.convertir(this.solicitud);
        this.detalle = new ArrayList<>();
        this.producto = new SolicitudProducto();
        try {
            this.dao = new DAOSolicitudes();
            for (TOSolicitudProducto to : this.dao.obtenerDetalle(toMov)) {
                this.detalle.add(this.convertir(to));
            }
            this.solicitud.setIdUsuario(toMov.getIdUsuario());
            this.solicitud.setPropietario(toMov.getPropietario());
            this.solicitud.setEstatus(toMov.getEstatus());
            this.setLocked(this.solicitud.getIdUsuario() == this.solicitud.getPropietario());
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private Solicitud convertir(TOSolicitud toMov) {
        Solicitud mov = new Solicitud(this.mbAlmacenes.getToAlmacen(), this.getAlmacen());
        mov.setIdSolicitud(toMov.getIdSolicitud());
        mov.setFolio(toMov.getFolio());
        mov.setFecha(toMov.getFecha());
        mov.setIdUsuarioOrigen(toMov.getIdUsuarioOrigen());
        mov.setIdUsuario(toMov.getIdUsuario());
        mov.setPropietario(toMov.getPropietario());
        mov.setEstatus(toMov.getEstatus());
        return mov;
    }

    public void obtenerSolicitudes() {
        this.solicitudes = new ArrayList<>();
        try {
            this.dao = new DAOSolicitudes();
            for (TOSolicitud to : this.dao.obtenerSolicitudes(this.getAlmacen().getIdAlmacen(), "0".equals(this.pendientes) ? 0 : 5)) {
                this.solicitudes.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private MovimientoOficinaProductoReporte convertirProductoReporte(SolicitudProducto prod) {
        MovimientoOficinaProductoReporte rep = new MovimientoOficinaProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantSolicitada());
        return rep;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<MovimientoOficinaProductoReporte> detalleReporte = new ArrayList<>();
        for (SolicitudProducto p : this.detalle) {
            if (p.getCantSolicitada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Solicitud.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.solicitud.getAlmacen().getEmpresa());

        parameters.put("cedis", this.solicitud.getAlmacen().getCedis());
        parameters.put("almacen", this.solicitud.getAlmacen().getAlmacen());

        parameters.put("concepto", "SOLICITUD DE TRASPASO");

        parameters.put("cedisOrigen", this.solicitud.getAlmacenOrigen().getCedis());
        parameters.put("almacenOrigen", this.solicitud.getAlmacenOrigen().getAlmacen());

        parameters.put("capturaFolio", this.solicitud.getFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.solicitud.getFecha()));
        parameters.put("capturaHora", formatoHora.format(this.solicitud.getFecha()));

        parameters.put("idUsuario", this.solicitud.getIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Solicitud_" + this.solicitud.getFolio() + ".pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.almacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        this.pendientes = "0";
        this.solicitudes = new ArrayList<>();
        this.setLocked(false);
    }

    public void grabar() {
        try {
            if (this.detalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
//            } else if(this.solicitud.getIdUsuario()!=this.solicitud.getPropietario()) {
//                Mensajes.mensajeAlert("Operacion invalida. No es propietario de la solicitud !!!");
            } else {
                double total = 0;
                for (SolicitudProducto e : this.detalle) {
                    total += e.getCantSolicitada();
                }
                if (total != 0) {
                    TOSolicitud to = this.convertir(this.solicitud);
                    this.dao = new DAOSolicitudes();
                    this.dao.grabar(to);
                    this.solicitud.setFolio(to.getFolio());
                    this.solicitud.setFecha(to.getFecha());
                    this.solicitud.setIdUsuario(to.getIdUsuario());
                    this.solicitud.setPropietario(to.getPropietario());
                    this.solicitud.setEstatus(to.getEstatus());
                    this.setLocked(this.solicitud.getIdUsuario() == this.solicitud.getPropietario());
                    Mensajes.mensajeSucces("La solicitud se grabo correctamente !!!");
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

    public void salir() {
        this.modoEdicion = false;
        this.obtenerSolicitudes();
        this.liberar();
    }

    private void liberar() {
        boolean ok = false;
        if (this.solicitud == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else {
            try {
                this.dao = new DAOSolicitudes();
                this.dao.liberar(this.solicitud.getIdSolicitud());
                ok=true;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okSolicitud", ok);
    }

    public void gestionar() {
        if (this.producto.getCantSolicitada() < 0) {
            this.producto.setCantSolicitada(this.producto.getSeparados());
            Mensajes.mensajeAlert("La cantidad no debe ser menor que cero !!!");
//        } else if (this.solicitud.getIdUsuario()!=this.solicitud.getPropietario()) {
//            this.producto.setCantSolicitada(this.producto.getSeparados());
//            Mensajes.mensajeAlert("Operacion invalida. No es propietario de la solicitud !!!");
        } else if (this.producto.getCantSolicitada() != this.producto.getSeparados()) {
            TOSolicitudProducto toProd = this.convertir(this.producto);
            this.producto.setCantSolicitada(this.producto.getSeparados());
            try {
                this.dao = new DAOSolicitudes();
                this.dao.modificarProducto(toProd);

                this.producto.setCantSolicitada(toProd.getCantSolicitada());
                this.producto.setSeparados(toProd.getCantSolicitada());
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

    private TOSolicitudProducto convertir(SolicitudProducto prod) {
        TOSolicitudProducto toProd = new TOSolicitudProducto();
        toProd.setIdSolicitud(prod.getIdSolicitud());
        toProd.setIdProducto(prod.getProducto().getIdProducto());
        toProd.setCantSolicitada(prod.getCantSolicitada());
        return toProd;
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        SolicitudProducto prod = new SolicitudProducto(this.mbBuscar.getProducto());
        for (SolicitudProducto p : this.detalle) {
            if (p.equals(prod)) {
                this.producto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            prod.setIdSolicitud(this.solicitud.getIdSolicitud());
            try {
                TOSolicitudProducto toProd = this.convertir(prod);

                this.dao = new DAOSolicitudes();
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

    private TOSolicitud convertir(Solicitud mov) {
        TOSolicitud toMov = new TOSolicitud();
        toMov.setIdSolicitud(mov.getIdSolicitud());
        toMov.setIdEmpresa(mov.getAlmacen().getIdEmpresa());
        toMov.setIdAlmacen(mov.getAlmacen().getIdAlmacen());
        toMov.setFolio(mov.getFolio());
        toMov.setFecha(mov.getFecha());
        toMov.setIdUsuario(mov.getIdUsuario());
        toMov.setIdAlmacenOrigen(mov.getAlmacenOrigen().getIdAlmacen());
        toMov.setIdUsuarioOrigen(mov.getIdUsuarioOrigen());
        toMov.setPropietario(mov.getPropietario());
        toMov.setEstatus(mov.getEstatus());
        return toMov;
    }

    public void nuevaSolicitud() {
        this.detalle = new ArrayList<>();
        this.solicitud = new Solicitud(this.getAlmacen(), this.mbAlmacenes.getToAlmacen());
        TOSolicitud toMov = this.convertir(this.solicitud);
        try {
            this.dao = new DAOSolicitudes();
            this.dao.agregar(toMov);
            this.solicitud.setIdSolicitud(toMov.getIdSolicitud());
            this.solicitud.setFolio(toMov.getFolio());
            this.solicitud.setFecha(toMov.getFecha());
            this.solicitud.setIdUsuario(toMov.getIdUsuario());
            this.solicitud.setPropietario(toMov.getPropietario());
            this.solicitud.setEstatus(toMov.getEstatus());
            this.setLocked(this.solicitud.getIdUsuario() == this.solicitud.getPropietario());
            this.modoEdicion = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    public void cargaAlmacenesCedisEmpresa() {
        this.getMbAlmacenes().cargaAlmacenesEmpresa(this.mbCedis.getCedis().getIdCedis(), this.almacen.getIdEmpresa(), this.almacen.getIdAlmacen());
        this.solicitudes = new ArrayList<>();
    }

    public TraspasoProducto getResSolicitudProducto() {
        return resSolicitudProducto;
    }

    public void setResSolicitudProducto(TraspasoProducto resSolicitudProducto) {
        this.resSolicitudProducto = resSolicitudProducto;
    }

    public SolicitudProducto getProducto() {
        return producto;
    }

    public void setProducto(SolicitudProducto producto) {
        this.producto = producto;
    }

    public ArrayList<SolicitudProducto> getDetalle() {
        return detalle;
    }

    public void setDetalle(ArrayList<SolicitudProducto> detalle) {
        this.detalle = detalle;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public ArrayList<Solicitud> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(ArrayList<Solicitud> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
    }

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

    public String getPendientes() {
        return pendientes;
    }

    public void setPendientes(String pendientes) {
        this.pendientes = pendientes;
    }

    public boolean isModoEdicion() {
        return modoEdicion;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(17);
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

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
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

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
