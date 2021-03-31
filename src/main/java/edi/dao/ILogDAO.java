/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;


import java.util.Date;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface ILogDAO {

   
   public List listar(String codTerritorio, Date fechaReporte) throws Exception;
  
}
