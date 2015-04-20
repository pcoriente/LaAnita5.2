package empresas;

import Message.Mensajes;
import empresas.dao.DAOMiniEmpresas;
import empresas.dominio.MiniEmpresa;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 *
 * @author JULIOS
 */
@Named(value = "mbMiniEmpresa")
@SessionScoped
public class MbMiniEmpresa implements Serializable {

    private MiniEmpresa empresa;
    private ArrayList<MiniEmpresa> lstEmpresas;
    private DAOMiniEmpresas dao;
    private ArrayList<SelectItem> listaMiniEmpresasCmb = null;

    public MbMiniEmpresa() {
        try {
            this.dao = new DAOMiniEmpresas();
        } catch (NamingException ex) {
            Logger.getLogger(MbMiniEmpresa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<SelectItem> obtenerListaMiniEmpresas() {
        ArrayList<SelectItem> listaEmpresas = new ArrayList<SelectItem>();
        try {
            MiniEmpresa e0 = new MiniEmpresa();
            e0.setIdEmpresa(0);
            e0.setCodigoEmpresa("0");
            e0.setNombreComercial("Empresa");
            listaEmpresas.add(new SelectItem(e0, e0.toString()));

            ArrayList<MiniEmpresa> empresas = this.dao.obtenerMiniEmpresas();
            for (MiniEmpresa e : empresas) {
                listaEmpresas.add(new SelectItem(e, e.toString()));
            }
        } catch (SQLException e) {
            Mensajes.mensajeError(e.getMessage());
            Logger.getLogger(MbMiniEmpresas.class.getName()).log(Level.SEVERE, null, e);
        }
        return listaEmpresas;
    }

    public MiniEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(MiniEmpresa empresa) {
        this.empresa = empresa;
    }

    public ArrayList<MiniEmpresa> getLstEmpresas() {
        if (lstEmpresas == null) {
            this.obtenerListaMiniEmpresas();
        }
        return lstEmpresas;
    }

    public void setLstEmpresas(ArrayList<MiniEmpresa> lstEmpresas) {
        this.lstEmpresas = lstEmpresas;
    }

    public DAOMiniEmpresas getDao() {
        return dao;
    }

    public void setDao(DAOMiniEmpresas dao) {
        this.dao = dao;
    }

    public ArrayList<SelectItem> getListaMiniEmpresasCmb() {
        if (listaMiniEmpresasCmb == null) {
            try {
                listaMiniEmpresasCmb = new ArrayList<SelectItem>();
                MiniEmpresa mini = new MiniEmpresa();
                mini.setIdEmpresa(0);
                mini.setNombreComercial("Empresa");
                listaMiniEmpresasCmb.add(new SelectItem(mini, mini.getNombreComercial()));
                for (MiniEmpresa miniEmpresa : dao.obtenerMiniEmpresas()) {
                    listaMiniEmpresasCmb.add(new SelectItem(miniEmpresa,miniEmpresa.getNombreComercial()));
                }
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
                Logger.getLogger(MbMiniEmpresa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return listaMiniEmpresasCmb;
    }

    public void setListaMiniEmpresasCmb(ArrayList<SelectItem> listaMiniEmpresasCmb) {
        this.listaMiniEmpresasCmb = listaMiniEmpresasCmb;
    }

}
