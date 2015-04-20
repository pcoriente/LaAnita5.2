package cotizaciones;

import cotizaciones.dao.DAOCotizaciones;
import cotizaciones.dominio.CotizacionDetalle;
import cotizaciones.dominio.CotizacionEncabezado;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;
import producto2.MbProductosBuscar;
import proveedores.MbProveedores;

@Named(value = "mbCotizaciones")
@SessionScoped
public class MbCotizaciones implements Serializable {
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;

    private ArrayList<CotizacionEncabezado> listaCotizacionEncabezado;
    private CotizacionEncabezado cotizacionEncabezado;
    private ArrayList<CotizacionDetalle> listaCotizacionDetalle;
//    ----------------Pablo-------------------------
    private CotizacionDetalle cotizacionDeta = new CotizacionDetalle();
    private CotizacionDetalle productoElegido = new CotizacionDetalle();
    private ArrayList<CotizacionDetalle> listaCotizacionDetalleProductos;
    private ArrayList<CotizacionDetalle> ordenCompra = new ArrayList<CotizacionDetalle>();
//    ----------------------------------------------
    private ArrayList<CotizacionEncabezado> miniCotizacionProveedor;
    @ManagedProperty(value = "#{mbProveedores}")
    private MbProveedores mbProveedores;
    private String nombreProduc;
    private CotizacionEncabezado cotizacionesEncabezadoToOrden;
    private String navega;

    //CONSTRUCTORES-------------------------------------------------------------------------------------------------------------------------------------------------------
    public MbCotizaciones() {
        this.mbBuscar=new MbProductosBuscar();
    }
    //METODOS ---------------------------------------------------------------------------------------------------------------------------------------------------------

    private void cargaCotizaciones() throws NamingException, SQLException {

        listaCotizacionEncabezado = new ArrayList<CotizacionEncabezado>();
        DAOCotizaciones daoCot = new DAOCotizaciones();
        ArrayList<CotizacionEncabezado> lista = daoCot.listaCotizaciones();
        for (CotizacionEncabezado d : lista) {
            listaCotizacionEncabezado.add(d);
        }
    }

    public void cargaCotizacionesProveedor() throws NamingException, SQLException {
        int idReq = cotizacionesEncabezadoToOrden.getIdRequisicion();
        ordenCompra = new ArrayList<CotizacionDetalle>();
        listaCotizacionDetalleProductos = new ArrayList<CotizacionDetalle>();
        listaCotizacionDetalle = new ArrayList<CotizacionDetalle>();
        DAOCotizaciones daoCot = new DAOCotizaciones();
        ArrayList<CotizacionDetalle> lista = daoCot.dameProductoCotizacionesProveedores(idReq);
        for (CotizacionDetalle d : lista) {
            d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
            listaCotizacionDetalle.add(d);
        }
    }

//    public void cargaCotizacionesProveedorEncabezado(int idReq) throws NamingException, SQLException {
//        listaCotizacionEncabezado = new ArrayList<CotizacionEncabezado>();
//        DAOCotizaciones daoCot = new DAOCotizaciones();
//        ArrayList<CotizacionEncabezado> lista = daoCot.consultaCotizacionesProveedoresEncabezado(idReq);
//        for (CotizacionEncabezado d : lista) {
//
//            listaCotizacionEncabezado.add(d);
//        }
//    }
    public void cotizacionxProveedor() {
        miniCotizacionProveedor = new ArrayList<CotizacionEncabezado>();



    }
//    Pablo

    public void dameProductos() {

        listaCotizacionDetalleProductos = new ArrayList<CotizacionDetalle>();
        try {
            int idCotizacionDetalle = cotizacionDeta.getIdCotizacion();
            int idProducto = cotizacionDeta.getProducto().getIdProducto();
            DAOCotizaciones daoCot = new DAOCotizaciones();
            listaCotizacionDetalleProductos = daoCot.consultaCotizacionesProveedores(idCotizacionDetalle, idProducto);

            for (CotizacionDetalle d : listaCotizacionDetalleProductos) {
                d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
                
                double neto = d.getNeto();
                double neto2 = neto - neto * (d.getCotizacionEncabezado().getDescuentoCotizacion() / 100);
                double neto3 = neto2 - neto2 * (d.getCotizacionEncabezado().getDescuentoProntoPago() / 100);
                d.setNeto(neto3);

                d.setSubtotal(d.getNeto() * d.getCantidadCotizada());

                double ivaProducto = 0.16;
                double desc = d.getSubtotal();                         //
                if (desc > 0) {
                    d.setIva((desc) * ivaProducto);
                } else {
                    d.setIva(0);
                }

                d.setTotal(d.getSubtotal() + d.getIva());


                this.setNombreProduc(d.getProducto().toString());

            }


        } catch (NamingException ex) {
            Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            System.out.println("");
        }

    }

    public void ordCompra() {
        ordenCompra.add(productoElegido);


        for (CotizacionDetalle d : listaCotizacionDetalle) {

            if (d.getProducto().getIdProducto() == productoElegido.getProducto().getIdProducto()) {
                listaCotizacionDetalle.remove(d);
                break;
            }
        }
    }

    public void guardarOrdenCompra() {
        //   int longOC = 0;
        int longCots;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "guardarOrdenCompra");
        Collections.sort(ordenCompra, new Comparator<CotizacionDetalle>() {
            @Override
            public int compare(CotizacionDetalle o1, CotizacionDetalle o2) {
                int x = 0;
                if (o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial()) == 0) {
                    x = o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial());
                } else if (o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial()) < 0) {
                    x = o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial());
                } else if (o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial()) > 0) {
                    x = o1.getProveedor().getNombreComercial().compareTo(o2.getProveedor().getNombreComercial());
                }
                return x;
            }
        });

        try {
            DAOCotizaciones daoCot = new DAOCotizaciones();
            try {

                longCots = listaCotizacionDetalle.size();
                if (longCots == 0) {
                    daoCot.guardarOrdenCompraTotal(cotizacionesEncabezadoToOrden, ordenCompra);
                    fMsg.setDetail("La orden de compra ha sido generada...");
                    //      this.salirMenuCotizaciones();
                    this.listaCotizacionEncabezado = null;
                    this.cargaCotizaciones();
                } else {
                    fMsg.setDetail("La orden de compra está incompleta, faltan productos por integrar...");
                }
            } catch (SQLException ex) {
                Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public String salirMenuCotizaciones() throws NamingException {

        String salir = "menuRequisiciones.xhtml";
        return salir;
    }

    public void cargaCotizacionesRequisicion(int idReq) throws NamingException, SQLException {
        ordenCompra = new ArrayList<CotizacionDetalle>();
        listaCotizacionDetalleProductos = new ArrayList<CotizacionDetalle>();
        listaCotizacionDetalle = new ArrayList<CotizacionDetalle>();
        DAOCotizaciones daoCot = new DAOCotizaciones();
        ArrayList<CotizacionDetalle> lista = daoCot.dameProductoCotizacionesProveedores(idReq);
        for (CotizacionDetalle d : lista) {
            d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
            listaCotizacionDetalle.add(d);
        }
    }

    public void eliminarProducto(int idProd) {
        int idProducto;
        int longitud = ordenCompra.size();
        for (int y = 0; y < longitud; y++) {
            idProducto = ordenCompra.get(y).getProducto().getIdProducto();
            if (idProducto == idProd) {
                listaCotizacionDetalle.add(ordenCompra.get(y));
                ordenCompra.remove(y);
                break;
            }
        }
    }

    public String menu(int opcion) throws NamingException {
        if (opcion == 0) {
            navega = "index.xhtml";
        } else if (opcion == 1) {
            navega = "menuRequisiciones.xhtml";
        } else if (opcion == 2) {
            navega = "menuCotizaciones.xhtml";
        } else if (opcion == 3) {
            navega = "menuOrdenesDeCompra.xhtml";
        }

        return navega;
    }

    //GETS Y SETS------------------------------------------------------------------------------------------------------------------------------------------------------
    public ArrayList<CotizacionEncabezado> getListaCotizacionEncabezado() throws SQLException {
        if (listaCotizacionEncabezado == null) {
            try {
                this.cargaCotizaciones();
            } catch (NamingException ex) {
                Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return listaCotizacionEncabezado;
    }

    public void setListaCotizacionEncabezado(ArrayList<CotizacionEncabezado> listaCotizacionEncabezado) {
        this.listaCotizacionEncabezado = listaCotizacionEncabezado;
    }

    public CotizacionEncabezado getCotizacionEncabezado() throws SQLException {

        return cotizacionEncabezado;
    }

    public void setCotizacionEncabezado(CotizacionEncabezado cotizacionEncabezado) {
        this.cotizacionEncabezado = cotizacionEncabezado;
    }

    public ArrayList<CotizacionDetalle> getListaCotizacionDetalle() {
        return listaCotizacionDetalle;
    }

    public void setListaCotizacionDetalle(ArrayList<CotizacionDetalle> listaCotizacionDetalle) {
        this.listaCotizacionDetalle = listaCotizacionDetalle;
    }

    public ArrayList<CotizacionEncabezado> getMiniCotizacionProveedor() {
        return miniCotizacionProveedor;
    }

    public void setMiniCotizacionProveedor(ArrayList<CotizacionEncabezado> miniCotizacionProveedor) {
        this.miniCotizacionProveedor = miniCotizacionProveedor;
    }

    public MbProveedores getMbProveedores() {
        return mbProveedores;
    }

    public void setMbProveedores(MbProveedores mbProveedores) {
        this.mbProveedores = mbProveedores;
    }

    public CotizacionDetalle getCotizacionDeta() {
        return cotizacionDeta;
    }

    public void setCotizacionDeta(CotizacionDetalle cotizacionDeta) {
        this.cotizacionDeta = cotizacionDeta;

    }

    public ArrayList<CotizacionDetalle> getListaCotizacionDetalleProductos() {
        return listaCotizacionDetalleProductos;
    }

    public void setListaCotizacionDetalleProductos(ArrayList<CotizacionDetalle> listaCotizacionDetalleProductos) {
        this.listaCotizacionDetalleProductos = listaCotizacionDetalleProductos;
    }

    public CotizacionDetalle getProductoElegido() {
        return productoElegido;
    }

    public void setProductoElegido(CotizacionDetalle productoElegido) {
        this.productoElegido = productoElegido;
    }

    public ArrayList<CotizacionDetalle> getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(ArrayList<CotizacionDetalle> ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getNombreProduc() {

        return nombreProduc;
    }

    public void setNombreProduc(String nombreProduc) {
        this.nombreProduc = nombreProduc;
    }

    public CotizacionEncabezado getCotizacionesEncabezadoToOrden() {
        return cotizacionesEncabezadoToOrden;
    }

    public void setCotizacionesEncabezadoToOrden(CotizacionEncabezado cotizacionesEncabezadoToOrden) {
        this.cotizacionesEncabezadoToOrden = cotizacionesEncabezadoToOrden;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }
}
