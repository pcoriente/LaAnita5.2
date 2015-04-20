package requisiciones.mb;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import requisiciones.dao.DAODepto;
import requisiciones.dominio.Depto;

@Named(value = "mbDepto")
@SessionScoped
public class MbDepto implements Serializable {

    private Depto deptos;
    private ArrayList<SelectItem> listaDeptos;
    private DAODepto dao;

    public MbDepto() {
    }

    public Depto getDeptos() {
        return deptos;
    }

    public void setDeptos(Depto deptos) {
        this.deptos = deptos;
    }

    public DAODepto getDao() {
        return dao;
    }

    public void setDao(DAODepto dao) {
        this.dao = dao;
    }
    
    

    public ArrayList<SelectItem> getListaDeptos() throws SQLException {
        if (this.listaDeptos == null) {
            this.cargarDeptos();
        }
        return listaDeptos;
    }

    public void setListaDeptos(ArrayList<SelectItem> listaDeptos) {
        this.listaDeptos = listaDeptos;
    }

    private void cargarDeptos() throws SQLException {
        this.listaDeptos = new ArrayList<SelectItem>();
        Depto dep = new Depto(0, "Seleccione un departamento");
        this.listaDeptos.add(new SelectItem(dep, dep.toString()));
        try {
            this.dao = new DAODepto();
            ArrayList<Depto> lstDeptos = this.dao.obtenerDeptos();
            for (Depto z : lstDeptos) {
                this.listaDeptos.add(new SelectItem(z, z.toString()));
            }

        } catch (NamingException ex) {
            Logger.getLogger(MbDepto.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
