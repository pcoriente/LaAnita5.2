package devoluciones;

import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import clientes.MbMiniClientes;
import comprobantes.MbComprobantes;
import devoluciones.dao.DAODevoluciones;
import devoluciones.dominio.Devolucion;
import devoluciones.dominio.DevolucionProducto;
import devoluciones.to.TODevolucionProducto;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import movimientos.to.TOMovimientoOficina;
import producto2.MbProductosBuscar;
import tiendas.MbMiniTiendas;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import ventas.dao.DAOVentas;
import ventas.to.TOVenta;

/**
 *
 * @author jesc
 */
@Named(value = "mbDevoluciones")
@SessionScoped
public class MbDevoluciones implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbClientes;
    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;
    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private boolean modoEdicion;
    private Devolucion devolucion;
    private ArrayList<DevolucionProducto> detalle;
    private DAODevoluciones dao;

    public MbDevoluciones() throws NamingException {
        this.mbAcciones = new MbAcciones();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbClientes = new MbMiniClientes();
        this.mbTiendas = new MbMiniTiendas();
        this.mbComprobantes = new MbComprobantes();
        this.mbBuscar = new MbProductosBuscar();

        this.inicializar();
    }
    
    private DevolucionProducto convertir(TODevolucionProducto toProd) throws SQLException {
        DevolucionProducto prod = new DevolucionProducto();
        prod.setCantVendida(toProd.getCantVendida());
        prod.setCantDevuelta(toProd.getCantDevuelta());
        prod.setProducto(this.mbBuscar.obtenerProducto(toProd.getIdProducto()));
        movimientos.Movimientos.convertir(toProd, prod);
        prod.setNeto(prod.getUnitario() + this.dao.obtenerImpuestosProducto(toProd.getIdMovto(), toProd.getIdProducto(), prod.getImpuestos()));
        return prod;
    }
    
    private TOMovimientoOficina convertir(Devolucion devolucion) {
        TOMovimientoOficina toDev = new TOMovimientoOficina();
        movimientos.Movimientos.convertir(devolucion, toDev);
        toDev.setIdComprobante(devolucion.getComprobante().getIdComprobante());
        toDev.setIdImpuestoZona(devolucion.getTienda().getIdImpuestoZona());
        toDev.setIdReferencia(devolucion.getTienda().getIdTienda());
        toDev.setReferencia(devolucion.getIdMovtoVenta());
        return toDev;
    }

    public void nuevaDevolucion() {
        if (this.mbAlmacenes.getToAlmacen().getIdAlmacen() == 0) {
            Mensajes.mensajeAlert("Se requiere un almacén !!!");
        } else if (this.mbClientes.getCliente().getIdCliente() == 0) {
            Mensajes.mensajeAlert("Se requere un cliente !!!");
        } else if (this.mbComprobantes.getSeleccion() == null) {
            Mensajes.mensajeAlert("Se requiere una factura !!!");
        } else {
            this.detalle = new ArrayList<>();
            try {
                DAOVentas daoVtas = new DAOVentas();
                TOVenta toVta = daoVtas.obtenerVentaOficina(this.mbComprobantes.getSeleccion().getIdComprobante());
                if (toVta.getIdUsuario() == toVta.getPropietario()) {
                    this.mbComprobantes.convierteSeleccion();
                    this.mbTiendas.setTienda(this.mbTiendas.obtenerTienda(toVta.getIdReferencia()));

                    this.devolucion = new Devolucion(this.mbAlmacenes.getToAlmacen(), this.mbTiendas.getTienda(), this.mbComprobantes.getComprobante());
                    this.devolucion.setIdMovtoVenta(toVta.getIdMovto());
                    TOMovimientoOficina toDev = this.convertir(this.devolucion);
                    
                    this.dao = new DAODevoluciones();
                    for(TODevolucionProducto toProd : this.dao.crear(toDev)) {
                        this.detalle.add(this.convertir(toProd));
                    }
                } else {
                    Mensajes.mensajeAlert("La venta esta siendo utilizada por otro usuario !!!");
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
    }

    public void actualizaComprobanteCliente() {
        this.mbComprobantes.setIdReferencia(this.mbClientes.getCliente().getIdCliente());
        this.mbComprobantes.setComprobante(null);
        this.mbComprobantes.setSeleccion(null);
    }

    public void actualizaComprobanteAlmacen() {
        this.mbComprobantes.setIdEmpresa(this.mbAlmacenes.getToAlmacen().getIdEmpresa());
    }

    private void inicializar() {
        this.mbAlmacenes.setListaAlmacenes(null);
        this.mbClientes.obtenerClientesCedis();
        this.mbComprobantes.setIdTipoMovto(28);
        this.mbBuscar.inicializar();
        this.modoEdicion = false;
    }

    public ArrayList<Accion> obtenerAcciones(int idModulo) {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(idModulo);
        }
        return acciones;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(31);
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

    public MbMiniClientes getMbClientes() {
        return mbClientes;
    }

    public void setMbClientes(MbMiniClientes mbClientes) {
        this.mbClientes = mbClientes;
    }

    public MbMiniTiendas getMbTiendas() {
        return mbTiendas;
    }

    public void setMbTiendas(MbMiniTiendas mbTiendas) {
        this.mbTiendas = mbTiendas;
    }

    public MbComprobantes getMbComprobantes() {
        return mbComprobantes;
    }

    public void setMbComprobantes(MbComprobantes mbComprobantes) {
        this.mbComprobantes = mbComprobantes;
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
}
