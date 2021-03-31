/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;


import edi.dao.hibernate.Estado;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IEstadoDAO {

   public String guardar(Estado estado);
   public Estado obtener(String codEstado);
   public List<Estado> listar(String prefijo, boolean incluirSoloOrdenados);
  
}
