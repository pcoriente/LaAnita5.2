package producto2;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import producto2.dao.DAOArticulosBuscar;
import producto2.dominio.Articulo;
import producto2.dominio.ArticuloBuscar;
import producto2.dominio.Grupo;
import producto2.dominio.Parte;
import producto2.dominio.SubGrupo;
import producto2.dominio.Tipo;

/**
 *
 * @author jesc
 */
@Named(value = "mbArticulosBuscar")
@SessionScoped
public class MbArticulosBuscar implements Serializable {
    @ManagedProperty(value = "#{mbParte}")
    private MbParte mbParte;
    
    private ArrayList<Articulo> lstArticulos;
    private String tipoBuscar;
    private String strBuscar;
    private String filtroTipo;
    private String filtroGrupo;
    private String filtroSubGrupo;
    private ArticuloBuscar articulo;
    private ArrayList<ArticuloBuscar> articulos;
    private ArrayList<ArticuloBuscar> filtrados;
    private SelectItem[] arrayTipos;
    private SelectItem[] arrayGrupos;
    private SelectItem[] arraySubGrupos;
    private DAOArticulosBuscar dao;
    
    public MbArticulosBuscar() {
        this.mbParte=new MbParte();
        this.inicializaLocales();
    }
    
    public void inicializar() {
        this.inicializa();
    }
    
    private void inicializa() {
        this.inicializaLocales();
        this.mbParte.nueva();
//        this.mbParte.setListaPartes(null);
    }
    
    private void inicializaLocales() {
        this.tipoBuscar="2";
        this.strBuscar="";
        this.articulo=null;
        this.articulos=null;
        this.filtrados=null;
        this.arrayTipos = new SelectItem[1];
        this.arrayTipos[0] = new SelectItem("", "Seleccione un tipo");
        this.arrayGrupos = new SelectItem[1];
        this.arrayGrupos[0] = new SelectItem("", "Seleccione un grupo");
        this.arraySubGrupos = new SelectItem[1];
        this.arraySubGrupos[0] = new SelectItem("", "Seleccione un subgrupo");
    }
    
    public Articulo obtenerArticulo(int idArticulo) {
        Articulo a=null;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerArticulo");
        try {
            this.dao=new DAOArticulosBuscar();
            a=dao.obtenerArticulo(idArticulo);
            ok=true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if(!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return a;
    }
    
//    public void cambioDeFiltroGrupo(String nuevo) {
//        int i;
//        this.articulo=null;
//        String str=this.arrayGrupos[0].getLabel();
//        this.filtroSubGrupo=this.arraySubGrupos[0].getLabel();
//        ArrayList<SubGrupo> lstSubGrupos = new ArrayList<SubGrupo>();
//        for(Articulo a: this.lstArticulos) {
//            if(a.getTipo().getTipo().equals(this.filtroTipo) 
//                    && (a.getGrupo().getGrupo().equals(nuevo) || a.getGrupo().getGrupo().equals(str))) {
//                if (lstSubGrupos.indexOf(a.getSubGrupo()) == -1) {
//                    lstSubGrupos.add(a.getSubGrupo());
//                }
//            }
//        }
//        Collections.sort(lstSubGrupos, new Comparator<SubGrupo>() {
//            @Override
//            public int compare(SubGrupo  subGrupo1, SubGrupo  subGrupo2) {
//                return  subGrupo1.getSubGrupo().compareTo(subGrupo2.getSubGrupo());
//            }
//        });
//        i = 0;
//        this.arraySubGrupos = new SelectItem[lstSubGrupos.size() + 1];
//        this.arraySubGrupos[i++] = new SelectItem("", "Seleccione un subGrupo");
//        for (SubGrupo sg : lstSubGrupos) {
//            this.arraySubGrupos[i++] = new SelectItem(sg.getSubGrupo(), sg.getSubGrupo());
//        }
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "cambioDeFiltroGrupo");
//        fMsg.setDetail("Cambio de filtro detectado: "+nuevo);
//        FacesContext.getCurrentInstance().addMessage(null, fMsg);
//    }
//    
//    public void cambioDeFiltroTipo(String nuevo) {
//        int i;
//        this.articulo=null;
//        String str=this.arrayTipos[0].getLabel();
//        this.filtroGrupo=this.arrayGrupos[0].getLabel();
//        this.filtroSubGrupo=this.arraySubGrupos[0].getLabel();
//        ArrayList<Grupo> lstGrupos = new ArrayList<Grupo>();
//        ArrayList<SubGrupo> lstSubGrupos = new ArrayList<SubGrupo>();
//        for(Articulo a: this.lstArticulos) {
//            if(a.getTipo().getTipo().equals(nuevo) || a.getTipo().getTipo().equals(str)) {
//                if (lstGrupos.indexOf(a.getGrupo()) == -1) {
//                    lstGrupos.add(a.getGrupo());
//                }
//                if (lstSubGrupos.indexOf(a.getSubGrupo()) == -1) {
//                    lstSubGrupos.add(a.getSubGrupo());
//                }
//            }
//        }
//        Collections.sort(lstGrupos, new Comparator<Grupo>() {
//            @Override
//            public int compare(Grupo  grupo1, Grupo  grupo2) {
//                return  grupo1.getGrupo().compareTo(grupo2.getGrupo());
//            }
//        });
//        i = 0;
//        this.arrayGrupos = new SelectItem[lstGrupos.size() + 1];
//        this.arrayGrupos[i++] = new SelectItem("", "Seleccione un grupo");
//        for (Grupo g : lstGrupos) {
//            this.arrayGrupos[i++] = new SelectItem(g.getGrupo(), g.getGrupo());
//        }
//        Collections.sort(lstSubGrupos, new Comparator<SubGrupo>() {
//            @Override
//            public int compare(SubGrupo  subGrupo1, SubGrupo  subGrupo2) {
//                return  subGrupo1.getSubGrupo().compareTo(subGrupo2.getSubGrupo());
//            }
//        });
//        i = 0;
//        this.arraySubGrupos = new SelectItem[lstSubGrupos.size() + 1];
//        this.arraySubGrupos[i++] = new SelectItem("", "Seleccione un subGrupo");
//        for (SubGrupo sg : lstSubGrupos) {
//            this.arraySubGrupos[i++] = new SelectItem(sg.getSubGrupo(), sg.getSubGrupo());
//        }
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "cambioDeFiltroTipo");
//        fMsg.setDetail("Cambio de filtro detectado: "+nuevo);
//        FacesContext.getCurrentInstance().addMessage(null, fMsg);
//    }
    
    public void cambioDeFiltroGrupo(String nuevo) {
        this.articulo=null;
        this.filtroSubGrupo=this.arraySubGrupos[0].getLabel();
        for(SelectItem sg: this.arraySubGrupos) {
            if(!sg.isNoSelectionOption()) {
                sg.setDisabled(true);
                for(Articulo a: this.lstArticulos) {
                    if(a.getGrupo().getGrupo().equals(nuevo) && a.getSubGrupo().getSubGrupo().equals(sg.getLabel())) {
                        sg.setDisabled(false);
                        break;
                    }
                }
            }
        }
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "cambioDeFiltroTipo");
        fMsg.setDetail("Cambio de filtro detectado: "+nuevo);
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public void cambioDeFiltroTipo(String nuevo) {
        this.articulo=null;
        this.filtroGrupo=this.arrayGrupos[0].getLabel();
        for(SelectItem g: this.arrayGrupos) {
            if(!g.isNoSelectionOption()) {
                g.setDisabled(true);
                for(Articulo a: this.lstArticulos) {
                    if(a.getTipo().getTipo().equals(nuevo) && a.getGrupo().getGrupo().equals(g.getLabel())) {
                        g.setDisabled(false);
                        break;
                    }
                }
            }
        }
        this.filtroSubGrupo=this.arraySubGrupos[0].getLabel();
        for(SelectItem sg: this.arraySubGrupos) {
            if(!sg.isNoSelectionOption()) {
                sg.setDisabled(true);
                for(Articulo a: this.lstArticulos) {
                    if(a.getTipo().getTipo().equals(nuevo) && a.getSubGrupo().getSubGrupo().equals(sg.getLabel())) {
                        sg.setDisabled(false);
                        break;
                    }
                }
            }
        }
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "cambioDeFiltroTipo");
        fMsg.setDetail("Cambio de filtro detectado: "+nuevo);
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }
    
    public void cambioDeFiltro(ValueChangeEvent event) {
        String nuevo=event.getNewValue().toString();
        if(event.getComponent().getId().equals("idListaTipos")) {
            this.cambioDeFiltroTipo(nuevo);
        } else {
            this.cambioDeFiltroGrupo(nuevo);
        }
    }
    
    public void procesaLista() {
        this.articulo=null;
        this.filtroTipo=this.arrayTipos[0].getLabel();
        this.filtroGrupo=this.arrayGrupos[0].getLabel();
        this.filtroSubGrupo=this.arraySubGrupos[0].getLabel();
        this.articulos=new ArrayList<ArticuloBuscar>();
        ArrayList<Tipo> lstTipos = new ArrayList<Tipo>();
        ArrayList<Grupo> lstGrupos = new ArrayList<Grupo>();
        ArrayList<SubGrupo> lstSubGrupos = new ArrayList<SubGrupo>();
        for(Articulo a: this.lstArticulos) {
            if (lstTipos.indexOf(a.getTipo()) == -1) {
                lstTipos.add(a.getTipo());
            }
            if (lstGrupos.indexOf(a.getGrupo()) == -1) {
                lstGrupos.add(a.getGrupo());
            }
            if (lstSubGrupos.indexOf(a.getSubGrupo()) == -1) {
                lstSubGrupos.add(a.getSubGrupo());
            }
            this.articulos.add(new ArticuloBuscar(a.getIdArticulo(),a.getTipo().getTipo(), a.getGrupo().getGrupo(), a.getSubGrupo().getSubGrupo(), a.toString()));
        }
        this.filtrados=this.articulos;
        Collections.sort(lstTipos, new Comparator<Tipo>() {
            @Override
            public int compare(Tipo  tipo1, Tipo  tipo2) {
                return  tipo1.getTipo().compareTo(tipo2.getTipo());
            }
        });
        int i = 0;
        this.arrayTipos = new SelectItem[lstTipos.size() + 1];
        this.arrayTipos[i++] = new SelectItem(null, "Seleccione un tipo");
        this.arrayTipos[0].setNoSelectionOption(true);
        for (Tipo t : lstTipos) {
            this.arrayTipos[i++] = new SelectItem(t.getTipo(), t.getTipo());
        }
        Collections.sort(lstGrupos, new Comparator<Grupo>() {
            @Override
            public int compare(Grupo  grupo1, Grupo  grupo2) {
                return  grupo1.getGrupo().compareTo(grupo2.getGrupo());
            }
        });
        i = 0;
        this.arrayGrupos = new SelectItem[lstGrupos.size() + 1];
        this.arrayGrupos[i++] = new SelectItem(null, "Seleccione un grupo");
        this.arrayGrupos[0].setNoSelectionOption(true);
        for (Grupo g : lstGrupos) {
            this.arrayGrupos[i++] = new SelectItem(g.getGrupo(), g.getGrupo());
        }
        Collections.sort(lstSubGrupos, new Comparator<SubGrupo>() {
            @Override
            public int compare(SubGrupo  subGrupo1, SubGrupo  subGrupo2) {
                return  subGrupo1.getSubGrupo().compareTo(subGrupo2.getSubGrupo());
            }
        });
        i = 0;
        this.arraySubGrupos = new SelectItem[lstSubGrupos.size() + 1];
        this.arraySubGrupos[i++] = new SelectItem(null, "Seleccione un subGrupo");
        this.arraySubGrupos[0].setNoSelectionOption(true);
        for (SubGrupo sg : lstSubGrupos) {
            this.arraySubGrupos[i++] = new SelectItem(sg.getSubGrupo(), sg.getSubGrupo());
        }
    }
    
    public void buscarLista() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "buscarLista");
        try {
            this.dao=new DAOArticulosBuscar();
            if(this.tipoBuscar.equals("1")) {
                this.strBuscar="1";
            } else {
                if(this.tipoBuscar.equals("2")) {
                    if(this.mbParte.getParte()==null) {
                        this.mbParte.setParte(new Parte());
                    }
                    lstArticulos=this.dao.obtenerArticulos(this.mbParte.getParte());
                } else {
                    lstArticulos=this.dao.obtenerArticulos(this.strBuscar);
                }
                this.procesaLista();
                if(this.articulos.isEmpty()) {
                    fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
                    fMsg.setDetail("No se encontraron productos en la busqueda");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg);
                }
            }
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okBuscar", ok);
    }
    
    public void verCambio() {
        if (this.tipoBuscar.equals("2")) {
            this.mbParte.nueva();
        } else {
            this.strBuscar = "";
        }
        this.articulo=null;
        this.articulos=null;
        this.filtrados=null;
        this.arrayTipos=new SelectItem[0];
        this.arrayGrupos=new SelectItem[0];
        this.arraySubGrupos=new SelectItem[0];
    }

    public MbParte getMbParte() {
        return mbParte;
    }

    public void setMbParte(MbParte mbParte) {
        this.mbParte = mbParte;
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

    public ArticuloBuscar getArticulo() {
        return articulo;
    }

    public void setArticulo(ArticuloBuscar articulo) {
        this.articulo = articulo;
    }

    public ArrayList<ArticuloBuscar> getArticulos() {
        return articulos;
    }

    public void setArticulos(ArrayList<ArticuloBuscar> articulos) {
        this.articulos = articulos;
    }

    public ArrayList<ArticuloBuscar> getFiltrados() {
        return filtrados;
    }

    public void setFiltrados(ArrayList<ArticuloBuscar> filtrados) {
        this.filtrados = filtrados;
    }

    public SelectItem[] getArrayTipos() {
        return arrayTipos;
    }

    public void setArrayTipos(SelectItem[] arrayTipos) {
        this.arrayTipos = arrayTipos;
    }

    public SelectItem[] getArrayGrupos() {
        return arrayGrupos;
    }

    public void setArrayGrupos(SelectItem[] arrayGrupos) {
        this.arrayGrupos = arrayGrupos;
    }

    public SelectItem[] getArraySubGrupos() {
        return arraySubGrupos;
    }

    public void setArraySubGrupos(SelectItem[] arraySubGrupos) {
        this.arraySubGrupos = arraySubGrupos;
    }

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    public String getFiltroGrupo() {
        return filtroGrupo;
    }

    public void setFiltroGrupo(String filtroGrupo) {
        this.filtroGrupo = filtroGrupo;
    }

    public String getFiltroSubGrupo() {
        return filtroSubGrupo;
    }

    public void setFiltroSubGrupo(String filtroSubGrupo) {
        this.filtroSubGrupo = filtroSubGrupo;
    }
}
