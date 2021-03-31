/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;


import edi.dao.hibernate.PlanAccionVentaPerdida;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IVentaDAO {

   public void guardarCausalVentaPerdida(List<Object[]> arrayCausalVentaPerdida) throws Exception;
   public List<Object[]> listarCausalVtaPerdida() throws Exception;
   public List<Object[]> listarProductoVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception;
   public void consolidarCausalVentaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception;
   public List<Object[]> listarCabeceraPlanAccion(String codTerritorio) throws Exception;
   public List<Object[]> listarValorVentaPerdidaSeguimiento(int codSeguimiento) throws Exception;
   public List<Object[]> listarDetallePlanAccion(int codSeguimiento) throws Exception;
   public List<Object[]> listarVentaPerdidaProductoPorSeguimiento(int codSeguimiento) throws Exception;
   public void guardarPlanAccion(PlanAccionVentaPerdida oPlanAccion) throws Exception;
   public String obtenerObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception;
public void guardarObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana, String observaciones) throws Exception;
}
