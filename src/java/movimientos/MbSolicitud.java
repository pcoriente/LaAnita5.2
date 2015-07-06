package movimientos;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import almacenes.to.TOAlmacenJS;
import cedis.MbMiniCedis;
import cedis.dominio.MiniCedis;
import entradas.dominio.MovimientoOficinaProductoReporte;
import entradas.dominio.MovimientoProducto;
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
import movimientos.dao.DAOMovimientos;
import movimientos.dominio.Solicitud;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.event.CellEditEvent;
import producto2.MbProductosBuscar;
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
    private boolean modoEdicion;
    private MiniCedis cedis;
    private TOAlmacenJS toAlmacen;
    private ArrayList<SelectItem> listaAlmacenes;
    private Solicitud solicitud;
    private ArrayList<MovimientoProducto> solicitudDetalle;
    private MovimientoProducto solicitudProducto;
    private MovimientoProducto resSolicitudProducto;
    private DAOMovimientos dao;

    public MbSolicitud() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbCedis = new MbMiniCedis();
        this.mbBuscar = new MbProductosBuscar();
//        this.mbComprobantes = new MbComprobantes();
        this.inicializa();
    }

    private MovimientoOficinaProductoReporte convertirProductoReporte(MovimientoProducto prod) {
        MovimientoOficinaProductoReporte rep = new MovimientoOficinaProductoReporte();
        rep.setSku(prod.getProducto().getCod_pro());
        rep.setEmpaque(prod.getProducto().toString());
        rep.setCantFacturada(prod.getCantOrdenada());
        return rep;
    }

    public void imprimir() {
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        ArrayList<MovimientoOficinaProductoReporte> detalleReporte = new ArrayList<>();
        for (MovimientoProducto p : this.solicitudDetalle) {
            if (p.getCantOrdenada() != 0) {
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

    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && newValue != oldValue) {
            oldValue = newValue;
        } else {
            newValue = oldValue;
            Mensajes.mensajeAlert("Checar que pasa !!!");
        }
    }

    private void inicializa() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.listaAlmacenes = this.mbAlmacenes.getListaAlmacenes();
        this.toAlmacen = (TOAlmacenJS) this.listaAlmacenes.get(0).getValue();
        this.mbCedis.cargaMiniCedisTodos();
        this.mbBuscar.inicializar();
    }

    private TOMovimiento convertir(Solicitud solicitud) {
        TOMovimiento to = new TOMovimiento();
        to.setIdCedis(solicitud.getAlmacenOrigen().getIdCedis());
        to.setIdEmpresa(solicitud.getAlmacenOrigen().getIdEmpresa());
        to.setIdAlmacen(solicitud.getAlmacenOrigen().getIdAlmacen());
        to.setIdMoneda(1);
        to.setTipoDeCambio(1);
        to.setIdTipo(2); // Entrada por traspaso
        to.setIdImpuestoZona(0);
        to.setIdReferencia(solicitud.getAlmacen().getIdAlmacen());
        return to;
    }

    public void grabar() {
        try {
            if (this.solicitudDetalle.isEmpty()) {
                Mensajes.mensajeAlert("No hay productos en el movimiento !!!");
            } else {
                double total = 0;
                for (MovimientoProducto e : this.solicitudDetalle) {
                    total += e.getCantOrdenada();
                }
                if (total != 0) {
                    this.dao = new DAOMovimientos();
                    TOMovimiento to = this.convertir(this.solicitud);
                    this.dao.grabarTraspasoSolicitud(to, this.solicitudDetalle);
                    this.solicitud.setIdUsuario(to.getIdUsuario());
                    this.solicitud.setFolio(to.getFolio());
                    this.solicitud.setEstatus(1);
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
    }

    public void actualizaProductoSeleccionado() {
        boolean nuevo = true;
        MovimientoProducto producto = new MovimientoProducto();
        producto.setProducto(this.mbBuscar.getProducto());
        for (MovimientoProducto p : this.solicitudDetalle) {
            if (p.equals(producto)) {
                this.solicitudProducto = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            this.solicitudDetalle.add(producto);
            this.solicitudProducto = producto;
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
        if(this.resSolicitudProducto==null) {
            this.resSolicitudProducto=new MovimientoProducto();
        }
        this.resSolicitudProducto.setCantOrdenada(this.solicitudProducto.getCantOrdenada());
        this.resSolicitudProducto.setCantFacturada(this.solicitudProducto.getCantFacturada());
        this.resSolicitudProducto.setCantRecibida(this.solicitudProducto.getCantRecibida());
        this.resSolicitudProducto.setDesctoConfidencial(this.solicitudProducto.getDesctoConfidencial());
        this.resSolicitudProducto.setDesctoProducto1(this.solicitudProducto.getDesctoProducto1());
        this.resSolicitudProducto.setDesctoProducto2(this.solicitudProducto.getDesctoProducto2());
        this.resSolicitudProducto.setProducto(this.solicitudProducto.getProducto());
        this.resSolicitudProducto.setImporte(this.solicitudProducto.getImporte());
        this.resSolicitudProducto.setNeto(this.solicitudProducto.getNeto());
        this.resSolicitudProducto.setUnitario(this.solicitudProducto.getUnitario());
        this.resSolicitudProducto.setCosto(this.solicitudProducto.getCosto());
    }

    public void nuevaSolicitud() {
        this.solicitud = new Solicitud();
        this.solicitud.setAlmacen(this.getToAlmacen());
        this.solicitud.setAlmacenOrigen(this.mbAlmacenes.getToAlmacen());
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
    }

    public MovimientoProducto getResSolicitudProducto() {
        return resSolicitudProducto;
    }

    public void setResSolicitudProducto(MovimientoProducto resSolicitudProducto) {
        this.resSolicitudProducto = resSolicitudProducto;
    }

    public MovimientoProducto getSolicitudProducto() {
        return solicitudProducto;
    }

    public void setSolicitudProducto(MovimientoProducto solicitudProducto) {
        this.solicitudProducto = solicitudProducto;
    }

    public ArrayList<MovimientoProducto> getSolicitudDetalle() {
        return solicitudDetalle;
    }

    public void setSolicitudDetalle(ArrayList<MovimientoProducto> solicitudDetalle) {
        this.solicitudDetalle = solicitudDetalle;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
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
}
