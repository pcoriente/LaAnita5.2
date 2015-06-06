package producto2;

import Message.Mensajes;
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
    private String filtro;
    private String filtroTipo;
    private String filtroGrupo;
    private String filtroSubGrupo;
    private ArticuloBuscar articulo;
    private ArrayList<ArticuloBuscar> articulos;
    private ArrayList<ArticuloBuscar> filtrados;
    ArrayList<Tipo> lstTipos;
    ArrayList<Grupo> lstGrupos;
    ArrayList<SubGrupo> lstSubGrupos;
    private SelectItem[] arrayTipos;
    private SelectItem[] arrayGrupos;
    private SelectItem[] arraySubGrupos;
    private DAOArticulosBuscar dao;

    public MbArticulosBuscar() {
        this.mbParte = new MbParte();
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
        this.tipoBuscar = "2";
        this.strBuscar = "";
        this.articulo = null;
        this.articulos = null;
        this.filtrados = null;
//        this.arrayTipos = new SelectItem[1];
//        this.arrayTipos[0] = new SelectItem("", "Seleccione un tipo");
//        this.arrayGrupos = new SelectItem[1];
//        this.arrayGrupos[0] = new SelectItem("", "Seleccione un grupo");
//        this.arraySubGrupos = new SelectItem[1];
//        this.arraySubGrupos[0] = new SelectItem("", "Seleccione un subgrupo");
    }

    public Articulo obtenerArticulo(int idArticulo) {
        Articulo a = null;
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerArticulo");
        try {
            this.dao = new DAOArticulosBuscar();
            a = dao.obtenerArticulo(idArticulo);
            ok = true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
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
    
    public void cambioDeFiltroGrupo() {
        int max=0;
        for (SubGrupo sg : this.lstSubGrupos) {
            for(ArticuloBuscar a : this.filtrados) {
                if(a.getSubGrupo().equals(sg.getSubGrupo())) {
                    max++;
                    break;
                }
            }
        }
        this.arraySubGrupos=new SelectItem[max];
        
        max=0;
        for (SubGrupo sg : this.lstSubGrupos) {
            for(ArticuloBuscar a : this.filtrados) {
                if(a.getSubGrupo().equals(sg.getSubGrupo())) {
                    this.arraySubGrupos[max++] = new SelectItem(sg.getSubGrupo(), sg.getSubGrupo());
                    break;
                }
            }
        }
        this.filtroSubGrupo = null;
    }

    public void cambioDeFiltroTipo() {
        int max=0;
        for (Grupo g : this.lstGrupos) {
            for(ArticuloBuscar a : this.filtrados) {
                if(a.getGrupo().equals(g.getGrupo())) {
                    max++;
                    break;
                }
            }
        }
        this.arrayGrupos=new SelectItem[max];
        
        max=0;
        for (Grupo g : this.lstGrupos) {
            for(ArticuloBuscar a : this.filtrados) {
                if(a.getGrupo().equals(g.getGrupo())) {
                    this.arrayGrupos[max++] = new SelectItem(g.getGrupo(), g.getGrupo());
                    break;
                }
            }
        }
        this.filtroGrupo = null;
    }
    
    public void cambioDeFiltro() {
////        String[] aFiltro = this.filtro.split(";");
////        if(aFiltro[1].equals("idListaTipos")) {
//        if(this.filtro.equals("idListaTipos")) {
//            this.cambioDeFiltroTipo();
//        } else if(this.filtro.equals("idListaGrupos")) {
//            this.cambioDeFiltroGrupo();
//        }
        this.articulo = null;
//        this.cambioDeFiltroTipo();
//        this.cambioDeFiltroGrupo();
    }

    public void cambioDeFiltro(ValueChangeEvent event) {
//        this.filtro="Todos";
//        if(event.getNewValue()!=null) {
//            this.filtro=event.getNewValue().toString();
//        }
//        this.filtro+=(";"+event.getComponent().getId());
        this.filtro=event.getComponent().getId();
    }

    public void procesaLista() {
        this.articulo = null;
        this.articulos = new ArrayList<>();
//        int maxTipo, maxGrupo, maxSubGrupo;
//        maxTipo=0; maxGrupo=0; maxSubGrupo=0;
        this.lstTipos = new ArrayList<>();
        this.lstGrupos = new ArrayList<>();
        this.lstSubGrupos = new ArrayList<>();
        for (Articulo a : this.lstArticulos) {
            if (this.lstTipos.indexOf(a.getTipo()) == -1) {
                this.lstTipos.add(a.getTipo());
            }
            if (this.lstGrupos.indexOf(a.getGrupo()) == -1) {
                this.lstGrupos.add(a.getGrupo());
            }
            if (this.lstSubGrupos.indexOf(a.getSubGrupo()) == -1) {
                this.lstSubGrupos.add(a.getSubGrupo());
            }
            this.articulos.add(new ArticuloBuscar(a.getIdArticulo(), a.getTipo().getIdTipo()==0?"-- Sin Tipo --":a.getTipo().getTipo(), a.getGrupo().getIdGrupo()==0?"-- Sin Grupo --":a.getGrupo().getGrupo(), a.getSubGrupo().getIdSubGrupo()==0?"-- Sin Subgrupo --":a.getSubGrupo().getSubGrupo(), a.toString()));
        }
        this.filtrados = this.articulos;
        
        Collections.sort(this.lstTipos, new Comparator<Tipo>() {
            @Override
            public int compare(Tipo tipo1, Tipo tipo2) {
                return tipo1.getTipo().compareTo(tipo2.getTipo());
            }
        });
        int i = 0;
//        this.arrayTipos = new SelectItem[lstTipos.size() + 1];
        this.arrayTipos = new SelectItem[this.lstTipos.size()];
//        this.arrayTipos[i++] = new SelectItem(null, "-- Sin Tipo --");
//        this.arrayTipos[0].setNoSelectionOption(true);
//        this.filtroTipo = this.arrayTipos[0].getLabel();
        for (Tipo t : this.lstTipos) {
            if(t.getIdTipo()==0) {
                t.setTipo("-- Sin Tipo --");
            }
            this.arrayTipos[i++] = new SelectItem(t.getTipo(), t.getTipo());
        }
//        this.filtroTipo=this.arrayTipos[0].getLabel();
        this.filtroTipo="Seleccione un Tipo";
        
        Collections.sort(this.lstGrupos, new Comparator<Grupo>() {
            @Override
            public int compare(Grupo grupo1, Grupo grupo2) {
                return grupo1.getGrupo().compareTo(grupo2.getGrupo());
            }
        });
        i = 0;
//        this.arrayGrupos = new SelectItem[lstGrupos.size() + 1];
        this.arrayGrupos = new SelectItem[this.lstGrupos.size()];
//        this.arrayGrupos[i++] = new SelectItem(null, "-- Sin Grupo --");
//        this.arrayGrupos[0].setNoSelectionOption(true);
//        this.filtroGrupo = this.arrayGrupos[0].getLabel();
        for (Grupo g : this.lstGrupos) {
            if(g.getIdGrupo()==0) {
                g.setGrupo("-- Sin Grupo --");
            }
            this.arrayGrupos[i++] = new SelectItem(g.getGrupo(), g.getGrupo());
        }
//        this.filtroGrupo=this.arrayGrupos[0].getLabel();
        this.filtroGrupo="Seleccione un Grupo";
        
        Collections.sort(this.lstSubGrupos, new Comparator<SubGrupo>() {
            @Override
            public int compare(SubGrupo subGrupo1, SubGrupo subGrupo2) {
                return subGrupo1.getSubGrupo().compareTo(subGrupo2.getSubGrupo());
            }
        });
        i = 0;
//        this.arraySubGrupos = new SelectItem[lstSubGrupos.size() + 1];
        this.arraySubGrupos = new SelectItem[this.lstSubGrupos.size()];
//        this.arraySubGrupos[i++] = new SelectItem(null, "-- Sin Subgrupo --");
//        this.arraySubGrupos[0].setNoSelectionOption(true);
//        this.filtroSubGrupo = this.arraySubGrupos[0].getLabel();
        for (SubGrupo sg : this.lstSubGrupos) {
            if(sg.getIdSubGrupo()==0) {
                sg.setSubGrupo("-- Sin Subgrupo --");
            }
            this.arraySubGrupos[i++] = new SelectItem(sg.getSubGrupo(), sg.getSubGrupo());
        }
//        this.filtroSubGrupo = this.arraySubGrupos[0].getLabel();
        this.filtroSubGrupo="Seleccione un Subgrupo";
    }

    public void buscarLista() {
        boolean ok = false;
        try {
            this.dao = new DAOArticulosBuscar();
            if (this.tipoBuscar.equals("1")) {
                this.strBuscar = "1";
            } else {
                if (this.tipoBuscar.equals("2")) {
                    if (this.mbParte.getParte() == null) {
                        this.mbParte.setParte(new Parte());
                    }
                    lstArticulos = this.dao.obtenerArticulos(this.mbParte.getParte());
                } else {
                    lstArticulos = this.dao.obtenerArticulos(this.strBuscar);
                }
                this.procesaLista();
                if (this.articulos.isEmpty()) {
                    Mensajes.mensajeAlert("No se encontraron productos en la busqueda");
                }
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okBuscar", ok);
    }

    public void verCambio() {
        if (this.tipoBuscar.equals("2")) {
            this.mbParte.nueva();
        } else {
            this.strBuscar = "";
        }
        this.articulo = null;
        this.articulos = null;
        this.filtrados = null;
        this.arrayTipos = new SelectItem[0];
        this.arrayGrupos = new SelectItem[0];
        this.arraySubGrupos = new SelectItem[0];
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
