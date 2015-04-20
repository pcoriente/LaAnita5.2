/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leyenda;

import leyenda.dao.DAOBancosLeyendas;
import leyenda.dominio.LeyendaBanco;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "mbBancoLeyenda")
@SessionScoped
public class MbBancoLeyenda {

    LeyendaBanco l = new LeyendaBanco();

    public LeyendaBanco getL() {
        return l;
    }

    public void setL(LeyendaBanco L) {
        this.l = L;
    }

    public MbBancoLeyenda() {
    }

    public ArrayList<LeyendaBanco> verTablaLB() throws SQLException {
        ArrayList<LeyendaBanco> Tabla;
        DAOBancosLeyendas dao = new DAOBancosLeyendas();
        Tabla = dao.dameDatosLeyenda();
        return Tabla;
    }

    public String d() throws SQLException {

        LeyendaBanco lb = new LeyendaBanco();
        lb.setIdLeyenda(l.idLeyenda);
        lb.setLeyenda(l.leyenda);
        DAOBancosLeyendas bl = new DAOBancosLeyendas();
        bl.Mactualizar(lb);
        String well = "Datos.Actualizados";
        return well;
    }

    public String eliminarL(int id) throws SQLException {
        DAOBancosLeyendas datos = new DAOBancosLeyendas();
        datos.eliminarUsuariol(id);
        String bad = "DatosEliminado";
        return bad;
    }

    public void guardarL() throws SQLException {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", null);
        DAOBancosLeyendas b = new DAOBancosLeyendas();
        if (l.getIdLeyenda() == 0) {
            b.guardarL(l.getLeyenda());
            fMsg.setDetail("Datos Insertados Correctamente");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        } else {
            l.getIdLeyenda();
            l.getLeyenda();
            b.Mactualizar(l);
            fMsg.setDetail("Datos Actualizados Correctamente");
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }





    }

    public void cancelar() {
        l.leyenda = "";
    }

    public String navegarSalir() {
        String salir = "SalirNavegando";
        return salir;
    }
//ISC.Pablo.

    public String actualizar(int id) throws SQLException {
        String x = "leyenda.mantenimiento";
        if (id == 0) {
            l.setIdLeyenda(0);
            l.setLeyenda("");
        } else {
            DAOBancosLeyendas daoL = new DAOBancosLeyendas();
            l = daoL.obtenerLeyendas(id);
            if (daoL == null) {
                x = null;
            }
        }
        return x;
    }

    public LeyendaBanco nuevaLeyenda() {
        LeyendaBanco le = new LeyendaBanco();
        le.setIdLeyenda(0);
        le.setLeyenda(null);
        return le;
    }

    public String terminar() {
        String x = "Salir";
        return x;
    }

    public String finalizar() {
        String x = "Salir.f";
        return x;
    }
}
