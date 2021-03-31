/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.IVentaDAO;
import edi.dao.hibernate.PlanAccionVentaPerdida;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public class VentaServicio implements IVentaServicio {
    
    private IVentaDAO ventaDAO;
    

    public void setVentaDAO(IVentaDAO ventaDAO) {
        this.ventaDAO=ventaDAO;
    }

    
    public void guardarCausalVentaPerdida(List<Object[]> arrayCausalVentaPerdida) throws Exception {
        this.ventaDAO.guardarCausalVentaPerdida(arrayCausalVentaPerdida);
    }
    
    public List<Object[]> listarCausalVtaPerdida() throws Exception {
        return this.ventaDAO.listarCausalVtaPerdida();
    }
    
    public List<Object[]> listarProductoVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        return this.ventaDAO.listarProductoVtaPerdida(codTerritorio, anioPeriodo, semana);
    }
    
    public void consolidarCausalVentaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        this.ventaDAO.consolidarCausalVentaPerdida(codTerritorio, anioPeriodo, semana);
    }
    
    public List<Object[]> listarCabeceraPlanAccion(String codTerritorio) throws Exception {
        return this.ventaDAO.listarCabeceraPlanAccion(codTerritorio);
    }
    
    public List<Object[]> listarValorVentaPerdidaSeguimiento(int codSeguimiento) throws Exception {
        return this.ventaDAO.listarValorVentaPerdidaSeguimiento(codSeguimiento);
    }
    
    public List<Object[]> listarDetallePlanAccion(int codSeguimiento) throws Exception {
        return this.ventaDAO.listarDetallePlanAccion(codSeguimiento);
    }
    
    public List<Object[]> listarVentaPerdidaProductoPorSeguimiento(int codSeguimiento) throws Exception {
        return this.ventaDAO.listarVentaPerdidaProductoPorSeguimiento(codSeguimiento);
    }
    
    public void guardarPlanAccion(PlanAccionVentaPerdida oPlanAccion) throws Exception {
        this.ventaDAO.guardarPlanAccion(oPlanAccion);
    }
    
    
    public String obtenerObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        return this.ventaDAO.obtenerObservacionVtaPerdida(codTerritorio, anioPeriodo, semana);
    }
    
    
    public void guardarObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana, String observaciones) throws Exception {
        this.ventaDAO.guardarObservacionVtaPerdida(codTerritorio, anioPeriodo, semana, observaciones);
    }
}
