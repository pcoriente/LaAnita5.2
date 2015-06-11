package cotizaciones;

//import cotizaciones.dominio.CotizacionDetalle;
//import cotizaciones.dominio.CotizacionEncabezado;
//import cotizaciones.to.TOCotizacionDetalle;
//import java.util.HashSet;
//import monedas.MbMonedas;
//import proveedores.MbMiniProveedor;
import Message.Mensajes;
import cotizaciones.dao.DAOCotizaciones;
import cotizaciones.dominio.CotizacionDetalle;
import cotizaciones.dominio.CotizacionEncabezado;
import cotizaciones.to.TOCotizacionDetalle;
import empresas.MbMiniEmpresa;
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
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import monedas.MbMonedas;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
//import producto2.dominio.Producto;
import proveedores.MbMiniProveedor;
import proveedores.MbProveedores;
import requisiciones.dao.DAORequisiciones;
//import requisiciones.dominio.RequisicionDetalle;
import requisiciones.mb.MbRequisiciones;

@Named(value = "mbCotizaciones")
@SessionScoped
public class MbCotizaciones implements Serializable {

    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private ArrayList<CotizacionEncabezado> listaCotizacionEncabezado;
    private CotizacionEncabezado cotizacionEncabezado = new CotizacionEncabezado();
    private ArrayList<CotizacionDetalle> listaCotizacionDetalle;
//    ----------------Pablo-------------------------
    private CotizacionDetalle cotizacionDeta = new CotizacionDetalle();
    private CotizacionDetalle productoElegido = new CotizacionDetalle();
    private ArrayList<CotizacionDetalle> listaCotizacionDetalleProductos;
    private ArrayList<CotizacionDetalle> ordenCompra = new ArrayList<>();
//    ----------------------------------------------
    private ArrayList<CotizacionEncabezado> miniCotizacionProveedor;
    @ManagedProperty(value = "#{mbProveedores}")
    private MbProveedores mbProveedores;
    private String nombreProduc;
    private CotizacionEncabezado cotizacionesEncabezadoToOrden;
    private String navega;

    private int miIdReq;

    //CONSTRUCTORES-------------------------------------------------------------------------------------------------------------------------------------------------------
    public MbCotizaciones() {
        this.mbBuscar = new MbProductosBuscar();

        this.mbMiniProveedor = new MbMiniProveedor();
        this.mbMonedas = new MbMonedas();
    }

    //METODOS ---------------------------------------------------------------------------------------------------------------------------------------------------------
    public int getMiIdReq() {
        return miIdReq;
    }

    public void setMiIdReq(int miIdReq) {
        this.miIdReq = miIdReq;
    }

    private void cargaCotizaciones() throws NamingException, SQLException {

        listaCotizacionEncabezado = new ArrayList<>();
        DAOCotizaciones daoCot = new DAOCotizaciones();
        ArrayList<CotizacionEncabezado> lista = daoCot.listaCotizaciones();
        for (CotizacionEncabezado d : lista) {
            listaCotizacionEncabezado.add(d);
        }
    }

    public void cargaCotizacionesProveedor(SelectEvent event) throws NamingException, SQLException {
        this.cotizacionesEncabezadoToOrden=(CotizacionEncabezado)event.getObject();
        
        int idReq = cotizacionesEncabezadoToOrden.getIdRequisicion();
        ordenCompra = new ArrayList<>();
        listaCotizacionDetalleProductos = new ArrayList<>();
        listaCotizacionDetalle = new ArrayList<>();
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
        miniCotizacionProveedor = new ArrayList<>();
    }
//    Pablo

    public void dameProductos() {

        listaCotizacionDetalleProductos = new ArrayList<>();
        try {

            int idRequi = this.cotizacionesEncabezadoToOrden.getIdRequisicion();
            //  int idRequi = cotizacionDeta.getIdRequisicion();
            //  int idRequi= cotizacionDeta.getCotizacionEncabezado().getIdRequisicion();
            int idProducto = cotizacionDeta.getProducto().getIdProducto();
            DAOCotizaciones daoCot = new DAOCotizaciones();
            listaCotizacionDetalleProductos = daoCot.consultaCotizacionesProveedores(idRequi, idProducto);

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
                //  if (d.getProveedor().getIdProveedor() == productoElegido.getProveedor().getIdProveedor()) {
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
        ordenCompra = new ArrayList<>();
        listaCotizacionDetalleProductos = new ArrayList<>();
        listaCotizacionDetalle = new ArrayList<>();
        DAOCotizaciones daoCot = new DAOCotizaciones();
        ArrayList<CotizacionDetalle> lista = daoCot.dameProductoCotizacionesProveedores(idReq);
        for (CotizacionDetalle d : lista) {
            d.setProducto(this.mbBuscar.obtenerProducto(d.getProducto().getIdProducto()));
            listaCotizacionDetalle.add(d);
        }
    }

    public void eliminarProducto(int idProveedor) {
        //    int idProducto;
        int longitud = ordenCompra.size();
        for (int y = 0; y < longitud; y++) {
            //        idProducto = ordenCompra.get(y).getProducto().getIdProducto();
            int idProv = ordenCompra.get(y).getProveedor().getIdProveedor();
            if (idProveedor == idProv) {
                listaCotizacionDetalle.add(ordenCompra.get(y));
                ordenCompra.remove(y);
                break;
            }
        }
    }

//    public String menu(int opcion) throws NamingException {
//        
//        if (opcion == 0) {
//            navega = "index.xhtml";
//        } else if (opcion == 1) {
//            navega = "menuRequisiciones.xhtml";
//        } else if (opcion == 2) {
//            this.limpiaCotizacion();
//            navega = "menuCotizaciones.xhtml";
//        } else if (opcion == 3) {
//            navega = "menuOrdenesDeCompra.xhtml";
//        }
//
//        return navega;
//    }
    //GETS Y SETS------------------------------------------------------------------------------------------------------------------------------------------------------
    public ArrayList<CotizacionEncabezado> getListaCotizacionEncabezado() throws SQLException, NamingException {
//        if (listaCotizacionEncabezado == null) {
//            try {
                this.cargaCotizaciones();
//            } catch (NamingException ex) {
//                Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
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
    ////////////////////////////////////////////////////////////////////////////////////////////
    //COTIZACION
    @ManagedProperty(value = "#{mbMiniEmpresa}")
    private MbMiniEmpresa mbMiniEmpresa;
    private ArrayList<SelectItem> listaMini = new ArrayList<>();
    private ArrayList<CotizacionDetalle> cotizacionDetalles;
    private ArrayList<CotizacionDetalle> cotizacionDetallesG = new ArrayList<>();
    private CotizacionDetalle cotizacionDetalle = new CotizacionDetalle();
    private int numCotizacion = 0;
    private double subtotalGeneral;
    private double sumaDescuentosProductos;
    private double impuesto;
    private double total;
    private double iva = 0.16;
    private double descuentoGeneralAplicado;
    private double sumaDescuentoTotales;
    @ManagedProperty(value = "#{mbMiniProveedor}")
    private MbMiniProveedor mbMiniProveedor = new MbMiniProveedor();
    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas = new MbMonedas();
    private ArrayList<CotizacionEncabezado> cotizacionesEncabezado;
    // private CotizacionEncabezado cotizacionEncabezado = new CotizacionEncabezado();
    private double subtotalBruto;
    private String subtotalBrutoF;

    //CAMBIANDO DE PRODUCTOS A EMPAQUES
//    @ManagedProperty(value = "#{mbProductosBuscar}")
//    private MbProductosBuscar mbBuscar;
//    private ArrayList<Producto> listaEmpaque = new ArrayList<Producto>();
//    private Producto empaque;
    //GET Y SETS 25MAYO2015
    public ArrayList<SelectItem> getListaMini() throws SQLException {
        listaMini = this.mbMiniEmpresa.obtenerListaMiniEmpresas();
        return listaMini;
    }

    public void setListaMini(ArrayList<SelectItem> listaMini) {
        this.listaMini = listaMini;
    }

    public CotizacionDetalle getCotizacionDetalle() {
        return cotizacionDetalle;
    }

    public void setCotizacionDetalle(CotizacionDetalle cotizacionDetalle) {
        this.cotizacionDetalle = cotizacionDetalle;
    }

    public int getNumCotizacion() {
        return numCotizacion;
    }

    public void setNumCotizacion(int numCotizacion) {
        this.numCotizacion = numCotizacion;
    }

    public ArrayList<CotizacionDetalle> getCotizacionDetalles() {
        return cotizacionDetalles;
    }

    public void setCotizacionDetalles(ArrayList<CotizacionDetalle> cotizacionDetalles) {
        this.cotizacionDetalles = cotizacionDetalles;
    }

    public double getSubtotalGeneral() {
        this.calculoSubtotalGeneral();
        return subtotalGeneral;
    }

    public void setSubtotalGeneral(double subtotalGeneral) {
        this.subtotalGeneral = subtotalGeneral;
    }

    public double getSumaDescuentosProductos() {
        this.calculoDescuentoProducto();
        return sumaDescuentosProductos;
    }

    public void setSumaDescuentosProductos(double sumaDescuentosProductos) {
        this.sumaDescuentosProductos = sumaDescuentosProductos;
    }

    public double getImpuesto() {
        this.calculoIVA();
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        this.calculoTotal();
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDescuentoGeneralAplicado() {
        this.calculoDescuentoGeneral();
        return descuentoGeneralAplicado;
    }

    public void setDescuentoGeneralAplicado(double descuentoGeneralAplicado) {
        this.descuentoGeneralAplicado = descuentoGeneralAplicado;
    }

    public double getSumaDescuentoTotales() {
        this.calculoDescuentoTotales();
        return sumaDescuentoTotales;
    }

    public void setSumaDescuentoTotales(double sumaDescuentoTotales) {
        this.sumaDescuentoTotales = sumaDescuentoTotales;
    }

    public MbMiniProveedor getMbMiniProveedor() {
        return mbMiniProveedor;
    }

    public void setMbMiniProveedor(MbMiniProveedor mbMiniProveedor) {
        this.mbMiniProveedor = mbMiniProveedor;
    }

    public ArrayList<CotizacionEncabezado> getCotizacionesEncabezado() {
        return cotizacionesEncabezado;
    }

    public void setCotizacionesEncabezado(ArrayList<CotizacionEncabezado> cotizacionesEncabezado) {
        this.cotizacionesEncabezado = cotizacionesEncabezado;
    }

    //LOS COMENTE CUANDO LOS PASE PORQUE SUPUESTAMENTE ESTAN DUPLICADOS
//    public CotizacionEncabezado getCotizacionEncabezado() {
//        return cotizacionEncabezado;
//    }
//
//    public void setCotizacionEncabezado(CotizacionEncabezado cotizacionEncabezado) {
//        this.cotizacionEncabezado = cotizacionEncabezado;
//    }
    public double getSubtotalBruto() {
        this.calcularSubtotalBruto();
        return subtotalBruto;
    }

    public void setSubtotalBruto(double subtotalBruto) {
        this.subtotalBruto = subtotalBruto;
    }

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }

    public String getSubtotalBrutoF() {
        subtotalBrutoF = utilerias.Utilerias.formatoMonedas(this.getSubtotalBruto());
        return subtotalBrutoF;
    }

    public void setSubtotalBrutoF(String subtotalBrutoF) {

        this.subtotalBrutoF = subtotalBrutoF;
    }

    public ArrayList<CotizacionDetalle> getCotizacionDetallesG() {
        return cotizacionDetallesG;
    }

    public void setCotizacionDetallesG(ArrayList<CotizacionDetalle> cotizacionDetallesG) {
        this.cotizacionDetallesG = cotizacionDetallesG;
    }

    //COTIZACIONES 24mayo2015
    public void guardaCotizacion(int idReq) throws NamingException  {
            double dc = mbMiniProveedor.getMiniProveedor().getDesctoComercial();
            double dpp = mbMiniProveedor.getMiniProveedor().getDesctoProntoPago();
            DAORequisiciones daoReq = new DAORequisiciones();
            FacesMessage msg = null;
            cotizacionDetallesG = new ArrayList<>();
            try {
                int idProv = this.mbMiniProveedor.getMiniProveedor().getIdProveedor();
                int idMon = this.mbMonedas.getMoneda().getIdMoneda();
                if (idProv != 0 && idMon != 0) {
                    if (this.total != 0) {
                        this.cotizacionEncabezado.setIdRequisicion(idReq);
                        this.cotizacionEncabezado.setIdProveedor(idProv);
                        this.cotizacionEncabezado.setDescuentoCotizacion(dc);
                        this.cotizacionEncabezado.setDescuentoProntoPago(dpp);
                        this.cotizacionEncabezado.setIdMoneda(idMon);
                        for (CotizacionDetalle cd : cotizacionDetalles) {
                            if (cd.getCostoCotizado() != 0) {
                                cotizacionDetallesG.add(cd);
                            }
                        }
                        daoReq.grabarCotizacion(this.cotizacionEncabezado, this.cotizacionDetallesG);
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La cotización ha sido registrada..");
                        this.limpiaCotizacion();
                        mbMiniProveedor.getMiniProveedor().setIdProveedor(0);
                        mbMonedas.getMoneda().setIdMoneda(0);
                        this.setMiIdReq(idReq);
                        int coti = daoReq.numCotizaciones(idReq);
                        this.setNumCotizacion(coti);
                        
                    } else {
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "Capture al menos la cotización para un empaque..");
                    }
                } else {
                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "Falta información para realizar la cotización");
                }
            } catch (NamingException ex) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "Error en la aprobación, verifique su información...");
            } catch (SQLException ex) {
                Mensajes.MensajeErrorP(ex.getMessage());
            }
            FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void limpiaCotizacion() throws NamingException {
        //ACTUALIZACION DE CODIGO
        for (CotizacionDetalle d : cotizacionDetalles) {
            //    d.setCantidadCotizada(0);
            d.setCostoCotizado(0);
            d.setDescuentoProducto(0);
            d.setDescuentoProducto2(0);
            d.setNeto(0);
            d.setSubtotal(0);
        }

//        for (CotizacionDetalle d : cotizacionDetallesG) {
//            //    d.setCantidadCotizada(0);
//            d.setCostoCotizado(0);
//            d.setDescuentoProducto(0);
//            d.setDescuentoProducto2(0);
//            d.setNeto(0);
//            d.setSubtotal(0);
//        }
        this.subtotalGeneral = 0.00;
        this.sumaDescuentosProductos = 0.00;
        this.descuentoGeneralAplicado = 0.00;
        this.sumaDescuentoTotales = 0.00;
        this.subtotalBruto = 0.00;
        this.impuesto = 0.00;
        this.total = 0.00;
        this.mbMiniProveedor = new MbMiniProveedor();

        this.listaCotizacionEncabezado = null;

    }

    ///nueva propuesta
    public void cargaRequisicionesDetalleCotizar(int id, int modi) {

        //      int id = seleccionRequisicionEncabezado.getIdRequisicion();
        try {
            DAORequisiciones daoReq = new DAORequisiciones();
            this.setNumCotizacion(0);
            this.subtotalGeneral = 0;
            this.sumaDescuentosProductos = 0;
            this.descuentoGeneralAplicado = 0;
            this.sumaDescuentoTotales = 0;
            this.impuesto = 0;
            this.total = 0;
            mbMiniProveedor = new MbMiniProveedor();
            cotizacionDetalles = new ArrayList<>();
            for (TOCotizacionDetalle rd : daoReq.dameRequisicionDetalleCotizar(id)) {
                cotizacionDetalles.add(this.convertir(rd));
            }
            int coti = daoReq.numCotizaciones(id);
            this.setNumCotizacion(coti);
        } catch (NamingException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private CotizacionDetalle convertir(TOCotizacionDetalle to) {
        CotizacionDetalle rd = new CotizacionDetalle();
        rd.setIdRequisicion(to.getIdRequisicion());
        rd.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        rd.setCantidadAutorizada(to.getCantidadAutorizada());
        rd.setCantidadCotizada(to.getCantidadAutorizada());
        return rd;
    }

    public void calculoSubtotalGeneral() {
        subtotalGeneral = 0;
        for (CotizacionDetalle e : cotizacionDetalles) {
            subtotalGeneral = subtotalGeneral + e.getSubtotal();
        }
    }

    public void calculoIVA() {
        impuesto = 0;
        double desc = this.subtotalBruto;                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            impuesto = (desc) * this.iva;
        } else {
            impuesto = 0;
        }
    }

    public void calculoTotal() {
        total = 0;
        double desc = this.subtotalBruto;                                           //subtotalGeneral - descuentoGeneralAplicado;
        if (desc > 0) {
            total = (desc) + this.getImpuesto();
        } else {
            total = 0;
        }
    }

    public void calculoDescuentoGeneral() {
        descuentoGeneralAplicado = 0;
        double sumaCostoCotizado = 0;
        double descuentoC;
        double descuentoPP;
        double descuentoGA;
        for (CotizacionDetalle e : cotizacionDetalles) {
            sumaCostoCotizado += (e.getCantidadCotizada() * e.getNeto());
        }
        descuentoC = sumaCostoCotizado * (this.mbMiniProveedor.getMiniProveedor().getDesctoComercial() / 100);
        sumaCostoCotizado = sumaCostoCotizado - descuentoC;
        descuentoPP = sumaCostoCotizado * (this.mbMiniProveedor.getMiniProveedor().getDesctoProntoPago() / 100);
        descuentoGA = descuentoC + descuentoPP;
        this.setDescuentoGeneralAplicado(descuentoGA);
    }

    public void calculaPrecioDescuento(int idEmp) {
        System.out.println("entr");
        subtotalGeneral = 0;
        try {
            for (CotizacionDetalle e : cotizacionDetalles) {
                if (e.getProducto().getIdProducto() == idEmp) {
                    double neto = e.getCostoCotizado() - (e.getCostoCotizado() * (e.getDescuentoProducto() / 100));
                    double neto2 = neto - neto * (e.getDescuentoProducto2() / 100);
                    e.setNeto(neto2);
                    e.setSubtotal(e.getNeto() * e.getCantidadCotizada());
                    break;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void calculoDescuentoProducto() {
        sumaDescuentosProductos = 0;
        for (CotizacionDetalle e : cotizacionDetalles) {
            sumaDescuentosProductos += (e.getCantidadCotizada() * (e.getCostoCotizado() - e.getNeto()));
        }
    }

    public void calculoDescuentoTotales() {
        sumaDescuentoTotales = 0;
        sumaDescuentoTotales = this.getSumaDescuentosProductos() + this.getDescuentoGeneralAplicado();
    }

    public void limpiaDetalle() throws NamingException {
        //ACTUALIZACION CODIGO
        for (CotizacionDetalle d : cotizacionDetalles) {
            d.setCostoCotizado(0);
            d.setDescuentoProducto(0);
            d.setNeto(0);
            d.setSubtotal(0);
        }
        this.subtotalGeneral = 0;
        this.sumaDescuentosProductos = 0;
        this.descuentoGeneralAplicado = 0;
        this.sumaDescuentoTotales = 0;
        this.subtotalBruto = 0.0;
        this.impuesto = 0;
        this.total = 0;
    }

    public void calcularSubtotalBruto() {
        subtotalBruto = 0.0;
        subtotalBruto = this.getSubtotalGeneral() - this.getDescuentoGeneralAplicado();
    }

    public void irMenuCotizaciones() {
    }
    // EMPAQUES

    public String cerrarCotizacion(int idReq) {
        String navegar = "";
        FacesMessage msg = null;
        try {
            if (numCotizacion == 0) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "No ha realizado ninguna cotización, por lo que no puede ser CERRADA..");
            } else {
                DAORequisiciones daoReq = new DAORequisiciones();
                daoReq.cerrarCotizacion(idReq);
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La cotización ha sido CERRADA..");
                //    this.setNumCotizacion(numCotizacion);
                this.limpiaCotizacion();
                mbMiniProveedor.getMiniProveedor().setIdProveedor(0);
                mbMonedas.getMoneda().setIdMoneda(0);
                navegar = "menuRequisiciones.xhtml";
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "Error en la aprobación, verifique su información...");
        } catch (SQLException ex) {
            Mensajes.MensajeErrorP(ex.getMessage());
            Logger.getLogger(MbCotizaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return navegar;
    }
}
