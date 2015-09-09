package traspasos;

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
import java.util.Date;
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
import movimientos.dao.DAOMovimientos;
import traspasos.dominio.Traspaso;
import traspasos.dominio.TraspasoProducto;
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
import traspasos.to.TOTraspaso;
import traspasos.to.TOTraspasoProducto;
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
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private Traspaso solicitud;
    private ArrayList<Traspaso> solicitudes;
    private ArrayList<TraspasoProducto> solicitudDetalle;
    private TraspasoProducto solicitudProducto;
    private TraspasoProducto resSolicitudProducto;
    private DAOMovimientos dao;
    private TimeZone zonaHoraria = TimeZone.getDefault();

    public MbSolicitud() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
//        this.mbComprobantes = new MbComprobantes();
        this.inicializa();
    }
    
    public void eliminar() {
        try {
            this.dao = new DAOMovimientos();
            TOTraspaso to = this.convertir(this.solicitud);
            this.dao.eliminarTraspasoSolicitud(to);
            this.solicitudes.remove(this.solicitud);
            this.modoEdicion=false;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }
    
    private TraspasoProducto convertir(TOTraspasoProducto to) throws SQLException {
        TraspasoProducto p = new TraspasoProducto();
        p.setIdSolicitud(to.getIdSolicitud());
        p.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        p.setCantSolicitada(to.getCantSolicitada());
        movimientos.Movimientos.convertir(to, p);
        p.setLotes(this.dao.obtenerLotes(this.solicitud.getIdMovtoAlmacen(), to.getIdProducto()));
        return p;
    }
    
    private void cargaDetalle() {
        try {
            this.dao = new DAOMovimientos();
            this.solicitudDetalle = new ArrayList<>();
            for (TOTraspasoProducto to : this.dao.obtenerTraspasoDetalle(this.solicitud.getIdSolicitud())) {
                this.solicitudDetalle.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void cargarDetalle(SelectEvent event) {
        this.solicitud = (Traspaso) event.getObject();
        this.cargaDetalle();
        this.solicitudProducto = new TraspasoProducto();
        this.modoEdicion = true;
    }
    
    private Traspaso convertir(TOTraspaso toTraspaso) {
        Traspaso traspaso = new Traspaso();
        traspaso.setIdSolicitud(toTraspaso.getReferencia());
        traspaso.setSolicitudFolio(toTraspaso.getSolicitudFolio());
        traspaso.setSolicitudFecha(toTraspaso.getSolicitudFecha());
        traspaso.setSolicitudIdUsuario(toTraspaso.getSolicitudIdUsuario());
        traspaso.setSolicitudProietario(toTraspaso.getSolicitudProietario());
        traspaso.setSolicitudEstatus(toTraspaso.getSolicitudEstatus());
        traspaso.setAlmacen(this.mbAlmacenes.getToAlmacen());
        movimientos.Movimientos.convertir(toTraspaso, traspaso);
        traspaso.setAlmacenDestino(this.toAlmacen);
        return traspaso;
    }

    public void obtenerSolicitudes() {
        this.solicitudes = new ArrayList<>();
        try {
            this.dao = new DAOMovimientos();
            for(TOTraspaso to: this.dao.obtenerTraspasos(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), Integer.parseInt(this.pendientes), new Date())) {
                this.solicitudes.add(this.convertir(to));
            }
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void actualizarProducto() {
        double cantSolicitada = this.solicitudProducto.getCantSolicitada();
    }

    private MovimientoOficinaProductoReporte convertirProductoReporte(TraspasoProducto prod) {
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
        for (TraspasoProducto p : this.solicitudDetalle) {
            if (p.getCantSolicitada() != 0) {
                detalleReporte.add(this.convertirProductoReporte(p));
            }
        }
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\Solicitud.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(detalleReporte);
        Map parameters = new HashMap();
        parameters.put("empresa", this.solicitud.getAlmacenDestino().getEmpresa());

        parameters.put("cedis", this.solicitud.getAlmacenDestino().getCedis());
        parameters.put("almacen", this.solicitud.getAlmacenDestino().getAlmacen());

        parameters.put("concepto", "SOLICITUD DE TRASPASO");

        parameters.put("cedisOrigen", this.solicitud.getAlmacen().getCedis());
        parameters.put("almacenOrigen", this.solicitud.getAlmacen().getAlmacen());

        parameters.put("capturaFolio", this.solicitud.getSolicitudFolio());
        parameters.put("capturaFecha", formatoFecha.format(this.solicitud.getSolicitudFecha()));
        parameters.put("capturaHora", formatoHora.format(this.solicitud.getSolicitudFecha()));

        parameters.put("idUsuario", this.solicitud.getSolicitudIdUsuario());

        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=Solicitud_" + this.solicitud.getSolicitudFolio() + ".pdf");
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
        this.solicitudProducto = this.solicitudDetalle.get(event.getRowIndex());
        if (newValue != null && newValue != oldValue) {
            TOTraspasoProducto to = this.convertir(this.solicitudProducto);
            this.solicitudProducto.setCantSolicitada((Double) oldValue);
            try {
                this.dao = new DAOMovimientos();
                this.dao.modificarSolicitudProducto(to);
                this.solicitudProducto.setCantSolicitada(to.getCantSolicitada());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        } else {
            this.solicitudProducto.setCantSolicitada((Double) oldValue);
        }
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
        this.pendientes = "0";
    }

    private TOTraspaso convertir(Traspaso solicitud) {
        TOTraspaso to = new TOTraspaso();
        to.setSolicitudFolio(solicitud.getSolicitudFolio());
        to.setSolicitudFecha(solicitud.getSolicitudFecha());
        to.setSolicitudIdUsuario(solicitud.getSolicitudIdUsuario());
        to.setSolicitudProietario(solicitud.getSolicitudProietario());
        to.setSolicitudEstatus(solicitud.getSolicitudEstatus());
        movimientos.Movimientos.convertir(solicitud, to);
        to.setIdReferencia(solicitud.getAlmacenDestino().getIdAlmacen());
        to.setReferencia(solicitud.getIdSolicitud());
        return to;
    }

    public void grabar() {
        try {
            if (this.solicitudDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (TraspasoProducto e : this.solicitudDetalle) {
                    total += e.getCantSolicitada();
                }
                if (total != 0) {
                    TOTraspaso to = this.convertir(this.solicitud);
//                    ArrayList<TOTraspasoProducto> tos=new ArrayList<>();
//                    for(TraspasoProducto prod: this.solicitudDetalle) {
//                        if(prod.getCantSolicitada()!=0) {
//                            tos.add(this.convertir(prod));
//                        }
//                    }
                    this.dao = new DAOMovimientos();
                    this.dao.grabarTraspasoSolicitud(to);
                    this.solicitud.setSolicitudFolio(to.getSolicitudFolio());
                    this.solicitud.setSolicitudFecha(to.getSolicitudFecha());
                    this.solicitud.setSolicitudIdUsuario(to.getSolicitudIdUsuario());
                    this.solicitud.setSolicitudEstatus(to.getSolicitudEstatus());
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
//        this.inicializa();
        this.modoEdicion = false;
        this.obtenerSolicitudes();
    }

    private TOTraspasoProducto convertir(TraspasoProducto solicitudProducto) {
        TOTraspasoProducto toSolicitudProducto = new TOTraspasoProducto();
        toSolicitudProducto.setIdSolicitud(solicitudProducto.getIdSolicitud());
        toSolicitudProducto.setCantSolicitada(solicitudProducto.getCantSolicitada());
        movimientos.Movimientos.convertir(solicitudProducto, toSolicitudProducto);
        return toSolicitudProducto;
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        TraspasoProducto producto = new TraspasoProducto();
        producto.setProducto(this.mbBuscar.getProducto());
        for (TraspasoProducto p : this.solicitudDetalle) {
            if (p.equals(producto)) {
                this.solicitudProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            TOTraspasoProducto to = this.convertir(producto);
            to.setIdSolicitud(this.solicitud.getIdSolicitud());
            try {
                this.dao = new DAOMovimientos();
                this.dao.agregarSolicitudProducto(to);
                producto.setIdSolicitud(to.getIdSolicitud());

                this.solicitudDetalle.add(producto);
                this.solicitudProducto = producto;
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            }
        }
        this.respaldaFila();
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void respaldaFila() {
        if (this.resSolicitudProducto == null) {
            this.resSolicitudProducto = new TraspasoProducto();
        }
        this.resSolicitudProducto.setCantSolicitada(this.solicitudProducto.getCantSolicitada());
//        this.resSolicitudProducto.setCantFacturada(this.solicitudProducto.getCantFacturada());
//        this.resSolicitudProducto.setCantRecibida(this.solicitudProducto.getCantRecibida());
//        this.resSolicitudProducto.setDesctoConfidencial(this.solicitudProducto.getDesctoConfidencial());
//        this.resSolicitudProducto.setDesctoProducto1(this.solicitudProducto.getDesctoProducto1());
//        this.resSolicitudProducto.setDesctoProducto2(this.solicitudProducto.getDesctoProducto2());
        this.resSolicitudProducto.setProducto(this.solicitudProducto.getProducto());
//        this.resSolicitudProducto.setImporte(this.solicitudProducto.getImporte());
//        this.resSolicitudProducto.setNeto(this.solicitudProducto.getNeto());
//        this.resSolicitudProducto.setUnitario(this.solicitudProducto.getUnitario());
//        this.resSolicitudProducto.setCosto(this.solicitudProducto.getCosto());
    }

    public void nuevaSolicitud() {
        this.solicitud = new Traspaso();
        this.solicitud.setAlmacen(this.mbAlmacenes.getToAlmacen());
        this.solicitud.setIdTipo(35);
//        this.solicitud.setIdMoneda(1);
//        this.solicitud.setTipoDeCambio(1);
        this.solicitud.setAlmacenDestino(this.getToAlmacen());
        TOTraspaso to = this.convertir(this.solicitud);
        try {
            this.dao = new DAOMovimientos();
            this.dao.agregarSolicitud(to);
            this.solicitud.setIdSolicitud(to.getReferencia());
            this.solicitud.setSolicitudFecha(to.getSolicitudFecha());
            this.solicitud.setSolicitudIdUsuario(to.getSolicitudIdUsuario());
            this.solicitud.setIdMovto(to.getIdMovto());
            this.solicitud.setIdMovtoAlmacen(to.getIdMovtoAlmacen());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        this.solicitudDetalle = new ArrayList<>();
        this.modoEdicion = true;
    }

    public String terminar() {
        this.modoEdicion = false;
        this.acciones = null;
        this.inicializa();
        return "index.xhtml";
    }

    public void cargaAlmacenesCedisEmpresa() {
        this.getMbAlmacenes().cargaAlmacenesEmpresa(this.mbCedis.getCedis().getIdCedis(), this.toAlmacen.getIdEmpresa(), this.toAlmacen.getIdAlmacen());
        this.solicitudes = new ArrayList<>();
    }

    public TraspasoProducto getResSolicitudProducto() {
        return resSolicitudProducto;
    }

    public void setResSolicitudProducto(TraspasoProducto resSolicitudProducto) {
        this.resSolicitudProducto = resSolicitudProducto;
    }

    public TraspasoProducto getSolicitudProducto() {
        return solicitudProducto;
    }

    public void setSolicitudProducto(TraspasoProducto solicitudProducto) {
        this.solicitudProducto = solicitudProducto;
    }

    public ArrayList<TraspasoProducto> getSolicitudDetalle() {
        return solicitudDetalle;
    }

    public void setSolicitudDetalle(ArrayList<TraspasoProducto> solicitudDetalle) {
        this.solicitudDetalle = solicitudDetalle;
    }

    public Traspaso getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Traspaso solicitud) {
        this.solicitud = solicitud;
    }

    public ArrayList<Traspaso> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(ArrayList<Traspaso> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public MiniCedis getCedis() {
        return cedis;
    }

    public void setCedis(MiniCedis cedis) {
        this.cedis = cedis;
    }

    public TOAlmacenJS getToAlmacen() {
        return toAlmacen;
    }

    public void setToAlmacen(TOAlmacenJS toAlmacen) {
        this.toAlmacen = toAlmacen;
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
}
