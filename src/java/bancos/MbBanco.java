package bancos;

import bancos.dao.DAOBancos;
import bancos.dominio.Banco;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import leyenda.dao.DAOBancosLeyendas;
import leyenda.dominio.BancoLeyenda;

@ManagedBean(name = "mbBanco")
@SessionScoped
public class MbBanco {

    BancoLeyenda b = new BancoLeyenda();
    private Banco objBanco = new Banco();
    private ArrayList<Banco> lstBancos = new ArrayList<Banco>();
    private ArrayList<SelectItem> listaBancos;
    private ArrayList<SelectItem> listaTodosBancos;
    BancoLeyenda banco = new BancoLeyenda();

    public BancoLeyenda getB() {
        return b;
    }

    public void setB(BancoLeyenda b) {
        this.b = b;
    }

    public ArrayList<SelectItem> getListaBancos() {
        return listaBancos;
    }

    public void setListaBancos(ArrayList<SelectItem> listaBancos) {
        this.listaBancos = listaBancos;
    }

//    public List<SelectItem> getListaBancos() {
////        if (this.listaBancos == null) {
////            try {
////                this.listaBancos = this.obtenerBancos();
////            } catch (Exception ex) {
////            }
////        }
//        return listaBancos;
//    }
//
//    public void setListaBancos(List<SelectItem> listaBancos) {
//        this.listaBancos = listaBancos;
//    }
//CONSTRUCTOR DE LA CLASE
    public MbBanco() {
    }

    public String terminar() {
        return "menuBancos.terminar";
    }

    public ArrayList<BancoLeyenda> verTabla() throws SQLException {
        ArrayList<BancoLeyenda> Tabla;
        DAOBancosLeyendas dao = new DAOBancosLeyendas();
        Tabla = dao.dameBancos();
        return Tabla;
    }

    public String eliminar(int id) throws SQLException {
        DAOBancosLeyendas datos = new DAOBancosLeyendas();
        datos.eliminarUsuario(id);
        String Eliminado = "Dato.eliminado";
        return Eliminado;

    }

    public void cargarBancos(int idCliente) {
        try {
            BancoLeyenda banco = new BancoLeyenda();
            banco.setIdBanco(0);
            banco.setNombreCorto("Seleccione un país");
            SelectItem cero = new SelectItem(banco, banco.getNombreCorto());
            DAOBancos dao = new DAOBancos();
            ArrayList<Banco> lstBanco = dao.dameBancos(idCliente);
        } catch (NamingException ex) {
            Logger.getLogger(MbBanco.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbBanco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Codigo Pablo//
    public void guardar() throws SQLException {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", null);
        FacesMessage fMsg2 = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", null);
        DAOBancosLeyendas db = new DAOBancosLeyendas();
        if (b.getIdBanco() == 0) {
            try {
                try {
                    db.agregarDato(b.getRfc(), b.getCodigoBanco(), b.getRazonSocial(), b.getNombreCorto());
                    fMsg2.setDetail("Datos Insertados Correctamente");
                    FacesContext.getCurrentInstance().addMessage(null, fMsg2);
                } catch (Exception e) {
                    System.err.println(e);
                }
            } catch (Exception e) {
                fMsg.setDetail("Codigo de Banco es un número teclee nuevamente");
                FacesContext.getCurrentInstance().addMessage(null, fMsg);
            }
        } else {
            b.getCodigoBanco();
            b.getIdBanco();
            b.getNombreCorto();
            b.getRazonSocial();
            b.getRfc();
            db.dameUsuario(b);
            fMsg2.setDetail("Datos Actualizados Correctamente");
            FacesContext.getCurrentInstance().addMessage(null, fMsg2);
        }

    }

    public List<SelectItem> obtenerBancos() throws SQLException {
        List<SelectItem> bancos = new ArrayList<SelectItem>();
        BancoLeyenda B = new BancoLeyenda();
        B.setIdBanco(0);
        B.setNombreCorto("Seleccione un país");
        SelectItem cero = new SelectItem(B, B.getNombreCorto());
        bancos.add(cero);

        DAOBancosLeyendas dao = new DAOBancosLeyendas();
        BancoLeyenda[] aBancos = dao.obtenerPaises();
        for (BancoLeyenda p : aBancos) {
            bancos.add(new SelectItem(p, p.getNombreCorto()));
        }
        return bancos;
    }

    public void obtenerBancos(int idCliente) throws SQLException {
        try {
            listaBancos = new ArrayList<SelectItem>();
            Banco objBanco = new Banco();
            objBanco.setIdBanco(0);
            objBanco.setNombreCorto("Seleccione un Banco");
            SelectItem cero = new SelectItem(objBanco, objBanco.getNombreCorto());
            listaBancos.add(cero);
            DAOBancos dao = new DAOBancos();
            ArrayList<Banco> lstBancos = dao.dameBancos(idCliente);
            for (Banco banco : lstBancos) {
                listaBancos.add(new SelectItem(banco, banco.getNombreCorto()));
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbBanco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String nuevoBanco() throws SQLException {
        BancoLeyenda b = new BancoLeyenda();

        b.setIdBanco(0);
        b.setRfc("");
        b.setCodigoBanco(0);
        b.setRazonSocial("");
        b.setNombreCorto("");
        String navegar = "banco.nuevo";

        return navegar;
    }

    public String actualizar(int id) throws SQLException {
        DAOBancosLeyendas banco = new DAOBancosLeyendas();
        String navegar = "menu.banco";
        if (id == 0) {
            b.setCodigoB("");
            b.setCodigoBanco(0);
            b.setIdBanco(0);
            b.setNombreCorto("");
            b.setRazonSocial("");
            b.setRfc("");
        } else {
            b = banco.obtenerDatos(id);
        }
        return navegar;
    }

    public ArrayList<Banco> getLstBancos() {
        return lstBancos;
    }

    public void setLstBancos(ArrayList<Banco> lstBancos) {
        this.lstBancos = lstBancos;
    }

    public ArrayList<SelectItem> getListaTodosBancos() {
        if (listaTodosBancos == null) {
            this.cargarTodosBancos();
        }
        return listaTodosBancos;
    }

    public void setListaTodosBancos(ArrayList<SelectItem> listaTodosBancos) {
        this.listaTodosBancos = listaTodosBancos;
    }

    private void cargarTodosBancos() {
        try {
            listaTodosBancos = new ArrayList<SelectItem>();
            DAOBancos dao = new DAOBancos();
            ArrayList<Banco> lstBanco = dao.dameBancos();
            Banco banco = new Banco();
            banco.setIdBanco(0);
            banco.setNombreCorto("Nuevo Banco");
            listaTodosBancos.add(new SelectItem(banco, banco.getNombreCorto()));
            for (Banco b : lstBanco) {
                listaTodosBancos.add(new SelectItem(b, b.getNombreCorto()));
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbBanco.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbBanco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Banco getObjBanco() {
        return objBanco;
    }

    public void setObjBanco(Banco objBanco) {
        this.objBanco = objBanco;
    }

}
