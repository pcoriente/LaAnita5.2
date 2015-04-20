package proveedores;

import impuestos.MbGrupos;
import impuestos.dominio.ImpuestoGrupo;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.MbEmpaques;
import producto2.MbMarca;
import producto2.MbPresentacion;
import producto2.MbProductosBuscar;
import producto2.dao.DAOEmpaques;
import producto2.dao.DAOMarcas;
import producto2.dominio.Empaque;
import producto2.dominio.Marca;
import producto2.dominio.Presentacion;
//import productos.MbMarca;
//import productos.MbProducto;
//import productos.MbUnidadEmpaque;
//import productos.MbPresentacion;
//import productos.dao.DAOEmpaques;
//import productos.dao.DAOMarcas;
//import productos.dao.DAOProductos;
//import productos.dao.DAOUnidadesEmpaque;
//import productos.dominio.Empaque;
//import productos.dominio.Marca;
//import productos.dominio.Presentacion;
//import productos.dominio.Producto;
//import productos.dominio.UnidadEmpaque;
//import productos.to.TOEmpaque;
import proveedores.dao.DAOProveedoresProductos;
import proveedores.dominio.ProveedorProducto;
import unidadesMedida.DAOUnidadesMedida;
import unidadesMedida.MbUnidadMedida;
import unidadesMedida.UnidadMedida;
import utilerias.Utilerias;

/**
 *
 * @author jsolis
 */
@Named(value = "mbProveedorProducto")
@SessionScoped
public class MbProveedorProducto implements Serializable {

    private int idProveedor;
    private ProveedorProducto producto;
    private DAOProveedoresProductos dao;
    private ArrayList<SelectItem> listaMarcas;
    @ManagedProperty(value = "{mbMarca}")
    private MbMarca mbMarca;
    private ArrayList<SelectItem> listaEmpaques;
    @ManagedProperty(value = "#{mbEmpaques}")
    private MbEmpaques mbEmpaques;
    @ManagedProperty(value = "#{mbPresentacion}")
    private MbPresentacion mbPresentacion;
    private ArrayList<SelectItem> listaUnidadesMedida;
    @ManagedProperty(value = "#{mbUnidadMedida}")
    private MbUnidadMedida mbUnidadMedida;
    @ManagedProperty(value = "#{mbGrupos}")
    private MbGrupos mbImpuestoGrupo;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;

    public MbProveedorProducto(int idProveedor) {
        this.idProveedor = idProveedor;
        this.producto = new ProveedorProducto();
        this.mbMarca = new MbMarca();
        this.mbEmpaques = new MbEmpaques();
        this.mbPresentacion = new MbPresentacion();
        this.mbUnidadMedida = new MbUnidadMedida();
        this.mbImpuestoGrupo = new MbGrupos();
        this.mbBuscar = new MbProductosBuscar();
    }

    public void salir() {
    }

    public boolean grabar() {
        boolean resultado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        this.producto.setContenido(Utilerias.Round(this.producto.getContenido(), 3));
        if (this.producto.getSku().isEmpty()) {
            fMsg.setDetail("Se requiere el código interno del producto para este proveedor");
        } else if (this.producto.getDiasEntrega() <= 0) {
            fMsg.setDetail("Los dias de entrega debe ser un número mayor o igual a cero");
        } else if (this.producto.getEmpaque().getIdEmpaque() == 0) {
            fMsg.setDetail("Se requiere la unidad de empaque del producto");
        } else if (this.producto.getPiezas() < 1) {
            fMsg.setDetail("Las piezas deben ser un número mayor o igual a 1");
        } else if (this.producto.getProducto().isEmpty()) {
            fMsg.setDetail("Se requiere la descripción del producto");
        } else if (this.producto.getPresentacion().getIdPresentacion() == 0) {
            fMsg.setDetail("Se requiere la presentación del producto");
//        } else if (this.producto.getPresentacion().getIdPresentacion() > 1 && (this.producto.getContenido() <= 0 || this.producto.getContenido() >= 1000)) {
//            this.producto.setContenido(0);
//            fMsg.setDetail("El contenido debe ser un número de 0 a 1000");
        } else if (this.producto.getUnidadMedida().getIdUnidadMedida() == 0) {
            fMsg.setDetail("Se requiere la unidad de medida !!");
        } else if (this.producto.getImpuestoGrupo().getIdGrupo() == 0) {
            fMsg.setDetail("Se requiere un impuesto !!");
        } else {
            try {
                this.dao = new DAOProveedoresProductos();
                if (this.producto.getIdProducto() == 0) {
                    this.producto.setIdProducto(this.dao.agregar(this.producto, this.idProveedor));
                } else {
                    this.dao.modificar(this.producto, this.idProveedor);
                }
                resultado = true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if (!resultado) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okProveedorProducto", resultado);
        return resultado;
    }
    
//    private Empaque convertir(TOEmpaque to, Producto p) throws SQLException {
//        Empaque e=new Empaque();
//        e.setIdEmpaque(to.getIdEmpaque());
//        e.setCod_pro(to.getCod_pro());
//        e.setProducto(p);
//        e.setPiezas(to.getPiezas());
//        e.setUnidadEmpaque(to.getUnidadEmpaque());
//        e.setSubEmpaque(to.getSubEmpaque());
//        e.setDun14(to.getDun14());
//        e.setPeso(to.getPeso());
//        e.setVolumen(to.getVolumen());
//        return e;
//    }

    public ArrayList<ProveedorProducto> obtenerProductos(int idProveedor) {
        boolean resultado=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        ArrayList<ProveedorProducto> productos=new ArrayList<ProveedorProducto>();
        try {
            int idEquivalencia;
            this.dao = new DAOProveedoresProductos();
            productos = this.dao.obtenerProductos(idProveedor);
//            DAOEmpaques daoEpq=new DAOEmpaques();
//            DAOProductos daoProds=new DAOProductos();
            for(ProveedorProducto pp: productos) {
                idEquivalencia=pp.getEquivalencia().getIdProducto();
                if(idEquivalencia!=0) {
                    pp.setEquivalencia(this.mbBuscar.obtenerProducto(idEquivalencia));
//                    pp.setEquivalencia(convertir(daoEpq.obtenerEmpaque(idEmpaque),daoProds.obtenerProducto(idProveedor)));
                }
            }
            resultado=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!resultado) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return productos;
    }

    public void eliminarPresentacion() {
        if (this.mbPresentacion.eliminar()) {
            this.producto.setPresentacion(new Presentacion());
            this.producto.setContenido(0);
            this.producto.setUnidadMedida(new UnidadMedida(0, "", ""));
            this.mbPresentacion.setListaPresentaciones(null);
        }
    }

    public void grabarPresentacion() {
        if (this.mbPresentacion.grabar()) {
            this.producto.setPresentacion(this.mbPresentacion.getPresentacion());
            this.mbPresentacion.setListaPresentaciones(null);
        }
    }

    public void mttoPresentacion() {
        if (this.producto.getPresentacion().getIdPresentacion() == 0) {
            this.mbPresentacion.setPresentacion(new Presentacion());
        } else {
            this.mbPresentacion.copia(this.producto.getPresentacion());
//            this.mbPresentacion.setPresentacion(this.mbPresentacion.copia(this.producto.getPresentacion()));
        }
    }

    public void eliminarMarca() {
        if (this.mbMarca.eliminar()) {
            this.producto.setMarca(this.mbMarca.getMarca());
            this.listaMarcas = null;
        }
    }

    public void grabarMarca() {
        if (this.mbMarca.grabar()) {
            this.producto.setMarca(this.mbMarca.getMarca());
            this.listaMarcas = null;
        }
    }

    public void mttoMarcas() {
        if (this.producto.getMarca().getIdMarca() == 0) {
            this.mbMarca.setMarca(new Marca(0, "", false));
        } else {
            this.mbMarca.copia(this.producto.getMarca());
        }
    }

    public void eliminarUnidadEmpaque() {
        if (this.mbEmpaques.eliminar()) {
            this.producto.setEmpaque(this.mbEmpaques.getEmpaque());
            this.listaEmpaques = null;
        }
    }

    public void grabarUnidadEmpaque() {
        if (this.mbEmpaques.grabar()) {
            this.producto.setEmpaque(this.mbEmpaques.getEmpaque());
            this.listaEmpaques = null;
        }
    }

    public void mttoUnidadesEmpaque() {
        if (this.producto.getEmpaque().getIdEmpaque() == 0) {
            this.mbEmpaques.setEmpaque(new Empaque(0, "", ""));
        } else {
            this.mbEmpaques.copia(this.producto.getEmpaque());
        }
    }

    public void actualizarContenido() {
        if (this.producto.getPresentacion().getIdPresentacion() == 0) {
            this.producto.setContenido(0);
        } else if (this.producto.getPresentacion().getIdPresentacion() == 1) {
            this.producto.setContenido(0);
        } else if (this.producto.getContenido() == 0) {
            this.producto.setContenido(1);
        }
        this.producto.setUnidadMedida(new UnidadMedida(0, "", ""));
    }

    public void actualizarPiezas() {
        if (this.producto.getEmpaque().getIdEmpaque() == 0) {
            this.producto.setPiezas(0);
        } else {
            this.producto.setPiezas(1);
        }
    }

    public void mantenimiento(ProveedorProducto producto) {
        this.copia(producto);
    }

    public void cargaMarcas() {
        boolean oki = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");

        this.listaMarcas = new ArrayList<SelectItem>();
        Marca mark = new Marca(0, "SELECCIONE UNA MARCA", true);
        this.listaMarcas.add(new SelectItem(mark, mark.toString()));
        try {
            DAOMarcas daoMarcas = new DAOMarcas();
            ArrayList<Marca> lstMarcas = daoMarcas.obtenerMarcas();
            for (Marca m : lstMarcas) {
                this.listaMarcas.add(new SelectItem(m, m.toString()));
            }
            oki = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!oki) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void cargaUnidadesMedida() {
        this.listaUnidadesMedida = new ArrayList<SelectItem>();
        UnidadMedida unid = new UnidadMedida(0, "SELECCIONE", "");
        this.listaUnidadesMedida.add(new SelectItem(unid, unid.toString()));
        try {
            DAOUnidadesMedida daoUnidades = new DAOUnidadesMedida();
            ArrayList<UnidadMedida> lstUnidades = daoUnidades.obtenerUnidades();
            for (UnidadMedida u : lstUnidades) {
                this.listaUnidadesMedida.add(new SelectItem(u, u.toString()));
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbProveedorProducto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbProveedorProducto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cargaUnidadesEmpaque() {
        boolean oki = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");

        this.listaEmpaques = new ArrayList<SelectItem>();
        Empaque unidad = new Empaque(0, "SELECCIONE UN EMPAQUE", "");
        this.listaEmpaques.add(new SelectItem(unidad, unidad.toString()));

        try {
            DAOEmpaques daoEmpaques = new DAOEmpaques();
            ArrayList<Empaque> lstEmpaques = daoEmpaques.obtenerEmpaques();
            for (Empaque u : lstEmpaques) {
                listaEmpaques.add(new SelectItem(u, u.toString()));
            }
            oki = true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!oki) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    private void copia(ProveedorProducto producto) {
        this.producto = new ProveedorProducto();
        this.producto.setIdProducto(producto.getIdProducto());
        this.producto.setProducto(producto.getProducto());
        this.producto.setDiasEntrega(producto.getDiasEntrega());
        Marca m = new Marca();
        m.setIdMarca(producto.getMarca().getIdMarca());
        m.setMarca(producto.getMarca().getMarca());
        m.setProduccion(producto.getMarca().isProduccion());
        this.producto.setMarca(m);
        this.producto.setPiezas(producto.getPiezas());
        this.producto.setContenido(producto.getContenido());
        this.producto.setSku(producto.getSku());
        Empaque unidadEmpaque = new Empaque();
        unidadEmpaque.setIdEmpaque(producto.getEmpaque().getIdEmpaque());
        unidadEmpaque.setEmpaque(producto.getEmpaque().getEmpaque());
        unidadEmpaque.setAbreviatura(producto.getEmpaque().getAbreviatura());
        this.producto.setEmpaque(unidadEmpaque);
        UnidadMedida unidadMedida = new UnidadMedida(producto.getUnidadMedida().getIdUnidadMedida(), producto.getUnidadMedida().getUnidadMedida(), producto.getUnidadMedida().getAbreviatura());
        this.producto.setUnidadMedida(unidadMedida);
        UnidadMedida unidadMedida2 = new UnidadMedida(producto.getUnidadMedida2().getIdUnidadMedida(), producto.getUnidadMedida2().getUnidadMedida(), producto.getUnidadMedida2().getAbreviatura());
        this.producto.setUnidadMedida2(unidadMedida2);
        this.producto.setPresentacion(new Presentacion(producto.getPresentacion().getIdPresentacion(), producto.getPresentacion().getPresentacion(), producto.getPresentacion().getAbreviatura()));
        this.producto.setImpuestoGrupo(new ImpuestoGrupo(producto.getImpuestoGrupo().getIdGrupo(), producto.getImpuestoGrupo().getGrupo()));
        this.producto.setEquivalencia(producto.getEquivalencia());
    }

    public ProveedorProducto getProducto() {
        return producto;
    }

    public void setProducto(ProveedorProducto producto) {
        this.producto = producto;
    }

    public ArrayList<SelectItem> getListaMarcas() {
        if (this.listaMarcas == null) {
            this.cargaMarcas();
        }
        return listaMarcas;
    }

    public void setListaMarcas(ArrayList<SelectItem> listaMarcas) {
        this.listaMarcas = listaMarcas;
    }

    public MbMarca getMbMarca() {
        return mbMarca;
    }

    public void setMbMarca(MbMarca mbMarca) {
        this.mbMarca = mbMarca;
    }

    public MbPresentacion getMbPresentacion() {
        return mbPresentacion;
    }

    public void setMbPresentacion(MbPresentacion mbPresentacion) {
        this.mbPresentacion = mbPresentacion;
    }

    public ArrayList<SelectItem> getListaUnidadesMedida() {
        if (this.listaUnidadesMedida == null) {
            this.cargaUnidadesMedida();
        }
        return listaUnidadesMedida;
    }

    public void setListaUnidadesMedida(ArrayList<SelectItem> listaUnidadesMedida) {
        this.listaUnidadesMedida = listaUnidadesMedida;
    }

    public MbUnidadMedida getMbUnidadMedida() {
        return mbUnidadMedida;
    }

    public void setMbUnidadMedida(MbUnidadMedida mbUnidadMedida) {
        this.mbUnidadMedida = mbUnidadMedida;
    }

    public ArrayList<SelectItem> getListaEmpaques() {
        if (this.listaEmpaques == null) {
            this.cargaUnidadesEmpaque();
        }
        return listaEmpaques;
    }

    public void setListaEmpaques(ArrayList<SelectItem> listaEmpaques) {
        this.listaEmpaques = listaEmpaques;
    }

    public MbGrupos getMbImpuestoGrupo() {
        return mbImpuestoGrupo;
    }

    public void setMbImpuestoGrupo(MbGrupos mbImpuestoGrupo) {
        this.mbImpuestoGrupo = mbImpuestoGrupo;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public MbEmpaques getMbEmpaques() {
        return mbEmpaques;
    }

    public void setMbEmpaques(MbEmpaques mbEmpaques) {
        this.mbEmpaques = mbEmpaques;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }
}
