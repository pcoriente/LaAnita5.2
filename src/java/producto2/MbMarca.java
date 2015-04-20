package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOMarcas;
import producto2.dominio.Marca;


/**
 *
 * @author jesc
 */
@Named(value = "mbMarca1")
@SessionScoped
public class MbMarca implements Serializable {
    private String strFabricante;
    private Marca marca;
    private ArrayList<Marca> marcas;
    private ArrayList<SelectItem> listaMarcas;
    private DAOMarcas dao;
    
    public MbMarca() {
        this.inicializa();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.strFabricante="";
        this.marca=new Marca();
        this.setListaMarcas(null);
    }
    
    public boolean eliminar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        try {
            this.dao=new DAOMarcas();
            this.dao.eliminar(this.marca.getIdMarca());
//            this.marcas=this.dao.obtenerMarcas();
//            this.marca=new Marca();
            ok=true;
        } catch (NamingException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okMarca", ok);
        return ok;
    }
    
    public boolean grabar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");
        if(this.strFabricante==null || this.strFabricante.equals("0")) {
            fMsg.setDetail("Se requiere identificar al fabricante");
        } else if(this.marca.getMarca().trim().isEmpty()) {
            fMsg.setDetail("Se requiere la marca");
        } else {
            try {
                this.dao=new DAOMarcas();
                if(this.marca.getIdMarca() == 0) {
                    this.marca.setIdMarca(this.dao.agregar(this.marca));
                } else {
                    this.dao.modificar(this.marca);
                }
//                this.marcas=dao.obtenerMarcas();
                ok=true;
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okMarca", ok);
        return ok;
    }
    
    public void nueva() {
        this.marca.setIdMarca(0);
        this.marca.setMarca("");
        this.marca.setProduccion(true);
    }
    
    public void copia(Marca m) {
        this.marca.setIdMarca(m.getIdMarca());
        this.marca.setMarca(m.getMarca());
        this.marca.setProduccion(m.isProduccion());
    }
    
    private void cargaListaMarcas() {
        boolean ok=true;
        this.listaMarcas=new ArrayList<SelectItem>();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
        try {
            this.dao=new DAOMarcas();
            Marca m0=new Marca(0,"SELECCIONE",true);
            this.listaMarcas.add(new SelectItem(m0, m0.toString()));
            for(Marca m:this.dao.obtenerMarcas()) {
                this.listaMarcas.add(new SelectItem(m, m.toString()));
            }
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }
    
    public String cancelar() {
        return "marcas.salir";
    }
    
    public String terminar() {
        return "menuMarcas.terminar";
    }
    
//    public String mantenimiento(int idMarca) {
//        boolean ok=false;
//        String destino="marcas.mantenimiento";
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "");
//        try {
//            if(idMarca == 0) {
//                this.marca=new Marca();
//            } else {
//                this.dao=new DAOMarcas();
//                this.marca=this.dao.obtenerMarca(idMarca);
//                if(this.marca == null) {
//                    destino=null;
//                }
//            }
//            ok=true;
//        } catch (NamingException ex) {
//            fMsg.setDetail(ex.getMessage());
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        }
//        if(!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//        return destino;
//    }

    public String getStrFabricante() {
        return strFabricante;
    }

    public void setStrFabricante(String strFabricante) {
        this.strFabricante = strFabricante;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public ArrayList<Marca> getMarcas() {
        return marcas;
    }

    public void setMarcas(ArrayList<Marca> marcas) {
        this.marcas = marcas;
    }

    public ArrayList<SelectItem> getListaMarcas() {
        if(this.listaMarcas==null) {
            this.cargaListaMarcas();
        }
        return listaMarcas;
    }

    public void setListaMarcas(ArrayList<SelectItem> listaMarcas) {
        this.listaMarcas = listaMarcas;
    }
}
