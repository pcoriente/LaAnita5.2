package producto2;

import impuestos.FrmImpuestos;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import producto2.dao.DAOArticulos;
import producto2.dominio.Articulo;
import producto2.to.TOArticulo;
import unidadesMedida.DAOUnidadesMedida;
import unidadesMedida.UnidadMedida;

/**
 *
 * @author jesc
 */
@Named(value = "mbArticulos")
@SessionScoped
public class MbArticulos implements Serializable {
    @ManagedProperty(value = "#{mbParte}")
    private MbParte mbParte;
    @ManagedProperty(value = "#{mbTipo}")
    private MbTipo mbTipo;
    @ManagedProperty(value = "#{mbGrupo}")
    private MbGrupo mbGrupo;
    @ManagedProperty(value = "{mbMarca}")
    private MbMarca mbMarca;
    @ManagedProperty(value = "#{mbPresentacion}")
    private MbPresentacion mbPresentacion;
    @ManagedProperty(value = "#{mbImpuesto}")
    private FrmImpuestos mbImpuesto;
    @ManagedProperty(value = "#{mbArticulosBuscar}")
    private MbArticulosBuscar mbBuscar;
    
    private Articulo articulo;
    private DAOArticulos dao;
    private ArrayList<SelectItem> listaUnidadesMedida;
    
    public MbArticulos() {
        this.mbParte=new MbParte();
        this.mbTipo=new MbTipo();
        this.mbGrupo=new MbGrupo();
        this.mbMarca=new MbMarca();
        this.mbPresentacion=new MbPresentacion();
        this.mbImpuesto = new FrmImpuestos();
        this.mbBuscar=new MbArticulosBuscar();
        this.inicializa();
    }
    
    public void actualizaArticuloSeleccionado() {
        this.articulo=this.mbBuscar.obtenerArticulo(this.mbBuscar.getArticulo().getIdArticulo());
        this.mbGrupo.getMbSubGrupo().cargaListaSubGrupos(this.articulo.getGrupo().getIdGrupo());
    }
    
    public void buscar() {
        this.mbBuscar.buscarLista();
        if(this.mbBuscar.getArticulo()!=null) {
            this.articulo=this.mbBuscar.obtenerArticulo(this.mbBuscar.getArticulo().getIdArticulo());
        }
    }
    
    private void cargaUnidadesMedida() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        this.listaUnidadesMedida = new ArrayList<>();
        try {
            DAOUnidadesMedida daoUnidades = new DAOUnidadesMedida();
            UnidadMedida u0=new UnidadMedida(0,"SELECCIONE","");
            this.listaUnidadesMedida.add(new SelectItem(u0,u0.toString()));
            for (UnidadMedida u : daoUnidades.obtenerUnidades()) {
                this.listaUnidadesMedida.add(new SelectItem(u, u.toString()));
            }
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public void eliminarPresentacion() {
        if (this.mbPresentacion.eliminar()) {
            this.articulo.getPresentacion().setIdPresentacion(0);
            this.mbPresentacion.setListaPresentaciones(null);
            this.articulo.setContenido(0);
            this.articulo.getUnidadMedida().setIdUnidadMedida(0);
        }
    }
    
    public void grabarPresentacion() {
        if (this.mbPresentacion.grabar()) {
            this.articulo.getPresentacion().setIdPresentacion(this.mbPresentacion.getPresentacion().getIdPresentacion());
            this.mbPresentacion.setListaPresentaciones(null);
            this.articulo.setContenido(0);
            this.articulo.getUnidadMedida().setIdUnidadMedida(0);
        }
    }
    
    public void mttoPresentacion() {
        if (this.articulo.getPresentacion().getIdPresentacion() == 0) {
            this.mbPresentacion.nueva();
        } else {
            this.mbPresentacion.copia(this.articulo.getPresentacion());
        }
    }
    
    public void actualizaContenido() {
        this.articulo.setContenido(0);
        this.articulo.getUnidadMedida().setIdUnidadMedida(0);
//        this.articulo.getUnidadMedida().setUnidadMedida("");
//        this.articulo.getUnidadMedida().setAbreviatura("");
//        this.articulo.getUnidadMedida().setIdTipo(0);
    }
    
    public void eliminarMarca() {
        if (this.mbGrupo.eliminar()) {
            this.articulo.getMarca().setIdMarca(0);
//            this.articulo.getMarca().setMarca("");
//            this.articulo.getMarca().setProduccion(true);
            this.mbMarca.setListaMarcas(null);
        }
    }
    
    public void grabarMarca() {
        if (this.mbMarca.grabar()) {
            this.articulo.getMarca().setIdMarca(this.mbMarca.getMarca().getIdMarca());
            this.mbMarca.setListaMarcas(null);
        }
    }
    
    public void mttoMarca() {
        if(this.articulo.getMarca().getIdMarca()==0) {
            this.mbMarca.nueva();
        } else {
            this.mbMarca.copia(this.articulo.getMarca());
        }
    }
    
    public void eliminarSubGrupo() {
        if(this.mbGrupo.getMbSubGrupo().eliminar(this.articulo.getSubGrupo().getIdSubGrupo())) {
            this.articulo.getSubGrupo().setIdSubGrupo(0);
//            this.articulo.getSubGrupo().setSubGrupo("");
            this.mbGrupo.getMbSubGrupo().cargaListaSubGrupos(this.articulo.getGrupo().getIdGrupo());
        }
    }
    
    public void grabarSubGrupo() {
        if(this.mbGrupo.getMbSubGrupo().grabar(this.articulo.getGrupo().getIdGrupo())) {
            this.articulo.getSubGrupo().setIdSubGrupo(this.mbGrupo.getMbSubGrupo().getSubGrupo().getIdSubGrupo());
            this.mbGrupo.getMbSubGrupo().cargaListaSubGrupos(this.articulo.getGrupo().getIdGrupo());
        }
    }
    
    public void mttoSubGrupo() {
        if (this.articulo.getSubGrupo().getIdSubGrupo() == 0) {
            this.mbGrupo.getMbSubGrupo().nuevo();
        } else {
            this.mbGrupo.getMbSubGrupo().copia(this.articulo.getSubGrupo());
        }
    }
    
    public void cambioDeGrupo() {
        this.mbGrupo.getMbSubGrupo().cargaListaSubGrupos(this.articulo.getGrupo().getIdGrupo());
    }
    
    public void eliminarGrupo() {
        if (this.mbGrupo.eliminar()) {
            this.articulo.getGrupo().setIdGrupo(0);
//            this.articulo.getGrupo().setCodigo(0);
//            this.articulo.getGrupo().setGrupo("");
            this.mbGrupo.setListaGrupos(null);
        }
    }
    
    public void grabarGrupo() {
        int idGrupo = this.articulo.getGrupo().getIdGrupo();
        if (this.mbGrupo.grabar()) {
            this.articulo.getGrupo().setIdGrupo(this.mbGrupo.getGrupo().getIdGrupo());
            this.mbGrupo.setListaGrupos(null);
            if(idGrupo==0) {
                this.mbGrupo.getMbSubGrupo().inicializar(this.articulo.getGrupo().getIdGrupo());
            }
//            this.articulo.setGrupo(this.mbGrupo.getGrupo());
//            this.mbGrupo.setListaGrupos(null);
//            if (this.articulo.getGrupo().getIdGrupo() != idGrupo) {
//                this.mbGrupo.getMbSubGrupo().inicializar(this.articulo.getGrupo().getIdGrupo());
//            }
        }
    }
    
    public void mttoGrupo() {
        if (this.articulo.getGrupo().getIdGrupo() == 0) {
            this.mbGrupo.nuevo();
        } else {
            this.mbGrupo.copia(this.articulo.getGrupo());
        }
    }
    
    public void eliminar() {
        boolean ok=false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOArticulos();
            this.dao.eliminar(this.articulo.getIdArticulo());
            this.articulo=new Articulo();
            fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
            fMsg.setDetail("El producto se ha eliminado !!!");
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
//        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
    }

    public void grabar() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
//        this.articulo.setContenido(Utilerias.Round(this.articulo.getContenido(), 4));
        if (this.articulo.getParte().getIdParte() == 0) {
            fMsg.setDetail("Se requiere una parte !!");
        } else if (this.articulo.getTipo().getIdTipo() == 0) {
            fMsg.setDetail("Se requiere un tipo de producto !!");
        } else if (this.articulo.getGrupo().getIdGrupo() == 0) {
            fMsg.setDetail("Se requiere el grupo !!");
        } else if(this.articulo.getPresentacion().getIdPresentacion() == 0) {
            fMsg.setDetail("Se requiere una presentacion");
        } else if(this.articulo.getPresentacion().getIdPresentacion() > 1 && (this.articulo.getContenido() < 0 || this.articulo.getContenido() >= 1000)) {
            this.articulo.setContenido(0);
            fMsg.setDetail("Se requiere un número mayor que cero y menor a 1000");
        } else if (this.articulo.getPresentacion().getIdPresentacion() > 1 && this.articulo.getUnidadMedida().getIdUnidadMedida() == 0) {
            fMsg.setDetail("Se requiere la unidad de medida !!");
        } else if (this.articulo.getImpuestoGrupo().getIdGrupo() == 0) {
            fMsg.setDetail("Se requiere un impuesto !!");
        } else {
            try {
                this.dao=new DAOArticulos();
                if (this.articulo.getIdArticulo() == 0) {
                    this.articulo.setIdArticulo(this.dao.agregar(this.convertir(this.articulo)));
                } else {
                    this.dao.modificar(this.convertir(this.articulo));
                }
                fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                fMsg.setDetail("El producto se grabo correctamente !!!");
                ok=true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
//        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return ok;
    }
    
    private TOArticulo convertir(Articulo articulo) {
        TOArticulo to=new TOArticulo();
        to.setIdArticulo(articulo.getIdArticulo());
        to.setIdMarca(articulo.getMarca().getIdMarca());
        to.setIdParte(articulo.getParte().getIdParte());
        to.setDescripcion(articulo.getDescripcion());
        to.setIdPresentacion(articulo.getPresentacion().getIdPresentacion());
        to.setContenido(articulo.getContenido());
        to.setIdUnidadMedida(articulo.getUnidadMedida().getIdUnidadMedida());
        to.setIdTipo(articulo.getTipo().getIdTipo());
        to.setIdGrupo(articulo.getGrupo().getIdGrupo());
        to.setIdSubGrupo(articulo.getSubGrupo().getIdSubGrupo());
        to.setIdImpuestoGrupo(articulo.getImpuestoGrupo().getIdGrupo());
        return to;
    }
    
    public void nuevo() {
        this.articulo=new Articulo();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.articulo=new Articulo();
//        this.mbParte.setListaPartes(null);
        this.mbTipo.inicializar();
        this.mbGrupo.inicializar();
        this.mbMarca.inicializar();
        this.mbPresentacion.inicializar();
        this.setListaUnidadesMedida(null);
        this.mbImpuesto.getMbGrupos().setListaGrupos(null);
        this.mbBuscar.inicializar();
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public MbParte getMbParte() {
        return mbParte;
    }

    public void setMbParte(MbParte mbParte) {
        this.mbParte = mbParte;
    }

    public MbTipo getMbTipo() {
        return mbTipo;
    }

    public void setMbTipo(MbTipo mbTipo) {
        this.mbTipo = mbTipo;
    }

    public MbGrupo getMbGrupo() {
        return mbGrupo;
    }

    public void setMbGrupo(MbGrupo mbGrupo) {
        this.mbGrupo = mbGrupo;
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

    public FrmImpuestos getMbImpuesto() {
        return mbImpuesto;
    }

    public void setMbImpuesto(FrmImpuestos mbImpuesto) {
        this.mbImpuesto = mbImpuesto;
    }

    public MbArticulosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbArticulosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ArrayList<SelectItem> getListaUnidadesMedida() {
        if(this.listaUnidadesMedida==null) {
            this.cargaUnidadesMedida();
        }
        return listaUnidadesMedida;
    }

    public void setListaUnidadesMedida(ArrayList<SelectItem> listaUnidadesMedida) {
        this.listaUnidadesMedida = listaUnidadesMedida;
    }
}
