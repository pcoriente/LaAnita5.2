package pedidos.dominio;

import java.io.Serializable;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CargaPedido{
	private String ordcomp;
	private String posicion;
	private Date fechaesp;
	private Date fechacan;
	private String numtda;
	private String codbar;
	private String modelo;
	private String descrip;
	private String empaque;
	private float cantidad;
	private float costo;
	private String cnose;
	private Date fechaord;
	private String numprov;
	private float cant;
	
	public String getOrdcomp() {
		return ordcomp;
	}
	public void setOrdcomp(String ordcomp) {
		this.ordcomp = ordcomp;
	}
	public String getPosicion() {
		return posicion;
	}
	public void setPosicion(String posicion) {
		this.posicion = posicion;
	}
	public Date getFechaesp() {
		return fechaesp;
	}
	public void setFechaesp(Date fechaesp) {
		this.fechaesp = fechaesp;
	}
	public Date getFechacan() {
		return fechacan;
	}
	public void setFechacan(Date fechacan) {
		this.fechacan = fechacan;
	}
	public String getCodbar() {
		return codbar;
	}
	public void setCodbar(String codbar) {
		this.codbar = codbar;
	}
	public String getModelo() {
		return modelo;
	}
	public void setModelo(String modelo) {
		this.modelo = modelo;
	}
	public String getDescrip() {
		return descrip;
	}
	public void setDescrip(String descrip) {
		this.descrip = descrip;
	}
	public String getEmpaque() {
		return empaque;
	}
	public void setEmpaque(String empaque) {
		this.empaque = empaque;
	}
	public String getCnose() {
		return cnose;
	}
	public void setCnose(String cnose) {
		this.cnose = cnose;
	}
	public Date getFechaord() {
		return fechaord;
	}
	public void setFechaord(Date fechaord) {
		this.fechaord = fechaord;
	}
	public String getNumprov() {
		return numprov;
	}
	public void setNumprov(String numprov) {
		this.numprov = numprov;
	}
	public float getCantidad() {
		return cantidad;
	}
	public void setCantidad(float cantidad) {
		this.cantidad = cantidad;
	}
	public float getCosto() {
		return costo;
	}
	public void setCosto(float costo) {
		this.costo = costo;
	}
	public float getCant() {
		return cant;
	}
	public void setCant(float cant) {
		this.cant = cant;
	}
		
	public String toString()
	{
		return this.ordcomp+"-"+this.numtda+" "+this.codbar+this.numprov+'\n';
	}
	public String getNumtda() {
		return numtda;
	}
	public void setNumtda(String numtda) {
		this.numtda = numtda;
	}

}
