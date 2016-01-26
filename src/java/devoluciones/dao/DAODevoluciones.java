package devoluciones.dao;

import devoluciones.to.TODevolucionProducto;
import impuestos.dominio.ImpuestosProducto;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import movimientos.to.TOMovimientoOficina;
import usuarios.dominio.UsuarioSesion;

/**
 *
 * @author jesc
 */
public class DAODevoluciones {
    int idUsuario, idCedis;
    private DataSource ds = null;

    public DAODevoluciones() throws NamingException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(false);
        UsuarioSesion usuarioSesion = (UsuarioSesion) httpSession.getAttribute("usuarioSesion");
        this.idUsuario = usuarioSesion.getUsuario().getId();
        this.idCedis = usuarioSesion.getUsuario().getIdCedis();

        Context cI = new InitialContext();
        ds = (DataSource) cI.lookup("java:comp/env/" + usuarioSesion.getJndi());
    }
    
    public double obtenerImpuestosProducto(int idMovto, int idEmpaque, ArrayList<ImpuestosProducto> impuestos) throws SQLException {
        double importeImpuestos = 0;
        try (Connection cn = this.ds.getConnection()) {
            importeImpuestos = movimientos.Movimientos.obtenImpuestosProducto(cn, idMovto, idEmpaque, impuestos);
        }
        return importeImpuestos;
    }
//
//SELECT ISNULL(Dev.cantDevuelta, 0) AS cantDevuelta, Vta.*
//FROM (SELECT D.idMovto, MD.idEmpaque, SUM(MD.cantFacturada+MD.cantSinCargo) AS cantDevuelta
//	FROM movimientosDetalle MD
//	INNER JOIN devoluciones D ON D.idDevolucion=MD.idMovto
//	WHERE D.idMovto=4
//	GROUP BY D.idMovto, MD.idEmpaque) Dev
//RIGHT JOIN movimientosDetalle Vta ON Vta.idMovto=Dev.idMovto AND Vta.idEmpaque=Dev.idEmpaque
//WHERE Vta.idMovto=2
//
//SELECT ISNULL(Dev.cantDevuelta, 0) AS cantDevuelta, Vta.*
//FROM (SELECT V.idMovto, MD.idEmpaque, SUM(MD.cantFacturada+MD.cantSinCargo) AS cantDevuelta
//	FROM movimientosDetalle MD
//	INNER JOIN movimientos M ON M.idMovto=MD.idMovto
//	INNER JOIN movimientos V ON V.idMovto=M.referencia
//	WHERE M.idTipo=2 AND V.idMovto=4
//	GROUP BY V.idMovto, MD.idEmpaque) Dev
//RIGHT JOIN movimientosDetalle Vta ON Vta.idMovto=Dev.idMovto AND Vta.idEmpaque=Dev.idEmpaque
//WHERE Vta.idMovto=2
//
    public ArrayList<TODevolucionProducto> crear(TOMovimientoOficina toDev) {
        ArrayList<TODevolucionProducto> productos = new ArrayList<>();
        return productos;
    }
}
