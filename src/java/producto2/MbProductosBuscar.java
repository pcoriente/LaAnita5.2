package producto2;

import Message.Mensajes;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOProductosBuscar;
import producto2.dao.DAOSubProductos;
import producto2.dominio.Articulo;
import producto2.dominio.Producto;
import producto2.dominio.Upc;
import producto2.to.TOProducto;

/**
 *
 * @author jesc
 */
@Named(value = "mbProductosBuscar")
@SessionScoped
public class MbProductosBuscar implements Serializable {

    @ManagedProperty(value = "#{mbArticulosBuscar}")
    private MbArticulosBuscar mbBuscar1;
    @ManagedProperty(value = "#{mbUpc}")
    private MbUpc mbUpc;
    @ManagedProperty(value = "#{mbGrupo}")
    private MbGrupo mbGrupo;
    @ManagedProperty(value = "#{mbParte}")
    private MbParte mbParte;
    private int idEmpresa;
    private String tipoBuscar;
    private String tipoSeleccion;
    private String strBuscar;
    private String update;
    private Producto producto;
    private ArrayList<Producto> productos;
//    private Producto[] seleccionados;
    private ArrayList<Producto> seleccionados;
    private DAOProductosBuscar dao;
    private DAOSubProductos daoSubProductos;

    public MbProductosBuscar() {
        this.mbBuscar1 = new MbArticulosBuscar();
        this.mbUpc = new MbUpc();
        this.mbGrupo = new MbGrupo();
        this.mbParte = new MbParte();
        this.inicializaLocales();
    }

    public void inicializar() {
        this.mbBuscar1.inicializar();
        this.mbUpc.nuevo(0);
        this.mbGrupo.inicializar();
        this.mbParte.nueva();
        this.inicializaLocales();
    }

    private void inicializaLocales() {
        this.strBuscar = "";
        this.tipoBuscar = "2";
        this.tipoSeleccion = "single";
//        this.update="";
        this.producto = null;
        this.productos = new ArrayList<>();
//        this.seleccionados = new Producto[]{};
        this.seleccionados = new ArrayList<>();
    }

    public void limpiarBuscador() {
        productos.clear();
    }

    public boolean buscarPorClasificacion(int idTipo, int idGrupo, int idSubGrupo, int idInsumo) {
        boolean ok=false;
        this.productos=new ArrayList<>();
        Articulo a = null;
        int idArticulo = 0;
        try {
            this.dao = new DAOProductosBuscar();
            this.daoSubProductos = new DAOSubProductos();
            for (TOProducto to : this.dao.obtenerFormulasClasificacion(idTipo, idGrupo, idSubGrupo, idInsumo)) {
                if (to.getIdArticulo() != idArticulo) {
                    idArticulo = to.getIdArticulo();
                    a = this.mbBuscar1.obtenerArticulo(idArticulo);
                }
                this.productos.add(this.convertir(to, a, this.mbUpc.obtenerUpc(to.getIdProducto())));
            }
            ok=true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return ok;
    }

    public void buscarLista() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "buscarLista");
        try {
            this.dao = new DAOProductosBuscar();
            this.daoSubProductos = new DAOSubProductos();
            if (this.getTipoBuscar().equals("1")) {
                TOProducto to = this.dao.obtenerProductoSku(this.strBuscar);
                if (to == null) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
                    fMsg.setDetail("No se encontró producto con el SKU proporcionado");
                } else {
                    this.setProducto(this.convertir(to, this.mbBuscar1.obtenerArticulo(to.getIdArticulo()), this.mbUpc.obtenerUpc(to.getIdProducto())));
                    ok = true;
                }
            } else {
                Articulo a = null;
                int idArticulo = 0;
                ArrayList<TOProducto> tos;

                this.producto = null;
                this.productos = new ArrayList<>();
                switch (this.getTipoBuscar()) {
                    case "2":
                        tos = this.dao.obtenerProductosParte(this.mbParte.getParte().getIdParte());
                        break;
                    case "3":
                        tos = dao.obtenerProductosDescripcion(this.strBuscar);
                        break;
                    default:
                        tos = dao.obtenerProductosClasificacion(this.mbGrupo.getGrupo().getIdGrupo(), this.mbGrupo.getMbSubGrupo().getSubGrupo().getIdSubGrupo());
                        break;
                }
                for (TOProducto to : tos) {
                    if (to.getIdArticulo() != idArticulo) {
                        idArticulo = to.getIdArticulo();
                        a = this.mbBuscar1.obtenerArticulo(idArticulo);
                    }
                    this.productos.add(this.convertir(to, a, this.mbUpc.obtenerUpc(to.getIdProducto())));
                }
                ok = true;
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscar", ok);
    }

    public void verCambio() {
        if (this.getTipoBuscar().equals("2")) {
            this.getMbParte().nueva();
        } else {
            this.setStrBuscar("");
        }
        this.productos = null;
    }

    public Producto obtenerProducto(int idProducto) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerProducto");
        Producto p = new Producto();
        try {
            this.dao = new DAOProductosBuscar();
            this.daoSubProductos = new DAOSubProductos();
            TOProducto to = this.dao.obtenerProducto(idProducto);
            p = this.convertir(to, this.mbBuscar1.obtenerArticulo(to.getIdArticulo()), this.mbUpc.obtenerUpc(to.getIdProducto()));
            ok = true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.err.println("hubo un null Ponter exception al buscar un producto y su id es " + idProducto);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return p;
    }
    
    public ArrayList<Producto> obtenerSimilares(int idProducto) throws NamingException, SQLException {
        boolean ok=false;
        ArrayList<Producto> similares=new ArrayList<>();
//        RequestContext context = RequestContext.getCurrentInstance();
//        try {
            this.dao=new DAOProductosBuscar();
            this.daoSubProductos = new DAOSubProductos();
            for(TOProducto to: this.dao.obtenerSimilares(idProducto)) {
                similares.add(this.convertir(to, this.mbBuscar1.obtenerArticulo(to.getIdArticulo()), this.mbUpc.obtenerUpc(to.getIdProducto())));
            }
            ok=true;
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        context.addCallbackParam("okSimilares", ok);
        return similares;
    }

    public void obtenerProductos(int idArticulo) {
        boolean ok = false;
        this.productos = new ArrayList<>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerProductos");
        RequestContext context = RequestContext.getCurrentInstance();
        try {
            this.dao = new DAOProductosBuscar();
            this.daoSubProductos = new DAOSubProductos();
            Articulo articulo = this.mbBuscar1.obtenerArticulo(idArticulo);
            for (TOProducto to : this.dao.obtenerProductos(idArticulo)) {
                this.productos.add(this.convertir(to, articulo, this.mbUpc.nuevoLista(to.getIdProducto())));
            }
            if (this.productos.isEmpty()) {
                fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
                fMsg.setDetail("No se encontraron productos");
            } else {
                this.producto = null;
                ok = true;
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okEmpaque", ok);
    }

    private Producto convertir(TOProducto to, Articulo a, Upc u) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(to.getIdProducto());
        p.setCod_pro(to.getCod_pro());
        p.setUpc(u);
        p.setArticulo(a);
        p.setPiezas(to.getPiezas());
        p.setEmpaque(to.getEmpaque());
        if (to.getSubProducto().getIdProducto() == 0) {
            p.setSubProducto(null);
        } else {
            p.setSubProducto(this.daoSubProductos.obtenerSubProducto(to.getSubProducto().getIdProducto()));
        }
        p.setDun14(to.getDun14());
        p.setPeso(to.getPeso());
        p.setVolumen(to.getVolumen());
        return p;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<Producto> productos) {
        this.productos = productos;
    }

    public MbArticulosBuscar getMbBuscar1() {
        return mbBuscar1;
    }

    public void setMbBuscar1(MbArticulosBuscar mbBuscar1) {
        this.mbBuscar1 = mbBuscar1;
    }

    public MbUpc getMbUpc() {
        return mbUpc;
    }

    public void setMbUpc(MbUpc mbUpc) {
        this.mbUpc = mbUpc;
    }

    public MbGrupo getMbGrupo() {
        return mbGrupo;
    }

    public void setMbGrupo(MbGrupo mbGrupo) {
        this.mbGrupo = mbGrupo;
    }

    //    public Producto[] getSeleccionados() {
    //        return seleccionados;
    //    }
    //
    //    public void setSeleccionados(Producto[] seleccionados) {
    //        this.seleccionados = seleccionados;
    //    }
    public ArrayList<Producto> getSeleccionados() {
        return seleccionados;
    }

    public void setSeleccionados(ArrayList<Producto> seleccionados) {
        this.seleccionados = seleccionados;
    }

    public String getTipoBuscar() {
        return tipoBuscar;
    }

    public void setTipoBuscar(String tipoBuscar) {
        this.tipoBuscar = tipoBuscar;
    }

    public String getStrBuscar() {
        return strBuscar;
    }

    public void setStrBuscar(String strBuscar) {
        this.strBuscar = strBuscar;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getTipoSeleccion() {
        return tipoSeleccion;
    }

    public void setTipoSeleccion(String tipoSeleccion) {
        this.tipoSeleccion = tipoSeleccion;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public MbParte getMbParte() {
        return mbParte;
    }

    public void setMbParte(MbParte mbParte) {
        this.mbParte = mbParte;
    }
}
