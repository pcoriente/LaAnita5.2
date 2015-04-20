package impuestos.dao;

import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jsolis
 */
public class DAOImpuestosProducto {
    private DataSource ds;

    public DAOImpuestosProducto() throws NamingException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            HttpSession httpSession = (HttpSession) externalContext.getSession(false);
            UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");

            Context cI = new InitialContext();
            ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
        } catch (NamingException ex) {
            throw (ex);
        }
    }
    /*
    public void agregarImpuestosProducto(int idImpuestoGrupo, int idZona, int idMovto, int idEmpaque) throws SQLException {
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            String strSQL="insert into movimientosDetalleImpuestos (idMovto, idEmpaque, idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe) " +
                            "select "+idMovto+", "+idEmpaque+", id.idImpuesto, i.impuesto, id.valor, i.aplicable, i.modo, i.acreditable, 0.00 as importe " +
                            "from impuestosDetalle id " +
                            "inner join impuestos i on i.idImpuesto=id.idImpuesto " +
                            "where id.idGrupo="+idImpuestoGrupo+" and id.idZona="+idZona+" and GETDATE() between fechaInicial and fechaFinal";
            st.executeUpdate(strSQL);
        } finally {
            cn.close();
        }
    }
    * */
    public ArrayList<ImpuestosProducto> obtenerImpuestosProducto(int idMovto, int idEmpaque) throws SQLException {
        ArrayList<ImpuestosProducto> impuestos=new ArrayList<ImpuestosProducto>();
        Connection cn = this.ds.getConnection();
        Statement st = cn.createStatement();
        try {
            String strSQL="select idImpuesto, impuesto, valor, aplicable, modo, acreditable, importe, acumulable\n" +
                            "from movimientosDetalleImpuestos\n" +
                            "where idMovto="+idMovto+" and idEmpaque="+idEmpaque;
            ResultSet rs=st.executeQuery(strSQL);
            while(rs.next()) {
                impuestos.add(construir(rs));
            }
        } finally {
            cn.close();
        }
        return impuestos;
    }
    
    private ImpuestosProducto construir(ResultSet rs) throws SQLException {
        ImpuestosProducto ip=new ImpuestosProducto();
        ip.setIdImpuesto(rs.getInt("idImpuesto"));
        ip.setImpuesto(rs.getString("impuesto"));
        ip.setValor(rs.getDouble("valor"));
        ip.setAplicable(rs.getBoolean("aplicable"));
        ip.setModo(rs.getInt("modo"));
        ip.setAcreditable(rs.getBoolean("acreditable"));
        ip.setImporte(rs.getDouble("importe"));
        ip.setAcumulable(rs.getBoolean("acumulable"));
        return ip;
    }
}
