package ventas;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import formatos.MbFormatos;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import ventas.dao.DAOVentas;
import ventas.dominio.Venta;
import ventas.dominio.VentaProductoAlmacen;
import ventas.to.TOVenta;
import ventas.to.TOVentaProductoAlmacen;

/**
 *
 * @author jesc
 */
@Named(value = "mbVentasAlmacen")
@SessionScoped
public class MbVentasAlmacen implements Serializable {

    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMenuClientesGrupos}")
    private MbClientesGrupos mbGrupos;
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbClientes;
    @ManagedProperty(value = "#{mbFormatos}")
    private MbFormatos mbFormatos;
    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Accion> acciones;
    
    private Venta venta;
    private ArrayList<Venta> ventas;
    private boolean ventaAsegurada;
    private VentaProductoAlmacen loteOrigen, loteDestino;
    private ArrayList<VentaProductoAlmacen> detalleAlmacen, empaqueLotes;
    private double cantTraspasar;
    private boolean pendientes;
    private Date fechaInicial;
    private TimeZone zonaHoraria = TimeZone.getDefault();
    private DAOVentas dao;

    public MbVentasAlmacen() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();

        this.mbGrupos = new MbClientesGrupos();
        this.mbClientes = new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbBuscar = new MbProductosBuscar();
        this.mbComprobantes = new MbComprobantes();

        this.inicializa();
    }
    
    public void liberarVenta() {
        boolean ok = false;
        if (this.venta == null) {
            ok = true;    // Para que no haya problema al cerrar despues de eliminar un pedido
        } else if (this.ventaAsegurada) {
            try {

                this.dao = new DAOVentas();
                this.dao.liberarVenta(this.venta.getIdMovto());
                this.ventaAsegurada = false;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            } catch (Exception ex) {
                Mensajes.mensajeAlert(ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void cerrarVentaAlmacen() {
        boolean ok = false;
        try {
            TOVenta toMov = this.convertir(this.venta);

            this.dao = new DAOVentas();
            this.dao.cerrarVentaAlmacen(toMov);
            Mensajes.mensajeSucces("El pedido se cerró correctamente !!!");
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    public void actualizaTraspasoLote() {
        boolean ok = false;
        try {
            this.dao = new DAOVentas();
            this.dao.traspasarLote(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen), this.convertirAlmacenProducto(this.loteDestino), this.cantTraspasar);
            this.loteOrigen.setCantidad(this.loteOrigen.getCantidad()-this.cantTraspasar);
            this.loteOrigen.setSeparados(this.loteOrigen.getCantidad());
            if(this.loteDestino.getIdMovtoAlmacen()!=0) {
                int idx = this.detalleAlmacen.indexOf(this.loteDestino);
                this.setLoteDestino(this.detalleAlmacen.get(idx));
            } else {
                this.loteDestino.setIdMovtoAlmacen(this.venta.getIdMovtoAlmacen());
                this.detalleAlmacen.add(this.loteDestino);
                this.loteDestino.setDisponibles(0);
            }
            this.loteDestino.setCantidad(this.loteDestino.getCantidad()+this.cantTraspasar);
            this.loteDestino.setSeparados(this.loteDestino.getCantidad());
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }
    
    private TOVentaProductoAlmacen convertirAlmacenProducto(VentaProductoAlmacen prod) {
        TOVentaProductoAlmacen toProd = new TOVentaProductoAlmacen();
        movimientos.Movimientos.convertir(prod, toProd);
        toProd.setDisponibles(prod.getDisponibles());
        toProd.setFechaCaducidad(prod.getFechaCaducidad());
        return toProd;
    }
    
    public void inicializaTraspasoLote() {
        boolean ok = false;
        this.cantTraspasar = 0;
        this.loteDestino = null;
        this.empaqueLotes = new ArrayList<>();
        try {
            this.dao = new DAOVentas();
            for (TOVentaProductoAlmacen to : this.dao.obtenerLotesDisponibles(this.venta.getAlmacen().getIdAlmacen(), this.convertirAlmacenProducto(this.loteOrigen))) {
                this.empaqueLotes.add(this.convertirAlmacenProducto(to));
            }
            if(this.empaqueLotes.isEmpty()) {
                Mensajes.mensajeAlert("No hay lotes con existencia disponible para traspasar !!");
            } else {
                ok = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okLote", ok);
    }
    
    private TOVenta convertir(Venta venta) {
        return Ventas.convertir(venta);
    }
    
    private VentaProductoAlmacen convertirAlmacenProducto(TOVentaProductoAlmacen toProd) throws SQLException {
        VentaProductoAlmacen prod = new VentaProductoAlmacen();
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setSeparados(toProd.getCantidad());
        prod.setDisponibles(toProd.getDisponibles());
        prod.setFechaCaducidad(toProd.getFechaCaducidad());
        return prod;
    }
    
    public void obtenerDetalleAlmacen(SelectEvent event) {
        boolean ok = false;
        this.loteOrigen = null;
        this.venta = (Venta) event.getObject();
        this.detalleAlmacen = new ArrayList<>();
        try {
            TOVenta toVta = this.convertir(this.venta);
            this.dao = new DAOVentas();
            for (TOVentaProductoAlmacen to : this.dao.obtenerDetalleAlmacen(toVta)) {
                this.detalleAlmacen.add(this.convertirAlmacenProducto(to));
            }
            this.venta.setIdUsuario(toVta.getIdUsuario());
            this.venta.setPropietario(toVta.getPropietario());
            this.venta.setEstatus(toVta.getEstatus());
            this.ventaAsegurada = this.venta.getIdUsuario() == this.venta.getPropietario();
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okPedido", ok);
    }
    
    private Venta convertir(TOVenta toVta) {
        Venta vta = new Venta(this.mbAlmacenes.obtenerAlmacen(toVta.getIdAlmacen()), this.mbTiendas.obtenerTienda(toVta.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toVta.getIdComprobante()));
        Ventas.convertir(toVta, vta);
        this.mbClientes.setCliente(this.mbClientes.obtenerCliente(vta.getTienda().getIdCliente()));
        return vta;
    }
    
    public void obtenerVentasAlmacen() {
        try {   // Segun fecha y status
            this.ventas = new ArrayList<>();
            this.dao = new DAOVentas();
            for (TOVenta to : this.dao.obtenerVentasAlmacen(this.mbAlmacenes.getToAlmacen().getIdAlmacen(), (this.pendientes ? 5 : 7), this.fechaInicial)) {
                this.ventas.add(this.convertir(to));
            }
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
    
    public void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);

        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();
        
        this.mbBuscar.inicializar();
        this.mbComprobantes.getMbMonedas().setListaMonedas(null);

        this.pendientes = true;
        this.fechaInicial = new Date();
        this.ventas = new ArrayList<>();
        this.venta = new Venta();
    }
    
    private void inicializa() {
        this.inicializar();
    }

    public MbAlmacenesJS getMbAlmacenes() {
        return mbAlmacenes;
    }

    public void setMbAlmacenes(MbAlmacenesJS mbAlmacenes) {
        this.mbAlmacenes = mbAlmacenes;
    }

    public MbClientesGrupos getMbGrupos() {
        return mbGrupos;
    }

    public void setMbGrupos(MbClientesGrupos mbGrupos) {
        this.mbGrupos = mbGrupos;
    }

    public MbMiniClientes getMbClientes() {
        return mbClientes;
    }

    public void setMbClientes(MbMiniClientes mbClientes) {
        this.mbClientes = mbClientes;
    }

    public MbFormatos getMbFormatos() {
        return mbFormatos;
    }

    public void setMbFormatos(MbFormatos mbFormatos) {
        this.mbFormatos = mbFormatos;
    }

    public MbMiniTiendas getMbTiendas() {
        return mbTiendas;
    }

    public void setMbTiendas(MbMiniTiendas mbTiendas) {
        this.mbTiendas = mbTiendas;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }
    
    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(36);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public ArrayList<Venta> getVentas() {
        return ventas;
    }

    public void setVentas(ArrayList<Venta> ventas) {
        this.ventas = ventas;
    }

    public boolean isVentaAsegurada() {
        return ventaAsegurada;
    }

    public void setVentaAsegurada(boolean ventaAsegurada) {
        this.ventaAsegurada = ventaAsegurada;
    }

    public VentaProductoAlmacen getLoteOrigen() {
        return loteOrigen;
    }

    public void setLoteOrigen(VentaProductoAlmacen loteOrigen) {
        this.loteOrigen = loteOrigen;
    }

    public VentaProductoAlmacen getLoteDestino() {
        return loteDestino;
    }

    public void setLoteDestino(VentaProductoAlmacen loteDestino) {
        this.loteDestino = loteDestino;
    }

    public ArrayList<VentaProductoAlmacen> getDetalleAlmacen() {
        return detalleAlmacen;
    }

    public void setDetalleAlmacen(ArrayList<VentaProductoAlmacen> detalleAlmacen) {
        this.detalleAlmacen = detalleAlmacen;
    }

    public ArrayList<VentaProductoAlmacen> getEmpaqueLotes() {
        return empaqueLotes;
    }

    public void setEmpaqueLotes(ArrayList<VentaProductoAlmacen> empaqueLotes) {
        this.empaqueLotes = empaqueLotes;
    }

    public double getCantTraspasar() {
        return cantTraspasar;
    }

    public void setCantTraspasar(double cantTraspasar) {
        this.cantTraspasar = cantTraspasar;
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

    public TimeZone getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(TimeZone zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }
}
